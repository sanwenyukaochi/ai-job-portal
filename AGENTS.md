# AI Job Portal 工程约束

本文件作用于整个仓库。子目录若存在更具体的 `AGENTS.md`，只能补充局部规则，不能放宽这里的架构边界。

## 项目目标

将旧版 AI Job Portal 增量重写为 Gradle 多模块、微服务、六边形架构项目。业务行为来自旧项目，依赖方向参考 Food Ordering System；禁止机械复制旧实现或参考项目的业务代码。

- 目标仓库：`/Users/sanwenyukaochi/Desktop/JAVA_PROJECT/ai-job-portal`
- 旧项目（只读业务参考）：`/Users/sanwenyukaochi/Downloads/source code ai job portal/backend`
- 架构参考（只读）：`/Users/sanwenyukaochi/Desktop/JAVA_PROJECT/food-ordering-system`

## Spec 驱动流程

1. 开始实现前，先创建或更新 `docs/specs/NNNN-<name>.md`。
2. Spec 必须包含目标、非目标、行为、接口/数据影响、验收标准和未决问题。
3. `Draft` spec 只能用于调研和文档；用户确认或明确要求实现后改为 `Accepted`。
4. 实现必须能追溯到 Accepted spec 的验收标准；范围变化先改 spec，再改代码。
5. 完成实现和验证后，将状态改为 `Implemented`，并记录实际验证命令。
6. 架构现状更新到 `docs/architecture/`；旧系统映射更新到 `docs/migration/`。不要把计划藏在代码注释或聊天记录里。

文档入口和状态定义见 `docs/README.md`。

## 六边形架构边界

- `domain-core`：实体、聚合、值对象、领域服务、领域事件和领域异常；仅依赖 JDK 和必要的通用领域原语。
- `application-service`：用例、命令/查询、输入端口、输出端口；依赖 `domain-core`，不依赖具体适配器。
- 入站适配器：REST、消息消费者等；负责协议、认证上下文和输入校验，然后调用输入端口。
- 出站适配器：JPA、HTTP 客户端、Kafka、邮件、AI Provider 等；实现应用层输出端口。
- `container`：Spring Boot 启动、配置和装配；它是唯一同时依赖应用层与各适配器的模块。

依赖只能向内：`adapter -> application-service -> domain-core`。领域层不得导入 Spring、Jakarta Persistence、Kafka、Feign、Jackson 或数据库类型。

## 微服务边界

- 服务之间不得通过 Gradle 依赖共享领域实体、JPA Entity、内部 DTO 或 Repository。
- 同步协作通过明确的 HTTP 输出端口；异步协作通过版本化消息契约。
- `common` 只允许稳定、无业务归属的原语。不要重建旧项目的共享 DTO/枚举大杂烩。
- 每个服务拥有自己的数据模型；跨服务 ID 使用值对象表达，但不建立跨库外键。
- Outbox、Saga、缓存和重试只在 spec 明确需要一致性或性能保证时引入。

## 实现规则

- 使用 Java 26、Gradle Kotlin DSL 和当前版本目录；新增依赖先复用已有版本与 Spring 原生能力。
- 先完成一个可运行的纵向切片，再复制成熟结构；不要一次生成所有服务空壳。
- 业务规则放在领域模型或领域服务中，不放在 Controller、JPA Entity mapper 或 Feign/Kafka 适配器中。
- 端口按业务能力命名，不按框架命名，例如 `LoadJobPort`，而不是 `JobFeignService`。
- 持久化实体与领域实体分离；映射留在 persistence adapter。
- 对外错误、认证、输入校验不得为了简化而省略。
- 保留旧 API 兼容性必须由对应 spec 明确声明；没有声明时不得猜测兼容策略。
- 不创建“以后可能会用”的接口、工厂、基类、配置或模块。

## 验证要求

- 领域规则：纯单元测试，不启动 Spring。
- 应用用例：使用输出端口 fake/mock 验证，不连接真实基础设施。
- 适配器：仅为映射、序列化、数据库和协议边界编写集成测试。
- 架构边界：使用 ArchUnit 或 Gradle 模块依赖检查，防止向外依赖。
- 提交前至少运行与改动相关的测试、`./gradlew spotlessCheck` 和 `./gradlew check`；无法运行时在 spec 中记录原因。

## 修改纪律

- 保持改动小而完整，不顺带重构未进入当前 spec 的服务。
- 不修改两个参考项目。
- 不提交密钥、真实账号、生成目录或 IDE 文件。
- 不覆盖用户未提交的改动；发现冲突先停下说明。

