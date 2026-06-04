package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.dbclient.sql.page.ContextPage;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;

/**
 * DB2 分页策略
 * 使用 ROW_NUMBER() 窗口函数
 */
public class Db2PageStrategy extends AbstractPageStrategy {

    @Override
    public String countSql(String sql) {
        return String.format("SELECT COUNT(1) FROM (%s) temp", sql);
    }

    @Override
    public PageSql pageSql(String sql, Page page) {
        // DB2: 使用 ROW_NUMBER() 实现分页
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
        // 如果没有指定排序字段，则可以减少一层子查询
        if (ContainerUtils.isEmptyCollection(page.getOrderColumns())) {
            return "SELECT t.* FROM (SELECT ROW_NUMBER() OVER () AS rn, temp.* FROM (%s  %s) temp) t WHERE t.rn BETWEEN %s AND %s";
        }
        return "SELECT t.* FROM (SELECT ROW_NUMBER() OVER () AS rn, temp.* FROM (SELECT * FROM (%s)  %s) temp) t WHERE t.rn BETWEEN %s AND %s";
    }
}