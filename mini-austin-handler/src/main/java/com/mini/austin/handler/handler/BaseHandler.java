package com.mini.austin.handler.handler;

import com.mini.austin.common.domain.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * Handler 基类
 * <p>
 * 模板方法模式：定义公共处理流程，子类实现具体发送逻辑
 *
 * @author mini-austin
 */
@Slf4j
public abstract class BaseHandler implements Handler {

    /**
     * 渠道编码（子类初始化时指定）
     */
    protected Integer channelCode;

    @Autowired
    private HandlerHolder handlerHolder;

    /**
     * 初始化：注册到 HandlerHolder
     */
    @PostConstruct
    private void init() {
        handlerHolder.putHandler(channelCode, this);
        log.info("注册 Handler: channelCode={}, handler={}", channelCode, this.getClass().getSimpleName());
    }

    @Override
    public boolean doHandler(TaskInfo taskInfo) {
        // 这里可以加通用逻辑，如限流、日志等
        log.info("开始处理消息: messageId={}, channel={}, receivers={}",
                taskInfo.getMessageId(), channelCode, taskInfo.getReceiver());

        boolean success = handler(taskInfo);

        if (success) {
            log.info("消息发送成功: messageId={}", taskInfo.getMessageId());
        } else {
            log.error("消息发送失败: messageId={}", taskInfo.getMessageId());
        }

        return success;
    }

    @Override
    public Integer getChannelCode() {
        return channelCode;
    }

    /**
     * 具体发送逻辑（子类实现）
     */
    protected abstract boolean handler(TaskInfo taskInfo);
}
