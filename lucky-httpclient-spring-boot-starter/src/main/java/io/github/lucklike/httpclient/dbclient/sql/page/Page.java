package io.github.lucklike.httpclient.dbclient.sql.page;

import io.github.lucklike.httpclient.dbclient.sql.page.strategy.PageSql;
import io.github.lucklike.httpclient.dbclient.sql.page.strategy.PageStrategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 通用分页参数类
 * <p>
 * 提供完整的分页功能，包括：
 * <ul>
 *   <li>分页参数管理（页码、每页大小）</li>
 *   <li>总记录数和总页数自动计算与缓存</li>
 *   <li>多字段排序支持</li>
 *   <li>多种分页策略（MySQL、Oracle、PostgreSQL等）</li>
 *   <li>链式调用API</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 基础分页查询
 * Page page = Page.of(1, 10)
 *     .asc("create_time")
 *     .desc("id");
 *
 * // 不查询总数的分页（性能优化）
 * Page page2 = Page.notCount(1, 100);
 *
 * // 动态设置分页策略
 * Page page3 = new Page(1, 20)
 *     .setPageStrategy(new MySQLPageStrategy());
 *
 * // 获取分页信息
 * long offset = page.getOffset();      // 起始偏移量
 * long startRow = page.getStartRow();  // 起始行号（ROW_NUMBER用）
 * boolean hasNext = page.hasNext();    // 是否有下一页
 * }</pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 06:10
 */
public class Page implements Serializable {

    private static final long serialVersionUID = 895632478521469357L;

    /**
     * 当前页码，从1开始
     * 最小值为1，小于1时会自动修正
     */
    private long pageNum = 1;

    /**
     * 每页记录数
     * 最小值为1，小于1时会自动修正
     */
    private long pageSize = 10;

    /**
     * 总记录数
     * -1: 未查询
     * >=0: 已查询的实际总数
     */
    private long totalCount = -1;

    /**
     * 总页数（缓存值）
     * -1: 未计算
     * >=0: 已计算的总页数
     */
    private long totalPages = -1;

    /**
     * 排序字段列表
     * 支持多个排序规则，按添加顺序应用
     */
    private List<OrderColumns> orderColumns = Collections.emptyList();

    /**
     * 是否在执行分页查询时自动查询总记录数
     * true: 执行COUNT查询获取总数（默认）
     * false: 跳过COUNT查询，适用于大数据量或不需要总数的场景
     */
    private transient boolean countTotal = true;

    /**
     * 分页策略
     * 用于生成不同数据库的分页SQL（MySQL LIMIT、Oracle ROWNUM、PostgreSQL LIMIT/OFFSET等）
     * 不设置时会使用默认策略
     */
    private transient PageStrategy pageStrategy;

    /**
     * 默认构造函数
     * 创建默认分页对象：第1页，每页10条
     */
    public Page() {
    }

    /**
     * 构造分页对象
     *
     * @param pageNum  页码（从1开始），小于1时会自动设为1
     * @param pageSize 每页大小，小于1时会自动设为1
     */
    public Page(long pageNum, long pageSize) {
        this.pageNum = Math.max(1, pageNum);
        this.pageSize = Math.max(1, pageSize);
    }

    // ========== 静态工厂方法 ==========

    /**
     * 快速创建分页对象
     * <p>示例：{@code Page.of(1, 20)}
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页对象
     */
    public static Page of(long pageNum, long pageSize) {
        return new Page(pageNum, pageSize);
    }

    /**
     * 创建默认分页对象
     * <p>页码：1，每页大小：10
     *
     * @return 默认分页对象
     */
    public static Page def() {
        return new Page();
    }

    /**
     * 创建不进行COUNT查询的分页对象
     * <p>适用于不需要总记录数的大数据量查询，可提升性能
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 不查询总数的分页对象
     */
    public static Page notCount(long pageNum, long pageSize) {
        return of(pageNum, pageSize).setCountTotal(false);
    }

    /**
     * 创建默认参数且不进行COUNT查询的分页对象
     * <p>页码：1，每页大小：10，不查询总记录数
     *
     * @return 默认参数且不查询总数的分页对象
     */
    public static Page defNotCount() {
        return def().setCountTotal(false);
    }

    // ========== Getters and Setters ==========

    /**
     * 获取当前页码
     *
     * @return 当前页码（从1开始）
     */
    public long getPageNum() {
        return pageNum;
    }

    /**
     * 设置当前页码
     * <p>页码小于1时会自动修正为1
     *
     * @param pageNum 页码
     * @return 当前分页对象（支持链式调用）
     */
    public Page setPageNum(long pageNum) {
        this.pageNum = Math.max(1, pageNum);
        return this;
    }

    /**
     * 获取每页记录数
     *
     * @return 每页大小
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页记录数
     * <p>每页大小小于1时会自动修正为1
     * <p>修改后会重置总页数缓存
     *
     * @param pageSize 每页大小
     * @return 当前分页对象（支持链式调用）
     */
    public Page setPageSize(long pageSize) {
        this.pageSize = Math.max(1, pageSize);
        // 每页大小改变时，需要重新计算总页数
        this.totalPages = -1;
        return this;
    }

    /**
     * 获取总记录数
     *
     * @return 总记录数，-1表示未查询
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * 设置总记录数
     * <p>设置后会重置总页数缓存，下次调用{@link #getTotalPages()}时会重新计算
     *
     * @param totalCount 总记录数，小于-1时会自动修正为-1
     * @return 当前分页对象（支持链式调用）
     */
    public Page setTotalCount(long totalCount) {
        this.totalCount = Math.max(-1, totalCount);
        // 总记录数改变时，需要重新计算总页数
        this.totalPages = -1;
        return this;
    }

    /**
     * 判断是否已查询总记录数
     *
     * @return true: 已查询；false: 未查询
     */
    public boolean isTotalCountQueried() {
        return totalCount >= 0;
    }

    /**
     * 获取总页数（使用缓存）
     * <p>如果总记录数已查询，会自动计算总页数并缓存
     *
     * @return 总页数，如果总记录数未查询则返回-1
     */
    public long getTotalPages() {
        if (totalPages == -1 && totalCount >= 0) {
            totalPages = (totalCount + pageSize - 1) / pageSize;
        }
        return totalPages;
    }

    /**
     * 手动设置总页数
     * <p>通常不需要手动设置，系统会自动计算
     *
     * @param totalPages 总页数
     * @return 当前分页对象（支持链式调用）
     */
    public Page setTotalPages(long totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    /**
     * 获取排序字段列表
     *
     * @return 排序字段列表（不可变）
     */
    public List<OrderColumns> getOrderColumns() {
        return orderColumns;
    }

    /**
     * 设置排序字段列表
     * <p>会替换现有的排序规则
     *
     * @param orderColumns 排序字段列表，为null时清空排序
     * @return 当前分页对象（支持链式调用）
     */
    public Page setOrderColumns(List<OrderColumns> orderColumns) {
        this.orderColumns = orderColumns == null ? Collections.emptyList() : orderColumns;
        return this;
    }

    /**
     * 获取是否查询总记录数
     *
     * @return true: 查询总数；false: 不查询总数
     */
    public boolean isCountTotal() {
        return countTotal;
    }

    /**
     * 设置是否查询总记录数
     *
     * @param countTotal true: 查询总数；false: 不查询总数（性能优化）
     * @return 当前分页对象（支持链式调用）
     */
    public Page setCountTotal(boolean countTotal) {
        this.countTotal = countTotal;
        return this;
    }

    /**
     * 获取分页策略
     *
     * @return 分页策略，可能为null
     */
    public PageStrategy getPageStrategy() {
        return pageStrategy;
    }

    /**
     * 设置分页策略
     * <p>用于适配不同数据库的分页语法
     *
     * @param pageStrategy 分页策略（如MySQLPageStrategy、OraclePageStrategy等）
     * @return 当前分页对象（支持链式调用）
     */
    public Page setPageStrategy(PageStrategy pageStrategy) {
        this.pageStrategy = pageStrategy;
        return this;
    }

    /**
     * 如果分页策略不存在则设置
     * <p>延迟初始化模式，避免重复设置
     *
     * @param pageStrategySupplier 分页策略提供者
     * @return 当前分页对象（支持链式调用）
     */
    public Page setPageStrategyIfNotExist(Supplier<PageStrategy> pageStrategySupplier) {
        if (pageStrategy == null) {
            this.pageStrategy = pageStrategySupplier.get();
        }
        return this;
    }

    // ========== 计算属性 ==========

    /**
     * 获取起始行索引（从0开始）
     * <p>用于 MySQL/PgSQL 的 LIMIT offset, size
     * <p>计算公式：(pageNum - 1) * pageSize
     *
     * @return 起始偏移量
     */
    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取分页起始行号（从1开始）
     * <p>用于 SQL Server/Oracle 的 ROW_NUMBER() BETWEEN start AND end
     * <p>计算公式：(pageNum - 1) * pageSize + 1
     *
     * @return 起始行号（包含）
     */
    public long getStartRow() {
        return getOffset() + 1;
    }

    /**
     * 获取分页结束行号
     * <p>用于 SQL Server/Oracle 的 ROW_NUMBER() BETWEEN start AND end
     * <p>计算公式：pageNum * pageSize
     *
     * @return 结束行号（包含）
     */
    public long getEndRow() {
        return getOffset() + pageSize;
    }

    /**
     * 判断是否有上一页
     *
     * @return true: 有上一页；false: 当前是第一页
     */
    public boolean hasPrevious() {
        return pageNum > 1;
    }

    /**
     * 判断是否有下一页
     * <p>需要先设置总记录数或总页数
     *
     * @return true: 有下一页；false: 当前是最后一页或总记录数未查询
     */
    public boolean hasNext() {
        long totalPages = getTotalPages();
        return totalPages > 0 && pageNum < totalPages;
    }

    /**
     * 判断是否是第一页
     *
     * @return true: 是第一页；false: 不是第一页
     */
    public boolean isFirst() {
        return pageNum == 1;
    }

    /**
     * 判断是否是最后一页
     * <p>当总记录数为0时，第一页也被视为最后一页
     *
     * @return true: 是最后一页；false: 不是最后一页或总记录数未查询
     */
    public boolean isLast() {
        long totalPages = getTotalPages();
        return totalPages == 0 ? pageNum == 1 : pageNum == totalPages;
    }

    // ========== 排序相关方法 ==========

    /**
     * 添加升序排序字段
     * <p>多个字段按添加顺序排序
     *
     * @param columns 字段名（可变参数）
     * @return 当前分页对象（支持链式调用）
     * @example {@code page.asc("create_time", "id")}
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
     * <p>多个字段按添加顺序排序
     *
     * @param columns 字段名（可变参数）
     * @return 当前分页对象（支持链式调用）
     * @example {@code page.desc("update_time")}
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
     * <p>支持复杂的排序规则，如自定义排序表达式
     *
     * @param orderColumns 排序规则对象
     * @return 当前分页对象（支持链式调用）
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
     * 清空所有排序字段
     *
     * @return 当前分页对象（支持链式调用）
     */
    public Page clearOrder() {
        this.orderColumns = Collections.emptyList();
        return this;
    }

    // ========== SQL 构建方法 ==========

    /**
     * 构建 COUNT 查询 SQL
     * <p>将原始查询SQL转换为统计总数的COUNT SQL
     *
     * @param originalSql 原始查询SQL
     * @return COUNT查询SQL
     * @throws IllegalStateException 如果未设置分页策略
     */
    public String buildCountSql(String originalSql) {
        if (pageStrategy == null) {
            throw new IllegalStateException("PageStrategy is not set");
        }
        return pageStrategy.countSql(originalSql);
    }

    /**
     * 构建分页查询 SQL
     * <p>将原始查询SQL转换为当前数据库方言的分页SQL
     *
     * @param originalSql 原始查询SQL
     * @return 分页SQL对象（包含SQL语句和参数）
     * @throws IllegalStateException 如果未设置分页策略
     */
    public PageSql buildPageSql(String originalSql) {
        if (pageStrategy == null) {
            throw new IllegalStateException("PageStrategy is not set");
        }
        return pageStrategy.pageSql(originalSql, this);
    }

    /**
     * 重置分页状态
     * <p>清空总记录数和总页数缓存，但保留分页参数和排序信息
     *
     * @return 当前分页对象（支持链式调用）
     */
    public Page reset() {
        this.totalCount = -1;
        this.totalPages = -1;
        return this;
    }

    /**
     * 清空所有数据
     * <p>重置状态并清空排序信息
     *
     * @return 当前分页对象（支持链式调用）
     */
    public Page clear() {
        this.totalCount = -1;
        this.totalPages = -1;
        this.orderColumns = Collections.emptyList();
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