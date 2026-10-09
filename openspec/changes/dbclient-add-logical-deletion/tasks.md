# Tasks: dbclient-add-logical-deletion

## 1. 编译基线与注解/元数据基础

- [x] 1.1 建立编译与探针基线：用 javac（classpath 取 `.tmp-jdtest\cp.txt`，全量编译 `src/main/java` 至 `target\classes`）确认编译 exit 0，并运行既有探针脚本 `run_regress.ps1` 确认探针环境可用（无 FAILED）
- [x] 1.2 新增 `dbclient.annotation.LogicDelete` 注解：`@Target({FIELD, ANNOTATION_TYPE})`、`@Retention(RUNTIME)`，提供 `deletedValue()` 默认 `"1"`、`notDeletedValue()` 默认 `"0"`；验证：javac 编译通过，探针断言注解保留策略与默认值正确
- [x] 1.3 扩展元数据层：`ColumnMetadata` 增加逻辑删除标志与两个原始值字段，`EntityMetadata` 暴露逻辑删除列，`EntityMetadataFactory` 构建时解析 `@LogicDelete`（不改变其他列语义）；验证：探针断言标注实体元数据含逻辑删除列、未标注实体不含、普通字段元数据与既有行为一致
- [x] 1.4 在 `EntityUtils` 实现逻辑删除规则解析器（如 `getLogicDeleteRule(Class)`）：无标注返回 null；多字段标注抛 `IllegalArgumentException`；按字段类型转换取值（数值按数字解析，`boolean/Boolean` 支持 `true/false/1/0`），其余类型抛明确异常；验证：探针覆盖默认值、自定义值、布尔、非法类型、多字段、未标注六类场景，断言全部通过

## 2. 查询自动过滤（SqlBuilder 渲染期注入）

- [x] 2.1 扩展 `SqlBuilder`：增加逻辑删除列、已删除值、`includeDeleted` 标志配置，在 `buildSql()` 的 SELECT 分支与 `getAllParams()` 组合注入（仅 SELECT 生效；渲染 `column <> ?` 且参数位于 WHERE 参数之后、HAVING 参数之前；既有 WHERE 条件非空时整体括号包装）；验证：探针断言无 WHERE、有 WHERE、含 `or()` 条件三种形态的 SQL 文本与参数顺序，且 UPDATE/DELETE（含 `deleteById` 路径）渲染与变更前一致
- [x] 2.2 `LambdaSqlBuilder` 增加受保护启用钩子（解析 1.4 规则并置位过滤配置，无规则为 no-op），由 `LambdaQueryBuilder`、`LambdaCountBuilder`、`LambdaSingleColumnQueryBuilder` 构造器调用（含拷贝构造路径，保证 `toSelect()`/`toCount()` 转换后仍生效）；验证：探针断言三个构建器对标注实体生成的 SQL 均含过滤条件，对未标注实体与变更前完全一致
- [x] 2.3 `SQLFunctions.selectById` / `selectByEntity` / `selectByMap` 接入同一启用钩子（保持 `@SQL` 模板与异常语义不变，未标注实体零变化）；验证：javac 编译通过；探针直接断言公共钩子对标注实体渲染含过滤条件、对未标注实体不受影响（函数内部路径由 5.2 场景核对覆盖）

## 3. 逻辑删除与恢复操作

- [x] 3.1 新增 `LambdaLogicDeleteBuilder<T>` 与 `LambdaRestoreBuilder<T>`：构造时基于 `updateSQL()` 将规则列 set 为已删除值/未删除值，条件方法继承 `LambdaSqlBuilder`；未标注实体构造时抛明确异常；验证：探针断言两者生成的 SQL 为 `UPDATE ... SET 列 = ? WHERE ...`、参数为对应规则值，未标注实体构造抛异常
- [x] 3.2 新增客户端构建器 `LambdaClientLogicDeleteBuilder<T>` / `LambdaClientRestoreBuilder<T>`（继承 `AbstractLambdaClientBuilder`，执行方法委托 `BaseDBApi`），并给 `LambdaConditionBuilder` 增加 `toLogicDelete()` / `toRestore()`（拷贝构造切换语义，与 `toDelete()`/`toUpdate()` 同构）；验证：探针断言类型与转换后 SQL 形态（`toDelete()` 仍为物理删除、`toLogicDelete()` 为 UPDATE）
- [x] 3.3 按 ID 路径：`DbApi` 增加 `SQL_LOGIC_DELETE_BY_ID = "#{logicDeleteById($mc$)}"`、`SQL_RESTORE_BY_ID = "#{restoreById($mc$)}"` 常量；`WriteApi` 增加 `logicDeleteById(Object)` / `restoreById(Object)`；`SQLFunctions` 新增同名函数（先校验 id 非空、未标注实体抛异常，主键列沿用 `EntityUtils.getIdColumn`，生成 `UPDATE 表 SET 列 = ? WHERE 主键列 = ?`）；验证：探针断言常量值、方法存在性及 `@SQL` 模板绑定，javac 编译通过
- [x] 3.4 API 入口：`WriteApi` 增加 `logicDelete(LambdaLogicDeleteBuilder<E>)` / `restore(LambdaRestoreBuilder<E>)`（`@SQL(SQL_LAMBDA)`）及面向条件构建器的 default 重载；`LambdaBuilderApi` 增加 `lambdaLogicDelete()` / `lambdaRestore()`；`Lambda` 入口类增加静态 `logicDelete(Class<T>)` / `restore(Class<T>)`；验证：探针断言全部新方法存在、`@SQL` 模板值与客户端构建器类型正确

## 4. 显式包含已删除记录的查询入口

- [x] 4.1 在 `LambdaQueryBuilder`、`LambdaCountBuilder`、`LambdaSingleColumnQueryBuilder` 及对应客户端构建器（`LambdaClientQueryBuilder`、`LambdaClientCountBuilder`、`LambdaClientSingleColumnQueryBuilder`）增加链式 `includeDeleted()`：置位 SqlBuilder 标志并返回自身，调用时解析规则，未标注实体抛明确异常；验证：探针断言 `includeDeleted()` 后 SQL 不含过滤条件（分页/COUNT 加工口径一致）、对应常规查询仍含过滤条件、未标注实体抛异常

## 5. 回归与场景验证

- [x] 5.1 既有行为回归：扩展回归探针覆盖未标注实体的查询、物理删除、更新路径，断言 SQL 文本与参数与变更前完全一致（对照基线在 1.1 阶段先行记录）；验证：回归脚本运行无 FAILED、exit 0
- [x] 5.2 端到端场景核对：将 spec 的 25 个场景逐条映射为探针断言（各查询入口过滤一致性、分页含总数与 COUNT 口径一致、重复逻辑删除幂等、恢复后常规查询重新可见、`includeDeleted()` 全路径含分页口径、未标注/非法入参异常且不产生 SQL）；验证：场景探针全部断言通过、exit 0（数据层语义以写路径 SQL 与查询过滤条件的组合断言呈现）
