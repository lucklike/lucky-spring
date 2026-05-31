package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * Oracle 12c+ 分页策略
 * 使用 OFFSET FETCH 语法
 */
public class Oracle12cPageStrategy extends AbstractPageStrategy {

    @Override
    public String countSql(String sql) {
        return String.format("SELECT COUNT(1) FROM (%s) temp", sql);
    }

    @Override
    public PageSql pageSql(String sql, Page page) {
        // Oracle 12c+: SELECT ... OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        String pageSql = String.format(
                "SELECT temp.* FRON(%s) temp %s OFFSET %s ROWS FETCH NEXT %s ROWS ONLY",
                sql,
                buildOrderByClause(page),
                getOffsetParamName(page),
                getLimitParamName(page)
        );
        Object[] params = new Object[]{page.getOffset(), page.getPageSize()};
        return new PageSql(pageSql, params);
    }
}