package com.mini.austin.handler.handler.impl;

import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.dto.model.SmsContentModel;
import com.mini.austin.common.enums.ChannelType;
import com.mini.austin.handler.handler.BaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信发送 Handler（模拟）
 * <p>
 * 由于短信发送需要接入运营商API且收费，这里使用日志模拟
 * 面试时可以说明：实际项目中接入腾讯云/阿里云短信SDK
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class SmsHandler extends BaseHandler {

    public SmsHandler() {
        channelCode = ChannelType.SMS.getCode();
    }

    @Override
    protected boolean handler(TaskInfo taskInfo) {
        SmsContentModel contentModel = (SmsContentModel) taskInfo.getContentModel();

        // 模拟发送短信
        for (String phone : taskInfo.getReceiver()) {
            log.info("========================================");
            log.info("[模拟短信发送]");
            log.info("  手机号: {}", phone);
            log.info("  内容: {}", contentModel.getContent());
            if (contentModel.getUrl() != null) {
                log.info("  链接: {}", contentModel.getUrl());
            }
            log.info("========================================");
        }

        return true;
    }
}
