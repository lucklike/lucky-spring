package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import io.github.lucklike.httpclient.dbclient.sql.page.ContextPage;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * Oracle 分页策略（兼容版本，使用 ROWNUM）
 */
public class OraclePageStrategy extends AbstractPageStrategy {

    @Override
    public String countSql(String sql) {
        return String.format("SELECT COUNT(1) FROM (%s) temp", sql);
    }

    @Override
    public PageSql pageSql(String sql, Page page) {
        // Oracle ROWNUM 分页：使用子查询方式
        String pageSql = String.format(
                "SELECT * FROM (SELECT temp.*, ROWNUM rn FROM (SELECT * FROM (%s) %s) temp) WHERE rn BETWEEN %s AND %s",
                sql,
                buildOrderByClause(page),
                getOffsetParamName(page),
                getLimitParamName(page)
        );
        Object[] params = new Object[]{page.getStartRow(), page.getEndRow()};
        return new PageSql(pageSql, params);
    }

    protected String getOffsetParamName(Page page) {
        if (page instanceof ContextPage) {
            ContextPage contextPage = (ContextPage) page;
            int pageParamIndex = getPageParamIndex(contextPage);
            return String.format(":$%s.getStartRow", pageParamIndex);
        }
        return "?";
    }

    protected String getLimitParamName(Page page) {
        if (page instanceof ContextPage) {
            ContextPage contextPage = (ContextPage) page;
            int pageParamIndex = getPageParamIndex(contextPage);
            return String.format(":$%s.getEndRow", pageParamIndex);
        }
        return "?";
    }
}