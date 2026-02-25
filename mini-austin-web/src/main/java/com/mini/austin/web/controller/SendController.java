package com.mini.austin.web.controller;

import com.mini.austin.web.service.SendService;
import com.mini.austin.web.vo.SendRequest;
import com.mini.austin.web.vo.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送消息 Controller
 * <p>
 * 提供消息发送的 HTTP 接口
 *
 * @author mini-austin
 */
@Slf4j
@RestController
@RequestMapping("/send")
public class SendController {

    @Autowired
    private SendService sendService;

    /**
     * 单条发送
     * <p>
     * 请求示例：
     * POST /send
     * {
     *   "messageTemplateId": 1,
     *   "messageParam": {
     *     "receiver": "test@example.com",
     *     "variables": {"code": "123456"}
     *   }
     * }
     */
    @PostMapping
    public SendResponse send(@RequestBody SendRequest request) {
        log.info("收到发送请求：templateId={}, receiver={}",
                request.getMessageTemplateId(),
                request.getMessageParam() != null ? request.getMessageParam().getReceiver() : null);
        return sendService.send(request);
    }

    /**
     * 批量发送
     * <p>
     * 请求示例：
     * POST /send/batch
     * {
     *   "messageTemplateId": 1,
     *   "messageParamList": [
     *     {"receiver": "user1@example.com", "variables": {"name": "张三"}},
     *     {"receiver": "user2@example.com", "variables": {"name": "李四"}}
     *   ]
     * }
     */
    @PostMapping("/batch")
    public SendResponse batchSend(@RequestBody SendRequest request) {
        log.info("收到批量发送请求：templateId={}, count={}",
                request.getMessageTemplateId(),
                request.getMessageParamList() != null ? request.getMessageParamList().size() : 0);
        return sendService.batchSend(request);
    }
}
