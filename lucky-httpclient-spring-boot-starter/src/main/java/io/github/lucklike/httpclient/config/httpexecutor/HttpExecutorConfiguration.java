package io.github.lucklike.httpclient.config.httpexecutor;

import com.luckyframework.httpclient.core.executor.HttpClient5Executor;
import com.luckyframework.httpclient.core.executor.HttpClientExecutor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.executor.OkHttpExecutor;

import java.util.Map;

/**
 * Http执行器相关的配置
 */
public class HttpExecutorConfiguration extends GlobalConfiguration {


    /**
     * 备用的HTTP执行器配置
     */
    private Map<String, AlternativeConfiguration> alternative;


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


}
