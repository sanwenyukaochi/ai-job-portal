# AI Job Portal 工程约束

本文件作用于整个仓库。子目录中的 `AGENTS.md` 只能补充局部规则，不能放宽这里的阶段门禁和架构边界。

## 项目目标

从招聘业务出发重新设计 AI Job Portal，而不是把旧项目按新目录搬运。旧项目只提供业务线索和兼容性证据，Food Ordering System 只提供六边形依赖方向参考；旧服务、表、DTO 和模块都不是默认的目标边界。

- 目标仓库：`/Users/sanwenyukaochi/Desktop/JAVA_PROJECT/ai-job-portal`
- 旧项目（只读业务证据）：`/Users/sanwenyukaochi/Downloads/source code ai job portal/backend`
- 架构参考（只读）：`/Users/sanwenyukaochi/Desktop/JAVA_PROJECT/food-ordering-system`

## 六阶段 Spec 流程

每项业务能力必须在 `docs/specs/NNNN-<name>.md` 中严格依次完成以下阶段：

1. **业务功能分析**：参与者、目标、业务场景、业务价值、现状证据、术语和待确认问题。
2. **业务规则叙述**：用业务语言编号为 `BR-*`，给出前置条件、结果、拒绝条件、边界示例；此时不讨论 Java 或数据库实现。
3. **领域模型设计**：识别子域、聚合根、实体、值对象、领域服务、领域事件、策略和不变量，并追溯到 `BR-*`。
4. **边界范围确认**：确认限界上下文、上下文关系、数据所有权、事务/一致性边界、输入输出端口和外部契约；到此阶段才能决定微服务与 Gradle 模块。
5. **测试用例编写**：用 `TC-*` 覆盖业务规则，先写自动化测试并记录预期的失败结果；不得编写生产业务逻辑。
6. **功能实现**：只实现让已确认测试通过的最小代码，记录验证结果并回查规则、模型和边界。

### 不可绕过的讨论门禁

- 每次只处理当前阶段，不得提前生成下一阶段的结论、测试或代码。
- 当前阶段完成草案后必须停下，与用户讨论问题、取舍和修订。
- 只有用户明确表达“确认第 N 阶段”或同等明确意思，且内容不存在已知矛盾时，才能在 spec 的讨论记录中标记 `Confirmed` 并进入下一阶段。
- 沉默、模糊认可、无关任务或单纯要求加快进度，都不视为阶段确认。
- 用户提出修订时留在当前阶段，更新文档后再次讨论。
- 第 5 阶段必须展示测试清单和实际 Red 结果；用户确认覆盖范围后才能开始第 6 阶段。
- 第 6 阶段完成后仍需讨论实现与验证结果，经用户确认后才可标记整个 spec 完成。

阶段状态、模板和讨论记录格式见 `docs/README.md` 与 `docs/specs/_template.md`。

## 六边形架构边界

以下是全局技术约束，不替代第 3、4 阶段的领域与边界设计：

- `domain-core`：实体、聚合、值对象、领域服务、领域事件和领域异常；仅依赖 JDK 和经确认的通用领域原语。
- `application-service`：用例、命令/查询、输入端口、输出端口；依赖 `domain-core`，不依赖具体适配器。
- 入站适配器：REST、消息消费者等；负责协议、认证上下文和输入校验，然后调用输入端口。
- 出站适配器：JPA、HTTP 客户端、Kafka、邮件、AI Provider 等；实现应用层输出端口。
- `container`：Spring Boot 启动、配置和装配；它是唯一同时依赖应用层与各适配器的模块。

依赖只能向内：`adapter -> application-service -> domain-core`。领域层不得导入 Spring、Jakarta Persistence、Kafka、Feign、Jackson 或数据库类型。

## REST 响应规范

所有 REST 入站适配器必须遵守 `docs/architecture/rest-api-conventions.md`：

- 成功响应使用 HTTP 状态码表达结果，直接返回资源，不使用全局 `ApiResponse<T>` 包装。
- 创建资源返回 `201 Created` 和 `Location`；成功删除且无响应内容时返回 `204 No Content`。
- 错误响应使用 RFC 9457 `application/problem+json`，通过稳定的 `type` 和 `code` 表达机器可读语义。
- 不得返回 `200 OK` 错误体，不得在 `500` 响应中暴露异常消息、堆栈、SQL 或内部服务信息。
- HTTP 状态、Problem Details 和协议 DTO 只存在于入站适配器，不得进入应用层或领域层。

## 上下文与微服务规则

- 旧项目的一个 service 不自动等于一个限界上下文；限界上下文也不自动等于一个部署单元。
- 边界必须由业务语言、业务规则、一致性需求和团队自治性共同决定。
- 服务之间不得共享领域实体、JPA Entity、内部 DTO 或 Repository。
- 同步协作通过明确的 HTTP 输出端口；异步协作通过版本化消息契约。
- `common` 只允许稳定、无明确业务归属的原语，不得重建共享 DTO/枚举大杂烩。
- 项目不使用 Spring Modulith，也不创建 `shared OPEN`；模块级物理隔离由 Gradle 模块负责，代码级边界由 ArchUnit + Taikai 验证。
- 只有第 4 阶段确认的稳定跨模块契约才能通过专用 API 暴露，不得公开整个领域模型包。
- 每个确认后的服务拥有自己的数据模型；跨服务不得建立数据库外键。
- Outbox、Saga、缓存和重试只在业务规则及边界 spec 明确需要时引入。

## 实现规则

- 使用 Java 26、Gradle Kotlin DSL 和当前版本目录。
- 未完成第 4 阶段，不创建业务模块；未完成第 5 阶段，不写生产业务逻辑。
- 业务规则放在领域模型或领域服务中，不放在 Controller、Persistence Mapper 或外部客户端中。
- 端口按业务能力命名，例如 `LoadJobPort`，不按框架命名。
- 持久化实体与领域实体分离，映射留在 persistence adapter。
- 对外错误、认证、输入校验不得为了简化而省略。
- 不创建“以后可能会用”的接口、工厂、基类、配置或模块。

## 验证要求

- 每个 `BR-*` 至少映射一个 `TC-*`，每个 `TC-*` 能追溯到测试代码。
- 领域规则使用不启动 Spring 的单元测试。
- 应用用例使用输出端口 fake/mock，不连接真实基础设施。
- 适配器测试覆盖映射、序列化、数据库约束和协议边界。
- 架构测试遵守 `docs/architecture/architecture-testing.md`，使用 ArchUnit + Taikai 阻止反向依赖、循环依赖和框架越界。
- 每个确认后的部署单元都必须包含可执行的 `ArchitectureTests`，并启用 `failOnEmpty(true)`，禁止空规则集静默通过。
- 实现阶段至少运行相关测试、`./gradlew spotlessCheck` 和 `./gradlew check`；结果写回 spec。

## 修改纪律

- 保持改动小而完整，不顺带设计未进入当前 spec 的能力。
- 不修改两个参考项目。
- 不提交密钥、真实账号、生成目录或 IDE 文件。
- 不覆盖用户未提交的改动；发现冲突先停下说明。
