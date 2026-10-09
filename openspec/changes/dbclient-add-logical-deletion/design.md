# Design: dbclient-add-logical-deletion

> 动机与范围见 `proposal.md`；行为契约（Requirement / Scenario）见 `specs/dbclient-logical-deletion/spec.md`，本文只回答"如何实现"。

## Context

**现有链路**（dbclient 包，`lucky-httpclient-spring-boot-starter`）：

1. `BaseDBApi`/`WriteApi`/`QueryApi`/`SingleColumnApi` 接口方法通过 `@SQL(executor = ...)` 模板调用 `SQLFunctions` 中的静态函数；
2. 查询/写入方法大体分两路：
   - **Lambda 构建器路**：`SQL_LAMBDA` 模板 → `lambdaSql(mc)` → `SQLWrapperExecutor` 直接执行方法参数中的 `SQLWrapper`（`LambdaSqlBuilder` 家族，内嵌 `SqlBuilder`）；执行类型由内嵌 `SqlBuilder` 的 `SQLType` 决定（SELECT 走查询，UPDATE 走更新）；
   - **专用模板路**：`selectById`/`deleteById`/`updateById` 等函数直接构建 `SqlBuilder`；
3. **分页与 COUNT 在 SQL 字符串层加工**：`AbstractMCNamedJdbcTemplateSQLExecutor.queryPage` 对最终 SQL 调用 `Page.buildCountSql/buildPageSql`。因此只要条件进入 `SqlBuilder` 渲染产物，列表/分页/COUNT/流式/单列全部自动覆盖；
4. **构建器体系**：所有 Lambda 构建器继承 `LambdaSqlBuilder<T>`（持有 `entityClass` + 内嵌 `SqlBuilder`）；`LambdaConditionBuilder.toXxx()` 通过拷贝构造切换语义；`LambdaClient*` 客户构建器（`AbstractLambdaClientBuilder`）委托 `BaseDBApi` 执行；
5. **元数据体系**：`EntityMetadataFactory` 统一缓存 `EntityMetadata`/`ColumnMetadata`；`EntityUtils` 提供表名、主键列等辅助。

**约束**：既有 API 签名与行为不变；未标注实体全链路零变化；Java 8；不新增依赖。

## Goals / Non-Goals

**Goals:**

- 新增 `@LogicDelete` 字段注解 + 元数据解析（值类型转换、多字段校验）；
- 五类查询入口（Lambda 构建器 / 实体条件 / Map 条件 / 按 ID / 单列）自动追加"未删除"条件，且**仅在 SELECT 渲染时生效**；
- 提供 `includeDeleted()` 显式入口查询包含已删除记录；
- 逻辑删除与恢复的完整 API：按 ID、按条件、Lambda 构建器入口、客户端构建器；
- 物理删除（`delete`/`deleteById`/`lambdaDelete`）与更新路径零改动。

**Non-Goals:**

- 不做关联表（JOIN）的自动过滤，仅过滤主实体表；
- 不做批量逻辑删除/恢复的专用入口（条件构建器已可覆盖）；
- 不做删除审计字段（删除时间/操作人）自动填充；
- 不覆盖原生 SQL 路径（`@Delete`/`@Select`/`@Update` 原始 SQL、直接使用 `SqlBuilder` 的裸 API）；
- 不改变 `update`/`updateById`/`saveOrUpdate` 既有语义（被逻辑删除的记录仍可被更新）。

## Decisions

### D1. 标注方式：独立字段注解，而非扩展现有注解

新增 `dbclient.annotation.LogicDelete`（`@Target(FIELD, ANNOTATION_TYPE)`、`RUNTIME`），属性：

- `String deletedValue() default "1"`、`String notDeletedValue() default "0"`；
- **不**通过 `@AliasFor` 合并进 `@Column`：注解只作标记，列名仍由既有规则决定（`@Column.value` 或字段名），需要自定义列名时叠加 `@Column` 即可。

理由：保持与列映射的解耦，避免改动 `@Column`/`@Id` 的公共语义；`@Column` 已承载 `exist`、`condition` 等职责，再加逻辑删除标志会让既有消费方（`columnHandler`、`batchInsertSql` 等）被迫感知新概念。

*备选：扩展 `@Column`（改动面大、语义混杂）；`@Table(logicDeleteColumn=...)` 表级配置（拿不到字段类型，无法做值转换与类型校验）。*

### D2. 元数据承载与规则解析

- `ColumnMetadata` 增加 `logicDelete` 标志与两个原始 `String` 取值；`EntityMetadata` 暴露逻辑删除列集合；
- 新增规则解析器（`EntityUtils` 上的静态方法，如 `getLogicDeleteRule(Class)`）：返回 `(列名, 已删除值, 未删除值)` 的解析结果（按字段类型转换后），并做校验：
  - 无标注 → 返回 `null`（调用方走原逻辑，零影响）；
  - 多于一个标注字段 → 抛 `IllegalArgumentException`（配置错误 fail fast，查询过滤路径同样抛出）；
  - 值转换：数值类型按数字解析；`boolean/Boolean` 接受 `true/false/1/0`；其他字段类型抛明确异常。

理由：转换集中在解析器一处，构建器与 `SQLFunctions` 共用；元数据构造阶段不做多字段抛错，避免把"配置错误"扩散到与该功能无关的普通 CRUD 上（提案第 4 条约束）。

### D3. 查询过滤注入点：`SqlBuilder` 渲染期组合

`SqlBuilder` 新增可选项：`logicDeleteColumn` + `deletedValue`（已转换的强类型值）+ `includeDeleted` 标志；在 `buildSql()` 的 **SELECT 分支**与 `getAllParams()` 中组合输出：

- 仅当 "设置了过滤 && 非 includeDeleted && SQLType == SELECT" 时生效；UPDATE/DELETE/BATCH 与 `SimpleSqlBuilder`（批量插入等）完全不受影响 → 物理删除、更新、批量插入天然免疫；
- 条件渲染为 `column <> ?`（"不等于已删除值"，与 spec 措辞一致），参数追加在 WHERE 参数之后、HAVING 参数之前，保证与 SQL 文本顺序一致；
- **括号包装**：`whereFragments` 非空时渲染为 `WHERE (原有全部条件) AND column <> ?`，避免用户使用 `or()`/`or(Consumer)` 时出现 `a OR b AND deleted <> ?` 的优先级错误；
- 渲染期组合为幂等纯函数，`getSqlTemp()`/`getParams()` 多次调用结果一致（执行器会分别调用两者）。

启用方（谁调用 `applyLogicDeleteFilter`）：

- `LambdaSqlBuilder` 增加受保护的启用钩子（解析 D2 规则；无规则为 no-op），由三个 SELECT 构建器在构造器中调用：`LambdaQueryBuilder`、`LambdaCountBuilder`、`LambdaSingleColumnQueryBuilder`（含拷贝构造路径，保证 `toSelect()/toCount()` 转换后仍生效）；
- `SQLFunctions.selectById / selectByEntity / selectByMap` 直接调用同一钩子。

*备选：执行器层对最终 SQL 字符串做改写（需解析 WHERE/ORDER BY 边界，脆弱，否决）；在 `LambdaSqlBuilder` 覆写 `getSqlTemp()` 做字符串拼接（同样脆弱且参数顺序易错，否决）；逐个查询函数分别注入（Lambda 路径的 SQL 由构建器预生成，函数层拿不到结构化片段，覆盖不全）。*

### D4. 逻辑删除/恢复操作：复用 `SQL_LAMBDA` 的 UPDATE 构建器 + 按 ID 专用模板

**构建器路**（条件、Lambda 入口）：

- 新增 `LambdaLogicDeleteBuilder<T>`：构造时 `updateSQL()` + `set(规则列名, deletedValue)`，条件方法全部继承自 `LambdaSqlBuilder`；`LambdaRestoreBuilder<T>` 同理写入 `notDeletedValue`；无标注实体在构造时抛明确异常；
- 新增客户端构建器 `LambdaClientLogicDeleteBuilder<T>` / `LambdaClientRestoreBuilder<T>`（继承 `AbstractLambdaClientBuilder`），执行方法 `logicDelete()` / `restore()` 委托 `BaseDBApi`；
- `LambdaConditionBuilder` 增加 `toLogicDelete()` / `toRestore()`（拷贝构造，与既有 `toDelete()/toUpdate()` 同构）；
- `LambdaBuilderApi` 增加 `lambdaLogicDelete()` / `lambdaRestore()`；`Lambda` 入口类增加静态 `logicDelete(Class)` / `restore(Class)`；
- `WriteApi` 增加 `@SQL(executor = SQL_LAMBDA) int logicDelete(LambdaLogicDeleteBuilder<E>)` 与 `int restore(LambdaRestoreBuilder<E>)`，以及面向条件构建器的 default 重载。

理由：这两个构建器本质是"SET 固定列 + 用户条件"的 UPDATE，走 `SQL_LAMBDA` 即复用既有执行器（UPDATE 分支返回影响行数），无需新增执行逻辑；`SQLType` 为 UPDATE，天然不会被 D3 的 SELECT 过滤干扰。

**按 ID 路**：

- `DbApi` 增加模板常量 `SQL_LOGIC_DELETE_BY_ID = "#{logicDeleteById($mc$)}"`、`SQL_RESTORE_BY_ID = "#{restoreById($mc$)}"`；
- `WriteApi` 增加 `logicDeleteById(Object id)` / `restoreById(Object id)`；`SQLFunctions` 新增同名函数，流程对齐既有 `deleteById`：先校验 `id != null`，实体类经 `BaseDBApi` 泛型解析，规则经 D2 解析（未标注实体抛异常），主键列沿用 `EntityUtils.getIdColumn`（首个 `@Id` 列），生成 `UPDATE 表 SET 列 = ? WHERE 主键列 = ?`。

*备选：复用 `LambdaUpdateBuilder` 由使用者自行 `set(列, 值)`（没有"逻辑删除"语义入口，不满足 spec 的显式方法要求，否决）；在 `WriteApi` default 方法内拼 SQL（框架的 SQL 执行必须经 `@SQL` 模板 + 代理，default 方法无法执行，否决）。*

### D5. `includeDeleted` 显式入口：SELECT 构建器链式开关

- 公开链式方法 `includeDeleted()` 仅加在三个 SELECT 构建器（`LambdaQueryBuilder`、`LambdaCountBuilder`、`LambdaSingleColumnQueryBuilder`）及对应客户端构建器（`LambdaClientQueryBuilder`、`LambdaClientCountBuilder`、`LambdaClientSingleColumnQueryBuilder`）上；内部置位 `SqlBuilder` 的 `includeDeleted` 标志并返回自身保持链式；
- 调用时即校验（D2 规则解析）：未标注实体调用抛明确异常（对应 spec 场景，不执行任何 SQL）；
- 覆盖"列表、单条、分页（含总数口径一致）、COUNT、流式、单列"的显式查询，实体条件查询可通过 `lambdaQuery(条件实体).includeDeleted()` 表达。

*备选：为每个 `selectXxx` 增加 `selectXxxIncludeDeleted` 变体（API 数量翻倍、认知负担大，否决）；全局开关（粒度太粗，且违反"仅新增显式方法"的确认决策，否决）。*

### D6. 按 ID 操作的主键识别

沿用既有 `deleteById` 语义：取实体首个 `@Id` 列（`EntityUtils.getIdColumn`），不支持复合主键的按 ID 便捷操作（与现状一致）；复合主键场景使用条件构建器路径。null 校验在拼接 SQL 之前完成，保证"明确异常且不执行 SQL"。

## Risks / Trade-offs

- **[历史数据 NULL 被自动过滤]** 补列后旧行若为 NULL，`column <> ?` 的结果为 NULL → 行被排除 → **迁移必须回填默认值**（见 Migration Plan）。备选 `(column <> ? OR column IS NULL)` 会引入"NULL 视为未删除"的隐式语义，不采纳。
- **[JOIN 场景]** 过滤条件使用未限定列名且只过滤主实体表；若关联表存在同名列会产生歧义 → 文档与 javadoc 中明确，必要时使用方改用显式条件；主表别名限定支持列入后续增强。
- **[`<>` 的索引利用]** 二值域下普通索引仍可正常使用；若某些数据库对 `<>` 选择率不理想，后续可支持渲染为 `= 未删除值`（列入 Open Questions，不改变方案骨架）。
- **[saveOrUpdate 与逻辑删除联动]** `saveOrUpdate` 内部走被过滤的 `selectById`，已逻辑删除记录会被判定为"不存在"而走插入 → 文档说明该组合的预期；需要时用恢复或显式查询自检。
- **[raw SQL 路径不过滤]** 原生 SQL 与裸 `SqlBuilder` 用法无法可靠注入 → 属 Non-Goals，javadoc 注明。
- **[值转换失败]** 在规则解析/构建器构造阶段抛 `IllegalArgumentException`（fail fast），不会产生半成品 SQL。
- **[括号包装的兼容性]** 追加条件时对既有条件整体加括号，SQL 语义等价但文本形态变化：仅影响标注实体（新行为），未标注实体渲染完全不变。

## Migration Plan

1. 升级 lucky-spring 版本（向后兼容：未标注实体与既有 API 零变化）；
2. 使用方 DDL：为需要逻辑删除的表补列，必须带默认值并回填历史数据，例如
   `ALTER TABLE user ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;`
3. 实体字段加 `@LogicDelete`（默认 1/0；自定义值或列名按需配置）；
4. 删除调用逐步迁移到 `logicDelete*`/`lambdaLogicDelete()`；查询无需改动（自动过滤）；需要查已删除数据时用 `includeDeleted()`；
5. **回滚**：移除字段注解或不再调用新 API 即恢复旧行为；逻辑删除不物理删除数据，无不可逆数据风险。

## Open Questions

- 过滤条件渲染形式（`<> 已删除值` vs `= 未删除值`）若遇特定数据库索引问题，可后续优化为内部可切换实现。
- JOIN 场景下主表逻辑列的别名限定（如 `t.deleted <> ?`）可作为后续增强，不影响本方案骨架。
