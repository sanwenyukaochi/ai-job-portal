# REST API 契约与响应规范

- 状态：Confirmed
- 确认日期：2026-09-22
- 最近修订日期：2026-09-23
- 适用范围：所有对外和服务间 REST 入站适配器

## 标准基线

- HTTP 方法和状态码遵循 [RFC 9110 — HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110.html)。
- 错误响应遵循 [RFC 9457 — Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html)。
- API 主版本与兼容性遵循 [Google AIP-185 — API Versioning](https://google.aip.dev/185) 和 [AIP-180 — Backwards compatibility](https://google.aip.dev/180)。
- Spring MVC 使用 [Spring Framework 7 原生 API Versioning](https://docs.spring.io/spring-framework/reference/web/webmvc-versioning.html)，不自行实现版本 Filter、Interceptor 或 HandlerMapping。
- Spring MVC 使用原生 `ProblemDetail`、`ErrorResponse` 和 `ResponseEntityExceptionHandler`，不创建自定义通用错误框架。参考 [Spring Framework Error Responses](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html)。

## API 版本管理

### 对外契约

稳定 REST API 只公开主版本，版本是 URI path 的第一个业务路径段：

```text
/v1/job-postings
/v1/applications
/v2/job-postings
```

- 使用 `v1`、`v2`，禁止公开 `v1.0`、`v1.2`、`v1.2.3`。
- 不提供无版本业务路径，也不为缺失版本配置默认值。
- API Gateway 可以拥有独立部署前缀，但业务 API 的相对契约仍从 `/v{major}` 开始。
- 每个部署单元独立演进自己的 REST 主版本；BFF 或 API Facade 拥有独立契约，不泄漏下游服务版本。
- Header、Query Parameter 和 Media Type 版本选择不作为本项目的并行策略。
- OpenAPI 的 `openapi` 和 `info.version` 分别表示规范格式版本与文档发布版本，不替代 URI 主版本。

兼容性修改在当前主版本内原地演进，包括新增 endpoint、新增保持原行为的可选请求字段、新增响应字段和内部重构。以下变化必须在对应 spec 第 4 阶段确认消费者影响，并创建新主版本：

- 删除或重命名 endpoint、字段或参数。
- 修改字段类型、格式、含义或默认行为。
- 将可选输入改为必填，或收紧校验使原合法请求失败。
- 修改认证、授权、关键状态码或稳定错误码语义。
- 删除枚举值，或改变分页等可观察协议行为。

### Spring Framework 7 实现

版本解析由 `container` 中的 `WebMvcConfigurer` 集中配置。Controller 只声明资源路径和固定主版本：

```java
import java.util.regex.Pattern;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
class ApiVersionConfig implements WebMvcConfigurer {

    private static final Pattern VERSIONED_API_PATH =
            Pattern.compile("^/v[^/]+(?:/.*)?$");

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                "/{version}",
                HandlerTypePredicate.forBasePackage(
                        "com.portal.job.jobposting.adapter.in.web"));
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .usePathSegment(
                        0,
                        path -> VERSIONED_API_PATH
                                .matcher(path.pathWithinApplication().value())
                                .matches())
                .setVersionRequired(true)
                .detectSupportedVersions(false)
                .addSupportedVersions("1");
    }
}
```

基础包在对应 spec 第 4 阶段确认后替换。禁止使用 `@EnableWebMvc` 覆盖 Spring Boot MVC 自动配置。

Controller 使用 Spring 原生固定版本条件，不把版本硬编码到 Controller path：

```java
@RestController
@RequestMapping(path = "/job-postings", version = "1")
class JobPostingV1Controller {
}
```

最终路径是 `/v1/job-postings`。版本值只允许 `"1"`、`"2"` 等固定主版本；禁止使用 `"1.0"`、`"1.2"`、`"1+"` 或 `"1.2+"`。没有 `version` 条件的业务 Controller 可能成为所有版本的 fallback，因此禁止提交。

显式支持列表是版本发布门禁。新增 `v2` 时必须同时完成第 4 阶段确认、增加 `addSupportedVersions("1", "2")`、提供 V2 Adapter 契约和保留 V1 回归测试。

### 代码归属

REST 版本只存在于入站 Web Adapter：

```text
adapter.in.web
├── v1
│   ├── JobPostingV1Controller
│   ├── JobPostingRequest
│   ├── JobPostingResponse
│   └── JobPostingWebMapper
└── v2
    ├── JobPostingV2Controller
    ├── JobPostingRequest
    ├── JobPostingResponse
    └── JobPostingWebMapper
```

- 只有确认破坏性变化后才创建 `v2` 包，不预建空版本目录。
- Domain Core、Application Service、Persistence Adapter 和数据库模型不得使用 REST `v1`、`v2` 包或类型。
- 不同 REST 版本可以调用同一个输入端口；若业务语义变化，必须回到业务规则和边界阶段重新确认，不复制整个领域模型。

### 废弃与下线

新旧主版本必须在已确认的迁移期内并行运行。开始废弃时使用 Spring 的 `StandardApiVersionDeprecationHandler` 输出 [RFC 9745](https://www.rfc-editor.org/rfc/rfc9745.html) `Deprecation`、[RFC 8594](https://www.rfc-editor.org/rfc/rfc8594.html) `Sunset` 和迁移文档 `Link`；没有真实废弃计划时不预建处理器。

废弃只通知生命周期，不改变旧版本原有行为。迁移期和消费者验证完成后才能停止旧版本；需要明确标识已退役版本时，可以在有限的 tombstone 期间返回 `410 Gone`。

### OpenAPI 与服务间调用

- Springdoc API 文档端点统一配置为 `/openapi`，避免默认 `/v3/api-docs` 与业务主版本路径混淆。
- 只有一个主版本时只生成一份文档；多个主版本并行时，按 `adapter.in.web.v1`、`adapter.in.web.v2` 包分别生成文档。
- 服务间 `RestClient` 默认绑定明确的目标 base URL，例如 `http://job-posting-service/v1`；只有同一个客户端确实需要动态调用多个版本时才使用 `ApiVersionInserter`。

## 成功响应

HTTP 状态码表达请求是否成功。响应体直接表示资源或操作结果，不添加全局 `{ "success", "message", "data" }` 包装。

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "01J00000000000000000000000",
  "email": "user@example.com",
  "status": "ACTIVE"
}
```

### 状态码

| 场景 | 状态码 | 响应要求 |
|---|---:|---|
| 查询成功 | `200 OK` | 返回资源、集合或查询结果 |
| 创建资源成功 | `201 Created` | 返回新资源，并设置 `Location` |
| 异步任务已接受 | `202 Accepted` | 返回任务状态资源或状态查询位置 |
| 更新成功且返回资源 | `200 OK` | 返回更新后的资源 |
| 成功且无需响应体 | `204 No Content` | 不得包含响应体 |
| 删除成功 | `204 No Content` | 不返回成功消息包装 |

不得用 `200 OK` 承载失败结果，也不得同时返回错误状态码和 `success: true`。

## 集合和分页

不分页的小集合直接返回 JSON 数组，空集合返回 `[]`。

分页集合使用以下结构，页码从 `0` 开始：

```json
{
  "items": [],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

不直接暴露 Spring `Page`、JPA Entity 或框架生成的分页 JSON。若某项能力需要游标分页，在该能力的第 4 阶段单独确认契约。

## 错误响应

错误响应的媒体类型必须是 `application/problem+json`。

所有 REST 响应文本必须使用英文，包括 `title`、`detail`、字段校验 `message`、成功消息以及其他人类可读字段。错误 `code`、JSON 字段名和 URI 标识使用稳定的英文 ASCII 表达；不得向客户端返回中文文本。

```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json

{
  "type": "https://api.example.com/problems/email-already-registered",
  "title": "Email already registered",
  "status": 409,
  "detail": "The email address is already associated with an account.",
  "instance": "/v1/accounts",
  "code": "IDENTITY_EMAIL_ALREADY_REGISTERED",
  "traceId": "01J00000000000000000000000"
}
```

### 字段规则

| 字段 | 规则 |
|---|---|
| `type` | 稳定的问题类型 URI；同类错误不得随接口变化 |
| `title` | 简短、稳定的人类可读标题 |
| `status` | 与实际 HTTP 状态码一致 |
| `detail` | 当前错误实例的安全说明，不作为客户端分支依据 |
| `instance` | 当前请求或错误实例标识，通常为请求路径 |
| `code` | 项目内稳定、机器可读的错误码，使用 `SCREAMING_SNAKE_CASE` |
| `traceId` | 可选的链路追踪标识，不包含业务语义 |

客户端逻辑应依据 HTTP 状态、`type` 或 `code`，不得解析 `title` 和 `detail`。

### 校验错误

请求包含多个字段错误时，使用 RFC 9457 扩展字段 `errors`。`pointer` 使用 JSON Pointer 指向输入字段。

```json
{
  "type": "https://api.example.com/problems/validation",
  "title": "Request validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/v1/accounts",
  "code": "VALIDATION_FAILED",
  "errors": [
    {
      "pointer": "/email",
      "code": "INVALID_FORMAT",
      "message": "must be a valid email address"
    }
  ],
  "traceId": "01J00000000000000000000000"
}
```

字段错误按 `pointer` 稳定排序，保证测试和客户端展示结果可预测。

### 错误状态映射

| 状态码 | 使用场景 |
|---:|---|
| `400 Bad Request` | JSON、参数或字段格式无效 |
| `401 Unauthorized` | 缺少或无法验证身份凭证 |
| `403 Forbidden` | 身份有效但无权执行操作 |
| `404 Not Found` | 目标资源不存在，或安全策略要求隐藏其存在 |
| `409 Conflict` | 唯一性冲突、版本冲突或当前资源状态不允许操作 |
| `422 Unprocessable Content` | 请求格式正确，但业务输入本身违反规则 |
| `429 Too Many Requests` | 超过限流策略；可计算时返回 `Retry-After` |
| `500 Internal Server Error` | 未预期的服务端错误 |
| `503 Service Unavailable` | 必要下游暂不可用；可计算时返回 `Retry-After` |

`500` 和 `503` 不得暴露异常类名、堆栈、SQL、凭证、内部主机名或下游响应正文。

API 版本格式无效或版本未受支持时返回 `400 Bad Request`，并分别使用稳定错误码 `API_VERSION_INVALID` 或 `API_VERSION_NOT_SUPPORTED`。无版本业务路径不提供隐式 fallback；支持版本中的资源不存在才返回 `404 Not Found`。

## JSON 表示

- 字段使用 `camelCase`。
- 时间使用 ISO 8601 UTC，例如 `2026-09-22T08:30:00Z`。
- 空集合返回 `[]`，不得返回 `null`。
- 对外 DTO 由入站适配器拥有，不返回领域实体或持久化实体。
- 公开字段和错误码一旦发布即视为契约；破坏性修改必须在对应能力的第 4 阶段讨论。

## 六边形架构位置

- Domain Core 不知道 HTTP 状态、Problem Details 或 JSON。
- Application Service 返回用例结果或抛出应用/领域错误，不返回 `ResponseEntity`。
- REST 入站适配器负责把用例结果映射为成功响应，把错误映射为 RFC 9457 Problem Details。
- 日志和链路追踪负责保存内部诊断信息，客户端错误响应只暴露安全信息。

## 测试要求

REST 适配器测试至少验证：

- `/v{major}` 能路由到正确的固定版本映射，无版本路径不可访问。
- 未支持版本返回 RFC 9457 和稳定的 API 版本错误码。
- 并行版本返回各自的协议 DTO，且旧版本回归测试持续通过。
- 废弃版本在启用生命周期通知后返回正确的 `Deprecation`、`Sunset` 和 `Link`。
- `/actuator`、`/openapi` 和 Swagger UI 不受业务 API 版本解析影响。
- HTTP 状态码。
- `Content-Type`。
- 成功响应没有全局包装。
- 错误响应符合 Problem Details 必需字段及稳定 `code`。
- 校验错误的 `errors` 内容和顺序。
- 所有响应文本均为英文，不包含中文字符。
- `204` 响应没有响应体。
- `500` 响应不泄漏内部异常信息。
