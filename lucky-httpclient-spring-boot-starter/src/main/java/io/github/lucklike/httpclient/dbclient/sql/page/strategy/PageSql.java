package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

/**
 * 分页 SQL 和参数
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 09:46
 */
public class PageSql {
    private final String sql;
    private final Object[] pageParam;

    public PageSql(String sql, Object[] pageParam) {
        this.sql = sql;
        this.pageParam = pageParam;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getPageParam() {
        return pageParam;
    }
}
