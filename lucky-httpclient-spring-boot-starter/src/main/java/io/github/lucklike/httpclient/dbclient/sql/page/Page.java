package io.github.lucklike.httpclient.dbclient.sql.page;

import io.github.lucklike.httpclient.dbclient.sql.page.strategy.PageSql;
import io.github.lucklike.httpclient.dbclient.sql.page.strategy.PageStrategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 分页参数类
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 06:10
 */
public class Page implements Serializable {

    private static final long serialVersionUID = 895632478521469357L;

    /**
     * 当前页码，从1开始
     */
    private long pageNum = 1;

    /**
     * 每页大小
     */
    private long pageSize = 10;

    /**
     * 总记录数（-1表示未查询，>=0表示已查询）
     */
    private long totalCount = -1;

    /**
     * 总页数（缓存值，-1表示未计算）
     */
    private long totalPages = -1;

    /**
     * 排序字段列表
     */
    private List<OrderColumns> orderColumns = Collections.emptyList();

    /**
     * 是否查询总记录数，默认true
     */
    private transient boolean countTotal = true;

    /**
     * 分页策略
     */
    private transient PageStrategy pageStrategy;

    public Page() {
    }

    public Page(long pageNum, long pageSize) {
        this.pageNum = Math.max(1, pageNum);
        this.pageSize = Math.max(1, Math.min(pageSize, 1000));
    }

    // ========== Getters and Setters ==========

    public long getPageNum() {
        return pageNum;
    }

    public Page setPageNum(long pageNum) {
        this.pageNum = Math.max(1, pageNum);
        return this;
    }

    public long getPageSize() {
        return pageSize;
    }

    public Page setPageSize(long pageSize) {
        this.pageSize = Math.max(1, Math.min(pageSize, 1000));
        // 每页大小改变时，需要重新计算总页数
        this.totalPages = -1;
        return this;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public Page setTotalCount(long totalCount) {
        this.totalCount = Math.max(-1, totalCount);
        // 总记录数改变时，需要重新计算总页数
        this.totalPages = -1;
        return this;
    }

    /**
     * 判断是否已查询总记录数
     */
    public boolean isTotalCountQueried() {
        return totalCount >= 0;
    }


    /**
     * 获取总页数（使用缓存）
     */
    public long getTotalPages() {
        if (totalPages == -1) {
            if (totalCount >= 0) {
                totalPages = (totalCount + pageSize - 1) / pageSize;
            }
        }
        return totalPages;
    }

    /**
     * 手动设置总页数（通常不需要，由系统自动计算）
     */
    public Page setTotalPages(long totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public List<OrderColumns> getOrderColumns() {
        return orderColumns;
    }

    public Page setOrderColumns(List<OrderColumns> orderColumns) {
        this.orderColumns = orderColumns == null ? Collections.emptyList() : orderColumns;
        return this;
    }

    public boolean isCountTotal() {
        return countTotal;
    }

    public Page setCountTotal(boolean countTotal) {
        this.countTotal = countTotal;
        return this;
    }

    public PageStrategy getPageStrategy() {
        return pageStrategy;
    }

    public Page setPageStrategy(PageStrategy pageStrategy) {
        this.pageStrategy = pageStrategy;
        return this;
    }

    public Page setPageStrategyIfNotExist(Supplier<PageStrategy> pageStrategySupplier) {
        if (pageStrategy == null) {
            this.pageStrategy = pageStrategySupplier.get();
        }
        return this;
    }

    // ========== 计算属性 ==========

    /**
     * 获取起始行索引（从0开始）
     */
    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取分页起始行号（从1开始，用于 ROW_NUMBER() BETWEEN 查询）
     *
     * @return 起始行号（包含）
     */
    public long getStartRow() {
        return getOffset() + 1;
    }

    /**
     * 获取分页结束行号（用于 ROW_NUMBER() BETWEEN 查询）
     *
     * @return 结束行号（包含）
     */
    public long getEndRow() {
        return getOffset() + pageSize;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNum > 1;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        long totalPages = getTotalPages();
        return totalPages > 0 && pageNum < totalPages;
    }

    /**
     * 是否是第一页
     */
    public boolean isFirst() {
        return pageNum == 1;
    }

    /**
     * 是否是最后一页
     */
    public boolean isLast() {
        long totalPages = getTotalPages();
        return totalPages == 0 ? pageNum == 1 : pageNum == totalPages;
    }

    // ========== 排序相关方法 ==========

    /**
     * 添加升序排序字段
     */
    public Page asc(String... columns) {
        if (columns != null && columns.length > 0) {
            if (this.orderColumns.isEmpty()) {
                this.orderColumns = new ArrayList<>();
            }
            this.orderColumns.add(OrderColumns.asc(columns));
        }
        return this;
    }

    /**
     * 添加降序排序字段
     */
    public Page desc(String... columns) {
        if (columns != null && columns.length > 0) {
            if (this.orderColumns.isEmpty()) {
                this.orderColumns = new ArrayList<>();
            }
            this.orderColumns.add(OrderColumns.desc(columns));
        }
        return this;
    }

    /**
     * 添加排序规则
     */
    public Page order(OrderColumns orderColumns) {
        if (orderColumns != null) {
            if (this.orderColumns.isEmpty()) {
                this.orderColumns = new ArrayList<>();
            }
            this.orderColumns.add(orderColumns);
        }
        return this;
    }

    /**
     * 清空排序字段
     */
    public Page clearOrder() {
        this.orderColumns = Collections.emptyList();
        return this;
    }

    // ========== 链式调用方法 ==========

    /**
     * 快速创建分页对象
     */
    public static Page of(long pageNum, long pageSize) {
        return new Page(pageNum, pageSize);
    }

    /**
     * 创建空分页对象
     */
    public static Page empty() {
        return new Page();
    }

    // ========== 构建 SQL 相关方法 ==========

    /**
     * 构建 COUNT SQL
     */
    public String buildCountSql(String originalSql) {
        if (pageStrategy == null) {
            throw new IllegalStateException("PageStrategy is not set");
        }
        return pageStrategy.countSql(originalSql);
    }

    /**
     * 构建分页 SQL（返回 PageSql 对象，包含 SQL 和参数）
     */
    public PageSql buildPageSql(String originalSql) {
        if (pageStrategy == null) {
            throw new IllegalStateException("PageStrategy is not set");
        }
        return pageStrategy.pageSql(originalSql, this);
    }

    /**
     * 重置分页状态（清空总记录数和总页数缓存）
     */
    public Page reset() {
        this.totalCount = -1;
        this.totalPages = -1;
        return this;
    }

    /**
     * 清空数据但不重置分页状态（保留分页参数和排序信息）
     */
    public Page clear() {
        this.totalCount = -1;
        this.totalPages = -1;
        return this;
    }

    @Override
    public String toString() {
        return "Page{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", totalCount=" + totalCount +
                ", totalPages=" + getTotalPages() +
                ", orderColumns=" + orderColumns +
                '}';
    }
}