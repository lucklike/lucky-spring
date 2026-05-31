package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * 分页策略
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 06:10
 */
public interface PageStrategy {

    /**
     * COUNT SQL
     */
    String countSql(String sql);

    /**
     * 分页 SQL
     */
    PageSql pageSql(String sql, Page page);
}
