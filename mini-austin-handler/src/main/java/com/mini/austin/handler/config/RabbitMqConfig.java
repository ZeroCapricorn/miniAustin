package com.mini.austin.handler.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类（Handler端）
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

    @Bean
    public DirectExchange austinExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue austinQueue() {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Binding austinBinding(Queue austinQueue, DirectExchange austinExchange) {
        return BindingBuilder.bind(austinQueue).to(austinExchange).with(routingKey);
    }
}
