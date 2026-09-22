# REST API 响应规范

- 状态：Confirmed
- 确认日期：2026-09-22
- 适用范围：所有对外和服务间 REST 入站适配器

## 标准基线

- HTTP 方法和状态码遵循 [RFC 9110 — HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110.html)。
- 错误响应遵循 [RFC 9457 — Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html)。
- Spring MVC 使用原生 `ProblemDetail`、`ErrorResponse` 和 `ResponseEntityExceptionHandler`，不创建自定义通用错误框架。参考 [Spring Framework Error Responses](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html)。

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

```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json

{
  "type": "https://api.example.com/problems/email-already-registered",
  "title": "Email already registered",
  "status": 409,
  "detail": "The email address is already associated with an account.",
  "instance": "/api/v1/accounts",
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
  "instance": "/api/v1/accounts",
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

- HTTP 状态码。
- `Content-Type`。
- 成功响应没有全局包装。
- 错误响应符合 Problem Details 必需字段及稳定 `code`。
- 校验错误的 `errors` 内容和顺序。
- `204` 响应没有响应体。
- `500` 响应不泄漏内部异常信息。

