package com.mini.austin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态枚举
 *
 * @author mini-austin
 */
@Getter
@AllArgsConstructor
public enum RespStatusEnum {

    /**
     * 成功
     */
    SUCCESS("00000", "操作成功"),

    /**
     * 失败
     */
    FAIL("99999", "操作失败"),

    /**
     * 客户端参数错误
     */
    CLIENT_BAD_PARAMETERS("A0001", "客户端参数错误"),

    /**
     * 消息模板不存在
     */
    TEMPLATE_NOT_FOUND("B0001", "消息模板不存在"),

    /**
     * 消息模板已被删除
     */
    TEMPLATE_DELETED("B0002", "消息模板已被删除"),

    /**
     * 服务内部错误
     */
    SERVICE_ERROR("C0001", "服务内部错误"),

    ;

    private final String code;
    private final String msg;
}
