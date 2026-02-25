package com.mini.austin.web.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.enums.RespStatusEnum;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import com.mini.austin.common.vo.BasicResultVO;
import com.mini.austin.web.domain.SendTaskModel;
import com.mini.austin.web.mq.RabbitMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 发送MQ Action
 * <p>
 * 责任链最后一个节点，负责：
 * 1. 将 TaskInfo 序列化为 JSON
 * 2. 投递到 RabbitMQ
 * 3. 返回 messageId 列表给调用方
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class SendMqAction implements BusinessProcess<SendTaskModel> {

    @Autowired
    private RabbitMqService rabbitMqService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        SendTaskModel sendTaskModel = context.getProcessModel();
        List<TaskInfo> taskInfoList = sendTaskModel.getTaskInfoList();

        try {
            // 1. 序列化 TaskInfo 列表（使用 Jackson）
            String message = objectMapper.writeValueAsString(taskInfoList);

            // 2. 投递到 RabbitMQ
            rabbitMqService.send(message);

            // 3. 提取所有 messageId 作为响应
            List<String> messageIds = taskInfoList.stream()
                    .map(TaskInfo::getMessageId)
                    .collect(Collectors.toList());

            context.setResponse(BasicResultVO.success(messageIds));

            log.info("消息投递MQ成功：messageIds={}", messageIds);

        } catch (Exception e) {
            context.setNeedBreak(true);
            context.setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR.getMsg() + "：MQ投递失败"));
            log.error("消息投递MQ失败", e);
        }
    }
}
