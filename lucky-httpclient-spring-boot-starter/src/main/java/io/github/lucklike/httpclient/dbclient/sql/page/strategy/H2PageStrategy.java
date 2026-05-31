package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * H2 数据库分页策略
 * 使用 LIMIT 和 OFFSET
 */
public class H2PageStrategy extends AbstractPageStrategy {

    @Override
    public String countSql(String sql) {
        return String.format("SELECT COUNT(1) FROM (%s) temp", sql);
    }

    @Override
    public PageSql pageSql(String sql, Page page) {
        // H2: SELECT ... LIMIT ? OFFSET ?
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