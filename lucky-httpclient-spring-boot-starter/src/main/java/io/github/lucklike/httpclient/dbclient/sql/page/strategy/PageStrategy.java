package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * 分页策略接口
 * <p>
 * 用于定义不同数据库的分页SQL生成策略，支持：
 * <ul>
 *   <li>COUNT查询SQL生成</li>
 *   <li>分页查询SQL生成</li>
 *   <li>多数据库方言适配（MySQL、Oracle、SQL Server、PostgreSQL等）</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 使用预定义策略
 * PageStrategy strategy = PageStrategy.MYSQL;
 *
 * // 通过工厂自动识别
 * PageStrategy strategy = PageStrategyFactory.getStrategyByDataSource(dataSource);
 *
 * // 手动设置分页策略
 * Page page = new Page(1, 10)
 *     .setPageStrategy(PageStrategy.ORACLE_12C)
 *     .asc("create_time");
 * }</pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 06:10
 */
public interface PageStrategy {

    /**
     * 构建 COUNT 查询 SQL
     * <p>将原始查询SQL包装为统计总数的COUNT查询
     * <p>示例：SELECT COUNT(1) FROM (原始SQL) temp
     *
     * @param sql 原始查询SQL
     * @return COUNT查询SQL
     */
    String countSql(String sql);

    /**
     * 构建分页查询 SQL
     * <p>将原始查询SQL转换为当前数据库方言的分页SQL
     *
     * @param sql  原始查询SQL
     * @param page 分页参数对象
     * @return 分页SQL对象（包含SQL语句和参数）
     */
    PageSql pageSql(String sql, Page page);
}