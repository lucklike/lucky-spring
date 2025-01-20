package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.threadpool.ThreadPoolParam;

/**
 * 支持延迟加载配置的线程池参数
 */
public class LazyThreadPoolParam extends ThreadPoolParam {

    /**
     * 是否延时加载，默认：true
     */
    private boolean lazy = true;

    /**
     * 是否延迟加载，默认：true
     *
     * @return 是否延迟加载
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * 设置是否延迟加载
     *
     * @param lazy 延迟加载
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }
}
