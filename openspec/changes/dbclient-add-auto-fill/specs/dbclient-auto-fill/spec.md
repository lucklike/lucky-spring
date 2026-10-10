## Purpose

为 dbclient 提供实体字段自动填充能力，在 INSERT/UPDATE 操作时自动按场景（插入前、更新前）填充标注了 @AutoFill 注解的字段值，确保审计字段和业务字段的值一致性。

## ADDED Requirements

### Requirement: 支持注解标记自动填充字段
系统 SHALL 提供 @AutoFill 注解，允许开发者在实体字段上声明自动填充规则。注解必须包含两个属性：填充场景枚举（INSERT、UPDATE、INSERT_AND_UPDATE）和 SpEL 表达式字符串。同一字段只能应用一个填充规则。

#### Scenario: 标注插入时间字段
- **WHEN** 开发者在实体的 createTime 字段上标注 @AutoFill(expression = "T(java.time.LocalDateTime).now()", scene = INSERT)
- **THEN** 系统识别该字段为插入时自动填充字段，expression 存储为 "T(java.time.LocalDateTime).now()"，scene 存储为 INSERT

#### Scenario: 标注更新时间字段
- **WHEN** 开发者在实体的 updateTime 字段上标注 @AutoFill(expression = "T(java.time.LocalDateTime).now()", scene = UPDATE)
- **THEN** 系统识别该字段为更新时自动填充字段，expression 存储为 "T(java.time.LocalDateTime).now()"，scene 存储为 UPDATE

#### Scenario: 标注同时插入和更新的字段
- **WHEN** 开发者在实体的 modifyTime 字段上标注 @AutoFill(expression = "T(java.time.LocalDateTime).now()", scene = INSERT_AND_UPDATE)
- **THEN** 系统识别该字段为插入和更新时都需填充的字段

### Requirement: INSERT 操作时自动填充
系统在每次执行 INSERT 操作（包括单条 insert 和批量 batchInsert）前，SHALL 检查实体中标注了 @AutoFill 且 scene 包含 INSERT 的字段，评估其 SpEL 表达式并赋值到实体字段。如果表达式结果为 null，则跳过该字段（保留实体原始值）。

#### Scenario: 单条插入时填充创建时间
- **WHEN** 调用 mapper.insert(entity)，entity.createTime 字段标注 @AutoFill(scene = INSERT)，表达式为 "T(java.time.LocalDateTime).now()"
- **THEN** 系统在 SQL 生成前将 createTime 设置为当前 LocalDateTime，然后使用非空字段构建 INSERT 语句

#### Scenario: 批量插入时填充创建时间
- **WHEN** 调用 mapper.batchInsert(entityList)，列表中每个实体的 createTime 字段都标注 @AutoFill(scene = INSERT)
- **THEN** 系统逐条处理实体列表，为每条记录的 createTime 填充当前时间，然后构建批量 INSERT

#### Scenario: 表达式返回 null 时跳过
- **WHEN** 某 @AutoFill(scene = INSERT) 表达式的计算结果为 null
- **THEN** 系统跳过该字段不填充，保留实体原始值，不影响其他字段填充

### Requirement: UPDATE 操作时自动填充
系统在每次执行 UPDATE 操作（包括 updateById、batchUpdateById、Lambda update、logicDelete、restore）前，SHALL 检查实体或条件中标注了 @AutoFill 且 scene 包含 UPDATE 的字段，评估其 SpEL 表达式并赋值。对于 Lambda 路径的更新操作，系统 SHALL 通过查询参数或内部状态获取目标记录的主键信息后构造虚拟实体进行填充评估。如果表达式结果为 null，则跳过该字段。

#### Scenario: 根据 ID 更新时填充更新时间
- **WHEN** 调用 mapper.updateById(entity)，entity.updateTime 字段标注 @AutoFill(scene = UPDATE)，表达式为 "T(java.time.LocalDateTime).now()"
- **THEN** 系统在 SET 子句生成前将 updateTime 设置为目标 LocalDateTime，即使实体中 updateTime 原值为 null 也会强制加入 SET

#### Scenario: 批量更新时填充更新时间
- **WHEN** 调用 mapper.batchUpdateById(entityList)，每个实体的 updateTime 字段标注 @AutoFill(scene = UPDATE)
- **THEN** 系统逐条处理实体列表，为每条记录的 updateTime 填充当前时间后构建批量 UPDATE

#### Scenario: Lambda 更新时填充更新时间
- **WHEN** 调用 mapper.update(Lambda.update(User.class).set(User::getStatus, 1).eq(User::getId, 1L))，User 类有 updateTime 标注 @AutoFill(scene = UPDATE)
- **THEN** 系统从 Lambda 上下文提取更新目标信息，在 SQL 渲染前将 updateTime 加入 SET 子句

#### Scenario: 逻辑删除时填充更新时间
- **WHEN** 调用 mapper.logicDeleteById(id)，被删除实体标注了 updateTime 字段 @AutoFill(scene = UPDATE)
- **THEN** 系统在逻辑删除 UPDATE 前填充 updateTime 为当前时间

#### Scenario: 恢复记录时填充更新时间
- **WHEN** 调用 mapper.restoreById(id)，恢复操作涉及 UPDATE，对应实体标注了 updateTime @AutoFill(scene = UPDATE)
- **THEN** 系统在恢复 UPDATE 前填充 updateTime 为当前时间

### Requirement: SpEL 表达式求值
系统 MUST 使用 Spring SpEL（Spring Expression Language）引擎评估 @AutoFill 注解中的 expression 属性。求值上下文 SHALL 包含以下变量：
- `entity`：当前操作的实体对象
- `entityClass`：实体类型 Class
- `scene`：当前填充场景（INSERT 或 UPDATE）
- `methodName`：当前执行的 dbclient 方法名（如 insert、updateById）

#### Scenario: 调用静态工厂方法获取值
- **WHEN** 表达式为 "T(com.example.factory.IdGenerator).generate()"
- **THEN** 系统评估表达式并调用 IdGenerator.generate() 静态方法返回值作为字段值

#### Scenario: 访问 Spring Bean
- **WHEN** 表达式为 "@securityContext.currentUserId()"
- **THEN** 系统从 Spring 容器获取 securityContext Bean 并调用 currentUserId() 方法返回值

#### Scenario: 表达式解析失败
- **WHEN** SpEL 表达式语法错误或引用不存在的类/方法
- **THEN** 系统抛出 IllegalArgumentException 并附带表达式解析失败的详细信息，阻止操作执行

#### Scenario: 字段类型转换
- **WHEN** 表达式返回 long 类型值但字段类型为 Long
- **THEN** 系统自动完成基本类型到包装类型的转换

### Requirement: 元数据缓存与性能
系统 SHALL 在 EntityMetadataFactory 中缓存每个实体类的自动填充元数据（包括哪些字段需要填充、场景、表达式）。元数据仅在新实体类首次加载或缓存清除时重新解析。批量操作时，复用已解析的元数据避免重复反射。

#### Scenario: 首次访问新实体类
- **WHEN** 第一次对 User 实体执行 insert 操作
- **THEN** 系统反射解析 User 类的所有 @AutoFill 注解，构建元数据缓存

#### Scenario: 批量操作复用元数据
- **WHEN** 对 100 条 User 记录执行 batchInsert
- **THEN** 系统使用首次解析的元数据缓存，逐条填充时无额外反射开销

#### Scenario: 清除元数据缓存
- **WHEN** 调用 EntityMetadataFactory.clearCache(User.class)
- **THEN** 系统移除 User 类的缓存，下次访问时重新解析

### Requirement: 扩展点 SPI
系统 MUST 提供 FillHandler SPI 接口，允许外部实现自定义填充逻辑。FillHandler 接口包含方法 getFillers(Class<?> entityClass, AuditFillScene scene)，返回 Filler 列表。每个 Filler 包含方法 fill(Object entity, AuditFillScene scene)。系统默认实现负责 SpEL 表达式求值，SPI 实现可叠加或替换默认行为。

#### Scenario: 注册自定义 Filler
- **WHEN** 业务模块实现 Filler 接口并在容器中注册
- **THEN** 系统在执行填充时将 SpEL 默认填充器和自定义填充器依次执行

#### Scenario: FillHandler 为空时静默跳过
- **WHEN** 未注册任何 FillHandler 或某个场景下无可用 Filler
- **THEN** 系统跳过自动填充阶段，行为与未标注 @AutoFill 的实体一致，不抛出异常

### Requirement: 向后兼容性
系统 SHALL 保证 @AutoFill 注解对未标注该注解的实体完全透明。未标注 @AutoFill 的实体执行 INSERT/UPDATE 时行为与变更前完全一致，无任何性能损耗或语义变化。系统的存在不得修改原有 SQL 生成逻辑、事务管理、异常处理机制。

#### Scenario: 无注解实体行为不变
- **WHEN** 实体类未标注任何 @AutoFill 注解
- **THEN** 系统对该实体的 insert/update 操作与原逻辑完全一致，SQL 生成不受影响

#### Scenario: 混合标注实体
- **WHEN** 实体部分字段标注 @AutoFill，其他字段正常
- **THEN** 系统仅对标注字段执行自动填充，未标注字段遵循原有非空判断逻辑

## REMOVED Requirements

<!-- None -->
