package io.github.lucklike.httpclient.dbclient.sql.page;

import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果
 * <p>
 * 继承 {@link Page} 获得全部分页方法与分页参数，额外承载当前页的数据记录与数据量。
 * </p>
 *
 * @param <T> 记录类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 11:07
 */
public class PageResult<T> extends Page {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页数据记录
     */
    private List<T> records = Collections.emptyList();

    /**
     * 当前页数据量
     */
    private long dataSize;

    /**
     * 构造分页结果，复制源分页对象的分页参数
     *
     * @param page 源分页对象
     */
    public PageResult(Page page) {
        setPageNum(page.getPageNum());
        setPageSize(page.getPageSize());
        setTotalCount(page.getTotalCount());
        setTotalPages(page.getTotalPages());
        setCountTotal(page.isCountTotal());
        setOrderColumns(page.getOrderColumns());
    }

    /**
     * 获取当前页数据记录
     *
     * @return 当前页数据记录
     */
    public List<T> getRecords() {
        return records;
    }

    /**
     * 获取当前页数据量
     *
     * @return 当前页数据量
     */
    public long getDataSize() {
        return dataSize;
    }

    /**
     * 设置当前页数据记录
     *
     * @param records 当前页数据记录
     */
    public void setRecords(List<T> records) {
        this.records = records;
        this.dataSize = records.size();
    }

    @Override
    public String toString() {
        return "PageResult{" +
                super.toString() +
                ", dataSize=" + dataSize +
                ", records=" + records +
                '}';
    }
}
