package io.github.lucklike.httpclient.config.httpexecutor;

import com.luckyframework.httpclient.core.executor.HttpExecutor;

import java.net.HttpURLConnection;

import static io.github.lucklike.httpclient.config.httpexecutor.HttpExecutorConfiguration.createHttpClient5Executor;
import static io.github.lucklike.httpclient.config.httpexecutor.HttpExecutorConfiguration.createHttpClientExecutor;
import static io.github.lucklike.httpclient.config.httpexecutor.HttpExecutorConfiguration.createJdkHttpExecutor;
import static io.github.lucklike.httpclient.config.httpexecutor.HttpExecutorConfiguration.createOkHttpExecutor;

/**
 * 备用的HTTP执行器配置
 */
public class AlternativeConfiguration extends CommonConfiguration {

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