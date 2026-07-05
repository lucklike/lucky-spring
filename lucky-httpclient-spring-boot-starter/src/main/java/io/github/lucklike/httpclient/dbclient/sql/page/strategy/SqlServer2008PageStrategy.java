package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.dbclient.sql.page.ContextPage;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * SQL Server 2008 及以下版本分页策略
 * 使用 ROW_NUMBER() 窗口函数
 */
public class SqlServer2008PageStrategy extends AbstractPageStrategy {

    @Override
    public String countSql(String sql) {
        return String.format("SELECT COUNT(1) FROM (%s) temp", sql);
    }

    @Override
    public PageSql pageSql(String sql, Page page) {
        // SQL Server 2008: 使用 ROW_NUMBER() 实现分页
        String pageSql = String.format(
                "SELECT t.* FROM (SELECT ROW_NUMBER() OVER (ORDER BY (SELECT 0)) AS rn, temp.* FROM (SELECT * FROM (%s)  %s) temp) t WHERE t.rn BETWEEN %s AND %s",
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
            return "SELECT t.* FROM (SELECT temp.*, ROW_NUMBER() OVER (ORDER BY (SELECT 0)) AS rn FROM (%s %s) temp) t WHERE t.rn BETWEEN %s AND %s";
        }
        // 有排序：需要两层嵌套，先排序再赋ROWNUM
        return  "SELECT t.* FROM (SELECT temp.*, ROW_NUMBER() OVER (ORDER BY (SELECT 0)) AS rn FROM (SELECT * FROM (%s)  %s) temp) t WHERE t.rn BETWEEN %s AND %s";
    }
}