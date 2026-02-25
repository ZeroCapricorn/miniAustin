package com.mini.austin.handler.handler;

import com.mini.austin.common.domain.TaskInfo;

/**
 * 消息处理器接口
 * <p>
 * 不同渠道（邮件、短信、Push）实现此接口
 *
 * @author mini-austin
 */
public interface Handler {

    /**
     * 处理消息
     *
     * @param taskInfo 任务信息
     * @return 是否发送成功
     */
    boolean doHandler(TaskInfo taskInfo);

    /**
     * 获取渠道编码
     */
    Integer getChannelCode();
}
