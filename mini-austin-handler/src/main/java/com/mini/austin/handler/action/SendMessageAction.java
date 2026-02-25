package com.mini.austin.handler.action;

import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import com.mini.austin.handler.handler.Handler;
import com.mini.austin.handler.handler.HandlerHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发送消息 Action
 * <p>
 * 责任链最后一个节点，负责路由到具体渠道的 Handler 进行发送
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class SendMessageAction implements BusinessProcess<TaskInfo> {

    @Autowired
    private HandlerHolder handlerHolder;

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        TaskInfo taskInfo = context.getProcessModel();

        // 根据渠道类型路由到对应的 Handler
        Handler handler = handlerHolder.route(taskInfo.getSendChannel());

        if (handler == null) {
            log.error("未找到对应的Handler: channel={}", taskInfo.getSendChannel());
            return;
        }

        // 执行发送
        boolean success = handler.doHandler(taskInfo);

        if (success) {
            log.info("消息发送成功: messageId={}, channel={}", 
                    taskInfo.getMessageId(), taskInfo.getSendChannel());
        } else {
            log.error("消息发送失败: messageId={}, channel={}", 
                    taskInfo.getMessageId(), taskInfo.getSendChannel());
        }
    }
}
