package com.mini.austin.common.pipeline;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mini.austin.common.enums.RespStatusEnum;
import com.mini.austin.common.vo.BasicResultVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 责任链流程控制器
 * <p>
 * 核心调度类，负责：
 * 1. 根据业务编码选择对应的处理链模板
 * 2. 按顺序执行链上的每个处理节点
 * 3. 支持中断机制
 *
 * @author mini-austin
 */
@Data
@Slf4j
public class ProcessController {

    /**
     * 模板配置映射
     * key: 业务编码
     * value: 处理链模板
     */
    private Map<String, ProcessTemplate> templateConfig;

    /**
     * 执行责任链
     *
     * @param context 处理上下文
     * @return 处理结果
     */
    public ProcessContext process(ProcessContext context) {
        // 1. 前置校验
        if (!preCheck(context)) {
            return context;
        }

        // 2. 根据业务编码获取处理链模板
        ProcessTemplate processTemplate = templateConfig.get(context.getCode());
        List<BusinessProcess> processList = processTemplate.getProcessList();

        // 3. 按顺序执行处理链
        for (BusinessProcess businessProcess : processList) {
            // 执行当前节点
            businessProcess.process(context);

            // 检查是否需要中断
            if (Boolean.TRUE.equals(context.getNeedBreak())) {
                log.info("责任链中断，当前节点：{}", businessProcess.getClass().getSimpleName());
                break;
            }
        }

        return context;
    }

    /**
     * 前置校验
     */
    private boolean preCheck(ProcessContext context) {
        // 校验上下文
        if (context == null) {
            context = new ProcessContext();
            context.setResponse(BasicResultVO.fail("上下文不能为空"));
            return false;
        }

        // 校验业务编码
        if (StrUtil.isBlank(context.getCode())) {
            context.setResponse(BasicResultVO.fail("业务编码不能为空"));
            return false;
        }

        // 校验模板配置
        ProcessTemplate processTemplate = templateConfig.get(context.getCode());
        if (processTemplate == null || CollUtil.isEmpty(processTemplate.getProcessList())) {
            context.setResponse(BasicResultVO.fail("未找到对应的处理链配置"));
            return false;
        }

        return true;
    }
}
