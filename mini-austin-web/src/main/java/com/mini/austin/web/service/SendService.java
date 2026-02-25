package com.mini.austin.web.service;

import com.mini.austin.web.vo.SendRequest;
import com.mini.austin.web.vo.SendResponse;

/**
 * 发送服务接口
 *
 * @author mini-austin
 */
public interface SendService {

    /**
     * 单条发送
     *
     * @param request 发送请求
     * @return 发送响应
     */
    SendResponse send(SendRequest request);

    /**
     * 批量发送
     *
     * @param request 发送请求
     * @return 发送响应
     */
    SendResponse batchSend(SendRequest request);
}
