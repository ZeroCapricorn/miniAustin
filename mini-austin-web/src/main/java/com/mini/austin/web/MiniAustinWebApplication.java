package com.mini.austin.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mini Austin Web 启动类
 * <p>
 * 负责接收发送请求，执行API层责任链，将消息投递到MQ
 *
 * @author mini-austin
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.mini.austin")
public class MiniAustinWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniAustinWebApplication.class, args);
        log.info("====================================");
        log.info("  Mini Austin Web 启动成功！");
        log.info("  API文档：http://localhost:8080/doc.html");
        log.info("====================================");
    }
}
