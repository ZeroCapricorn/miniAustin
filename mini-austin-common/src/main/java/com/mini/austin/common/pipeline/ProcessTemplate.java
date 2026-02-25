package com.mini.austin.common.pipeline;

import lombok.Data;

import java.util.List;

/**
 * 处理链模板
 * <p>
 * 定义一条完整的处理链，包含多个有序的处理节点
 *
 * @author mini-austin
 */
@Data
public class ProcessTemplate {

    /**
     * 处理节点列表（按顺序执行）
     */
    private List<BusinessProcess> processList;
}
