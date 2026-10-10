## Context

dbclient 模块已具备完善的注解驱动元数据机制（以 `@LogicDelete` 为范式），通过 `@Column` → `ColumnMetadata` → `EntityMetadata` → `EntityMetadataFactory` 缓存链路运行。SpEL 求值基础设施也已完备：`BeanEvaluationContextFactory` + `SpELRuntime` 支持 Spring Bean 调用、静态方法访问、类型白/黑名单限制。

当前自动填充场景缺失，每次插入/更新都需手动设置审计字段（create_time、update_time 等）。需要在不侵入原有 SQL 生成逻辑的前提下，提供可扩展的自动填充能力。

## Goals / Non-Goals

**Goals:**
- 在 INSERT/UPDATE 操作入口统一拦截，按 `@AutoFill` 注解标注的场景（INSERT/UPDATE/INSERT_AND_UPDATE）自动评估 SpEL 表达式并填充实体字段
- 复用 EntityMetadataFactory 缓存机制，避免批量操作重复反射
- 提供 FillHandler SPI，允许业务叠加自定义填充逻辑（如从安全上下文获取用户 ID）
- 完全向后兼容：未标注 `@AutoFill` 的实体零感知、零性能损耗

**Non-Goals:**
- 不涉及修改底层 JDBC 执行器（`AbstractMCNamedJdbcTemplateSQLExecutor`）
- 不做运行时 AOP 或动态代理注入（框架本身不支持）
- v1 不实现 Lambda update 路径的自动填充（需从 SqlBuilder 内部状态提取实体，复杂度较高，v2 预留）
- 不做数据库DDL自动同步（仅应用层字段填充）

## Decisions

### Decision 1: 注解设计与元数据集成方式

**选择：** 新增 `@AutoFill` 注解，在 `EntityMetadataFactory.buildColumnMetadata()` 中统一解析，`ColumnMetadata` 扩展字段存储填充信息。

** rationale:**
- 与 `@LogicDelete` 采用同一元数据工厂流水线，共享 `EntityMetadataFactory.getMetadata(clazz)` 缓存
- `ColumnMetadata` 已是不可变 Value Object，扩展字段不影响现有消费者
- 避免新建独立 `AutoFillMetadata` 类导致元数据碎片化

**替代方案：**
1. 独立 `AutoFillMetadata` 类
   - 优点：职责单一
   - 缺点：增加一次反射查找开销、元数据分散
2. 在 `EntityMetadata` 中用 Map<Class<?>, List<Field>> 暂存
   - 缺点：类型不安全，违背已有列元数据模型

### Decision 2: 填充执行入口定位在 SQLFunctions

**选择：** 所有写入生成方法（`insertSql`、`batchInsertSql`、`updateById`、`batchUpdateById`、`logicDeleteById`、`restoreById`）开头统一调用 `fillAuditFields(entity, scene)` 私有方法。

**Rationale:**
- SQLFunctions 是所有 DBApi 写入操作的必经点（`@SQL(executor = SQL_INSERT_SQL)` 等模板会路由到此）
- 此处填充可确保：①先回写实体字段 ②后续 `columnHandler()` 扫描到非空值自动入 SET
- 与 `EntityUtils.applyLogicDeleteFilter(sqlBuilder, entityClass)` 位置平行，心智模型一致

**替代方案：**
1. 在执行器链 `SQLWrapperExecutor.execute()` 拦截
   - 缺点：需修改 execute 分发逻辑，且 batch 场景参数是 `Object[][]` 无法回填
2. 在 WriteApi 默认方法包装（如 `insert(entity)` 内调用 `_insert_()` 前填充）
   - 缺点：Lambda update 路径不经过这些默认方法，覆盖不全

### Decision 3: SpEL 求值上下文与引擎复用

**选择：** 复用现有 `SpELRuntime` + `BeanEvaluationContextFactory`，构造 `FillContext` 作为根变量传入。

**FillContext 字段：**
```java
class FillContext {
    Object entity;              // 当前实体实例
    Class<?> entityClass;       // 实体类型
    AuditFillScene scene;       // INSERT / UPDATE
    String methodName;          // "insert" / "updateById" / ...
}
```

**Rationale:**
- 项目中 `BeanSpELRuntimeFactoryFactory` 已提供 `SpELRuntime` 实例，支持：
  - `@beanName.method()` 访问 Spring Bean
  - `T(full.ClassName).staticMethod()` 调用静态方法
  - 类型白/黑名单安全控制
- 无需新建 SpEL 引擎，避免重复初始化开销

**替代方案：**
1. 新建轻量级表达式引擎（如 OGNL、MVEL）
   - 缺点：增加依赖，且项目已有完整 SpEL 基础设施
2. 用 String.replace() 简单替换占位符
   - 缺点：不支持复杂表达式、无安全校验

### Decision 4: 枚举命名 — AuditFillScene vs FillScene

**选择：** 使用 `AuditFillScene` 枚举名（值：`INSERT`、`UPDATE`、`INSERT_AND_UPDATE`）。

**Rationale:**
- 明确表达"审计字段"领域语义，与 `@LogicDelete` 同属审计治理范畴
- 若未来扩展"业务序列号填充"等非审计场景，可新建 `BusinessFillScene` 等枚举

**替代方案：**
1. 通用名 `FillScene`
   - 缺点：语义模糊，不知道 fill 什么内容

### Decision 5: SPI 设计 — FillHandler + Filler 双层接口

**选择：**
```java
interface FillHandler {
    List<Filler> getFillers(Class<?> entityClass, AuditFillScene scene);
    boolean needsBackfill(); // true=需要回写实体供后续 columnHandler 采集
}

interface Filler {
    /**
     * @param entity 实体对象
     * @param scene 场景
     * @return 填充后的字段 Map（fieldName -> fieldValue），null 表示跳过
     */
    Map<String, Object> fill(Object entity, AuditFillScene scene);
}
```

**Rationale:**
- `FillHandler` 负责按场景/实体类型路由可用 Filler（可注册多个）
- `Filler` 负责具体填充逻辑（默认实现做 SpEL 求值 + 类型转换）
- `needsBackfill()` 控制是否要先回写实体再走 SQL 生成——单条 insert/updateById 必须回填；批量可选择不回填直接处理集合

**替代方案：**
1. 单层接口 `Filler` + 静态注册表
   - 缺点：缺少场景路由能力，难以支持多租户/多上下文切换
2. 只用 ThreadLocal 传递填充值 Map
   - 缺点：线程安全问题，且批量操作无法隔离

### Decision 6: Lambda Update 路径的处理策略

**选择：** v1 **暂不支持** Lambda update 路径，在文档中标注为 Known Limitation。

**Rationale:**
- Lambda 更新入口 `lambdaSql(MethodContext mc)` 的参数是 `LambdaUpdateBuilder<T>`，不直接接收实体对象
- 要从 SqlBuilder 内部状态反推实体，需修改 `LambdaSqlBuilder` 构造函数签名，改动面大且易引入回归
- 实际场景中，Lambda update 通常用于批量条件更新（"将所有年龄>18的用户状态改为1"），此类操作本就不适合自动填充 updateTime（受影响记录的 updateTime 可能应不同）

**替代方案（v2 预留）：**
- 在 `AbstractLambdaClientBuilder` 的 `.set()` 链结束后触发填充钩子
- 或客户端构建器新增 `.fill(AuditFillScene scene)` 显式标记

### Decision 7: 类型转换策略

**选择：** 在 `Filler` 默认实现中做宽松类型转换，支持以下映射：
- `String` → 原始类型（`Integer`、`Long`、`BigDecimal` 等）用构造函数/valueOf
- `Number` → 兼容的基本类型包装类（`long` → `Long`）
- `LocalDateTime` / `Date` / `Long`(时间戳) → 相互按约定转换
- 其他类型直接赋值（由 SpEL 保证返回类型正确）

**Rationale:**
- Spring `ConversionService` 已支持大部分常见类型转换
- `ApplicationConversionService` 在项目启动时已初始化

**替代方案：**
1. 严格要求表达式返回类型与字段类型完全一致
   - 缺点：开发者体验差，`T(java.time.LocalDateTime).now()` 在某些 JDK 版本可能返回 `Date`
2. 用 Jackson/FASTJSON 序列化转换
   - 缺点：性能开销大，不适合高频填充场景

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| **SpEL 表达式注入攻击**：攻击者若能控制表达式字符串，可执行任意代码 | 高 | 复用项目现有类型白/黑名单机制（`SpELConfiguration.typeWhiteList`），`BeanEvaluationContextFactory` 已集成安全限制 |
| **批量操作性能下降**：逐条填充 1000 条记录时 SpEL 求值开销 | 中 | 元数据缓存已在第一次解析后命中；SpEL 求值本身 < 1ms/次；压测验证 |
| **updateById 误填充 null**：实体中 updateTime 原为 null，但 SpEL 返回 null 时不应写入 SET | 低 | 填充逻辑遵循 `@LogicDelete` 范式：表达式结果为 null 则跳过该字段 |
| **FillHandler 竞态条件**：静态注册表在多线程环境下被并发修改 | 低 | 使用 `ConcurrentHashMap` + 不可变列表（`Collections.unmodifiableList`） |
| **Lambda update 路径空白**：部分用户期望 lambda 更新也自动填充 updateTime | 中 | v1 文档明确说明；v2 根据反馈决定实现方案（显式标记或内部状态提取） |
| **事务边界问题**：填充发生在事务开始前的 DAO 层，若填充表达式有副作用可能导致重复执行 | 低 | 要求填充表达式必须是纯函数（推荐用 `T().now()` 而非 `@service.doSomething()`） |

## Migration Plan

### Phase 1: 基础架构搭建（第1周）
1. 创建 `@AutoFill` 注解、`AuditFillScene` 枚举
2. 扩展 `ColumnMetadata`、`EntityMetadata` 承载填充元数据
3. `EntityMetadataFactory.buildColumnMetadata()` 增加 `@AutoFill` 解析逻辑

### Phase 2: SPI 与默认 Filler（第2周）
4. 定义 `FillHandler`、`Filler` 接口
5. 实现 `DefaultSpELFiller`（SpEL 求值 + 类型转换）
6. 创建 `FillRegistry` 持有全局 Filler 列表（静态方法注册）

### Phase 3: SQLFunctions 集成（第3周）
7. 在 `insertSql()`、`batchInsertSql()` 加入填充调用
8. 在 `updateById()`、`batchUpdateById()` 加入填充调用
9. 处理 `updateById` 特殊逻辑：审计列强制入 SET（跳过非空判断）

### Phase 4: 逻辑删除/恢复路径 + 测试（第4周）
10. 在 `logicDeleteById()`、`restoreById()` 加入填充调用
11. 编写单元测试：
    - 单条插入/更新场景
    - 批量插入/更新场景
    - SpEL 表达式求值场景（静态方法、Spring Bean）
    - 空值跳过场景
    - 向后兼容性场景（无注解实体）
12. 压测验证批量操作性能

### Deployment Strategy
- **蓝绿部署兼容**：新代码部署后，未标注 `@AutoFill` 的实体行为不变
- **灰度开关**：可通过 `FillRegistry.clearAll()` 全局关闭填充功能
- **回滚策略**：回滚到旧版本代码即可，不影响任何数据

## Open Questions

1. **FillHandler 自动发现机制**：是否需要 Spring Boot AutoConfiguration 自动扫描 `@Component` Filler？
   - v1 建议手动 `FillRegistry.register(new MyFiller())`，v2 根据反馈添加自动扫描
2. **时区约定**：`LocalDateTime.now()` 在不同时区下填充值可能不一致，是否需要全局时区配置？
   - 当前依赖 JRE 默认时区，如需统一可在 v2 增加 `@AutoFill(timezone = "Asia/Shanghai")` 属性
3. **填充顺序冲突**：同一字段标注了 `@AutoFill` 和 `@FillColumn`（假设有其他填充注解），谁优先？
   - v1 只支持 `@AutoFill`，后续如有其他填充注解需定义优先级协议
4. **乐观锁冲突**：如果实体同时存在 `@Version` 乐观锁字段和 `updateTime` 自动填充，两者更新顺序是否有依赖？
   - 当前按字段声明顺序依次填充；如需严格排序可在 v2 增加 `@AutoFill(order = 1)` 属性
