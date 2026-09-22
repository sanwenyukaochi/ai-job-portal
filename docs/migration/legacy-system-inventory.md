# 旧系统盘点

- 来源：`/Users/sanwenyukaochi/Downloads/source code ai job portal/backend`
- 盘点日期：2026-09-22
- 状态：初步盘点；具体行为以各业务 spec 的逐项核对为准

## 业务上下文

| 旧模块 | 主要职责 | 外部依赖/协作 | 迁移状态 |
|---|---|---|---|
| `job-portal-user-service` | 注册、登录、用户管理、JWT | PostgreSQL、Security | 未开始 |
| `job-portal-company-service` | 公司资料与地址 | PostgreSQL | 未开始 |
| `job-portal-job-service` | 职位、分类、技能、标签和搜索 | PostgreSQL、Redis、Company HTTP | 未开始 |
| `job-portal-application-service` | 投递、状态、备注、筛选、AI 初筛 | PostgreSQL、多个 HTTP 服务、Kafka | 未开始 |
| `job-portal-resume-service` | 简历及教育、项目、经历等子资源 | PostgreSQL | 未开始 |
| `job-portal-preference-service` | 职位偏好、收藏、提醒 | PostgreSQL | 未开始 |
| `job-portal-ai-service` | Gemini 驱动的生成、解析、匹配和筛选 | Gemini API | 未开始 |
| `job-portal-notification-service` | 消费申请事件并发送邮件 | Kafka、SMTP | 未开始 |

## 平台模块

| 旧模块 | 职责 | 初步处理 |
|---|---|---|
| `job-portal-gateway` | 路由、CORS、JWT、fallback | 等首个服务 API 稳定后再迁移 |
| `job-portal-config-server` | 从远程 Git 加载配置 | 本地开发阶段不作为前置条件 |
| `job-portal-discovery` | Eureka 服务发现 | 出现第二个可运行服务时再评估 |
| `common-lib` | 共享枚举、DTO、事件和异常 | 不整体迁移；按归属拆回各上下文 |

## 当前观察

- 业务服务普遍采用 `Controller -> ServiceImpl -> JPA/Feign/Kafka`，领域与基础设施未隔离。
- `ApplicationServiceImpl` 同时承担业务规则、事务、远程聚合、持久化和事件发布。
- `common-lib` 含大量服务专属 DTO，并混有订单、支付、退款和订阅等疑似遗留类型。
- 通知服务目录存在，但没有加入 `services/pom.xml`；其父 artifactId 与父工程名称也不一致。
- 多数运行参数来自外部 Config Server，因此本地源码不是完整运行配置的唯一来源。

## 迁移规则

- 每迁移一个端点、事件或定时任务，都在对应业务 spec 中记录旧位置和新验收标准。
- “旧代码如此实现”不是保留缺陷的理由；但改变公开行为必须在 spec 中明确。
- 状态从“未开始”变更时，链接对应 spec，不使用模糊百分比。

