package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.executor.HttpClient5Executor;
import com.luckyframework.httpclient.core.executor.HttpClientExecutor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.executor.OkHttpExecutor;
import com.luckyframework.httpclient.core.meta.Version;
import io.github.lucklike.httpclient.config.impl.HttpExecutorEnum;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_CALL_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_CONNECTION_REQUEST_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_CONNECTION_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_KEEP_ALIVE_DURATION;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_MAX_IDLE_CONNECTIONS;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_MAX_PER_ROUTE;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_MAX_TOTAL;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_READ_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_VALIDATE_AFTER_INACTIVITY;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_WRITE_TIMEOUT;

/**
 * Http执行器相关的配置
 */
public class HttpExecutorConfiguration {

    /**
     * 全局通用的执行器配置
     */
    @NestedConfigurationProperty
    private GlobalConfiguration global = new GlobalConfiguration();

    /**
     * 备用的HTTP执行器配置
     */
    private Map<String, AlternativeConfiguration> alternative;

    /**
     * 获取全局通用的执行器配置
     *
     * @return 全局通用的执行器配置
     */
    public GlobalConfiguration getGlobal() {
        return global;
    }

    /**
     * 创建一个JDK执行器
     *
     * @param commonConfiguration 公共执行器配置
     * @return 公共执行器
     */
    public static HttpExecutor createJdkHttpExecutor(CommonConfiguration commonConfiguration) {
        return new JdkHttpExecutor(commonConfiguration.getConnectTimeout(), commonConfiguration.getReadTimeout());
    }

    /**
     * 创建一个OkHttp执行器
     *
     * @param commonConfiguration 公共执行器配置
     * @return OkHttp执行器
     */
    public static HttpExecutor createOkHttpExecutor(CommonConfiguration commonConfiguration) {
        OkHttpSpecialConfiguration okHttp = commonConfiguration.getOkHttp();
        return new OkHttpExecutor(
                commonConfiguration.getConnectTimeout(),
                commonConfiguration.getReadTimeout(),
                okHttp.getWriteTimeout(),
                okHttp.getCallTimeout(),
                okHttp.getMaxIdleConnections(),
                okHttp.getKeepAliveDuration(),
                okHttp.getKeepAliveTimeUnit(),
                okHttp.getHttpVersion()
        );
    }

    /**
     * 创建一个HttpClient执行器
     *
     * @param commonConfiguration 公共执行器配置
     * @return HttpClient执行器
     */
    public static HttpExecutor createHttpClientExecutor(CommonConfiguration commonConfiguration) {
        HttpClientSpecialConfiguration httpClient = commonConfiguration.getHttpClient();
        return new HttpClientExecutor(
                httpClient.getConnectionRequestTimeout(),
                commonConfiguration.getConnectTimeout(),
                commonConfiguration.getReadTimeout(),
                httpClient.getValidateAfterInactivity(),
                httpClient.getMaxTotal(),
                httpClient.getMaxPerRoute(),
                httpClient.getKeepAliveDuration(),
                httpClient.getKeepAliveTimeUnit(),
                httpClient.getHttpVersion()
        );
    }

    /**
     * 创建一个HttpClient5执行器
     *
     * @param commonConfiguration 公共执行器配置
     * @return HttpClient5执行器
     */
    public static HttpExecutor createHttpClient5Executor(CommonConfiguration commonConfiguration) {
        HttpClientSpecialConfiguration httpClient = commonConfiguration.getHttpClient();
        return new HttpClient5Executor(
                httpClient.getConnectionRequestTimeout(),
                commonConfiguration.getConnectTimeout(),
                commonConfiguration.getReadTimeout(),
                httpClient.getValidateAfterInactivity(),
                httpClient.getMaxTotal(),
                httpClient.getMaxPerRoute(),
                httpClient.getKeepAliveDuration(),
                httpClient.getKeepAliveTimeUnit(),
                httpClient.getHttpVersion()
        );
    }

    /**
     * 设置全局通用的执行器配置
     *
     * @param global 全局通用的执行器配置
     */
    public void setGlobal(GlobalConfiguration global) {
        this.global = global;
    }

    /**
     * 获取备用的HTTP执行器配置
     *
     * @return 备用的HTTP执行器配置
     */
    public Map<String, AlternativeConfiguration> getAlternative() {
        return alternative;
    }

    /**
     * 设置备用的HTTP执行器配置
     *
     * @param alternative 备用的HTTP执行器配置
     */
    public void setAlternative(Map<String, AlternativeConfiguration> alternative) {
        this.alternative = alternative;
    }

    /**
     * 全局通用的HTTP执行器配置
     */
    public static class GlobalConfiguration extends CommonConfiguration {

        /**
         * 指定使用的HTTP执行器Bean的名称
         */
        private String executorBean;

        /**
         * Http请求执行器工厂
         */
        private HttpExecutorFactory executorFactory;

        /**
         * 使用执行器枚举来指定执行器
         */
        private HttpExecutorEnum executor;


        //------------------------------------------------------------------------------------------------
        //                                Setter methods
        //------------------------------------------------------------------------------------------------


        /**
         * 设置{@link HttpExecutorFactory HTTP执行器工厂}
         *
         * @param executorFactory HTTP执行器工厂
         */
        public void setExecutorFactory(HttpExecutorFactory executorFactory) {
            this.executorFactory = executorFactory;
        }

        /**
         * 使用执行器枚举来指定执行器<br/>
         * {@link HttpExecutorEnum#JDK JDK}: 使用JDK的{@link HttpURLConnection}实现的执行器。<br/>
         * {@link HttpExecutorEnum#OKHTTP OK_HTTP}: 使用OkHttp3实现的执行器。<br/>
         * {@link HttpExecutorEnum#HTTP_CLIENT HTTP_CLIENT}: 使用Apache HttpClient实现的执行器。<br/>
         *
         * @param executor 执行器枚举
         */
        public void setExecutor(HttpExecutorEnum executor) {
            this.executor = executor;
        }

        /**
         * 设置使用HTTP执行器的SpringBean的名称
         *
         * @param executorBean HTTP执行器的SpringBean的名称
         */
        public void setExecutorBean(String executorBean) {
            this.executorBean = executorBean;
        }


        //------------------------------------------------------------------------------------------------
        //                                Getter methods
        //------------------------------------------------------------------------------------------------


        /**
         * 获取{@link HttpExecutorFactory HTTP请求执行器工厂}
         *
         * @return HTTP请求执行器工厂
         */
        public HttpExecutorFactory getExecutorFactory() {
            return executorFactory;
        }

        /**
         * 获取执行器对应的执行器枚举
         *
         * @return 执行器枚举
         */
        public HttpExecutorEnum getExecutor() {
            return executor;
        }

        /**
         * HTTP执行器的SpringBean的名称
         *
         * @return HTTP执行器的SpringBean的名称
         */
        public String getExecutorBean() {
            return executorBean;
        }


    }

    /**
     * 备用的HTTP执行器配置
     */
    public static class AlternativeConfiguration extends CommonConfiguration {

        /**
         * 是否延时加载，默认：true
         */
        private boolean lazy = true;

        /**
         * 执行器类型
         */
        private ExecutorType executor = ExecutorType.JDK;

        /**
         * 是否延迟加载
         *
         * @return 是否延迟加载
         */
        public boolean isLazy() {
            return lazy;
        }

        /**
         * 设置是否延迟加载
         *
         * @param lazy 是否延迟加载
         */
        public void setLazy(boolean lazy) {
            this.lazy = lazy;
        }

        /**
         * 获取执行器类型
         *
         * @return 执行器类型
         */
        public ExecutorType getExecutor() {
            return executor;
        }

        /**
         * 设置执行器类型
         *
         * @param executor 执行器类型
         */
        public void setExecutor(ExecutorType executor) {
            this.executor = executor;
        }

        public HttpExecutor createExecutor() {
            switch (executor) {
                case OKHTTP:
                    return createOkHttpExecutor(this);
                case HTTP_CLIENT:
                    return createHttpClientExecutor(this);
                case HTTP_CLIENT5:
                    return createHttpClient5Executor(this);
                default:
                    return createJdkHttpExecutor(this);
            }
        }
    }

    /**
     * 公共配置
     */
    public static class CommonConfiguration {

        /**
         * 连接建立超时时间，单位：ms
         */
        private Integer connectTimeout = DEFAULT_CONNECTION_TIMEOUT;

        /**
         * 数据读取超时时间，单位：ms
         */
        private Integer readTimeout = DEFAULT_READ_TIMEOUT;


        /**
         * 获取连接建立超时时间，单位：ms
         *
         * @return 连接建立超时时间
         */
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        /**
         * 设置连接建立超时时间，单位：ms
         *
         * @param connectTimeout 连接建立超时时间
         */
        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        /**
         * 获取数据读取超时时间，单位：ms
         *
         * @return 数据读取超时时间
         */
        public Integer getReadTimeout() {
            return readTimeout;
        }

        /**
         * 设置数据读取超时时间，单位：ms
         *
         * @param readTimeout 数据读取超时时间
         */
        public void setReadTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
        }

        /**
         * HttpClient执行器配置
         */
        @NestedConfigurationProperty
        private HttpClientSpecialConfiguration httpClient = new HttpClientSpecialConfiguration();

        /**
         * HttpClient执行器配置
         */
        @NestedConfigurationProperty
        private OkHttpSpecialConfiguration okHttp = new OkHttpSpecialConfiguration();

        /**
         * 设置HttpClient特有的参数
         *
         * @param httpClient HttpClient特有的参数
         */
        public void setHttpClient(HttpClientSpecialConfiguration httpClient) {
            this.httpClient = httpClient;
        }

        /**
         * 设置OkHttp特有的参数
         *
         * @param okHttp OkHttp特有的参数
         */
        public void setOkHttp(OkHttpSpecialConfiguration okHttp) {
            this.okHttp = okHttp;
        }

        /**
         * 获取HttpClient特有的参数
         *
         * @return HttpClient特有的参数
         */
        public HttpClientSpecialConfiguration getHttpClient() {
            return httpClient;
        }

        /**
         * 获取OkHttp特有的参数
         *
         * @return OkHttp特有的参数
         */
        public OkHttpSpecialConfiguration getOkHttp() {
            return okHttp;
        }


    }

    /**
     * 执行器类型
     */
    public enum ExecutorType {
        /**
         * 基于JDK{@link HttpURLConnection}实现的执行器枚举配置
         */
        JDK,

        /**
         * 基于Okhttp3实现的执行器枚举配置
         */
        OKHTTP,

        /**
         * 基于Apache HttpClient实现的执行器枚举配置
         */
        HTTP_CLIENT,

        /**
         * 基于Apache HttpClient5实现的执行器枚举配置
         */
        HTTP_CLIENT5;
    }

    /**
     * HttpClient执行器的特殊配置
     */
    public static class HttpClientSpecialConfiguration {

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

    /**
     * OkHttp执行器的特殊配置
     */
    public static class OkHttpSpecialConfiguration {

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
}
