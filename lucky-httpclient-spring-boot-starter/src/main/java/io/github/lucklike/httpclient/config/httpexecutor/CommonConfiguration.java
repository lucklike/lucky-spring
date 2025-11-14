package io.github.lucklike.httpclient.config.httpexecutor;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_CONNECTION_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.DEFAULT_READ_TIMEOUT;

/**
 * 公共配置
 */
public class CommonConfiguration {

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