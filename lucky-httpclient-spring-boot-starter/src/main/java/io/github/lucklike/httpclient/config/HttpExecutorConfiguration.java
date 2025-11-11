package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.executor.HttpClient5Executor;
import com.luckyframework.httpclient.core.executor.HttpClientExecutor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.executor.OkHttpExecutor;
import io.github.lucklike.httpclient.config.impl.HttpExecutorEnum;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.net.HttpURLConnection;
import java.util.Map;

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
    public static class GlobalConfiguration {

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

        /**
         * HTTP连接池相关配置
         */
        @NestedConfigurationProperty
        private HttpConnectionPoolConfiguration connectionPool = new HttpConnectionPoolConfiguration();


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


        /**
         * 设置HTTP连接池相关的配置
         *
         * @param httpConnectionPool HTTP连接池相关的配置
         */
        public void seConnectionPool(HttpConnectionPoolConfiguration httpConnectionPool) {
            this.connectionPool = httpConnectionPool;
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


        /**
         * 获取HTTP连接池相关的配置
         *
         * @return HTTP连接池相关的配置
         */
        public HttpConnectionPoolConfiguration getConnectionPool() {
            return connectionPool;
        }

    }

    /**
     * 备用的HTTP执行器配置
     */
    public static class AlternativeConfiguration {

        /**
         * 是否延时加载，默认：true
         */
        private boolean lazy = true;
        /**
         * 执行器类型
         */
        private ExecutorType type = ExecutorType.JDK;
        /**
         * 连接池配置
         */
        private HttpConnectionPoolConfiguration connectionPool = new HttpConnectionPoolConfiguration();

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
        public ExecutorType getType() {
            return type;
        }

        /**
         * 设置执行器类型
         *
         * @param type 执行器类型
         */
        public void setType(ExecutorType type) {
            this.type = type;
        }

        /**
         * 获取连接池信息
         *
         * @return 连接池信息
         */
        public HttpConnectionPoolConfiguration getConnectionPool() {
            return connectionPool;
        }

        /**
         * 设置连接池信息
         *
         * @param connectionPool 连接池信息
         */
        public void setConnectionPool(HttpConnectionPoolConfiguration connectionPool) {
            this.connectionPool = connectionPool;
        }

        public HttpExecutor createExecutor() {
            switch (type) {
                case OKHTTP:
                    return new OkHttpExecutor(connectionPool.getMaxIdleConnections(), connectionPool.getKeepAliveDuration(), connectionPool.getKeepAliveTimeUnit());
                case HTTP_CLIENT:
                    return new HttpClientExecutor(connectionPool.getMaxIdleConnections(), connectionPool.getKeepAliveDuration(), connectionPool.getKeepAliveTimeUnit());
                case HTTP_CLIENT5:
                    return new HttpClient5Executor(connectionPool.getMaxIdleConnections(), connectionPool.getKeepAliveDuration(), connectionPool.getKeepAliveTimeUnit());
                default:
                    return new JdkHttpExecutor();
            }
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
}
