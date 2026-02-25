package com.mini.austin.common.pipeline;

/**
 * 业务处理接口 - 责任链节点
 * <p>
 * 每个实现类代表一个处理环节，例如：
 * - 前置校验
 * - 参数拼装
 * - 发送MQ
 *
 * @author mini-austin
 */
public interface BusinessProcess<T extends ProcessModel> {

    /**
     * 执行处理逻辑
     *
     * @param context 处理上下文
     */
    void process(ProcessContext<T> context);
}
