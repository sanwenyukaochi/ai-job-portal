# 目标六边形架构

本文只规定已经确认的全局依赖原则。具体聚合属于第 3 阶段结论，限界上下文、微服务和 Gradle 模块属于第 4 阶段结论；不得从旧项目目录直接推导。

## 核心原则

业务核心定义端口，外部技术实现端口。数据库、Web、消息、认证和第三方 AI 都是可替换适配器，不得反向塑造领域模型。

```text
HTTP / Message In
        |
        v
 inbound adapter -> input port -> application use case -> domain core
                                      |
                                      v
                                  output port
                                 /     |      \
                              JPA    HTTP    Kafka/AI
```

## 层级职责

| 层级 | 可以包含 | 禁止包含 |
|---|---|---|
| Domain Core | 聚合、实体、值对象、领域服务、领域事件、业务异常 | Spring Bean、JPA 注解、Controller、DTO、Feign、Kafka |
| Application Service | 用例、命令/查询、输入端口、输出端口、事务意图 | JPA Repository、HTTP/Kafka 实现、协议状态码 |
| Inbound Adapter | REST/消息输入、协议 DTO、鉴权上下文、边界校验 | 核心业务决策、直接持久化 |
| Outbound Adapter | Persistence Entity、Mapper、JPA、HTTP Client、Publisher、AI Client | 用例编排、跨聚合业务规则 |
| Container | 应用入口、Bean 装配、运行配置 | 业务规则 |

## 允许的依赖

```text
domain-core                  -> JDK / approved common-domain primitives
application-service         -> domain-core
inbound-*                   -> application-service
outbound-*                  -> application-service + domain-core
container                   -> application-service + required adapters
```

同层适配器之间不得互相依赖。Application Service 通过输出端口协调外部能力；Container 是唯一装配点。

## 微服务协作

- 限界上下文是业务语言边界，微服务是部署边界，两者不得未经讨论自动画等号。
- 每个微服务拥有自己的数据库表和领域模型。
- HTTP 调用和消息发布都必须位于出站适配器之后。
- 远程返回模型在适配器边界转换为应用层需要的数据，不向领域层泄漏共享 DTO。
- 消息契约必须包含版本，并由生产者/消费者契约测试保护。
- 只有“数据库提交与消息发布必须原子一致”的用例才引入 Outbox。
- 只有跨服务、长事务且存在补偿行为的流程才引入 Saga。

## 通用代码边界

允许共享：基础 ID 类型、Money 等确实跨上下文稳定的值类型、通用错误协议、消息基础设施。

禁止共享：User、Job、Application 等领域实体，服务专属枚举，JPA Entity，Repository，业务请求/响应 DTO。重复两个小 DTO 优于制造跨服务编译耦合。

项目不使用 Spring Modulith，也不创建 `shared OPEN`。模块级物理隔离由 Gradle 模块负责，代码级边界由 ArchUnit + Taikai 验证；只有已确认的稳定跨模块契约可以通过专用 API 暴露，不得把整个领域模型包作为公共接口。

## 约束验证

- Gradle 模块依赖负责第一层隔离。
- ArchUnit 禁止领域包导入框架和 adapter 包。
- Domain Core 测试不得使用 Spring Test。
- Persistence 测试验证领域对象与数据库对象的双向映射和数据库约束。
