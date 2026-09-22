# 0001 — 重写基础与首个纵向切片

- 状态：Draft
- 创建日期：2026-09-22
- 关联文档：[目标六边形架构](../architecture/hexagonal-architecture.md)、[旧系统盘点](../migration/legacy-system-inventory.md)

## 背景

旧项目包含完整招聘业务，但业务规则、Spring MVC、JPA、Feign 和 Kafka 混合在服务实现中。目标仓库已经清空业务模块，保留 Gradle、格式化和依赖版本基础，适合按可验收的纵向切片重新建立边界。

Food Ordering System 用于验证依赖方向和适配器拆分方式，不作为模块数量或实现代码的强制模板。

## 目标

- 建立一条可以编译、测试和启动的业务纵向切片。
- 用 Gradle 模块依赖强制 `adapter -> application-service -> domain-core`。
- 证明领域规则不依赖 Spring、JPA、HTTP、Kafka 或具体数据库。
- 为后续服务提供经过测试的最小结构，而不是提前生成全部微服务。
- 将旧行为逐项映射到独立 spec，做到可追溯迁移。

## 非目标

- 本 spec 不迁移全部八个业务服务。
- 不自动复制旧项目的 Controller、Entity、DTO 或 `common-lib`。
- 不在没有一致性需求的情况下引入 Saga、Outbox 或分布式事务。
- 不承诺旧 API、数据库结构或消息格式全部兼容；兼容范围由业务 spec 决定。
- 不部署 Eureka、Config Server、Gateway、Kafka、Redis 或完整容器环境。

## 基础行为

1. 目标仓库能够通过 Gradle 发现并构建首个纵向切片的所有模块。
2. 领域规则可以通过不启动 Spring 的单元测试执行。
3. 入站适配器只调用输入端口，不直接访问 Repository、Feign Client 或 Kafka。
4. 应用用例只依赖输出端口，不依赖 JPA Repository、HTTP Client 或消息实现。
5. 出站适配器完成领域模型与外部模型的映射。
6. Container 负责启动和装配，不承载业务规则。

## 模块策略

首个切片只创建实际需要的模块：

```text
<context>-service/
├── <context>-domain-core
├── <context>-application-service
├── <context>-inbound-web
├── <context>-outbound-persistence
└── <context>-container
```

仅当首个用例确实调用外部服务、发布消息或处理安全令牌时，增加相应 adapter 模块。后续服务复用依赖规则和构建约定，不复制无用模块。

## 首个切片候选

建议选择用户注册与登录，因为它能覆盖领域校验、持久化、安全适配器和 REST 边界，同时不依赖其他业务服务。最终范围需要在独立业务 spec 中定义，至少包括：

- 注册规则与重复邮箱处理。
- 密码哈希边界。
- 登录凭证校验和令牌签发边界。
- 用户状态与角色的最小集合。
- API 与旧系统的兼容要求。

## 验收标准

- [ ] 首个业务 spec 状态为 Accepted，并解决所有阻塞性未决问题。
- [ ] `settings.gradle.kts` 只注册首个切片所需模块。
- [ ] Domain Core 的运行时依赖中不存在 Spring、JPA、Kafka、Feign 和 Jackson。
- [ ] Application Service 不依赖任何 adapter 模块。
- [ ] 至少一个成功场景和关键拒绝场景由自动化测试覆盖。
- [ ] 架构测试能在出现反向依赖时失败。
- [ ] `./gradlew spotlessCheck` 和 `./gradlew check` 通过。
- [ ] 已迁移的旧行为在 migration 文档中标记并链接对应 spec。

## 未决问题

- 首个切片是否确定为用户注册与登录？
- 是否要求保持旧 `/api/auth/**`、请求/响应 JSON 和 JWT Claims 完全兼容？
- 是否迁移旧数据库数据，还是只定义新 schema？
- 第一阶段是否需要服务发现和配置中心，还是先使用本地配置启动？

## 实现与验证记录

尚未实现。

