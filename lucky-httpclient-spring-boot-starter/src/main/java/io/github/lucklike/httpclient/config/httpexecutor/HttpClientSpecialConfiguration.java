package io.github.lucklike.httpclient.config.httpexecutor;

import com.luckyframework.httpclient.core.meta.Version;

import java.util.concurrent.TimeUnit;

import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_CONNECTION_REQUEST_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_KEEP_ALIVE_DURATION;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_MAX_PER_ROUTE;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_MAX_TOTAL;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_VALIDATE_AFTER_INACTIVITY;

/**
 * HttpClient执行器的特殊配置
 */
public class HttpClientSpecialConfiguration {

    /**
     * 连接获取超时时间，单位：ms
     */
    private Integer connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;

    /**
     * 连接验证间隔，单位：ms
     */
    private Integer validateAfterInactivity = DEFAULT_VALIDATE_AFTER_INACTIVITY;

    /**
     * 最大总连接数
     */
    private Integer maxTotal = DEFAULT_MAX_TOTAL;

    /**
     * 单个路由最大的连接数
     */
    private Integer maxPerRoute = DEFAULT_MAX_PER_ROUTE;

    /**
     * 保持存活时间
     */
    private Integer keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;

    /**
     * 保持存活时间的时间单位，默认:min
     */
    private TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;

    /**
     * 使用的HTTP版本
     */
    private Version httpVersion = Version.NON;


    /**
     * 获取连接获取超时时间，单位：ms
     *
     * @return 连接获取超时时间
     */
    public Integer getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * 设置连接获取超时时间，单位：ms
     *
     * @param connectionRequestTimeout 连接获取超时时间
     */
    public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    /**
     * 获取连接验证间隔，单位：ms
     *
     * @return 连接验证间隔
     */
    public Integer getValidateAfterInactivity() {
        return validateAfterInactivity;
    }

    /**
     * 设置连接验证间隔，单位：ms
     *
     * @param validateAfterInactivity 连接验证间隔
     */
    public void setValidateAfterInactivity(Integer validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
    }

    /**
     * 获取最大总连接数
     *
     * @return 最大总连接数
     */
    public Integer getMaxTotal() {
        return maxTotal;
    }

    /**
     * 设置最大总连接数
     *
     * @param maxTotal 最大总连接数
     */
    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    /**
     * 获取单个路由最大的连接数
     *
     * @return 单个路由最大的连接数
     */
    public Integer getMaxPerRoute() {
        return maxPerRoute;
    }

    /**
     * 设置单个路由最大的连接数
     *
     * @param maxPerRoute 单个路由最大的连接数
     */
    public void setMaxPerRoute(Integer maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
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
     * 获取保持存活时间的时间单位，默认：min
     *
     * @return 保持存活时间的时间单位
     */
    public TimeUnit getKeepAliveTimeUnit() {
        return keepAliveTimeUnit;
    }

    /**
     * 设置保持存活时间的时间单位，默认：min
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