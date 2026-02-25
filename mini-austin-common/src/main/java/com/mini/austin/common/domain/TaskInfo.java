package com.mini.austin.common.domain;

import com.mini.austin.common.pipeline.ProcessModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 发送任务信息 - 核心运行时对象
 * <p>
 * 当API层接收到发送请求后，会将 MessageTemplate + MessageParam 拼装成 TaskInfo
 * 然后投递到MQ，Handler层消费后进行去重、限流、发送等操作
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfo implements ProcessModel {

    /**
     * 消息唯一标识（UUID）
     * 用于链路追踪
     */
    private String messageId;

    /**
     * 业务ID（模板类型 + 模板ID + 日期）
     * 用于数据统计聚合
     */
    private Long businessId;

    /**
     * 消息模板ID
     */
    private Long messageTemplateId;

    /**
     * 接收者集合（手机号/邮箱/userId等）
     */
    private Set<String> receiver;

    /**
     * 发送渠道类型
     * @see com.mini.austin.common.enums.ChannelType
     */
    private Integer sendChannel;

    /**
     * 消息类型（通知/营销/验证码）
     * @see com.mini.austin.common.enums.MessageType
     */
    private Integer msgType;

    /**
     * 消息内容模型（不同渠道有不同的内容结构）
     */
    private ContentModel contentModel;

    /**
     * 发送账号ID（比如多个邮箱账号轮询）
     */
    private Integer sendAccount;

}
