package io.github.lucklike.httpclient.config.simple;

import com.luckyframework.httpclient.proxy.configapi.MultipartFormData;
import io.github.lucklike.httpclient.config.GenerateEntry;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleHttpClientConfiguration {

    /**
     * 服务地址
     */
    private String url;

    /**
     * 请求头参数
     */
    private final Map<String, Object> headerParams = new LinkedHashMap<>();

    /**
     * 路径请求参数
     */
    private final Map<String, Object> pathParams = new LinkedHashMap<>();

    /**
     * Query请求参数
     */
    private final Map<String, Object> queryParams = new LinkedHashMap<>();

    /**
     * application/x-www-form-urlencoded请求参数
     */
    private final Map<String, Object> formParams = new LinkedHashMap<>();

    /**
     * multipart/form-data类型请求参数
     */
    @NestedConfigurationProperty
    private MultipartFormData multipartFormParams = new MultipartFormData();

    /**
     * 初始化参数
     */
    private Map<String, Object> initParams = new LinkedHashMap<>();

    /**
     * 额外的自定义请求参数
     */
    private Map<String, Object> additionalParams = new LinkedHashMap<>();

    /**
     * 生命周期管理器
     */
    @NestedConfigurationProperty
    private GenerateEntry<LifeCycleManager> lifecycleManager;

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
    public MultipartFormData getMultipartFormParams() {
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
    public void setMultipartFormParams(MultipartFormData multipartFormParams) {
        this.multipartFormParams = multipartFormParams;
    }

    /**
     * 初始化参数
     *
     * @return 初始化参数
     */
    public Map<String, Object> getInitParams() {
        return initParams;
    }

    /**
     * 设置初始化参数
     *
     * @param initParams 初始化参数
     */
    public void setInitParams(Map<String, Object> initParams) {
        this.initParams = initParams;
    }

    /**
     * 额外的自定义请求参数
     *
     * @return 额外的自定义请求参数
     */
    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    /**
     * 设置额外的自定义请求参数
     *
     * @param additionalParams 额外的自定义请求参数
     */
    public void setAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }

    /**
     * 获取生命周期管理器对象
     *
     * @return 生命周期管理器对象
     */
    public GenerateEntry<LifeCycleManager> getLifecycleManager() {
        return lifecycleManager;
    }

    /**
     * 设置生命周期管理器对象
     *
     * @param lifecycleManager 生命周期管理器对象
     */
    public void setLifecycleManager(GenerateEntry<LifeCycleManager> lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }
}

