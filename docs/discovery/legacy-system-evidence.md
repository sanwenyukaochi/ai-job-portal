# 旧系统代码证据

- 来源：`/Users/sanwenyukaochi/Downloads/source code ai job portal/backend`
- 采集日期：2026-09-22
- 用途：支持业务讨论，不定义目标领域或微服务边界

## 可观察功能

| 旧模块 | 可观察功能 | 技术协作 |
|---|---|---|
| `job-portal-user-service` | 注册、登录、个人资料、用户状态与角色管理 | PostgreSQL、Spring Security、JWT |
| `job-portal-company-service` | 公司资料、地点、验证、停用 | PostgreSQL |
| `job-portal-job-service` | 职位、发布/关闭、搜索、分类、技能、标签 | PostgreSQL、Redis、Company HTTP |
| `job-portal-application-service` | 投递、查询、状态推进、撤回、备注、筛选 | PostgreSQL、多个 HTTP 调用、Kafka |
| `job-portal-resume-service` | 简历及教育、经历、项目、技能、证书、语言、奖项 | PostgreSQL |
| `job-portal-preference-service` | 求职偏好、收藏职位、职位提醒 | PostgreSQL |
| `job-portal-ai-service` | 内容生成、简历解析、匹配和筛选辅助 | Gemini API |
| `job-portal-notification-service` | 消费申请事件并发送邮件 | Kafka、SMTP |

上表按旧代码目录记录，不代表这些目录应该一对一成为新微服务。

## 可观察参与者和状态词

- 用户角色：`ADMIN`、`JOB_SEEKER`、`EMPLOYER`。
- 用户状态：`ACTIVE`、`INACTIVE`、`SUSPENDED`、`DELETED`。
- 公司状态：待验证、有效、暂停、拒绝。
- 职位状态：草稿、开放、关闭、过期、招满。
- 申请状态：待处理、审核中、候选、面试、拒绝、录用、撤回。
- AI 筛选标签：未筛选、自动入围、建议审核、待审核、低匹配。

这些名称可能包含技术偏好或不合理规则，只有在第 2 阶段经讨论后才成为目标业务规则。

## 需要警惕的旧设计

- `Controller -> ServiceImpl -> JPA/Feign/Kafka` 混合业务与基础设施职责。
- `common-lib` 共享大量服务专属 DTO、事件和枚举，并含没有完整业务流程的支付/订阅遗留类型。
- Application Service 在单个类中承担规则、事务、远程聚合、持久化和事件发布。
- 通知服务未加入父模块，父 artifactId 也与父工程不一致。
- 运行配置依赖外部 Config Server，本地源码不能证明完整运行行为。

