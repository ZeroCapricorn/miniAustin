package com.mini.austin.handler.action;

import cn.hutool.core.collection.CollUtil;
import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 频率限制 Action
 * <p>
 * ★★★ Phase 4 功能：防止对同一用户过度发送消息 ★★★
 * <p>
 * 场景：
 * - 限制对同一用户的发送频率（如每天最多发送 5 条营销消息）
 * - 防止恶意调用导致用户被骚扰
 * - 满足运营商/平台的发送频率限制要求
 * <p>
 * 实现原理：
 * - 使用 Redis + Lua 脚本实现滑动窗口限流
 * - 原子操作保证并发安全
 * - 支持多维度限流（按天/按小时）
 * <p>
 * 面试亮点：
 * - 滑动窗口 vs 固定窗口 vs 令牌桶的区别
 * - Redis Lua 脚本保证原子性
 * - 限流策略的可配置化
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class FrequencyLimitAction implements BusinessProcess<TaskInfo> {

    private static final String LIMIT_KEY_PREFIX = "austin:freq:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 是否启用频率限制
     */
    @Value("${mini-austin.frequency-limit.enabled:true}")
    private boolean enabled;

    /**
     * 每日限制次数（每个用户每天最多收到多少条消息）
     */
    @Value("${mini-austin.frequency-limit.day-max:5}")
    private int dayMax;

    /**
     * 每小时限制次数
     */
    @Value("${mini-austin.frequency-limit.hour-max:2}")
    private int hourMax;

    /**
     * Lua 脚本（滑动窗口限流）
     */
    private DefaultRedisScript<Long> limitScript;

    @PostConstruct
    public void init() {
        // 初始化 Lua 脚本
        limitScript = new DefaultRedisScript<>();
        limitScript.setScriptText(buildLuaScript());
        limitScript.setResultType(Long.class);
        log.info("频率限制初始化完成: dayMax={}, hourMax={}", dayMax, hourMax);
    }

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        if (!enabled) {
            return;
        }

        TaskInfo taskInfo = context.getProcessModel();
        Set<String> receivers = taskInfo.getReceiver();

        if (CollUtil.isEmpty(receivers)) {
            return;
        }

        // 检查每个接收者的发送频率
        Set<String> filteredReceivers = new HashSet<>();
        int beforeCount = receivers.size();

        for (String receiver : receivers) {
            if (checkAndIncrement(taskInfo, receiver)) {
                filteredReceivers.add(receiver);
            } else {
                log.info("【频率限制】用户被限流: receiver={}, templateId={}",
                        receiver, taskInfo.getMessageTemplateId());
            }
        }

        // 更新接收者列表
        taskInfo.setReceiver(filteredReceivers);

        // 如果所有接收者都被限流，中断责任链
        if (CollUtil.isEmpty(filteredReceivers)) {
            context.setNeedBreak(true);
            log.info("【频率限制】所有接收者都被限流: messageId={}", taskInfo.getMessageId());
            return;
        }

        int afterCount = filteredReceivers.size();
        if (beforeCount != afterCount) {
            log.info("频率限制过滤完成: messageId={}, before={}, after={}",
                    taskInfo.getMessageId(), beforeCount, afterCount);
        }
    }

    /**
     * 检查并增加计数
     * <p>
     * 使用 Lua 脚本保证原子性：
     * 1. 获取当前计数
     * 2. 如果超过限制，返回 0（拒绝）
     * 3. 如果未超限，增加计数并返回 1（通过）
     *
     * @return true=通过，false=被限流
     */
    private boolean checkAndIncrement(TaskInfo taskInfo, String receiver) {
        // 生成限流 Key（按天）
        String dayKey = buildLimitKey(taskInfo, receiver, "day");
        // 生成限流 Key（按小时）
        String hourKey = buildLimitKey(taskInfo, receiver, "hour");

        try {
            // 检查日限制
            Long dayResult = redisTemplate.execute(
                    limitScript,
                    Collections.singletonList(dayKey),
                    String.valueOf(dayMax),
                    String.valueOf(86400) // 24小时过期
            );

            if (dayResult == null || dayResult == 0) {
                return false;
            }

            // 检查小时限制
            Long hourResult = redisTemplate.execute(
                    limitScript,
                    Collections.singletonList(hourKey),
                    String.valueOf(hourMax),
                    String.valueOf(3600) // 1小时过期
            );

            return hourResult != null && hourResult == 1;

        } catch (Exception e) {
            log.error("频率限制检查异常: {}", e.getMessage(), e);
            // 异常时放行，避免影响正常业务
            return true;
        }
    }

    /**
     * 构建限流 Key
     * <p>
     * Key 格式：austin:freq:{dimension}:{templateId}:{receiver}:{timeWindow}
     */
    private String buildLimitKey(TaskInfo taskInfo, String receiver, String dimension) {
        String timeWindow;
        if ("day".equals(dimension)) {
            // 按天：格式 20260130
            timeWindow = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMdd");
        } else {
            // 按小时：格式 2026013020
            timeWindow = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMddHH");
        }

        return LIMIT_KEY_PREFIX + dimension + ":" 
                + taskInfo.getMessageTemplateId() + ":" 
                + receiver + ":" 
                + timeWindow;
    }

    /**
     * 构建 Lua 限流脚本
     * <p>
     * 原理：
     * 1. 获取当前 Key 的值
     * 2. 如果值 >= 限制，返回 0（拒绝）
     * 3. 否则 INCR 增加计数，设置过期时间，返回 1（通过）
     * <p>
     * ★★★ 面试重点：为什么用 Lua？★★★
     * - 保证"检查+增加"的原子性
     * - 避免并发下的竞态条件
     * - 减少网络往返次数
     */
    private String buildLuaScript() {
        return "local key = KEYS[1]\n" +
                "local limit = tonumber(ARGV[1])\n" +
                "local expire = tonumber(ARGV[2])\n" +
                "local current = tonumber(redis.call('GET', key) or '0')\n" +
                "if current >= limit then\n" +
                "    return 0\n" +
                "end\n" +
                "current = redis.call('INCR', key)\n" +
                "if current == 1 then\n" +
                "    redis.call('EXPIRE', key, expire)\n" +
                "end\n" +
                "return 1";
    }
}
