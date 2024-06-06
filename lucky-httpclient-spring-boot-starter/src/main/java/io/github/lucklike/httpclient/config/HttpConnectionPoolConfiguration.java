package io.github.lucklike.httpclient.config;

import java.util.concurrent.TimeUnit;

/**
 * HTTP连接池配置
 */
public class HttpConnectionPoolConfiguration {

    /**
     * 连接池最大连接数，默认：5
     */
    private Integer maxIdleConnections = 10;

    /**
     * 连接池空闲连接的保活时间，默认：5
     */
    private Long keepAliveDuration = 5L;

    /**
     * 连接池空闲连接的保活时间单位，默认：ms
     */
    private TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;

    /**
     * 设置连接池的最大连接数
     *
     * @param maxIdleConnections 最大连接数
     */
    public void setMaxIdleConnections(Integer maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    /**
     * 设置连接池空闲连接的保活时间
     *
     * @param keepAliveDuration 连接池空闲连接的保活时间
     */
    public void setKeepAliveDuration(Long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    /**
     * 设置连接池空闲连接的保活时间单位
     *
     * @param keepAliveTimeUnit 连接池空闲连接的保活时间单位
     */
    public void setKeepAliveTimeUnit(TimeUnit keepAliveTimeUnit) {
        this.keepAliveTimeUnit = keepAliveTimeUnit;
    }


    /**
     * 连接池的最大连接数
     *
     * @return 连接池的最大连接数
     */
    public Integer getMaxIdleConnections() {
        return maxIdleConnections;
    }

    /**
     * 连接池空闲连接的保活时间
     *
     * @return 连接池空闲连接的保活时间
     */
    public Long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * 连接池空闲连接的保活时间单位
     *
     * @return 连接池空闲连接的保活时间单位
     */
    public TimeUnit getKeepAliveTimeUnit() {
        return keepAliveTimeUnit;
    }

}
