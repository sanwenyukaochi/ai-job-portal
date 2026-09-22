# 项目文档

本目录是业务分析、领域设计和实现验收的事实来源。旧代码只是证据，不是需求本身；目标微服务边界必须通过讨论形成。

## 目录

- `discovery/`：从旧代码提取的事实、疑点和业务线索，不包含目标设计结论。
- `specs/`：每项业务能力的六阶段工作文档与讨论记录。
- `architecture/`：已经确认的全局架构约束；具体上下文边界只能在 spec 第 4 阶段写入。

已确认的全局规范：

- [六边形架构依赖原则](architecture/hexagonal-architecture.md)
- [Gradle 模块与 Java 包结构规范](architecture/package-structure.md)
- [REST API 响应规范](architecture/rest-api-conventions.md)
- [ArchUnit 架构测试规范](architecture/architecture-testing.md)

只有出现多个真实方案、并作出长期有效的技术决策时，才新增 `docs/adr/NNNN-<decision>.md`。

## 阶段流转

```text
1 业务分析 --确认--> 2 业务规则 --确认--> 3 领域模型 --确认-->
4 边界范围 --确认--> 5 测试用例与 Red --确认--> 6 功能实现与 Green --确认--> 完成
```

每个阶段只有三种状态：

- `Draft`：代理正在收集事实和形成草案。
- `In Discussion`：草案已提交用户讨论，必须停留在当前阶段。
- `Confirmed`：用户明确确认，且已将结论与修订写入 spec。

不得跳阶段或一次性请求确认多个尚未讨论的阶段。

## 每轮工作方式

1. 只读取当前阶段所需的代码与文档证据。
2. 更新当前 spec，不提前设计后续阶段。
3. 在回复中概括结论、分歧和需要用户确认的问题。
4. 用户修订后继续当前阶段；用户明确确认后记录讨论结论。
5. 下一轮才开始下一阶段。

当前入口：[0001 — 业务全景与首个能力选择](specs/0001-business-capability-analysis.md)，目前停留在第 1 阶段。
