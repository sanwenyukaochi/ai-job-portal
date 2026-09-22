# 项目文档

本目录是重写工作的事实来源。代码回答“如何实现”，这里回答“为什么做、做什么、如何验收”。

## 目录

- `specs/`：待实现或已实现的可验收需求。
- `architecture/`：当前目标架构及稳定边界。
- `migration/`：旧系统盘点、行为映射和迁移状态。

暂不创建 ADR 目录。出现至少两个真实可选方案、且决定会长期影响项目时，再新增 `docs/adr/NNNN-<decision>.md`。

## Spec 生命周期

```text
Draft -> Accepted -> Implemented
             |
             -> Superseded
```

- `Draft`：仍可讨论，不允许开始业务实现。
- `Accepted`：范围和验收标准已确认，可以实现。
- `Implemented`：验收标准已由测试或明确的验证命令证明。
- `Superseded`：被后续 spec 替代，必须链接替代文档。

## 工作方式

1. 从 `specs/_template.md` 创建编号连续的 spec。
2. 用 `migration/legacy-system-inventory.md` 定位旧行为，但不把旧代码当作正确设计。
3. 根据 `architecture/hexagonal-architecture.md` 划分端口和适配器。
4. 先确认验收标准，再实现最小纵向切片。
5. 在 spec 末尾记录测试、构建命令和仍存在的限制。

当前入口：[0001 — 重写基础与首个纵向切片](specs/0001-rewrite-foundation.md)。

