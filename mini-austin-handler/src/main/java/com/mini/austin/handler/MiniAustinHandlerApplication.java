package com.mini.austin.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mini Austin Handler 启动类
 * <p>
 * 负责消费MQ消息，执行去重、限流、路由发送等逻辑
 *
 * @author mini-austin
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.mini.austin")
public class MiniAustinHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniAustinHandlerApplication.class, args);
        log.info("====================================");
        log.info("  Mini Austin Handler 启动成功！");
        log.info("  开始监听消息队列...");
        log.info("====================================");
    }
}
