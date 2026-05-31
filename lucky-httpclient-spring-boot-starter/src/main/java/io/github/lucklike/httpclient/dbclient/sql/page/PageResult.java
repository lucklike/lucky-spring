package io.github.lucklike.httpclient.dbclient.sql.page;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 11:07
 */
public class PageResult<T> extends Page {

    private List<T> records = Collections.emptyList();
    private long dataSize;

    public PageResult(Page page) {
        setPageNum(page.getPageNum());
        setPageSize(page.getPageSize());
        setTotalCount(page.getTotalCount());
        setTotalPages(page.getTotalPages());
        setCountTotal(page.isCountTotal());
        setOrderColumns(page.getOrderColumns());
    }

    public List<T> getRecords() {
        return records;
    }

    public long getDataSize() {
        return dataSize;
    }

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
