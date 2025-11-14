package io.github.lucklike.httpclient.config.httpexecutor;

import com.luckyframework.httpclient.core.meta.Version;

import java.util.concurrent.TimeUnit;

import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_CALL_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_KEEP_ALIVE_DURATION;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_MAX_IDLE_CONNECTIONS;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_WRITE_TIMEOUT;

/**
 * OkHttp执行器的特殊配置
 */
public class OkHttpSpecialConfiguration {

    /**
     * 数据写入超时时间，单位：ms
     */
    private Integer writeTimeout = DEFAULT_WRITE_TIMEOUT;

    /**
     * 整体调用超时时间，单位：ms
     */
    private Integer callTimeout = DEFAULT_CALL_TIMEOUT;

    /**
     * 最大空闲连接数
     */
    private Integer maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;

    /**
     * 保持存活时间
     */
    private Integer keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;

    /**
     * 保持存活时间的时间单位，默认：min
     */
    private TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;

    /**
     * 使用的HTTP版本
     */
    private Version httpVersion = Version.NON;

    /**
     * 获取数据写入超时时间
     *
     * @return 数据写入超时时间
     */
    public Integer getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * 设置数据写入超时时间
     *
     * @param writeTimeout 数据写入超时时间
     */
    public void setWriteTimeout(Integer writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     * 获取整体调用超时时间
     *
     * @return 整体调用超时时间
     */
    public Integer getCallTimeout() {
        return callTimeout;
    }

    /**
     * 设置整体调用超时时间
     *
     * @param callTimeout 整体调用超时时间
     */
    public void setCallTimeout(Integer callTimeout) {
        this.callTimeout = callTimeout;
    }

    /**
     * 获取最大空闲连接数
     *
     * @return 最大空闲连接数
     */
    public Integer getMaxIdleConnections() {
        return maxIdleConnections;
    }

    /**
     * 设置最大空闲连接数
     *
     * @param maxIdleConnections 最大空闲连接数
     */
    public void setMaxIdleConnections(Integer maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    /**
     * 获取保持存活时间
     *
     * @return 保持存活时间
     */
    public Integer getKeepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * 设置保持存活时间
     *
     * @param keepAliveDuration 保持存活时间
     */
    public void setKeepAliveDuration(Integer keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    /**
     * 获取保持存活时间的时间单位
     *
     * @return 保持存活时间的时间单位
     */
    public TimeUnit getKeepAliveTimeUnit() {
        return keepAliveTimeUnit;
    }

    /**
     * 设置保持存活时间的时间单位
     *
     * @param keepAliveTimeUnit 保持存活时间的时间单位
     */
    public void setKeepAliveTimeUnit(TimeUnit keepAliveTimeUnit) {
        this.keepAliveTimeUnit = keepAliveTimeUnit;
    }

    /**
     * 获取使用的HTTP版本
     *
     * @return 使用的HTTP版本
     */
    public Version getHttpVersion() {
        return httpVersion;
    }

    /**
     * 设置使用的HTTP版本
     *
     * @param httpVersion 使用的HTTP版本
     */
    public void setHttpVersion(Version httpVersion) {
        this.httpVersion = httpVersion;
    }
}