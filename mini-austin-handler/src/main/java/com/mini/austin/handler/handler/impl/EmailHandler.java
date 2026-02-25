package com.mini.austin.handler.handler.impl;

import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.dto.model.EmailContentModel;
import com.mini.austin.common.enums.ChannelType;
import com.mini.austin.handler.handler.BaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮件发送 Handler
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class EmailHandler extends BaseHandler {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${mini-austin.mail.mock:true}")
    private boolean mockSend;

    public EmailHandler() {
        channelCode = ChannelType.EMAIL.getCode();
    }

    @Override
    protected boolean handler(TaskInfo taskInfo) {
        EmailContentModel contentModel = (EmailContentModel) taskInfo.getContentModel();

        try {
            // 未配置真实邮件账号/或开启模拟发送时，直接模拟
            if (mockSend || mailSender == null || fromEmail == null || fromEmail.isBlank() || "your-email@qq.com".equals(fromEmail)) {
                log.info("[模拟邮件发送] 收件人: {}, 标题: {}, 内容: {}",
                        taskInfo.getReceiver(), contentModel.getTitle(), contentModel.getContent());
                return true;
            }

            // 真实发送
            for (String receiver : taskInfo.getReceiver()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(receiver);
                message.setSubject(contentModel.getTitle());
                message.setText(contentModel.getContent());

                mailSender.send(message);
                log.info("邮件发送成功: to={}", receiver);
            }
            return true;

        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
