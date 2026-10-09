# Design: dbclient-optimize-logical-deletion-v1

> 动机与范围见 `proposal.md`；行为契约（Requirement / Scenario）见 `specs/dbclient-logical-deletion/spec.md`，本文只回答"如何实现"。

## Context

**现状**（`lucky-httpclient-spring-boot-starter` 的 dbclient 包）：

1. **客户端构建器体系分两层**：`LambdaClientConditionBuilder` 是"可转换的条件构建器"（继承 `AbstractLambdaClientBuilder`，持有 `BaseDBApi` 与一个内部 `LambdaConditionBuilder`）。其全部 `toXxx()` 转换为同一组装模式：以当前内部构建器 + `BaseDBApi` 构造目标客户端构建器，已构建条件由目标构建器的拷贝构造保留。
2. **缺口所在**：客户端条件构建器已支持 `toSelect()` / `toCount()` / `toDelete()` / `toUpdate()` / `toColumn()`，但独缺 `toLogicDelete()` / `toRestore()`；非客户端 `LambdaConditionBuilder` 两个转换均已具备。补齐这一对齐缺口即本次变更。
3. **目标构建器已就绪**：`LambdaClientLogicDeleteBuilder` / `LambdaClientRestoreBuilder` 均提供 `(BaseDBApi, LambdaSqlBuilder)` 公开构造器（设计用途即"用既有构建器构造"），当前仅 `(BaseDBApi, Class)` 形态在 `LambdaBuilderApi` 中被使用，`(BaseDBApi, LambdaSqlBuilder)` 构造器尚无调用方。
4. **执行路径已就绪**：客户端逻辑删除/恢复构建器的执行方法分别委托 `BaseDBApi.logicDelete(...)` / `BaseDBApi.restore(...)`（既有 `@SQL` 模板 + 执行器），无需新增执行逻辑。
5. **失败语义已就绪**：目标构建器构造时解析 `@LogicDelete` 规则（未标注 / 多标注 / 取值非法 → `IllegalArgumentException`）；非客户端同名转换即依赖该行为实现"转换即校验"。

**约束**：Java 8；不新增依赖；不改变既有 API 签名、行为与 SQL 渲染；变更面收敛在 lambda 构建器包。

## Goals / Non-Goals

**Goals:**

- 为 `LambdaClientConditionBuilder` 补齐 `toLogicDelete()` / `toRestore()`：返回 `LambdaClientLogicDeleteBuilder` / `LambdaClientRestoreBuilder`，完整保留已构建条件、可直接执行；
- 与非客户端 `LambdaConditionBuilder` 的同名转换保持同构：命名、返回形态、失败时机、错误类型一致；
- 零新增执行路径与 SQL 渲染逻辑——仅做构建器组装。

**Non-Goals:**

- 不调整 `@LogicDelete` 默认取值与常规查询过滤形式（`<> 已删除值` 保持现状，已确认整体取消）；
- 不改动既有转换方法及其目标构建器、`LambdaBuilderApi` 既有入口（lambdaLogicDelete/lambdaRestore）与 `WriteApi`/`SQLFunctions` 执行链；
- 不为其他客户端构建器（查询/统计/删除/更新/单列）新增转换方法；
- 不改元数据解析、SQL 渲染与既有行为。

## Decisions

### D1. 复用目标构建器已预留的 `(BaseDBApi, LambdaSqlBuilder)` 构造器，按既有转换模式组装

在 `LambdaClientConditionBuilder` 的类型转换区域新增两个方法：

```java
public final LambdaClientLogicDeleteBuilder<T> toLogicDelete() {
    return new LambdaClientLogicDeleteBuilder<>(this.baseDBApi, this.sqlBuilder);
}

public final LambdaClientRestoreBuilder<T> toRestore() {
    return new LambdaClientRestoreBuilder<>(this.baseDBApi, this.sqlBuilder);
}
```

- 条件保留链路：目标客户端构建器 → 非客户端构建器（`LambdaLogicDeleteBuilder` / `LambdaRestoreBuilder` 的 `(LambdaSqlBuilder)` 构造器）→ `LambdaSqlBuilder` 拷贝构造（深拷贝 `SqlBuilder`，源与副本互不影响），与 `toSelect()` / `toDelete()` 等既有转换完全同构；
- 两个 `(BaseDBApi, LambdaSqlBuilder)` 构造器此前已按此用途设计且无调用方，本次直接启用，不新增构造器、不新增拷贝逻辑。

*备选：为客户端构建器新增专用转换构造器——与既有 `(BaseDBApi, LambdaSqlBuilder)` 构造器职责重复，否决；在条件构建器内直接改写内部构建器的 SQLType/SET 片段——需侵入 `LambdaSqlBuilder` 内部结构，且破坏"转换返回新构建器、源构建器不受影响"的一致性，否决。*

### D2. 失败时机：转换即校验（fail-fast）

`toLogicDelete()` / `toRestore()` 调用时即构造目标构建器，构造器内解析 `@LogicDelete` 规则：实体未标注、标注多个或取值非法时抛出 `IllegalArgumentException`，不存在"转换成功、执行时才报错"的中间态。该行为直接满足规格"转换 MUST 失败、不执行任何 SQL"的场景，且与非客户端同名转换一致。

*备选：延迟到执行时校验——规格明确要求转换失败，且与非客户端入口行为不一致，否决。*

### D3. 方法形态与文档对齐

两个方法均为 `public final`，置于 `LambdaClientConditionBuilder` 的"类型转换方法"区域（`toUpdate()` 之后），javadoc 对齐既有 `toDelete()` / `toUpdate()` 的写法：说明可执行的操作、空条件风险提示（可能对全表执行逻辑删除/恢复）、使用示例与 `@throws IllegalArgumentException`；类级 javadoc 的转换示例补充这两个入口。

*备选：上提至 `AbstractLambdaClientBuilder` 基类——会让全部客户端构建器都暴露逻辑删除/恢复转换，超出范围且职责不当（"先构建条件、再定操作语义"是条件构建器的职责），否决。*

## Risks / Trade-offs

- **[空条件转换影响全表]** 条件为空时转换执行会影响全表 → 与既有 `lambdaLogicDelete()` / `lambdaRestore()` 入口的风险一致，不新增也不弱化保护；javadoc 保持同样的显式警告。
- **[源构建器复用 / 重复转换]** 每次转换生成独立深拷贝 → 源条件构建器与已转换结果互不影响，无共享可变状态，与既有 `toSelect()` / `toDelete()` 行为一致。
- **[非 WHERE 片段随拷贝保留]** 条件构建器中若含 ORDER BY 等片段会随拷贝进入目标构建器 → 与既有 `toDelete()` / `toUpdate()` 转换行为完全一致，不引入新的语义差异。

## Migration Plan

1. 版本升级即可获得新入口（纯增量 API）；无数据与 SQL 迁移，未调用新方法时零影响；
2. 使用方式（新增能力）：对 `lambdaCondition()` 上已构建的条件，可直接 `.toLogicDelete().logicDelete()` / `.toRestore().restore()` 执行逻辑删除/恢复，无需在 `lambdaLogicDelete()` / `lambdaRestore()` 上重复构建条件；
3. 回滚：不使用新方法即可回到既有入口，无不可逆影响（逻辑删除不物理删除数据）。
