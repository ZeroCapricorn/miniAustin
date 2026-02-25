package com.mini.austin.handler.receiver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.austin.common.domain.ContentModel;
import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RabbitMQ 消息消费者
 * <p>
 * 监听发送队列，消费消息并执行处理流程
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class RabbitMqReceiver {

    @Autowired
    private ConsumeService consumeService;

    private final ObjectMapper objectMapper;

    public RabbitMqReceiver() {
        this.objectMapper = new ObjectMapper();
        // 忽略未知属性，避免反序列化失败
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 监听发送队列
     * <p>
     * 收到消息后：
     * 1. 反序列化为 TaskInfo 列表
     * 2. 交给 ConsumeService 处理
     */
    @RabbitListener(queues = "${mini-austin.mq.queue:austin-send-queue}")
    public void receive(String message) {
        log.info("收到MQ消息: {}", message);

        try {
            // 先读取为 JsonNode 数组
            List<JsonNode> jsonNodes = objectMapper.readValue(message, new TypeReference<List<JsonNode>>() {});

            for (JsonNode taskJson : jsonNodes) {
                // 获取渠道类型
                JsonNode channelNode = taskJson.get("sendChannel");
                Integer sendChannel = (channelNode != null && !channelNode.isNull()) ? channelNode.asInt() : null;

                // 先解析 contentModel（根据渠道类型）
                ContentModel contentModel = null;
                if (sendChannel != null) {
                    Class<? extends ContentModel> contentClass = ChannelType.getContentModelClassByCode(sendChannel);
                    JsonNode contentJson = taskJson.get("contentModel");
                    if (contentClass != null && contentJson != null && !contentJson.isNull()) {
                        contentModel = objectMapper.treeToValue(contentJson, contentClass);
                    }
                }

                // 构建 TaskInfo（手动填充避免接口反序列化问题）
                TaskInfo taskInfo = TaskInfo.builder()
                        .messageId(getTextValue(taskJson, "messageId"))
                        .businessId(getLongValue(taskJson, "businessId"))
                        .messageTemplateId(getLongValue(taskJson, "messageTemplateId"))
                        .sendChannel(sendChannel)
                        .msgType(getIntValue(taskJson, "msgType"))
                        .sendAccount(getIntValue(taskJson, "sendAccount"))
                        .contentModel(contentModel)
                        .build();

                // 解析 receiver 集合
                JsonNode receiverNode = taskJson.get("receiver");
                if (receiverNode != null && receiverNode.isArray()) {
                    java.util.Set<String> receivers = new java.util.HashSet<>();
                    for (JsonNode r : receiverNode) {
                        receivers.add(r.asText());
                    }
                    taskInfo.setReceiver(receivers);
                }

                consumeService.consume(taskInfo);
            }

        } catch (Exception e) {
            log.error("消费MQ消息失败: {}", e.getMessage(), e);
        }
    }

    private String getTextValue(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asText() : null;
    }

    private Long getLongValue(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asLong() : null;
    }

    private Integer getIntValue(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asInt() : null;
    }
}
