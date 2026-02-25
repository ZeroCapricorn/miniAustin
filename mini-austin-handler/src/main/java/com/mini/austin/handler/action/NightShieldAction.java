package com.mini.austin.handler.action;

import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.enums.MessageType;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 夜间屏蔽 Action
 * <p>
 * ★★★ Phase 4 功能：防止营销消息在夜间打扰用户 ★★★
 * <p>
 * 场景：
 * - 营销类消息不应在深夜发送（影响用户体验，可能导致投诉）
 * - 通知类和验证码不受限制（如验证码需即时送达）
 * <p>
 * 实现逻辑：
 * 1. 判断消息类型是否为营销类
 * 2. 判断当前时间是否在屏蔽时段（默认 21:00 - 08:00）
 * 3. 如果命中，直接丢弃或延迟到次日发送（本例选择丢弃）
 * <p>
 * 面试亮点：
 * - 体现对用户体验的关注
 * - 可配置化设计（屏蔽时段可通过配置文件调整）
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class NightShieldAction implements BusinessProcess<TaskInfo> {

    /**
     * 夜间屏蔽开始时间（小时），默认 21:00
     */
    @Value("${mini-austin.night-shield.start-hour:21}")
    private int startHour;

    /**
     * 夜间屏蔽结束时间（小时），默认 08:00
     */
    @Value("${mini-austin.night-shield.end-hour:8}")
    private int endHour;

    /**
     * 是否启用夜间屏蔽
     */
    @Value("${mini-austin.night-shield.enabled:true}")
    private boolean enabled;

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        TaskInfo taskInfo = context.getProcessModel();

        // 1. 检查开关
        if (!enabled) {
            return;
        }

        // 2. 只对营销类消息生效
        if (!MessageType.MARKETING.getCode().equals(taskInfo.getMsgType())) {
            return;
        }

        // 3. 判断是否在屏蔽时段
        if (isInShieldPeriod()) {
            context.setNeedBreak(true);
            log.info("【夜间屏蔽】营销消息被拦截: messageId={}, 当前时间={}, 屏蔽时段={}:00-{}:00",
                    taskInfo.getMessageId(),
                    LocalTime.now().toString(),
                    startHour,
                    endHour);
            return;
        }
    }

    /**
     * 判断当前时间是否在屏蔽时段
     * <p>
     * 屏蔽时段示例：21:00 - 08:00（跨天）
     * - 21:00 - 23:59 属于屏蔽时段
     * - 00:00 - 08:00 属于屏蔽时段
     */
    private boolean isInShieldPeriod() {
        int currentHour = LocalTime.now().getHour();

        // 跨天情况：startHour > endHour（如 21:00 - 08:00）
        if (startHour > endHour) {
            return currentHour >= startHour || currentHour < endHour;
        }

        // 不跨天情况：startHour < endHour（如 01:00 - 06:00）
        return currentHour >= startHour && currentHour < endHour;
    }
}
