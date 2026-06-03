package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.sql.page.ContextPage;
import io.github.lucklike.httpclient.dbclient.sql.page.OrderColumns;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;

import java.util.List;

/**
 * 分页策略抽象基类
 * <p>
 * 提供分页策略的公共实现，包括：
 * <ul>
 *   <li>ORDER BY 子句构建</li>
 *   <li>SQL中ORDER BY存在性检查</li>
 *   <li>分页参数的动态绑定支持</li>
 * </ul>
 *
 * <p>各数据库特定分页策略应继承此类，复用公共逻辑
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31
 */
public abstract class AbstractPageStrategy implements PageStrategy {

    /**
     * 构建 ORDER BY 子句
     * <p>
     * 根据Page对象中的排序字段列表生成标准的ORDER BY SQL片段
     *
     * <p><b>示例：</b>
     * <pre>{@code
     * Page page = new Page()
     *     .asc("create_time", "id")
     *     .desc("update_time");
     * // 生成: " ORDER BY create_time, id ASC, update_time DESC"
     * }</pre>
     *
     * @param page 分页参数对象，包含排序字段列表
     * @return ORDER BY子句字符串，如果没有排序字段则返回空字符串
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
     * 检查 SQL 是否已包含 ORDER BY 子句
     * <p>
     * 用于某些需要ORDER BY的数据库方言（如SQL Server 2012+）
     *
     * @param sql 待检查的SQL语句
     * @return true: 包含ORDER BY子句；false: 不包含
     */
    protected boolean hasOrderByInSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }
        String upperSql = sql.toUpperCase();
        return upperSql.contains("ORDER BY");
    }

    /**
     * 获取分页偏移量的参数名称（用于命名参数绑定）
     * <p>
     * 如果Page是ContextPage类型（框架上下文中的分页对象），返回命名参数格式如 ":$0.getOffset"
     * 否则返回占位符 "?"
     *
     * @param page 分页参数对象
     * @return 参数名称或占位符
     */
    protected String getOffsetParamName(Page page) {
        if (page instanceof ContextPage) {
            ContextPage contextPage = (ContextPage) page;
            int pageParamIndex = getPageParamIndex(contextPage);
            return String.format(":$%s.getOffset", pageParamIndex);
        }
        return "?";
    }

    /**
     * 获取分页大小的参数名称（用于命名参数绑定）
     * <p>
     * 如果Page是ContextPage类型，返回命名参数格式如 ":$0.getPageSize"
     * 否则返回占位符 "?"
     *
     * @param page 分页参数对象
     * @return 参数名称或占位符
     */
    protected String getLimitParamName(Page page) {
        if (page instanceof ContextPage) {
            ContextPage contextPage = (ContextPage) page;
            int pageParamIndex = getPageParamIndex(contextPage);
            return String.format(":$%s.getPageSize", pageParamIndex);
        }
        return "?";
    }

    /**
     * 获取Page对象在方法参数中的索引位置
     * <p>
     * 用于ContextPage动态绑定参数值
     *
     * @param page ContextPage对象，包含方法上下文信息
     * @return Page对象在方法参数数组中的索引，-1表示未找到
     */
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