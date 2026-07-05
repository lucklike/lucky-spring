package io.github.lucklike.httpclient.dbclient.sql.page;

import com.luckyframework.httpclient.proxy.context.MethodContext;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 13:24
 */
public class ContextPage extends Page {
    private final MethodContext mc;

    public ContextPage(MethodContext mc, Page page) {
        this.mc = mc;
        setPageNum(page.getPageNum());
        setPageSize(page.getPageSize());
        setTotalCount(page.getTotalCount());
        setTotalPages(page.getTotalPages());
        setOrderColumns(page.getOrderColumns());
        setCountTotal(page.isCountTotal());
        setPageStrategy(page.getPageStrategy());
    }

    public MethodContext getMc() {
        return mc;
    }
}
