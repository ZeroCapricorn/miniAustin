package com.mini.austin.web.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mini.austin.common.enums.RespStatusEnum;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import com.mini.austin.common.vo.BasicResultVO;
import com.mini.austin.web.domain.SendTaskModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 前置校验 Action
 * <p>
 * 责任链第一个节点，负责校验入参是否合法：
 * 1. messageTemplateId 不能为空
 * 2. messageParamList 不能为空
 * 3. 每个 MessageParam 的 receiver 不能为空
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class PreCheckAction implements BusinessProcess<SendTaskModel> {

    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        SendTaskModel sendTaskModel = context.getProcessModel();

        // 1. 校验模板ID
        Long messageTemplateId = sendTaskModel.getMessageTemplateId();
        if (messageTemplateId == null || messageTemplateId <= 0) {
            context.setNeedBreak(true);
            context.setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg() + "：模板ID不能为空"));
            log.warn("前置校验失败：模板ID为空");
            return;
        }

        // 2. 校验发送参数列表
        if (CollUtil.isEmpty(sendTaskModel.getMessageParamList())) {
            context.setNeedBreak(true);
            context.setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg() + "：发送参数不能为空"));
            log.warn("前置校验失败：发送参数列表为空");
            return;
        }

        // 3. 校验每个参数的接收者
        boolean hasEmptyReceiver = sendTaskModel.getMessageParamList().stream()
                .anyMatch(param -> StrUtil.isBlank(param.getReceiver()));
        if (hasEmptyReceiver) {
            context.setNeedBreak(true);
            context.setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg() + "：接收者不能为空"));
            log.warn("前置校验失败：存在空的接收者");
            return;
        }

        log.info("前置校验通过：templateId={}, paramCount={}", 
                messageTemplateId, sendTaskModel.getMessageParamList().size());
    }
}
