# 标识符与主键生成策略

- 状态：Confirmed
- 确认日期：2026-09-23
- 最近修订日期：2026-09-23
- 适用范围：所有新建限界上下文、聚合、持久化模型和公开契约

## 决策

新系统默认使用 CosId `SnowflakeId` 生成聚合根及可独立寻址实体的标识符。Java 使用正数 `long`，PostgreSQL 使用 `bigint`。

项目使用 CosId 3.x；当前批准版本为 `3.2.1`，与 Spring Boot 4.x 兼容。版本统一由 Version Catalog 管理，业务模块不得各自覆盖版本。

固定布局如下，不允许各微服务自行修改：

```text
0 | 41-bit timestamp | 10-bit machine | 12-bit sequence
```

| 字段 | 规则 |
|---|---|
| Sign | 固定为 `0`，确保 Java `long` 和 PostgreSQL `bigint` 中为正数 |
| Timestamp | 当前 UTC 毫秒减去项目纪元，占 41 bit |
| Machine | 由 CosId `RedisMachineIdDistributor` 分配，范围 `0..1023` |
| Sequence | 同一机器同一毫秒内的序号，范围 `0..4095` |

项目纪元固定为 `2026-01-01T00:00:00Z`（`1767225600000` 毫秒）。纪元、位布局与单位进入数据后即为永久协议；升级 CosId 前必须通过兼容性测试证明这些值及生成行为未改变。

本项目只启用 CosId Snowflake，不启用 `SegmentId`、`SegmentChainId`、`SafeJavaScriptSnowflakeId`、Friendly ID 或 Radix 转换。需要 JavaScript 安全性时在协议边界输出十进制字符串，不牺牲主键的位容量。

## 依赖与六边形边界

- CosId 负责 Snowflake 算法、Machine ID 分配、机器状态保存和时钟回拨同步。
- Redis 只参与 Machine ID 的分配、守护与状态保存，不参与每次 ID 生成。
- `cosid-spring-boot-starter`、`cosid-spring-redis` 和 Spring Data Redis 依赖只进入部署单元的 `container`；Domain Core 与 Application Service 不依赖 CosId、Spring 或 Redis。
- Container 获取 CosId 提供的共享 `SnowflakeId` Bean，并将 `snowflakeId::generate` 作为 JDK `LongSupplier` 装配给 Application Service。
- Application Service 在创建聚合前取得 ID，并将强类型 ID 传给领域工厂。
- Domain Core 只知道业务 ID 值对象，不知道生成器、Machine ID、Redis、Spring、JPA 或数据库。
- 不创建 `WorkerIdProvider`、`StaticWorkerIdProvider`、`RedisWorkerIdProvider`、自研 Redis Lua、Hutool 生成器、中央发号服务或跨微服务 ID Library。
- CosId 依赖与配置只能在对应 spec 完成第 4、5 阶段后加入；本规范不授权提前编写生产代码。

装配边界示意：

```java
LongSupplier idSupplier(@Qualifier("__share__SnowflakeId") SnowflakeId snowflakeId) {
    return snowflakeId::generate;
}
```

Application Service 仍只依赖 JDK 类型：

```java
var id = new JobPostingId(idSupplier.getAsLong());
var jobPosting = JobPosting.create(id, command.title());
```

单元测试注入固定或递增的 `LongSupplier`，不启动 CosId、Spring 或 Redis。

## CosId 配置基线

生产和多实例环境使用 CosId 的 Redis Machine ID Distributor。以下字段构成项目协议，具体 Redis 地址和凭据由环境配置提供：

```yaml
cosid:
  namespace: ai-job-portal-${DEPLOYMENT_ENV}
  machine:
    enabled: true
    stable: false
    machine-bit: 10
    instance-id: ${INSTANCE_ID}
    distributor:
      type: redis
      redis:
        timeout: 1s
    guarder:
      enabled: true
      initial-delay: 1m
      delay: 1m
      safe-guard-duration: 5m
    clock-backwards:
      spin-threshold: 10
      broken-threshold: 2000
  snowflake:
    enabled: true
    zone-id: UTC
    epoch: 1767225600000
    share:
      clock-sync: true
      timestamp-bit: 41
      machine-bit: 10
      sequence-bit: 12
      converter:
        type: to_string
  generator:
    enabled: false
  segment:
    enabled: false
```

- `DEPLOYMENT_ENV` 必须区分 local、test、staging、production 等环境。
- 同一环境中所有微服务使用相同的 `cosid.namespace` 和 Redis Machine ID 注册表，保证生成的 ID 在该环境内全局唯一；不得使用 `${spring.application.name}` 将编号空间按服务拆开。
- `INSTANCE_ID` 必须由部署系统显式提供，并在一个运行实例的生命周期内保持不变且不与活跃实例重复；动态实例保持 `stable: false`。禁止依赖 CosId 默认的 IP + PID 推导。
- 多实例生产环境不得使用 manual distributor，也不得在 Redis 故障时回退到手工、随机或本地编号。
- 本地单实例测试可以使用 CosId manual distributor；测试配置必须显式限定在 local/test profile。
- Redis 不可用、Machine ID 分配失败或编号空间耗尽时，应用必须启动失败。运行中的 Guarder 失败由 CosId Actuator health 报告为 DOWN；编排系统必须在 `safe-guard-duration` 内将实例移出全部写入流量并重启，包含 HTTP 与消息消费者，避免编号回收后旧实例继续生成 ID。
- 时钟回拨在 `10 ms` 内自旋等待，`10..2000 ms` 主动等待同步，超过 `2000 ms` 抛出 CosId 时钟回拨异常；不得捕获后切换生成策略。
- 监控记录 Machine ID 分配/守护失败、时钟回拨和序列耗尽事件，但不记录 Redis 凭据、内部状态令牌或批量生成的全部 ID。

上述属性路径已按 CosId 3.2.1 的配置类与绑定测试核对。升级 CosId 时必须先运行本项目配置兼容性测试并同步修订本文，不得保留无法绑定的“示意配置”。

## 领域标识符

每种领域概念使用所属上下文自己的强类型值对象，不共享业务 ID 类型，也不建立通用 `BaseId<T>` 继承体系：

```java
public record JobPostingId(long value) {
    public JobPostingId {
        if (value <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
    }
}
```

- 聚合根：默认使用 CosId Snowflake ID。
- 具有独立生命周期、可被单独引用的实体：默认使用 CosId Snowflake ID。
- 领域事件、Outbox 消息和幂等记录：需要跨进程关联 ID 时使用 CosId Snowflake ID。
- 值对象：没有身份，不添加 ID。
- 纯关联表：优先使用相关外键组成的复合主键或唯一约束，不添加无意义代理 ID。
- 聚合内部子项：只有领域规则需要持续身份时才创建 ID；不能因为数据库表存在就自动创建领域实体。

方法签名使用 `JobPostingId`、`ApplicationId` 等业务类型，不使用裸 `long`、`Long` 或 `String` 代替领域语义。

## 时钟、容量与顺序

- 使用 UTC 毫秒，不使用本地时区。
- 同一毫秒内序列递增；达到 `4095` 后等待下一毫秒，禁止回绕为 `0`。
- Snowflake ID 只提供本机单调递增和跨实例大致时间有序，不保证跨节点、跨事务或跨服务的严格全局顺序。
- 不从 ID 解析时间作为业务事实。创建时间、更新时间、事件时间和业务序列必须使用独立字段。
- 41-bit 时间字段约可使用 69 年。到期迁移必须在项目纪元容量耗尽前通过新协议和数据迁移解决，不能修改当前布局继续写入。
- 10-bit Machine ID 最多允许同一环境中 1024 个活跃生成器实例。接近容量前必须通过容量评审处理，不得临时缩减时间或序列位。

## Persistence 与 Flyway

Flyway 使用 PostgreSQL 原生 `bigint`：

```sql
CREATE TABLE job_posting (
    id bigint PRIMARY KEY,
    created_at timestamptz NOT NULL
);
```

JPA Entity 只映射应用已生成的值：

```java
@Id
@Column(nullable = false, updatable = false)
private Long id;
```

- 禁止在 Snowflake 主键上使用 `@GeneratedValue`、`GenerationType.IDENTITY`、sequence 或数据库默认值。
- 聚合在调用持久化端口、发布领域事件或创建 Outbox 消息前必须已经拥有 ID。
- PostgreSQL 已为主键创建唯一 B-tree 索引，不重复建立相同索引。
- 同一服务内的外键使用 `bigint`；跨服务只保存外部 ID 值，不建立数据库外键。
- 邮箱、名称、外部编号等自然键作为独立字段和唯一约束，不作为默认主键。

## REST、消息与安全

Snowflake ID 通常超过 JavaScript 的安全整数上限 `2^53 - 1`。所有外部 JSON、REST path、OpenAPI 和消息契约必须把 ID 表示为十进制字符串：

```json
{
  "id": "95936353140740097"
}
```

- Java 内部使用 `long`/`Long`，边界适配器负责十进制字符串转换。
- OpenAPI 使用 `type: string`、`pattern: ^[1-9][0-9]{0,18}$` 和 `maxLength: 19`，不使用 `format: int64`。
- 输入必须拒绝空值、负数、零、非十进制字符和超出 Java 正数 `long` 范围的值。
- ID 不添加服务名前缀，不承载租户、状态、API 版本或权限语义。
- Snowflake ID 包含近似生成时间且可被猜测；它不是访问控制，每个读取和修改操作都必须独立完成认证与授权。
- 默认排序使用明确业务字段；时间列表通常使用 `ORDER BY created_at, id` 获得稳定结果，不单独依赖 `ORDER BY id` 判断业务先后。

JavaScript 的安全整数限制见 [MDN Number.isSafeInteger](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isSafeInteger)。

## 旧数据迁移

旧系统的 `Long IDENTITY` 与新 Snowflake ID 都使用 `bigint`，但来源和唯一性规则不同，禁止直接混用：

- 新系统记录始终获得新的 Snowflake ID。
- 只有确需迁移现有数据或兼容旧引用时，才在所属服务内保存 `legacy_id bigint` 唯一映射或建立临时映射表。
- 不把旧主键直接复制为新主键，也不假设旧值不会与 Snowflake ID 冲突。
- 不把不同旧服务的数字 ID 合并为一个全局命名空间。
- `legacy_id` 不进入新 REST 契约、领域事件或新服务间契约。
- 映射字段的保留期限、删除条件和回滚方式必须在迁移 spec 中确认。

## 例外

纯关联表、固定参考代码和经测量证明需要其他策略的高密度内部记录可以提出例外。例外必须由对应 spec 第 3、4 阶段说明身份语义，并通过 ADR 记录范围、迁移方式和不影响外部契约的证据。

## 测试要求

- 兼容性测试固定项目纪元，并验证 sign、timestamp、machine 和 sequence 位段。
- 同一 Machine ID 同一毫秒内生成的 ID 唯一且递增。
- 不同 Machine ID 在相同时间和序列下生成的 ID 不冲突。
- 序列达到 `4095` 后等待下一毫秒，不发生回绕重复。
- 时钟回拨测试覆盖自旋、等待和超过 `2000 ms` 后失败的行为。
- CosId 配置测试验证 `__share__SnowflakeId` Bean、UTC、项目纪元、位布局、Redis distributor、Guarder 和十进制字符串 converter，并确认 Generator/Segment/Friendly/Safe-JS/Radix 均未启用。
- Redis 集成测试使用真实 Redis/Testcontainers，验证并发实例的 Machine ID 不重复、状态可恢复、Redis 启动故障和守护失败；Mock Redis 不作为分布式语义的证据。
- 装配测试验证 Domain/Application classpath 不含 CosId/Redis 依赖，Application Service 只接收 `LongSupplier`。
- 领域 ID 值对象拒绝非正数，不同领域 ID 类型不能互换。
- Persistence 集成测试验证 `Long`/`bigint` 往返映射、主键/外键约束及 Flyway 迁移。
- REST 与消息契约测试验证 ID 是十进制字符串，并覆盖超出 JavaScript 安全整数范围的值。
- 旧数据迁移测试验证每个 `legacy_id` 只映射到一个新 ID，并能安全重跑或恢复。

## 参考

- [CosId project and Spring Boot compatibility](https://github.com/Ahoo-Wang/CosId)
- [CosId Snowflake guide](https://cosid.ahoo.me/guide/snowflake.html)
- [CosId Snowflake configuration](https://cosid.ahoo.me/reference/config/snowflake.html)
- [CosId Redis module](https://cosid.ahoo.me/guide/extensions/cosid-redis.html)
- [CosId 3.2.1 release](https://github.com/Ahoo-Wang/CosId/releases/tag/v3.2.1)
- [PostgreSQL numeric types](https://www.postgresql.org/docs/current/datatype-numeric.html)
