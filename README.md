# Mini Austin - 消息推送平台

> 一个简化版的多渠道消息推送平台，用于学习分布式系统设计。

## 项目结构

```
miniAustin/
├── mini-austin-common/      # 公共模块（POJO、枚举、责任链框架）
├── mini-austin-web/         # Web接口模块（接收请求、投递MQ）
├── mini-austin-handler/     # 消息处理模块（消费MQ、去重、发送）
└── doc/
    └── sql/                 # 数据库脚本
```

## 核心设计

### 1. 责任链模式

- `BusinessProcess`: 处理节点接口
- `ProcessTemplate`: 处理链模板
- `ProcessController`: 流程控制器

### 2. 消息队列解耦

- API层：接收请求 → 责任链处理 → 投递MQ
- Handler层：消费MQ → 去重/限流 → 路由发送

### 3. 策略模式 + 模板方法

- `Handler`: 统一发送接口
- `BaseHandler`: 公共处理逻辑
- `XxxHandler`: 各渠道具体实现

## 快速开始

### 1. 初始化数据库

```sql
-- 执行 doc/sql/init.sql
```

### 2. 修改配置

```yaml
# mini-austin-web/src/main/resources/application.yml
# 修改 MySQL 和 Redis 连接信息
```

### 3. 启动服务

```bash
# 启动 Web 模块
cd mini-austin-web
mvn spring-boot:run

# 启动 Handler 模块（新终端）
cd mini-austin-handler
mvn spring-boot:run
```

### 4. 测试接口

```bash
# 查询模板列表
curl http://localhost:8080/messageTemplate/list

# 发送消息（待实现）
curl -X POST http://localhost:8080/send -H "Content-Type: application/json" -d '{
  "messageTemplateId": 1,
  "messageParam": {
    "receiver": "13800138000",
    "variables": {"code": "123456"}
  }
}'
```

## 学习路线

1. **Phase 1**: 理解项目结构和核心模型
2. **Phase 2**: 实现责任链模式（重点）
3. **Phase 3**: 实现MQ解耦
4. **Phase 4**: 实现Handler路由
5. **Phase 5**: 实现去重/限流

## 技术栈

- Spring Boot 2.7
- Spring Data JPA
- Redis (MQ + 去重)
- MySQL
- Hutool / Guava
