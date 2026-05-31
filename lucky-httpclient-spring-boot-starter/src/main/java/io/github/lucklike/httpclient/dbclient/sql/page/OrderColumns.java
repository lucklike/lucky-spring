package io.github.lucklike.httpclient.dbclient.sql.page;

import java.util.Arrays;
import java.util.List;

/**
 * 排序字段
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 09:35
 */
public class OrderColumns {
    private final OrderDirection order;
    private final List<String> columns;

    private OrderColumns(OrderDirection order, List<String> columns) {
        this.order = order;
        this.columns = columns;
    }

    public static OrderColumns asc(String ...column) {
        return new OrderColumns(OrderDirection.ASC, Arrays.asList(column));
    }

    public static OrderColumns desc(String ...column) {
        return new OrderColumns(OrderDirection.DESC, Arrays.asList(column));
    }

    public OrderDirection getOrder() {
        return order;
    }

    public List<String> getColumns() {
        return columns;
    }
}
