package com.mini.austin.web.vo;

import com.mini.austin.common.dto.MessageParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发送请求 VO
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendRequest {

    /**
     * 业务编码（默认为 send）
     */
    private String code;

    /**
     * 消息模板ID
     */
    private Long messageTemplateId;

    /**
     * 发送参数（单条）
     */
    private MessageParam messageParam;

    /**
     * 发送参数列表（批量）
     */
    private List<MessageParam> messageParamList;
}
