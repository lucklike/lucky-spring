# Tasks: dbclient-optimize-logical-deletion-v1

## 1. 基线准备

- [x] 1.1 建立变更前基线：依次运行 `.tmp-jdtest\build.ps1`、`run_logicdelete.ps1`、`run_scenarios.ps1`、`run_regress.ps1`；验证：输出 JAVAC_OK、各脚本 *_EXIT=0 且无 [FAIL]（作为后续对照基线）

## 2. 实现：客户端条件构建器转换入口

- [x] 2.1 在 `LambdaClientConditionBuilder` 的"类型转换方法"区域（`toUpdate()` 之后、`toColumn()` 之前）新增 `public final LambdaClientLogicDeleteBuilder<T> toLogicDelete()`，以 `this.baseDBApi` + `this.sqlBuilder` 复用目标构建器既有 `(BaseDBApi, LambdaSqlBuilder)` 构造器组装；javadoc 对齐 `toDelete()` 风格（操作说明、空条件全表影响警告、使用示例、`@throws IllegalArgumentException`）；验证：运行 `build.ps1` 输出 JAVAC_OK

- [x] 2.2 同区域新增 `public final LambdaClientRestoreBuilder<T> toRestore()`，同构组装；javadoc 说明恢复语义（写回未删除值、恢复后重新对常规查询可见）与空条件警告、`@throws`；验证：`build.ps1` 输出 JAVAC_OK

- [x] 2.3 更新 `LambdaClientConditionBuilder` 类级 javadoc 的转换示例，补充 `.toLogicDelete().logicDelete()` 与 `.toRestore().restore()` 用法；验证：`build.ps1` 输出 JAVAC_OK，且类头示例区含两个新入口（查阅源码确认）

## 3. 探针行为验证

- [x] 3.1 扩展 `LogicDeleteProbe`（编辑后先运行 `build.ps1` 刷新 target\classes）：新增客户端转换断言组——反射断言 `toLogicDelete()` / `toRestore()` 存在且返回类型为对应客户端构建器；以 JDK 动态代理构造 `BaseDBApi` 桩（捕获 `logicDelete` / `restore` 派发并返回哨兵行数），对新条件构建器（多条件含嵌套括号）断言：转换后执行派发的 SQL 为 `UPDATE t_logic_user SET deleted = ? WHERE <已建条件>`（toLogicDelete 写已删除值、toRestore 写未删除值）、参数为 SET 值在前且已建条件参数完整保留、执行返回值透传哨兵值；验证：`run_logicdelete.ps1` 输出 LD_EXIT=0 且无 [FAIL]

- [x] 3.2 扩展 `LogicDeleteProbe`（续）：失败语义与隔离断言——未标注实体转换抛 `IllegalArgumentException` 且桩未捕获任何 SQL 派发（不执行 SQL）；转换后继续在源构建器追加条件，已转换构建器再次执行派发的 SQL 保持不变；同一源连续两次转换结果互相独立；验证：`run_logicdelete.ps1` 输出 LD_EXIT=0 且无 [FAIL]

## 4. 场景映射与全量回归

- [x] 4.1 扩展 `LogicDeleteScenarioProbe`（编辑后先运行 `build.ps1`）：新增一节（R7）映射本变更 delta 规格的 3 个 Scenario——转换逻辑删除（WHERE 含全部已建条件、SET 已删除值、返回受影响行数）；转换恢复（SET 未删除值、恢复值满足常规查询过滤即恢复值≠过滤值，参照 S6.1 手法、返回受影响行数）；未标注实体转换失败且不执行任何 SQL；验证：`run_scenarios.ps1` 输出 SCENARIO_EXIT=0 且无 [FAIL]

- [x] 4.2 全量回归验收：在最终代码上依次运行 `build.ps1`（JAVAC_OK）→ `run_logicdelete.ps1`（LD_EXIT=0）→ `run_scenarios.ps1`（SCENARIO_EXIT=0）→ `run_regress.ps1`（SERIAL/JOIN/SPLIT/LEGACY_EXIT 均为 0）；验证：全部脚本无 [FAIL]、exit 0（`run_regress.ps1` 的 LegacyBehaviorProbe 覆盖未标注实体与既有路径行为与变更前一致）
