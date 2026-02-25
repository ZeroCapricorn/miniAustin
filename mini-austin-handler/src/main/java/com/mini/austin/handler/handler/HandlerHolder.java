package com.mini.austin.handler.handler;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler 路由持有器
 * <p>
 * 维护 渠道编码 -> Handler 的映射关系
 * 消费消息时根据 sendChannel 找到对应的 Handler
 *
 * @author mini-austin
 */
@Component
public class HandlerHolder {

    /**
     * 渠道编码 -> Handler 映射
     */
    private final Map<Integer, Handler> handlers = new HashMap<>(16);

    /**
     * 注册 Handler
     */
    public void putHandler(Integer channelCode, Handler handler) {
        handlers.put(channelCode, handler);
    }

    /**
     * 根据渠道编码获取 Handler
     */
    public Handler route(Integer channelCode) {
        return handlers.get(channelCode);
    }
}
