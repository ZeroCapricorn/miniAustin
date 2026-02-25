package com.mini.austin.handler.receiver;

import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.pipeline.ProcessContext;
import com.mini.austin.common.pipeline.ProcessController;
import com.mini.austin.common.vo.BasicResultVO;
import com.mini.austin.handler.config.HandlerThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 消费服务实现
 * <p>
 * ★★★ Handler 层核心入口 ★★★
 * <p>
 * 1. 接收 TaskInfo
 * 2. 提交到线程池异步处理
 * 3. 执行 Handler 层责任链
 *
 * @author mini-austin
 */
@Slf4j
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Autowired
    @Qualifier("handlerProcessController")
    private ProcessController processController;

    @Autowired
    private ThreadPoolExecutor handlerExecutor;

    @Override
    public void consume(TaskInfo taskInfo) {
        log.info("开始处理消息: messageId={}, channel={}, receivers={}",
                taskInfo.getMessageId(), taskInfo.getSendChannel(), taskInfo.getReceiver());

        // 提交到线程池异步处理
        handlerExecutor.execute(() -> {
            try {
                // 构建责任链上下文
                ProcessContext<TaskInfo> context = ProcessContext.<TaskInfo>builder()
                        .code(HandlerThreadPoolConfig.HANDLER_PIPELINE_CODE)
                        .processModel(taskInfo)
                        .needBreak(false)
                        .response(BasicResultVO.success())
                        .build();

                // 执行责任链
                processController.process(context);

            } catch (Exception e) {
                log.error("处理消息异常: messageId={}", taskInfo.getMessageId(), e);
            }
        });
    }
}
