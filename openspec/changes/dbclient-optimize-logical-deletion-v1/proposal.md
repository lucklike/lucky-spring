## Why

逻辑删除功能还需要补齐与客户端构建器体系的集成：客户端条件构建器构建好条件后，目前无法直接转换为逻辑删除/恢复操作

## What Changes

1.LambdaClientConditionBuilder系列支持逻辑删除的转换：客户端条件构建器可转换为逻辑删除/恢复操作并直接执行，转换后保留已构建的全部条件

## Capabilities

### New Capabilities
- 无

### Modified Capabilities
- `dbclient-logical-deletion`: 客户端条件构建器新增逻辑删除/恢复的操作转换入口（转换后可直接执行，保留已构建条件），既有行为不变

## Impact

- 影响范围：`io.github.lucklike.httpclient.dbclient` 包内 Lambda 客户端构建器相关代码（条件构建器及其转换目标构建器），不涉及与数据库操作无关的其他模块
- 新增 API：客户端条件构建器 → 逻辑删除/恢复操作的转换入口（转换后可直接执行）
- 兼容性：不改变任何既有 API 的签名与行为；`@LogicDelete` 默认取值与常规查询过滤语义（`<> 已删除值`）保持现状
- 依赖：无需新增第三方依赖
