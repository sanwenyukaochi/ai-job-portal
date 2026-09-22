# 测试策略与组织规范

- 状态：Confirmed
- 确认日期：2026-09-23
- 适用范围：所有业务上下文、适配器和部署单元

## 目的

测试按职责分层，而不是让一种工具覆盖所有问题。JUnit Jupiter 是默认测试工具；Cucumber 只把已经确认的关键业务规则表达为可执行验收规范；Testcontainers 验证真实基础设施边界；ArchUnit + Taikai 验证架构依赖。

`food-ordering-system` 只提供部分测试意图参考。其少量 `@SpringBootTest` 测试不能作为本项目的完整测试结构模板，本项目必须补齐领域单元测试、应用用例测试、Adapter 测试、验收测试和架构测试之间的职责边界。

参考资料：

- [Cucumber BDD](https://cucumber.io/docs/bdd/)
- [Cucumber Better Gherkin](https://cucumber.io/docs/bdd/better-gherkin/)
- [Cucumber State](https://cucumber.io/docs/cucumber/state/)
- [Cucumber Anti-patterns](https://cucumber.io/docs/guides/anti-patterns/?lang=java)
- [Cucumber JUnit Platform Engine](https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-junit-platform-engine/README.md)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
- [Spring Boot Testcontainers](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
- [Gradle JVM Test Suite](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html)

## 测试分层

| 层级 | 默认工具 | 所属模块 | 主要职责 |
|---|---|---|---|
| Domain 单元测试 | JUnit Jupiter | `domain-core` | 聚合不变量、值对象、状态转换、领域事件和失败规则 |
| Application 单元测试 | JUnit Jupiter + fake/mock | `application-service` | 用例编排、输出端口交互、业务错误传播 |
| Web Adapter 测试 | `@WebMvcTest` + MockMvc | `adapters/in/web` | 请求绑定、校验、状态码、RFC 9457 和协议映射 |
| Persistence Adapter 集成测试 | `@DataJpaTest` + Testcontainers | `adapters/out/persistence` | ORM 映射、查询、约束、事务、锁和迁移脚本 |
| HTTP/Messaging Adapter 测试 | JUnit + 必要的测试基础设施 | 对应 adapter | 序列化、协议映射、消费、发布、幂等和失败处理 |
| 装配集成测试 | `@SpringBootTest` + Testcontainers | `container` | Spring 配置、Bean 装配和跨 Adapter 协作 |
| 业务验收测试 | Cucumber + JUnit Platform | `container` 的 `acceptanceTest` 测试集 | 从公开入站边界验证关键业务结果 |
| 架构测试 | ArchUnit + Taikai | 每个部署单元 | 六边形依赖、循环依赖和框架越界 |

服务间 HTTP 或消息契约测试在对应 spec 第 4 阶段确认外部契约后设计。Cucumber 不替代消费者/提供者契约验证。

## 各层规则

### Domain Core

- 不启动 Spring，不连接数据库，不使用容器。
- 直接构造聚合、实体和值对象；没有真实协作者时不使用 mock。
- 覆盖正常路径、拒绝条件、边界值和所有业务不变量。
- 时间、ID 和随机性必须可控制，测试不得依赖当前时间或执行顺序。

### Application Service

- 默认直接实例化用例实现，不使用 `@SpringBootTest`。
- 使用最小 fake 或 mock 替代输出端口，不访问真实基础设施。
- 验证用例结果和必要的端口交互，不验证框架实现细节。
- 业务不变量应由 Domain 测试覆盖，不在应用测试中重复穷举。

### Adapter

- 入站 Adapter 只验证协议到输入端口的转换，不重复测试领域规则。
- Web Adapter 使用 MVC 切片验证 JSON、Bean Validation、HTTP 状态和 Problem Details。
- Persistence Adapter 使用与生产相同的数据库类型和迁移脚本；PostgreSQL 行为不得用 H2 代替。
- Kafka、数据库或其他容器只在真实协议语义需要时启动；纯映射测试不得启动容器。
- 并发和幂等测试必须断言最终状态及失败数量，禁止只捕获并记录异常。

### Container

- `@SpringBootTest` 只用于需要完整装配的测试，不作为单元测试的默认入口。
- 除不可控的第三方系统外，完整装配测试不得把本部署单元的 Adapter 全部 mock 掉。
- 外部资源通过 Testcontainers 或明确的测试替身提供，禁止连接生产或共享环境。

## Cucumber 使用规则

Cucumber 用于业务人员和工程人员需要共同确认的关键行为，例如发布职位、候选人申请、权限拒绝、状态流转、重复操作和跨角色流程。值对象判空、Repository 查询、JSON 字段映射等技术细节继续使用 JUnit。

- Feature、Scenario、Step、标签和测试代码全部使用英文。
- 每个 Scenario 必须关联已确认的 `BR-*` 与 `TC-*`，例如 `@BR-001 @TC-003`。
- Gherkin 描述业务行为和可观察结果，不出现 Controller、Repository、SQL、按钮选择器或内部类名。
- 一个 Scenario 表达一个主要行为；场景之间不得依赖顺序或共享静态状态。
- Spring 场景状态使用 `@ScenarioScope`；不得使用全局可变 `ScenarioContext`。
- Step Definition 按领域概念组织，例如 `JobPostingSteps`，不得按每个 Feature 创建重复步骤库。
- Cucumber 通过部署单元公开的入站边界执行；有 Web API 时默认通过 HTTP，消息驱动服务通过已确认的消息入站边界。
- 只有少量关键验收路径使用 Cucumber，不把每个 `TC-*` 都翻译成 Feature。

Java 集成使用 `cucumber-java`、`cucumber-junit-platform-engine` 和实际需要时的 `cucumber-spring`。所有 Cucumber 组件使用同一版本；禁止新增已弃用的 JUnit 4 `cucumber-junit`。依赖只在第 5 阶段确实存在验收场景时加入对应测试集。

## 目录与包组织

测试代码跟随其拥有者，不创建仓库级通用 Cucumber 模块：

```text
<context>/
├── domain-core/src/test/java/com/portal/job/<context>/domain/...
├── application-service/src/test/java/com/portal/job/<context>/application/...
├── adapters/
│   ├── in/web/src/test/java/com/portal/job/<context>/adapter/in/web/...
│   └── out/persistence/src/integrationTest/java/com/portal/job/<context>/adapter/out/persistence/...
└── container/
    ├── src/integrationTest/java/com/portal/job/<context>/container/...
    └── src/acceptanceTest/
        ├── java/com/portal/job/<context>/acceptance/
        │   ├── AcceptanceTestSuite.java
        │   ├── steps/
        │   └── support/
        └── resources/features/<capability>/*.feature
```

- `src/test` 保存快速单元测试和轻量 Adapter 切片测试。
- `src/integrationTest` 保存需要真实框架装配、数据库或消息设施的测试。
- `src/acceptanceTest` 只存在于确实拥有 Cucumber 场景的部署单元。
- Java 测试包镜像生产包；验收测试使用独立的 `.acceptance` 包。
- 测试类保持 package-private，并遵守 ArchUnit/Taikai 命名规则。
- 多个已部署微服务之间确有端到端验证需求时，才通过讨论创建独立 `system-tests` 模块；当前禁止预建。

## 命名与可追溯性

- 单元和切片测试：`*Test`。
- 基础设施及装配集成测试：`*IntegrationTest`。
- Cucumber Suite：`AcceptanceTestSuite`。
- 架构测试：`ArchitectureTests`。
- 每个 `TC-*` 在 spec 中记录对应测试类、方法或 Cucumber 标签。
- 测试名称描述行为与结果，不使用 `test1`、`happyPath` 等无业务语义名称。

## 测试隔离与数据

- 测试必须可独立、可重复运行，不依赖执行顺序或残留数据。
- Cucumber 每个 Scenario 开始前建立自己的前置状态，结束后清理持久化状态。
- 优先使用唯一业务标识和事务回滚；只有无法回滚的异步流程才使用显式清理。
- Testcontainers 使用生产同类基础设施；测试并行化必须先证明容器和数据隔离安全。
- 测试数据 Builder 留在拥有它的测试源集；没有实际重复前，不创建共享 fixture library。

## 构建与模板化

仓库级 `build-logic` 可以统一测试任务、依赖和报告，但不得共享业务测试代码：

- `test`：默认快速测试。
- `integrationTest`：集成测试任务。
- `acceptanceTest`：仅为拥有 Feature 的部署单元启用。
- `check`：在 Pull Request 门禁中执行适用的架构、单元、集成和验收测试。

Convention Plugin 只提取已经重复的构建配置。禁止预建全局基类、共享 Step Definition、共享 `ScenarioContext`、巨型测试数据工厂或空测试模块。

## 与六阶段流程的关系

1. 第 2 阶段确认 `BR-*`，但不写测试代码。
2. 第 4 阶段确认测试所属上下文、模块和公开边界。
3. 第 5 阶段先形成 `TC-*`，为每个用例选择最小且正确的测试层；只有关键业务验收行为使用 Cucumber。
4. 第 5 阶段提交测试并记录真实 Red 结果，测试覆盖范围经用户确认后才能进入第 6 阶段。
5. 第 6 阶段只实现让已确认测试通过的最小生产代码，并记录 Green、格式化和完整构建结果。

