package com.mini.austin.web.service.impl;

import com.mini.austin.common.dto.MessageParam;
import com.mini.austin.common.enums.RespStatusEnum;
import com.mini.austin.common.pipeline.ProcessContext;
import com.mini.austin.common.pipeline.ProcessController;
import com.mini.austin.common.vo.BasicResultVO;
import com.mini.austin.web.domain.SendTaskModel;
import com.mini.austin.web.enums.BusinessCode;
import com.mini.austin.web.service.SendService;
import com.mini.austin.web.vo.SendRequest;
import com.mini.austin.web.vo.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 发送服务实现
 * <p>
 * ★★★ 责任链入口 ★★★
 * <p>
 * 将请求参数封装为 ProcessContext，交给 ProcessController 执行责任链
 *
 * @author mini-austin
 */
@Slf4j
@Service
public class SendServiceImpl implements SendService {

    @Autowired
    @Qualifier("sendProcessController")
    private ProcessController processController;

    @Override
    public SendResponse send(SendRequest request) {
        // 参数校验
        if (request == null || request.getMessageTemplateId() == null) {
            return SendResponse.builder()
                    .code(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode())
                    .msg(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg())
                    .build();
        }

        // 构建发送任务模型
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(request.getMessageTemplateId())
                .messageParamList(Collections.singletonList(request.getMessageParam()))
                .build();

        // 构建责任链上下文
        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code(BusinessCode.SEND.getCode())
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // ★ 执行责任链
        ProcessContext<SendTaskModel> result = processController.process(context);

        // 封装响应
        return buildResponse(result);
    }

    @Override
    public SendResponse batchSend(SendRequest request) {
        // 参数校验
        if (request == null || request.getMessageTemplateId() == null) {
            return SendResponse.builder()
                    .code(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode())
                    .msg(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg())
                    .build();
        }

        // 构建发送任务模型
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(request.getMessageTemplateId())
                .messageParamList(request.getMessageParamList())
                .build();

        // 构建责任链上下文
        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code(BusinessCode.SEND.getCode())
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // ★ 执行责任链
        ProcessContext<SendTaskModel> result = processController.process(context);

        // 封装响应
        return buildResponse(result);
    }

    /**
     * 构建响应
     */
    @SuppressWarnings("unchecked")
    private SendResponse buildResponse(ProcessContext<SendTaskModel> context) {
        BasicResultVO<?> response = context.getResponse();

        SendResponse sendResponse = SendResponse.builder()
                .code(response.getStatus())
                .msg(response.getMsg())
                .build();

        // 如果成功，提取 messageIds
        if (RespStatusEnum.SUCCESS.getCode().equals(response.getStatus()) && response.getData() != null) {
            sendResponse.setMessageIds((List<String>) response.getData());
        }

        return sendResponse;
    }
}
