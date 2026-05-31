package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * SQL Server 分页策略（2012+）
 * 使用 OFFSET FETCH 语法
 */
public class SqlServerPageStrategy extends AbstractPageStrategy {

    @Override
    public String countSql(String sql) {
        return String.format("SELECT COUNT(1) FROM (%s) temp", sql);
    }

    @Override
    public PageSql pageSql(String sql, Page page) {
        String orderSql = buildOrderByClause(page);
        // 检查是否有 ORDER BY 子句（SQL Server 2012+ 必须要有 ORDER BY）
        if (!hasOrderByInSql(orderSql)) {
            throw new IllegalStateException("SQL Server requires ORDER BY clause for pagination with OFFSET FETCH");
        }

        // SQL Server 2012+: SELECT ... OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        String pageSql = String.format(
                "SELECT temp.* FROM (%s) temp %s OFFSET %s ROWS FETCH NEXT %s ROWS ONLY",
                sql,
                orderSql,
                getLimitParamName(page),
                getOffsetParamName(page)
        );
        Object[] params = new Object[]{page.getOffset(), page.getPageSize()};
        return new PageSql(pageSql, params);
    }
}