package com.mini.austin.web.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ 发送服务
 * <p>
 * 封装 RabbitTemplate，提供统一的消息发送接口
 *
 * @author mini-austin
 */
@Slf4j
@Service
public class RabbitMqService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${mini-austin.mq.exchange:austin-exchange}")
    private String exchange;

    @Value("${mini-austin.mq.routing-key:austin.send}")
    private String routingKey;

    /**
     * 发送消息到 RabbitMQ
     *
     * @param message 消息内容（JSON字符串）
     */
    public void send(String message) {
        log.debug("发送消息到RabbitMQ: exchange={}, routingKey={}, message={}", 
                exchange, routingKey, message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 发送消息到指定的 exchange 和 routingKey
     */
    public void send(String exchange, String routingKey, String message) {
        log.debug("发送消息到RabbitMQ: exchange={}, routingKey={}, message={}", 
                exchange, routingKey, message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
