package com.mini.austin.handler.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handler 线程池配置
 * <p>
 * ★★★ 面试重点：为什么需要线程池？ ★★★
 * <p>
 * 1. 控制并发数，防止系统过载
 * 2. 复用线程，减少创建销毁开销
 * 3. 解耦消费与处理，提高吞吐量
 *
 * @author mini-austin
 */
@Slf4j
@Configuration
public class HandlerThreadPoolConfig {

    public static final String HANDLER_PIPELINE_CODE = "handler";

    @Value("${mini-austin.thread-pool.core-size:4}")
    private int corePoolSize;

    @Value("${mini-austin.thread-pool.max-size:8}")
    private int maxPoolSize;

    @Value("${mini-austin.thread-pool.queue-capacity:128}")
    private int queueCapacity;

    @Value("${mini-austin.thread-pool.keep-alive:60}")
    private int keepAliveSeconds;

    /**
     * Handler 处理线程池
     * <p>
     * 面试考点：
     * - corePoolSize：核心线程数，始终保持存活
     * - maxPoolSize：最大线程数，队列满时才会创建
     * - queueCapacity：阻塞队列容量
     * - keepAliveSeconds：非核心线程空闲存活时间
     * - 拒绝策略：CallerRunsPolicy - 由调用者线程执行，起到削峰作用
     */
    @Bean
    public ThreadPoolExecutor handlerExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> new Thread(r, "handler-pool-" + r.hashCode()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        log.info("Handler线程池初始化: core={}, max={}, queue={}", 
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }
}
