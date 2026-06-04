package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import com.luckyframework.common.ContainerUtils;
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
                getPageSQLTemp(page),
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

    private String getPageSQLTemp(Page page) {
        // 没有排序：直接在原始SQL外层套分页
        if (ContainerUtils.isEmptyCollection(page.getOrderColumns())) {
            return "SELECT * FROM (SELECT temp.*, ROWNUM rn FROM (%s %s) temp) WHERE rn BETWEEN %s AND %s";
        }
        // 有排序：需要两层嵌套，先排序再赋ROWNUM
        return  "SELECT * FROM (SELECT temp.*, ROWNUM rn FROM (SELECT * FROM (%s) %s) temp) WHERE rn BETWEEN %s AND %s";
    }
}