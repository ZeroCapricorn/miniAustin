package com.mini.austin.web.action;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.austin.common.domain.ContentModel;
import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.dto.MessageParam;
import com.mini.austin.common.enums.ChannelType;
import com.mini.austin.common.enums.RespStatusEnum;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import com.mini.austin.common.vo.BasicResultVO;
import com.mini.austin.web.dao.MessageTemplateDao;
import com.mini.austin.web.domain.MessageTemplate;
import com.mini.austin.web.domain.SendTaskModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 参数拼装 Action
 * <p>
 * 责任链第二个节点，负责：
 * 1. 根据 templateId 查询模板
 * 2. 将模板内容中的占位符 ${xxx} 替换为实际参数
 * 3. 组装 TaskInfo 列表
 *
 * ★★★ 这是面试重点：如何实现模板变量替换 ★★★
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class AssembleAction implements BusinessProcess<SendTaskModel> {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        SendTaskModel sendTaskModel = context.getProcessModel();
        Long templateId = sendTaskModel.getMessageTemplateId();

        // 1. 查询消息模板
        Optional<MessageTemplate> templateOpt = messageTemplateDao.findById(templateId);
        if (templateOpt.isEmpty()) {
            context.setNeedBreak(true);
            context.setResponse(BasicResultVO.fail(RespStatusEnum.TEMPLATE_NOT_FOUND));
            log.warn("模板不存在：templateId={}", templateId);
            return;
        }

        MessageTemplate template = templateOpt.get();

        // 检查模板是否已删除
        if (template.getIsDeleted() != null && template.getIsDeleted() == 1) {
            context.setNeedBreak(true);
            context.setResponse(BasicResultVO.fail(RespStatusEnum.TEMPLATE_DELETED));
            log.warn("模板已删除：templateId={}", templateId);
            return;
        }

        sendTaskModel.setMessageTemplate(template);

        // 2. 组装 TaskInfo 列表
        List<TaskInfo> taskInfoList = assembleTaskInfoList(sendTaskModel, template);
        sendTaskModel.setTaskInfoList(taskInfoList);

        log.info("参数拼装完成：templateId={}, taskCount={}", templateId, taskInfoList.size());
    }

    /**
     * 组装 TaskInfo 列表
     */
    private List<TaskInfo> assembleTaskInfoList(SendTaskModel sendTaskModel, MessageTemplate template) {
        List<TaskInfo> taskInfoList = new ArrayList<>();

        for (MessageParam messageParam : sendTaskModel.getMessageParamList()) {
            TaskInfo taskInfo = TaskInfo.builder()
                    // 生成唯一消息ID（用于链路追踪）
                    .messageId(generateMessageId())
                    // 生成业务ID（模板ID + 日期，用于数据聚合）
                    .businessId(generateBusinessId(template.getId()))
                    .messageTemplateId(template.getId())
                    // 解析接收者（支持逗号分隔多个）
                    .receiver(parseReceiver(messageParam.getReceiver()))
                    .sendChannel(template.getSendChannel())
                    .msgType(template.getMsgType())
                    .sendAccount(template.getSendAccount())
                    // ★ 核心：替换占位符，生成内容模型
                    .contentModel(buildContentModel(template, messageParam))
                    .build();

            taskInfoList.add(taskInfo);
        }

        return taskInfoList;
    }

    /**
     * 生成消息唯一ID
     */
    private String generateMessageId() {
        return IdUtil.nanoId();
    }

    /**
     * 生成业务ID（模板ID + 当天日期）
     * 格式：templateId + yyyyMMdd，固定16位
     */
    private Long generateBusinessId(Long templateId) {
        String today = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMdd");
        return Long.parseLong(templateId + today);
    }

    /**
     * 解析接收者（逗号分隔）
     */
    private Set<String> parseReceiver(String receiver) {
        return new HashSet<>(Arrays.asList(receiver.split(StrPool.COMMA)));
    }

    /**
     * ★★★ 核心方法：构建内容模型，替换占位符 ★★★
     * <p>
     * 模板内容示例：{"title":"验证码","content":"您的验证码是${code}"}
     * 变量参数示例：{"code": "123456"}
     * 替换后结果：{"title":"验证码","content":"您的验证码是123456"}
     */
    private ContentModel buildContentModel(MessageTemplate template, MessageParam messageParam) {
        // 1. 获取渠道对应的 ContentModel 类型
        Integer sendChannel = template.getSendChannel();
        Class<? extends ContentModel> contentModelClass = ChannelType.getContentModelClassByCode(sendChannel);
        if (contentModelClass == null) {
            log.error("未找到渠道对应的ContentModel：channel={}", sendChannel);
            return null;
        }

        // 2. 解析模板内容JSON（使用 Jackson）
        String msgContent = template.getMsgContent();
        Map<String, String> contentMap;
        try {
            contentMap = objectMapper.readValue(msgContent, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("解析模板内容JSON失败: {}", e.getMessage());
            return null;
        }

        // 3. 获取用户传入的变量
        Map<String, String> variables = messageParam.getVariables();
        if (variables == null) {
            variables = new HashMap<>();
        }

        // 4. 通过反射创建 ContentModel 实例，并填充字段
        ContentModel contentModel = ReflectUtil.newInstance(contentModelClass);
        Field[] fields = ReflectUtil.getFields(contentModelClass);

        for (Field field : fields) {
            String fieldName = field.getName();
            String originalValue = contentMap.get(fieldName);

            if (StrUtil.isNotBlank(originalValue)) {
                // ★ 关键：替换占位符 ${xxx} 为实际值
                String replacedValue = replacePlaceholder(originalValue, variables);
                ReflectUtil.setFieldValue(contentModel, field, replacedValue);
            }
        }

        return contentModel;
    }

    /**
     * ★★★ 占位符替换 ★★★
     * <p>
     * 将 "您的验证码是${code}" 中的 ${code} 替换为变量值
     *
     * @param content   原始内容
     * @param variables 变量映射
     * @return 替换后的内容
     */
    private String replacePlaceholder(String content, Map<String, String> variables) {
        if (StrUtil.isBlank(content) || variables.isEmpty()) {
            return content;
        }

        // 遍历所有变量，进行替换
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            content = content.replace(placeholder, value);
        }

        return content;
    }
}
