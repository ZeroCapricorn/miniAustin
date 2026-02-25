package com.mini.austin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author mini-austin
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    /**
     * 通知类消息（如系统通知）
     */
    NOTICE(10, "通知类"),

    /**
     * 营销类消息（如促销活动）
     */
    MARKETING(20, "营销类"),

    /**
     * 验证码
     */
    AUTH_CODE(30, "验证码"),

    ;

    private final Integer code;
    private final String description;

    public static MessageType getByCode(Integer code) {
        for (MessageType value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
