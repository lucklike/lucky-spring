## Why

目前框架的数据库操作部分不支持逻辑功能，不满足某些使用场景

## What Changes

1.在现有的API中新增逻辑删除的方法（按 ID、按条件及 Lambda 构建器入口），同时新增配套的恢复（还原）方法与显式查询"包含已删除记录"的入口
2.新增字段注解用于标注逻辑删除字段，删除值与未删除值可配置（默认 1/0），适用于数值与布尔类型字段
3.标注逻辑删除字段的实体，常规查询（单条/列表/分页/统计/流式/单列）自动排除已删除记录；既有 delete/deleteById 等物理删除 API 的行为保持不变
4.不要改动与数据库操作功能无关的代码

## Capabilities

### New Capabilities
- `dbclient-logical-deletion`: 数据库客户端（dbclient）的逻辑删除能力，包括实体逻辑删除字段标注、新增逻辑删除与恢复方法、标注实体的常规查询自动排除已删除记录，以及显式查询包含已删除记录的入口

### Modified Capabilities
- 无（既有物理删除与全部查询方法对未标注实体的行为保持不变）

## Impact

- 影响范围：`io.github.lucklike.httpclient.dbclient` 包内的数据库操作相关代码（写操作 API、查询 API、实体元数据、SQL 构建与函数层），不涉及与数据库操作无关的其他模块
- 新增 API：逻辑删除方法（按 ID、按条件、Lambda 构建器入口）、恢复方法、包含已删除记录的查询入口、字段注解
- 兼容性：既有 API（delete、deleteById、lambdaDelete、各类查询方法）的签名与行为保持不变；未标注逻辑删除字段的实体行为完全不变
- 依赖：无需新增第三方依赖，基于现有 Spring JDBC 与 Lucky 代理体系扩展
