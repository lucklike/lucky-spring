package io.github.lucklike.httpclient.config;


import java.util.concurrent.TimeUnit;

/**
 * 带连接池参数的执行器工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/2/17 20:46
 */
public abstract class PoolParamHttpExecutorFactory implements HttpExecutorFactory{

    /**
     * 最大连接数
     */
    private int maxIdleConnections = 10;

    /**
     * 保活时间
     */
    private long keepAliveDuration = 5;

    /**
     * 保活时间单位
     */
    private TimeUnit timeUnit =TimeUnit.MINUTES;


    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
