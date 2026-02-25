package com.mini.austin.web.domain;

import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.dto.MessageParam;
import com.mini.austin.common.pipeline.ProcessModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发送任务模型
 * <p>
 * 在责任链中流转的数据对象，包含：
 * - 入参：messageTemplateId + messageParamList
 * - 中间产物：messageTemplate
 * - 出参：taskInfoList
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendTaskModel implements ProcessModel {

    /**
     * 消息模板ID
     */
    private Long messageTemplateId;

    /**
     * 发送参数列表
     */
    private List<MessageParam> messageParamList;

    /**
     * 消息模板（由 AssembleAction 查询填充）
     */
    private MessageTemplate messageTemplate;

    /**
     * 任务信息列表（由 AssembleAction 拼装生成）
     */
    private List<TaskInfo> taskInfoList;
}
