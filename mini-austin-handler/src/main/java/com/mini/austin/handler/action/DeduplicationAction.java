package com.mini.austin.handler.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 去重 Action
 * <p>
 * ★★★ 面试重点：如何实现消息去重？ ★★★
 * <p>
 * 场景：同一个用户在短时间内不应该收到重复内容的消息
 * <p>
 * 实现原理：
 * 1. 构建去重 Key = SHA-256(模板ID + 接收者 + 内容)
 * 2. 查询 Redis，如果 Key 存在，说明已发送过，过滤掉该接收者
 * 3. 如果 Key 不存在，写入 Redis 并设置过期时间
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class DeduplicationAction implements BusinessProcess<TaskInfo> {

    private static final String DEDUP_KEY_PREFIX = "austin:dedup:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 去重时间窗口（秒），默认5分钟
     */
    @Value("${mini-austin.deduplication.expire-seconds:300}")
    private long expireSeconds;

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        TaskInfo taskInfo = context.getProcessModel();

        // 记录去重前的接收者数量
        int beforeCount = taskInfo.getReceiver().size();

        // 执行去重
        Set<String> filteredReceivers = doDeduplication(taskInfo);

        // 更新接收者列表
        taskInfo.setReceiver(filteredReceivers);

        // 如果所有接收者都被过滤，中断责任链
        if (CollUtil.isEmpty(filteredReceivers)) {
            context.setNeedBreak(true);
            log.info("所有接收者都被去重过滤: messageId={}", taskInfo.getMessageId());
            return;
        }

        int afterCount = filteredReceivers.size();
        if (beforeCount != afterCount) {
            log.info("去重完成: messageId={}, before={}, after={}", 
                    taskInfo.getMessageId(), beforeCount, afterCount);
        }
    }

    /**
     * 执行去重逻辑
     */
    private Set<String> doDeduplication(TaskInfo taskInfo) {
        Set<String> result = new HashSet<>();

        for (String receiver : taskInfo.getReceiver()) {
            // 构建去重Key
            String dedupKey = buildDedupKey(taskInfo, receiver);

            // 检查是否已存在
            Boolean exists = redisTemplate.hasKey(dedupKey);
            if (Boolean.TRUE.equals(exists)) {
                log.debug("去重命中: receiver={}, key={}", receiver, dedupKey);
                continue;
            }

            // 不存在，写入 Redis 并设置过期时间
            redisTemplate.opsForValue().set(dedupKey, "1", expireSeconds, TimeUnit.SECONDS);
            result.add(receiver);
        }

        return result;
    }

    /**
     * 构建去重Key
     * <p>
     * Key = 前缀 + 模板ID + 接收者 + 内容SHA-256
     */
    private String buildDedupKey(TaskInfo taskInfo, String receiver) {
        String contentJson;
        try {
            contentJson = taskInfo.getContentModel() == null
                    ? ""
                    : objectMapper.writeValueAsString(taskInfo.getContentModel());
        } catch (Exception e) {
            contentJson = "";
        }
        // 使用 SHA-256 替代 MD5（更安全，抗碰撞）
        String contentHash = DigestUtil.sha256Hex(contentJson);
        return DEDUP_KEY_PREFIX + taskInfo.getMessageTemplateId() + ":" + receiver + ":" + contentHash;
    }
}
