# Mini Austin - 分布式消息推送平台
[![OSCS Status](https://www.oscs1024.com/platform/badge/ZeroCapricorn/miniAustin.svg)](https://www.oscs1024.com/project/ZeroCapricorn/miniAustin)
> 一个简化版的多渠道消息推送平台，基于 Spring Boot + RabbitMQ + Redis 构建。
> 专注于分布式系统中的解耦、削峰填谷、去重幂等与责任链扩展设计。
---
##  架构亮点
### 1. 异步解耦与削峰填谷
引入 **RabbitMQ** 将系统划分为 接入层（Web）与 推送层（Handler）。
- **接入层**：专注于参数校验与快速响应，避免同步调用导致的阻塞。
- **推送层**：负责耗时的第三方接口调用，通过 MQ 缓冲流量，保护下游服务。
### 2. 高度可扩展的流程编排引擎
- 核心设计：**责任链模式 (Chain of Responsibility)**
- 实现：将复杂的业务逻辑（校验、去重、参数拼装）拆解为独立的 Action 原子节点。
- 优势：通过 PipelineConfig 动态配置处理链，新增业务逻辑（如
夜间屏蔽）只需挂载新节点，实现核心代码 **零侵入**。
### 3. 多渠道统一分发策略
- 核心设计：**策略模式 (Strategy) + 模板方法模式 (Template Method)**
- 实现：
    - HandlerHolder：建立 channelCode 到 Handler 的路由映射。
    - BaseHandler：沉淀通用的限流、日志逻辑。
    - SmsHandler/EmailHandler：仅关注具体的 API 调用。
- 优势：消除了大量的 if-else 判断，支持新渠道插件式接入。
### 4. 高并发去重与防抖动
- 核心设计：**分布式幂等性控制**
- 实现：基于 **Redis** 存储消息指纹（SHA-256摘要）。
- 机制：对 模板ID + 接收者 + 内容 生成唯一 Key，在指定时间窗口内拦截重复请求，防止上游重试导致的用户打扰。
---

