## Why

某些字段每次新增或者更新时都需要自动设置某些值，例如create_time,uodate_time,create_by,update_by等，这些字段的填充我需要自动化

## What Changes

1.新增一个注解@AutoFill,一个枚举（INSERT、UPDATE、INSERT_AND_UPDATE）
2.@AutoFill注解的一个属性是上面说的枚举，另一个属性是一个支持SpEL表达式的String类型（#{表达式}）
3.在INSERT和UPDATE时检查对应的实体时候存在被该注解标注的属性，有的话需要为他们赋值
4.你可以修改原有代码的结构，为这种字段自动填充的场景设置一个扩展方案

## Capabilities

### New Capabilities
- **dbclient-auto-fill**：实体字段自动填充能力，支持在 INSERT/UPDATE 操作时按场景自动填充标注了 @AutoFill 注解的字段

### Modified Capabilities
<!-- Existing capabilities whose REQUIREMENTS are changing. Use existing spec names from openspec/specs/. -->

## Impact

**影响范围：**
- `lucky-httpclient-spring-boot-starter` 模块的 `io.github.lucklike.httpclient.dbclient` 包
- 新增注解类：`@AutoFill`、`AuditFillScene` 枚举
- 修改元数据层：`ColumnMetadata`、`EntityMetadata`、`EntityMetadataFactory`
- 修改 SQLFunctions 中的写入生成方法（insertSql、updateById、batchInsert、batchUpdateById 等）
- 提供 SPI：`FillHandler`、`Filler` 接口

**API 兼容性：**
- 完全向后兼容，未标注 @AutoFill 的实体无感知
- FillHandler 为可选 SPI，不注册时静默跳过

**性能影响：**
- 首次访问实体类时有反射开销，后续通过 EntityMetadata 缓存
- 批量操作复用元数据，逐条填充时无额外开销
