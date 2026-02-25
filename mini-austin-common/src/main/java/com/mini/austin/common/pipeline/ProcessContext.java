package com.mini.austin.common.pipeline;

import com.mini.austin.common.vo.BasicResultVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 责任链处理上下文
 * <p>
 * 在整个责任链执行过程中传递，包含：
 * - 业务编码（用于路由到不同的处理链）
 * - 处理数据模型
 * - 是否中断标识
 * - 响应结果
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessContext<T extends ProcessModel> {

    /**
     * 业务编码（如：send, recall）
     * 用于选择不同的处理链
     */
    private String code;

    /**
     * 处理数据模型
     */
    private T processModel;

    /**
     * 是否中断责任链
     * 当某个节点处理失败时，设置为true，后续节点不再执行
     */
    private Boolean needBreak;

    /**
     * 响应结果
     */
    private BasicResultVO<?> response;
}
