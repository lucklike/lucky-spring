package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.sql.page.ContextPage;
import io.github.lucklike.httpclient.dbclient.sql.page.OrderColumns;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;

import java.util.List;

/**
 * 分页策略的抽象基类，提取公共的排序逻辑
 */
public abstract class AbstractPageStrategy implements PageStrategy {

    /**
     * 构建 ORDER BY 子句
     */
    protected String buildOrderByClause(Page page) {
        List<OrderColumns> orderColumns = page.getOrderColumns();
        if (orderColumns == null || orderColumns.isEmpty()) {
            return "";
        }

        StringBuilder orderByBuilder = new StringBuilder();
        for (OrderColumns oc : orderColumns) {
            if (oc.getColumns() == null || oc.getColumns().isEmpty()) {
                continue;
            }

            if (orderByBuilder.length() > 0) {
                orderByBuilder.append(", ");
            }

            String columnsStr = String.join(", ", oc.getColumns());
            orderByBuilder.append(columnsStr).append(" ").append(oc.getOrder().name());
        }

        return orderByBuilder.length() > 0 ? " ORDER BY " + orderByBuilder.toString() : "";
    }

    /**
     * 检查 SQL 是否已包含 ORDER BY
     */
    protected boolean hasOrderByInSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }
        String upperSql = sql.toUpperCase();
        return upperSql.contains("ORDER BY");
    }

    protected String getOffsetParamName(Page page) {
        if (page instanceof ContextPage) {
            ContextPage contextPage = (ContextPage) page;
            int pageParamIndex = getPageParamIndex(contextPage);
            return String.format(":$%s.getOffset", pageParamIndex);
        }
        return "?";
    }

    protected String getLimitParamName(Page page) {
        if (page instanceof ContextPage) {
            ContextPage contextPage = (ContextPage) page;
            int pageParamIndex = getPageParamIndex(contextPage);
            return String.format(":$%s.getPageSize", pageParamIndex);
        }
        return "?";
    }

    protected int getPageParamIndex(ContextPage page) {
        MethodContext mc = page.getMc();
        Object[] arguments = mc.getArguments();
        int index = -1;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof Page) {
                index = i;
                break;
            }
        }
        return index;
    }
}