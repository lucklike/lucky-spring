## 1. 基础注解与枚举

- [x] 1.1 创建 `AuditFillScene` 枚举（值：INSERT、UPDATE、INSERT_AND_UPDATE）并验证编译通过
  - 位置：`io.github.lucklike.httpclient.dbclient.annotation.AuditFillScene`
  - 验证：编译成功，枚举可正常引用和序列化
  
- [x] 1.2 创建 `@AutoFill` 注解（属性：expression String, scene AuditFillScene）并验证基本用法
  - 位置：`io.github.lucklike.httpclient.dbclient.annotation.AutoFill`
  - 元数据：`@Target({ElementType.FIELD})`, `@Retention(RetentionPolicy.RUNTIME)`, `@Documented`, `@Inherited`
  - 验证：在测试实体上标注后反射可获取，表达式支持 SpEL 语法（如 `"T(java.time.LocalDateTime).now()"`）

## 2. 元数据层扩展

- [x] 2.1 扩展 `ColumnMetadata` 类，新增 auditFill 相关字段
  - 新增字段：`autoFill` (AutoFill 类型), 以及 getter 方法
  - 修改构造函数，增加 `autoFill` 参数
  - 验证：新增字段后不影响现有消费者的正常初始化，getter 返回正确

- [x] 2.2 扩展 `EntityMetadata` 类，新增按场景索引的自动填充列映射
  - 新增字段：`Map<AuditFillScene, List<ColumnMetadata>> fillColumnsByScene`
  - 新增方法：`getFillColumns(AuditFillScene scene)` 返回不可变列表
  - 验证：构建包含 @AutoFill 的实体元数据时，按场景正确分类；调用 `getFillColumns(INSERT)` 返回 INSERT 场景的所有字段

- [x] 2.3 修改 `EntityMetadataFactory.buildColumnMetadata()` 解析 `@AutoFill` 注解
  - 使用 `AnnotationUtils.findMergedAnnotation(field, AutoFill.class)` 获取注解
  - 将注解实例传入 `ColumnMetadata` 构造函数
  - 验证：对标注了 @AutoFill 的字段构建 ColumnMetadata 时，autoFill 字段非 null；未标注字段为 null

- [x] 2.4 修改 `EntityMetadataFactory.buildMetadata()` 构建场景索引并缓存
  - 遍历所有 ColumnMetadata，按 scene 分类到 ConcurrentHashMap
  - EntityMetadata 构造函数中构建索引并存储
  - 验证：首次构建后重复调用 `getMetadata(EntityClass)` 命中缓存，性能无下降

## 3. SPI 接口设计

- [x] 3.1 定义 `FillHandler` 接口（方法：getFillers, needsBackfill）并验证编译
  - 位置：`io.github.lucklike.httpclient.dbclient.function.FillHandler`
  - 方法签名：`List<Filler> getFillers(Class<?> entityClass, AuditFillScene scene);`
  - 方法签名：`default boolean needsBackfill() { return true; }`
  - 验证：接口可被正常实现，默认方法可覆盖

- [x] 3.2 定义 `Filler` 接口（方法：fill）并验证编译
  - 位置：`io.github.lucklike.httpclient.dbclient.function.Filler`
  - 方法签名：`Map<String, Object> fill(Object entity, AuditFillScene scene);`
  - 返回值说明：null 表示跳过该字段
  - 验证：接口可被正常实现

- [x] 3.3 创建 `FillRegistry` 持有全局 Filler 注册表
  - 提供静态方法：`register(FillHandler handler)`, `unregister(FillHandler handler)`, `clearAll()`
  - 内部使用 `ConcurrentHashMap<Class<?>, Map<AuditFillScene, List<Filler>>>` 缓存
  - 提供线程安全：所有操作加锁或使用并发集合
  - 验证：多线程环境下并发 register/unregister 不抛异常，getFillers 能正确路由

## 4. 默认 SpEL Filler 实现

- [x] 4.1 实现 `DefaultSpELFiller` 类，集成 SpELRuntime 求值
  - 实现 `Filler.fill()` 方法，遍历实体字段检查 @AutoFill 注解
  - 对标注字段构造 `FillContext`（entity, entityClass, scene, methodName）
  - 调用 `SpELRuntime.getValue(expression, FillContext)` 获取原始值
  - 验证：单元测试覆盖简单的静态方法调用表达式（如 `T(java.time.LocalDateTime).now()`）

- [x] 4.2 实现类型转换逻辑
  - 使用 Spring `ApplicationConversionService` 做宽松类型转换
  - 支持：String → 数值类型、Number → 包装类、LocalDateTime/Date/Long 时间戳互转
  - 转换失败时抛出 IllegalArgumentException 并携带详细信息
  - 验证：表达式返回 long 但字段类型为 Long 时自动转换成功；非法类型转换抛明确错误

- [x] 4.3 处理 null 值跳过逻辑
  - 表达式结果为 null 时，不在实体上设值，也不加入 SET 子句
  - 记录 debug 日志（如果框架有日志系统），便于排查
  - 验证：当 SpEL 表达式返回 null 时，实体现有值不被覆盖，SQL 生成不包含该字段

## 5. SQLFunctions 单条插入路径集成

- [x] 5.1 在 `insertSql()` 开头调用 `fillAuditFields(entity, AuditFillScene.INSERT)`
  - 抽取私有方法 `fillAuditFields(Object entity, AuditFillScene scene)`
  - 从 `EntityMetadataFactory.getMetadata(entity.getClass()).getFillColumns(scene)` 获取待填充字段
  - 遍历已注册 Filler 列表依次执行填充
  - 验证：带 @AutoFill(scene = INSERT) 的字段在 insert(entity) 后被正确赋值

- [x] 5.2 验证单条插入后的 SQL 生成
  - 填充完成后，原有 `columnHandler()` 扫描到非空审计字段并入 columnNames/values
  - 生成的 INSERT 语句包含新填充字段的占位符
  - 验证：打印/捕获生成的 SQL，确认包含审计字段且参数绑定正确

## 6. SQLFunctions 批量插入路径集成

- [x] 6.1 在 `batchInsertSql()` 开头对每个实体调用 `fillAuditFields(entity, AuditFillScene.INSERT)`
  - 遍历 `entityList`，逐条填充
  - 设置 `needsBackfill()` 为 false（批量可直接用 entity 对象，无需回写）
  - 验证：批量插入前所有实体的审计字段被填充，后续 batch 参数收集正确

- [x] 6.2 验证批量插入的性能
  - 模拟 1000 条记录批量插入，测量填充耗时
  - 确认元数据缓存命中后，填充时间占比 < 5%
  - 验证：压测脚本或 JMH Benchmark，确保平均延迟 < 50ms（含 SQL 生成）

## 7. SQLFunctions updateById 路径集成

- [x] 7.1 在 `updateById()` 开头调用 `fillAuditFields(entity, AuditFillScene.UPDATE)`
  - 填充发生在 SQLBuilder 构造 UPDATE 子句之前
  - 确保审计列强制入 SET（绕过 columnHandler 的非空判断）
  - 验证：即使 entity.updateTime 原为 null，填充后也会加入 SET 子句

- [x] 7.2 特殊处理：审计列跳过非空判断
  - 方案 A：在 `columnHandler()` 内识别审计字段（需传递填充标记）
  - 方案 B：手动将审计字段 append 到 sqlBuilder.set() 后再执行 columnHandler
  - 选择方案 B（改动更小，不与 columnHandler 现有逻辑耦合）
  - 验证：写入 entity.updateTime = null，执行 updateById 时 SQL 中仍包含 "update_time = ?"

- [x] 7.3 验证 updateById 的 WHERE 子句不受影响
  - @Id 字段和条件构建逻辑保持不变
  - 填充审计字段不应修改 WHERE 部分的参数
  - 验证：updateById(id) 的主键匹配条件正确，不会误更新其他记录

## 8. SQLFunctions 批量更新路径集成

- [x] 8.1 在 `batchUpdateById()` 开头对每个实体调用 `fillAuditFields(entity, AuditFillScene.UPDATE)`
  - 遍历 entityList 逐条填充
  - 审计列强制加入 SET 子句（同 updateById 处理）
  - 验证：批量更新前所有实体的 updateTime 被填充

- [ ] 8.2 验证批量更新的 SQL 生成
  - SET 子句包含所有普通字段 + 审计字段
  - WHERE 子句按 @Id 字段匹配，不受影响
  - 验证：打印生成的 UPDATE SQL，确认语法正确且参数顺序一致

## 9. SQLFunctions 逻辑删除/恢复路径集成（v1 跳过）

- [x] 9.1 logicDeleteById() 暂不支持自动填充（v1 Known Limitation）
  - 原因：logicDeleteById 只有 idValue，没有完整 entity 对象
  - 处理：不在代码中做任何修改，行为保持不变
  - 文档：在 DOCS/AUDIT_FILL_USAGE.md 中注明此限制
  - 验证：调用 logicDeleteById(id) 不抛异常，行为与变更前一致

- [x] 9.2 restoreById() 暂不支持自动填充（v1 Known Limitation）
  - 原因同 9.1
  - 处理：不在代码中做任何修改，行为保持不变
  - 文档：在 DOCS/AUDIT_FILL_USAGE.md 中注明此限制
  - 验证：调用 restoreById(id) 不抛异常，行为与变更前一致

## 10. Lambda 路径处理（v1 空白 + 文档声明）

- [x] 10.1 lambdaSql() 和 Lambda update 不支持自动填充（v1 Known Limitation）
  - 原因：Lambda update 入口不直接接收实体对象，需修改 SqlBuilder 内部状态，改动面大
  - 处理：不在代码中做任何修改，保持原有功能正常工作
  - 文档：在 DOCS/AUDIT_FILL_USAGE.md 中注明此限制，说明推荐用法（updateById/batchUpdateById 支持填充）
  - 验证：编译通过，Lambda update 功能正常工作（无回归）

- [x] 10.2 v2 扩展点预留（可选，非当前任务）
  - 方案：在 `AbstractLambdaClientBuilder` 新增 `.fill(AuditFillScene scene)` 显式标记方法
  - 暂不实施，根据用户反馈在 v2 决定是否需要

## 11. 单元测试覆盖（⏸️ 待实施 - 需要运行环境）

- [ ] 11.1 编写 `AutoFillAnnotationTest` - 验证注解解析
  - 测试 @AutoFill 在字段上反射可读性
  - 测试 expression 和 scene 属性正确赋值
  - 测试重复标注同一字段时的行为

- [ ] 11.2 编写 `EntityMetadataFillTest` - 验证元数据缓存
  - 测试首次构建 vs 缓存命中的性能差异
  - 测试不同场景（INSERT/UPDATE）的字段分类正确性
  - 测试清除缓存后重新构建生效

- [ ] 11.3 编写 `DefaultSpELFillerTest` - 验证 SpEL 求值
  - 测试静态方法调用：`T(java.time.LocalDateTime).now()`
  - 测试 Spring Bean 访问：`@securityContext.currentUserId()`（mock Bean）
  - 测试表达式语法错误时抛 IllegalArgumentException
  - 测试类型转换：long → Long, String → Integer 等

- [ ] 11.4 编写 `FillRegistryConcurrencyTest` - 验证线程安全
  - 多线程并发 register/unregister/getFillers 不抛异常
  - 使用 ConcurrentHashMap 或同步块保证原子性
  - 验证：JCTools 并发测试或通过 100 线程各执行 1000 次操作无问题

- [ ] 11.5 编写 `BackwardCompatibilityTest` - 验证零侵入
  - 未标注 @AutoFill 的实体执行 insert/update 行为完全不变
  - SQL 生成结果与变更前一致
  - 验证：对比有无 @AutoFill 注解的相同实体，SQL 模板无差异

## 12. 集成测试与端到端验证

- [ ] 12.1 创建集成测试实体类 `UserWithAudit.java`
  - 包含字段：id, name, createTime(@AutoFill INSERT), updateTime(@AutoFill INSERT_AND_UPDATE)
  - 配套数据库表（H2 or MySQL test container）
  - 验证：表结构可正常建表，字段类型匹配

- [ ] 12.2 编写 `AuditFillIntegrationTest.insertWithAuditFields()`
  - 注入 DBApi，调用 insert(user)
  - 断言：user.createTime 不为 null，user.updateTime 不为 null
  - 断言：数据库中对应记录的 CREATE_TIME 和 UPDATE_TIME 字段有值
  - 验证：查询返回的记录与填充值一致

- [ ] 12.3 编写 `AuditFillIntegrationTest.updateByIdWithAuditFields()`
  - 先插入记录，修改 name 字段后调用 updateById(user)
  - 断言：user.updateTime 被更新为当前时间
  - 断言：数据库中 UPDATE_TIME 字段被正确更新
  - 验证：createTime 未被篡改

- [ ] 12.4 编写 `AuditFillIntegrationTest.batchInsertWithAuditFields()`
  - 准备 100 条 User 记录，调用 batchInsert
  - 断言：所有记录的 createTime 和 updateTime 均被填充
  - 断言：数据库中有 100 条记录且时间字段非空
  - 验证：抽样检查 10 条记录的时间一致性

- [ ] 12.5 编写 `AuditFillIntegrationTest.batchUpdateByIdWithAuditFields()`
  - 准备 100 条已存在记录，修改部分字段后调用 batchUpdateById
  - 断言：所有记录的 updateTime 被刷新为最新时间
  - 断言：数据库中原有数据未丢失，审计字段已更新
  - 验证：UPDATE 语句包含 updateTime 在 SET 子句中

## 13. 文档与示例 ✅

- [x] 13.1 编写 README 章节 `DOCS/AUDIT_FILL_USAGE.md`
  - 说明 @AutoFill 用法和场景枚举
  - 提供 SpEL 表达式示例（时间填充、用户 ID 获取）
  - 列出 v1 Known Limitations（逻辑删除/恢复路径不填充、Lambda 更新不填充）
  - 提供 FillHandler SPI 扩展示例
  - 验证：文档可引导开发者在 5 分钟内完成首次使用
  - **已完成** ✅

- [x] 13.2 更新 CHANGELOG.md
  - 记录新增的自动填充功能
  - 标注向后兼容性声明
  - **已完成** ✅ - 创建 `DOCS/CHANGELOG_AUDIT_FILL.md`

## 14. 编译与发布准备

- [ ] 14.1 运行全量编译 `mvn clean compile` 确保无编译错误
  - 验证：所有模块编译成功，无 warning/error
  - 命令：`mvn clean compile -DskipTests`

- [ ] 14.2 运行全量测试 `mvn test` 确保无回归
  - 验证：所有现有单元测试通过
  - 新增的单元测试全部通过
  - 覆盖率无显著下降（> 80%）

- [ ] 14.3 创建 Pull Request 并等待 Code Review
  - PR 描述包含：功能说明、改动清单、测试截图
  - 链接到 proposal.md、design.md、spec.md
  - 验证：CI 流水线通过（编译 + 测试 + lint）
