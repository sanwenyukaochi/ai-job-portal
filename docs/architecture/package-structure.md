# Gradle 模块与 Java 包结构规范

- 状态：Confirmed
- 确认日期：2026-09-22
- 适用范围：所有业务上下文和部署单元

## 目的

Gradle 模块负责物理依赖隔离，Java 包负责模块内部职责表达，ArchUnit + Taikai 验证两者没有被绕过。本项目复用 `food-ordering-system` 的六边形依赖方向，但不复制其模块名称、混合包结构或共享业务模型。

本规范只定义结构模板。`<context>`、`<aggregate>` 和实际适配器均须由对应 spec 的第 3、4 阶段确认；确认前不得据此创建业务模块或空目录。

## Gradle 物理模块

一个确认后的部署单元使用自己的顶层目录。`domain-core`、`application-service` 和 `container` 保持同级，适配器按输入、输出方向分组：

```text
<context>/
├── domain-core/
├── application-service/
├── adapters/
│   ├── in/
│   │   ├── web/
│   │   └── messaging/
│   └── out/
│       ├── persistence/
│       ├── http/
│       └── messaging/
└── container/
```

对应的 Gradle 项目路径为：

```text
:<context>:domain-core
:<context>:application-service
:<context>:adapters:in:web
:<context>:adapters:in:messaging
:<context>:adapters:out:persistence
:<context>:adapters:out:http
:<context>:adapters:out:messaging
:<context>:container
```

`<context>`、`adapters`、`in` 和 `out` 是分组层级，不包含生产源码或业务依赖；只有叶子项目包含代码并产出 Jar。具体适配器仅在用例确实需要对应技术时创建，例如没有消息消费就不创建 `adapters/in/messaging`。

不在 `domain-core` 和 `application-service` 外增加 `domain` 分组，因为应用层不属于领域层；也不增加没有隔离作用的 `core` 空壳层。

依赖方向固定为：

```text
:<context>:application-service     -> :<context>:domain-core
:<context>:adapters:in:*           -> :<context>:application-service
:<context>:adapters:out:*          -> :<context>:application-service + :<context>:domain-core
:<context>:container               -> :<context>:application-service + 本部署单元所需的 adapter
```

- `domain-core` 不依赖其他业务模块或框架。
- `application-service` 不依赖任何 adapter 或 container。
- adapter 之间不得相互依赖。
- 只有 `container` 负责启动、配置和装配。
- 不使用 Spring Modulith；Gradle 与 ArchUnit + Taikai 是唯一模块边界机制。

## Java 逻辑包

Gradle 模块与基础包一一对应：

```text
<context>/domain-core
└── com.portal.job.<context>.domain
    └── <aggregate>

<context>/application-service
└── com.portal.job.<context>.application
    ├── port.in
    ├── port.out
    └── usecase

<context>/adapters/in/web
└── com.portal.job.<context>.adapter.in.web

<context>/adapters/in/messaging
└── com.portal.job.<context>.adapter.in.messaging

<context>/adapters/out/persistence
└── com.portal.job.<context>.adapter.out.persistence

<context>/adapters/out/http
└── com.portal.job.<context>.adapter.out.http

<context>/adapters/out/messaging
└── com.portal.job.<context>.adapter.out.messaging

<context>/container
└── com.portal.job.<context>.container
```

规则：

- `<context>` 使用第 4 阶段确认的限界上下文名称，不从旧服务名直接推导。
- Domain 按聚合组织；只有出现多个类型时才拆分更深的 `event`、`exception` 等子包。
- `application.port.in` 包含用例接口及应用命令/查询；`application.port.out` 包含应用所需的外部能力接口。
- `application.usecase` 只负责编排用例与事务意图，业务不变量留在 Domain。
- REST Controller、协议请求/响应和 Problem Details 映射只位于 `adapter.in.web`。
- 消息 listener 只位于 `adapter.in.messaging`；publisher 只位于 `adapter.out.messaging`。
- JPA Entity、Spring Data Repository 和持久化 Mapper 只位于 `adapter.out.persistence`。
- 外部 HTTP DTO、Client 和 Mapper 只位于 `adapter.out.http`。
- Spring Boot 启动类与 Bean 装配只位于 `container`，组件扫描不得扩大到其他限界上下文。
- 不建立全局 `controller`、`service`、`repository` 包来混放不同上下文。

## 可见性与契约

- 默认使用满足当前模块所需的最小可见性。
- 输入端口、输出端口以及确需跨 Gradle 模块使用的领域类型可以是 `public`。
- Controller、JPA Entity、Spring Data Repository、Mapper 和适配器实现保持 package-private，除非框架存在经过验证的硬性要求。
- REST DTO、数据库 Entity、外部服务 DTO 和消息传输模型不得作为跨层或跨服务领域契约。
- 默认不创建 `common` 或 `shared` 模块；只有第 4 阶段确认的稳定、无业务归属原语才允许进入独立公共模块。

## 测试包

- 测试包镜像被测生产包。
- Domain 单元测试位于 `domain-core`，不启动 Spring。
- 应用用例测试位于 `application-service`，通过 fake/mock 替代输出端口。
- 协议、持久化和消息测试留在对应 adapter。
- 跨模块装配测试位于 `container`。
- 每个部署单元的 `ArchitectureTests` 使用 `com.portal.job.<context>` 作为检查根包。

## 与参考项目的关系

参考项目用于验证 `domain -> application -> adapter -> container` 的依赖思想，不是目录模板。本项目明确改进以下结构：

- 应用层不再放入 `.domain` 包。
- `domain-core` 与 `application-service` 不再共同嵌套在名为 `domain` 的父项目中。
- REST 入站适配器不再使用含糊的 `application` 模块名。
- 消息入站与消息出站不放入同一个 adapter 模块。
- Container 不放入 `.domain` 包，也不扫描整个系统根包。
- 不共享服务专属状态、实体或 DTO。
