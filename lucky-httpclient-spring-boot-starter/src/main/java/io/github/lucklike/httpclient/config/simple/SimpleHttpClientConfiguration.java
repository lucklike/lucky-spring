package io.github.lucklike.httpclient.config.simple;

import com.luckyframework.common.ConfigurationMap;

import java.util.Map;

public class SimpleHttpClientConfiguration {

    /**
     * 服务地址
     */
    private String url;

    /**
     * 请求头参数
     */
    private final Map<String, Object> headerParams = new ConfigurationMap();

    /**
     * 路径请求参数
     */
    private final Map<String, Object> pathParams = new ConfigurationMap();

    /**
     * Query请求参数
     */
    private final Map<String, Object> queryParams = new ConfigurationMap();

    /**
     * application/x-www-form-urlencoded请求参数
     */
    private final Map<String, Object> formParams = new ConfigurationMap();

    /**
     * multipart/form-data类型请求参数
     */
    private final Map<String, Object> multipartFormParams = new ConfigurationMap();


    /**
     * 设置的请求头参数
     *
     * @param headerParams 公共的请求头参数
     */
    public void setHeaderParams(Map<String, Object> headerParams) {
        this.headerParams.putAll(headerParams);
    }

    /**
     * 设置的路径参数
     *
     * @param pathParams 公共的路径参数
     */
    public void setPathParams(Map<String, Object> pathParams) {
        this.pathParams.putAll(pathParams);
    }

    /**
     * 设置的URL参数
     *
     * @param queryParams 公共的URL参数
     */
    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams.putAll(queryParams);
    }

    /**
     * 获取的请求头参数
     *
     * @return 公共的请求头参数
     */
    public Map<String, Object> getHeaderParams() {
        return headerParams;
    }

    /**
     * 获取的路径参数
     *
     * @return 公共的路径参数
     */
    public Map<String, Object> getPathParams() {
        return pathParams;
    }

    /**
     * 获取的URL参数
     *
     * @return 公共的URL参数
     */
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    /**
     * 获取URL地址
     *
     * @return URL地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置URL地址
     *
     * @param url URL地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取application/x-www-form-urlencoded请求参数
     *
     * @return application/x-www-form-urlencoded请求参数
     */
    public Map<String, Object> getFormParams() {
        return formParams;
    }

    /**
     * 获取multipart/form-data类型请求参数
     *
     * @return multipart/form-data类型请求参数
     */
    public Map<String, Object> getMultipartFormParams() {
        return multipartFormParams;
    }

    /**
     * 设置application/x-www-form-urlencoded请求参数
     *
     * @param formParams application/x-www-form-urlencoded请求参数
     */
    public void setFormParams(Map<String, Object> formParams) {
        this.formParams.putAll(formParams);
    }

    /**
     * 设置multipart/form-data类型请求参数
     *
     * @param multipartFormParams multipart/form-data类型请求参数
     */
    public void setMultipartFormParams(Map<String, Object> multipartFormParams) {
        this.multipartFormParams.putAll(multipartFormParams);
    }
}
