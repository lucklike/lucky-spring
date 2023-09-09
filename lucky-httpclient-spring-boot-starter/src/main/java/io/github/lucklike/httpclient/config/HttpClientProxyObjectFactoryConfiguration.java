package io.github.lucklike.httpclient.config;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.threadpool.ThreadPoolParam;

/**
 * HttpClientProxyObjectFactory配置类
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/8 11:35
 */
public class HttpClientProxyObjectFactoryConfiguration {

    /**
     * 用于创建异步调用的线程池的参数
     */
    private ThreadPoolParam threadPoolParam;

    /**
     * SpEL运行时环境工厂
     */
    private SpELRuntimeFactory spelruntimeFactory;

    /**
     * 对象创建器工厂f
     */
    private ObjectCreatorFactory objectCreatorFactory;

    /**
     * Http请求执行器工厂
     */
    private HttpExecutorFactory httpExecutorFactory;

    /**
     * HTTP异常处理器工厂
     */
    private HttpExceptionHandleFactory httpExceptionHandleFactory;

    /**
     * 请求处理器工厂
     */
    private RequestAfterProcessorsFactory requestAfterProcessorsFactory;

    /**
     * 响应处理器工厂
     */
    private ResponseAfterProcessorsFactory responseAfterProcessorsFactory;

    /**
     * 连接超时时间
     */
    private Integer connectionTimeout;

    /**
     * 读超时时间
     */
    private Integer readTimeout;

    /**
     * 写超时时间
     */
    private Integer writeTimeout;

    /**
     * 公共请求头参数
     */
    private ConfigurationMap headerParams = new ConfigurationMap();

    /**
     * 公共路径请求参数
     */
    private ConfigurationMap pathParams = new ConfigurationMap();

    /**
     * 公共URL请求参数
     */
    private ConfigurationMap queryParams = new ConfigurationMap();

    /**
     * 公共Form请求参数
     */
    private ConfigurationMap formParams = new ConfigurationMap();

    /**
     * 公共资源参数
     */
    private ConfigurationMap resourceParams = new ConfigurationMap();

    /**
     * SpEL表达式参数
     */
    private ConfigurationMap expressionParams = new ConfigurationMap();

    //------------------------------------------------------------------------------------------------
    //                                Setter methods
    //------------------------------------------------------------------------------------------------

    /**
     * 设置线程池参数
     *
     * @param threadPoolParam 线程池参数
     */
    public void setThreadPoolParam(ThreadPoolParam threadPoolParam) {
        this.threadPoolParam = threadPoolParam;
    }


    /**
     * 设置{@link SpELRuntimeFactory SpEL运行时环境工厂}
     *
     * @param spelruntimeFactory SpEL运行时环境工厂
     */
    public void setSpelruntimeFactory(SpELRuntimeFactory spelruntimeFactory) {
        this.spelruntimeFactory = spelruntimeFactory;
    }

    /**
     * 设置{@link ObjectCreatorFactory 对象创建器工厂}
     *
     * @param objectCreatorFactory 对象创建器工厂
     */
    public void setObjectCreatorFactory(ObjectCreatorFactory objectCreatorFactory) {
        this.objectCreatorFactory = objectCreatorFactory;
    }

    /**
     * 设置{@link HttpExecutorFactory HTTP执行器工厂}
     *
     * @param httpExecutorFactory HTTP执行器工厂
     */
    public void setHttpExecutorFactory(HttpExecutorFactory httpExecutorFactory) {
        this.httpExecutorFactory = httpExecutorFactory;
    }

    /**
     * 设置{@link HttpExceptionHandleFactory 异常处理器工厂}
     *
     * @param httpExceptionHandleFactory 异常处理器工厂
     */
    public void setHttpExceptionHandleFactory(HttpExceptionHandleFactory httpExceptionHandleFactory) {
        this.httpExceptionHandleFactory = httpExceptionHandleFactory;
    }

    /**
     * 设置{@link RequestAfterProcessorsFactory 请求处理器工厂}
     *
     * @param requestAfterProcessorsFactory 请求处理器工厂
     */
    public void setRequestAfterProcessorsFactory(RequestAfterProcessorsFactory requestAfterProcessorsFactory) {
        this.requestAfterProcessorsFactory = requestAfterProcessorsFactory;
    }

    /**
     * 设置{@link ResponseAfterProcessorsFactory 响应处理器工厂}
     *
     * @param responseAfterProcessorsFactory 响应处理器工厂
     */
    public void setResponseAfterProcessorsFactory(ResponseAfterProcessorsFactory responseAfterProcessorsFactory) {
        this.responseAfterProcessorsFactory = responseAfterProcessorsFactory;
    }

    /**
     * 设置连接超时时间
     *
     * @param connectionTimeout 连接超时时间
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * 设置读超时时间
     *
     * @param readTimeout 读超时时间
     */
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * 设置写超时时间
     *
     * @param writeTimeout 写超时时间
     */
    public void setWriteTimeout(Integer writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     * 设置公共的请求头参数
     *
     * @param headerParams 公共的请求头参数
     */
    public void setHeaderParams(ConfigurationMap headerParams) {
        this.headerParams = headerParams;
    }

    /**
     * 设置公共的路径参数
     *
     * @param pathParams 公共的路径参数
     */
    public void setPathParams(ConfigurationMap pathParams) {
        this.pathParams = pathParams;
    }

    /**
     * 设置公共的URL参数
     *
     * @param queryParams 公共的URL参数
     */
    public void setQueryParams(ConfigurationMap queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * 设置公共的表单参数
     *
     * @param formParams 公共的表单参数
     */
    public void setFormParams(ConfigurationMap formParams) {
        this.formParams = formParams;
    }

    /**
     * 设置公共的资源参数
     *
     * @param resourceParams 公共的资源参数
     */
    public void setResourceParams(ConfigurationMap resourceParams) {
        this.resourceParams = resourceParams;
    }

    /**
     * 设置自定义SpEL表达式参数
     *
     * @param expressionParams 自定义参数
     */
    public void setExpressionParams(ConfigurationMap expressionParams) {
        this.expressionParams = expressionParams;
    }


    //------------------------------------------------------------------------------------------------
    //                                Getter methods
    //------------------------------------------------------------------------------------------------


    /**
     * 获取线程池参数
     *
     * @return 线程池参数
     */
    public ThreadPoolParam getThreadPoolParam() {
        return threadPoolParam;
    }

    /**
     * 获取{@link SpELRuntimeFactory SpEL运行时环境工厂}
     *
     * @return SpEL运行时环境工厂
     */
    public SpELRuntimeFactory getSpelruntimeFactory() {
        return spelruntimeFactory;
    }

    /**
     * 获取{@link ObjectCreatorFactory 对象创建器工厂}
     *
     * @return 对象创建器工厂
     */
    public ObjectCreatorFactory getObjectCreatorFactory() {
        return objectCreatorFactory;
    }

    /**
     * 获取{@link HttpExecutorFactory HTTP请求执行器工厂}
     *
     * @return HTTP请求执行器工厂
     */
    public HttpExecutorFactory getHttpExecutorFactory() {
        return httpExecutorFactory;
    }

    /**
     * 获取{@link HttpExceptionHandleFactory HTTP异常处理器工厂}
     *
     * @return HTTP异常处理器
     */
    public HttpExceptionHandleFactory getHttpExceptionHandleFactory() {
        return httpExceptionHandleFactory;
    }

    /**
     * 获取{@link HttpExceptionHandleFactory 请求处理器工厂}
     *
     * @return 请求处理器工厂
     */
    public RequestAfterProcessorsFactory getRequestAfterProcessorsFactory() {
        return requestAfterProcessorsFactory;
    }

    /**
     * 获取{@link ResponseAfterProcessorsFactory 响应处理器工厂}
     *
     * @return 响应处理器工厂
     */
    public ResponseAfterProcessorsFactory getResponseAfterProcessorsFactory() {
        return responseAfterProcessorsFactory;
    }

    /**
     * 获取链接超时时间
     *
     * @return 链接超时时间
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * 获取读超时时间
     *
     * @return 读超时时间
     */
    public Integer getReadTimeout() {
        return readTimeout;
    }

    /**
     * 获取写超时时间
     *
     * @return 写超时时间
     */
    public Integer getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * 获取公共的请求头参数
     *
     * @return 公共的请求头参数
     */
    public ConfigurationMap getHeaderParams() {
        return headerParams;
    }

    /**
     * 获取公共的路径参数
     *
     * @return 公共的路径参数
     */
    public ConfigurationMap getPathParams() {
        return pathParams;
    }

    /**
     * 获取公共的URL参数
     *
     * @return 公共的URL参数
     */
    public ConfigurationMap getQueryParams() {
        return queryParams;
    }

    /**
     * 获取公共的表单参数
     *
     * @return 公共的表单参数
     */
    public ConfigurationMap getFormParams() {
        return formParams;
    }

    /**
     * 获取公共的资源参数
     *
     * @return 公共的资源参数
     */
    public ConfigurationMap getResourceParams() {
        return resourceParams;
    }

    /**
     * 获取自定义SpEL表达式参数
     *
     * @return 自定义SpEL表达式参数
     */
    public ConfigurationMap getExpressionParams() {
        return expressionParams;
    }
}
