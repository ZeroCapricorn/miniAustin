package com.mini.austin.handler.config;

import com.mini.austin.common.pipeline.ProcessController;
import com.mini.austin.common.pipeline.ProcessTemplate;
import com.mini.austin.handler.action.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler 层责任链配置
 * <p>
 * ★★★ Phase 4 升级：完整的消费端处理流程 ★★★
 * <p>
 * Handler 层处理链（顺序很重要！）：
 * 1. NightShieldAction     - 夜间屏蔽（营销消息在 21:00-08:00 不发送）
 * 2. FrequencyLimitAction  - 频率限制（防止对用户过度发送）
 * 3. DeduplicationAction   - 去重（避免重复发送相同内容）
 * 4. SensitiveWordsAction  - 敏感词过滤（过滤违规内容）
 * 5. SendMessageAction     - 路由到具体 Handler 发送
 *
 * @author mini-austin
 */
@Configuration
public class HandlerPipelineConfig {

    @Autowired
    private NightShieldAction nightShieldAction;

    @Autowired
    private FrequencyLimitAction frequencyLimitAction;

    @Autowired
    private DeduplicationAction deduplicationAction;

    @Autowired
    private SensitiveWordsAction sensitiveWordsAction;

    @Autowired
    private SendMessageAction sendMessageAction;

    /**
     * Handler 处理流程模板
     * <p>
     * 执行顺序：夜间屏蔽 → 频率限制 → 去重 → 敏感词过滤 → 发送
     */
    @Bean("handlerTemplate")
    public ProcessTemplate handlerTemplate() {
        ProcessTemplate template = new ProcessTemplate();
        template.setProcessList(Arrays.asList(
                nightShieldAction,      // 1. 夜间屏蔽（营销消息）
                frequencyLimitAction,   // 2. 频率限制
                deduplicationAction,    // 3. 去重
                sensitiveWordsAction,   // 4. 敏感词过滤
                sendMessageAction       // 5. 发送
        ));
        return template;
    }

    /**
     * Handler 层流程控制器
     */
    @Bean("handlerProcessController")
    public ProcessController handlerProcessController() {
        ProcessController controller = new ProcessController();

        Map<String, ProcessTemplate> templateConfig = new HashMap<>(4);
        templateConfig.put(HandlerThreadPoolConfig.HANDLER_PIPELINE_CODE, handlerTemplate());

        controller.setTemplateConfig(templateConfig);
        return controller;
    }
}
