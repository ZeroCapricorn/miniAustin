package com.mini.austin.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务编码枚举
 * <p>
 * 用于选择不同的责任链处理流程
 *
 * @author mini-austin
 */
@Getter
@AllArgsConstructor
public enum BusinessCode {

    /**
     * 普通发送
     */
    SEND("send", "普通发送"),

    /**
     * 撤回消息
     */
    RECALL("recall", "撤回消息"),

    ;

    private final String code;
    private final String description;
}
