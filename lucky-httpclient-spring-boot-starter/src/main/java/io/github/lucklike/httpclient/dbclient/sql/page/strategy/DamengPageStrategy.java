package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * 达梦数据库分页策略
 * 使用 LIMIT 和 OFFSET（达梦支持 MySQL 类似语法）
 */
public class DamengPageStrategy extends AbstractPageStrategy {

    @Override
    public String countSql(String sql) {
        return String.format("SELECT COUNT(1) FROM (%s) temp", sql);
    }

    @Override
    public PageSql pageSql(String sql, Page page) {
        // 达梦数据库: SELECT ... LIMIT ? OFFSET ?
        String pageSql = String.format(
                "SELECT temp.* FROM (%s) temp %s LIMIT %s OFFSET %s",
                sql,
                buildOrderByClause(page),
                getLimitParamName(page),
                getOffsetParamName(page)
        );
        Object[] params = new Object[]{page.getPageSize(), page.getOffset()};
        return new PageSql(pageSql, params);
    }
}