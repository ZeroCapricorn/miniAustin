package com.mini.austin.common.enums;

import com.mini.austin.common.domain.ContentModel;
import com.mini.austin.common.dto.model.EmailContentModel;
import com.mini.austin.common.dto.model.SmsContentModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发送渠道类型枚举
 *
 * @author mini-austin
 */
@Getter
@AllArgsConstructor
public enum ChannelType {

    /**
     * IM（预留）
     */
    IM(10, "IM", null),

    /**
     * Push推送（预留）
     */
    PUSH(20, "Push推送", null),

    /**
     * 短信
     */
    SMS(30, "短信", SmsContentModel.class),

    /**
     * 邮件
     */
    EMAIL(40, "邮件", EmailContentModel.class),

    ;

    /**
     * 渠道编码
     */
    private final Integer code;

    /**
     * 渠道描述
     */
    private final String description;

    /**
     * 内容模型Class
     */
    private final Class<? extends ContentModel> contentModelClass;

    /**
     * 根据code获取枚举
     */
    public static ChannelType getByCode(Integer code) {
        for (ChannelType value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据code获取内容模型Class
     */
    public static Class<? extends ContentModel> getContentModelClassByCode(Integer code) {
        ChannelType channelType = getByCode(code);
        return channelType != null ? channelType.getContentModelClass() : null;
    }
}
