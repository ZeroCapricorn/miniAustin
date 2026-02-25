package com.mini.austin.web.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * <p>
 * 定义交换机、队列、绑定关系
 *
 * @author mini-austin
 */
@Configuration
public class RabbitMqConfig {

    @Value("${mini-austin.mq.exchange:austin-exchange}")
    private String exchange;

    @Value("${mini-austin.mq.queue:austin-send-queue}")
    private String queue;

    @Value("${mini-austin.mq.routing-key:austin.send}")
    private String routingKey;

    /**
     * 声明交换机（Direct类型）
     */
    @Bean
    public DirectExchange austinExchange() {
        return new DirectExchange(exchange, true, false);
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue austinQueue() {
        return QueueBuilder.durable(queue).build();
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding austinBinding(Queue austinQueue, DirectExchange austinExchange) {
        return BindingBuilder.bind(austinQueue).to(austinExchange).with(routingKey);
    }
}
