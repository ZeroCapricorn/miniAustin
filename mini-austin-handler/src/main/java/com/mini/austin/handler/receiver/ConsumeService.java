package com.mini.austin.handler.receiver;

import com.mini.austin.common.domain.TaskInfo;

/**
 * 消费服务接口
 *
 * @author mini-austin
 */
public interface ConsumeService {

    /**
     * 消费消息并处理
     *
     * @param taskInfo 任务信息
     */
    void consume(TaskInfo taskInfo);
}
