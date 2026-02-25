package com.mini.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发送响应 VO
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendResponse {

    /**
     * 状态码
     */
    private String code;

    /**
     * 响应信息
     */
    private String msg;

    /**
     * 消息ID列表（用于追踪）
     */
    private List<String> messageIds;
}
