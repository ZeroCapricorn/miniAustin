package com.mini.austin.web.config;

import com.mini.austin.common.pipeline.ProcessController;
import com.mini.austin.common.pipeline.ProcessTemplate;
import com.mini.austin.web.action.AssembleAction;
import com.mini.austin.web.action.PreCheckAction;
import com.mini.austin.web.action.SendMqAction;
import com.mini.austin.web.enums.BusinessCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 责任链配置类
 * <p>
 * ★★★ Phase 2 核心：配置责任链处理流程 ★★★
 * <p>
 * 这里定义了"发送消息"的完整处理链：
 * 1. PreCheckAction  - 前置参数校验
 * 2. AssembleAction  - 查询模板 + 占位符替换 + 组装TaskInfo
 * 3. SendMqAction    - 投递到RabbitMQ
 * <p>
 * 面试考点：
 * - 为什么用责任链？解耦，每个节点职责单一，方便扩展
 * - 如何新增处理逻辑？新建一个Action，加入到processList即可
 * - 如何支持多种业务流程？用不同的code映射不同的ProcessTemplate
 *
 * @author mini-austin
 */
@Configuration
public class PipelineConfig {

    @Autowired
    private PreCheckAction preCheckAction;

    @Autowired
    private AssembleAction assembleAction;

    @Autowired
    private SendMqAction sendMqAction;

    /**
     * 普通发送流程模板
     * <p>
     * 执行顺序：前置校验 -> 参数拼装 -> 发送MQ
     */
    @Bean("sendTemplate")
    public ProcessTemplate sendTemplate() {
        ProcessTemplate template = new ProcessTemplate();
        template.setProcessList(Arrays.asList(
                preCheckAction,     // 1. 前置校验
                assembleAction,     // 2. 参数拼装
                sendMqAction        // 3. 发送MQ
        ));
        return template;
    }

    /**
     * 责任链流程控制器
     * <p>
     * 通过 code 选择不同的处理链
     */
    @Bean("sendProcessController")
    public ProcessController sendProcessController() {
        ProcessController controller = new ProcessController();

        Map<String, ProcessTemplate> templateConfig = new HashMap<>(4);
        // 注册"发送"流程
        templateConfig.put(BusinessCode.SEND.getCode(), sendTemplate());
        // 后续可以注册更多流程，如：撤回、定时发送等
        // templateConfig.put(BusinessCode.RECALL.getCode(), recallTemplate());

        controller.setTemplateConfig(templateConfig);
        return controller;
    }
}
