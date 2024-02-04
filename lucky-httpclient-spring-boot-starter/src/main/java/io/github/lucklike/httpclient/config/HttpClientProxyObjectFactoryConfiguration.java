package io.github.lucklike.httpclient.config;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.httpclient.proxy.interceptor.Interceptor;
import com.luckyframework.threadpool.ThreadPoolParam;
import io.github.lucklike.httpclient.config.impl.HttpExecutorEnum;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private SpELRuntimeFactory springElRuntimeFactory;

    /**
     * 向SpEL运行时环境导入的包
     */
    private List<String> springElPackageImports;

    /**
     * 对象创建器工厂
     */
    private ObjectCreatorFactory objectCreatorFactory;

    /**
     * Http请求执行器工厂
     */
    private HttpExecutorFactory httpExecutorFactory;

    /**
     * 使用执行器枚举来指定执行器
     */
    private HttpExecutorEnum httpExecutor;

    /**
     * HTTP异常处理器工厂
     */
    private HttpExceptionHandleFactory httpExceptionHandleFactory;

    /**
     * 拦截器集合
     */
    private Interceptor[] interceptors;

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

    /**
     * 指定需要打印日志的包
     */
    private Set<String> printLogPackages = new HashSet<>();

    /**
     * 是否开启请求日志，默认开启
     */
    private boolean enableRequestLog = true;

    /**
     * 是否开启响应日志，默认开启
     */
    private boolean enableResponseLog = true;

    /**
     * MimeType为这些类型时，将打印响应体日志<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/xml</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
     * </ui>
     */
    private Set<String> allowPrintLogBodyMimeTypes;

    /**
     * 响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    private long allowPrintLogBodyMaxLength = -1L;

    /**
     * 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    private String printReqLogCondition;

    /**
     * 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    private String printRespLogCondition;

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
     * @param springElRuntimeFactory SpEL运行时环境工厂
     */
    public void setSpringElRuntimeFactory(SpELRuntimeFactory springElRuntimeFactory) {
        this.springElRuntimeFactory = springElRuntimeFactory;
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
     * 使用执行器枚举来指定执行器<br/>
     * {@link HttpExecutorEnum#JDK JDK}: 使用JDK的{@link HttpURLConnection}实现的执行器。<br/>
     * {@link HttpExecutorEnum#OKHTTP OK_HTTP}: 使用OkHttp实现的执行器。<br/>
     * {@link HttpExecutorEnum#HTTP_CLIENT HTTP_CLIENT}: 使用Apache HttpClient实现的执行器。<br/>
     *
     * @param httpExecutor 执行器枚举
     */
    public void setHttpExecutor(HttpExecutorEnum httpExecutor) {
        this.httpExecutor = httpExecutor;
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

    /**
     * 向SpEL运行时环境导入的包
     *
     * @param springElPackageImports 向SpEL运行时环境导入的包
     */
    public void setSpringElPackageImports(List<String> springElPackageImports) {
        this.springElPackageImports = springElPackageImports;
    }

    /**
     * 设置{@link Interceptor}拦截器集合
     *
     * @param interceptors 拦截器集合
     */
    public void setInterceptors(Interceptor[] interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * 指定需要打印日志的包
     *
     * @param printLogPackages 指定需要打印日志的包
     */
    public void setPrintLogPackages(Set<String> printLogPackages) {
        this.printLogPackages = printLogPackages;
    }

    /**
     * 设置是否开启请求日志的打印，默认开启
     *
     * @param enableRequestLog 是否开启请求日志的打印
     */
    public void setEnableRequestLog(boolean enableRequestLog) {
        this.enableRequestLog = enableRequestLog;
    }

    /**
     * 设置是否开启响应日志的打印，默认开启
     *
     * @param enableResponseLog 否开启响应日志的打印
     */
    public void setEnableResponseLog(boolean enableResponseLog) {
        this.enableResponseLog = enableResponseLog;
    }

    /**
     * 设置MimeType，当MimeType为这些类型时，将打印响应体日志<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/xml</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
     * </ui>
     *
     * @param allowPrintLogBodyMimeTypes 打印响应体内容的MimeType集合
     */
    public void setAllowPrintLogBodyMimeTypes(Set<String> allowPrintLogBodyMimeTypes) {
        this.allowPrintLogBodyMimeTypes = allowPrintLogBodyMimeTypes;
    }

    /**
     * 设置打印响应日志的阈值，响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public void setAllowPrintLogBodyMaxLength(long allowPrintLogBodyMaxLength) {
        this.allowPrintLogBodyMaxLength = allowPrintLogBodyMaxLength;
    }

    /**
     * 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @param printReqLogCondition 打印请求日志的条件
     */
    public void setPrintReqLogCondition(String printReqLogCondition) {
        this.printReqLogCondition = printReqLogCondition;
    }

    /**
     * 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @param printRespLogCondition 打印请求日志的条件
     */
    public void setPrintRespLogCondition(String printRespLogCondition) {
        this.printRespLogCondition = printRespLogCondition;
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
    public SpELRuntimeFactory getSpringElRuntimeFactory() {
        return springElRuntimeFactory;
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
     * 获取执行器对应的执行器枚举
     *
     * @return 执行器枚举
     */
    public HttpExecutorEnum getHttpExecutor() {
        return httpExecutor;
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

    /**
     * 向SpEL运行时环境导入的包
     *
     * @return 向SpEL运行时环境导入的包
     */
    public List<String> getSpringElPackageImports() {
        return springElPackageImports;
    }

    /**
     * 获取{@link Interceptor}拦截器集合
     *
     * @return 拦截器集合
     */
    public Interceptor[] getInterceptors() {
        return interceptors;
    }

    /**
     * 获取需要打印日志的包集合
     *
     * @return 需要打印日志的包集合
     */
    public Set<String> getPrintLogPackages() {
        return printLogPackages;
    }

    /**
     * 是否开启了请求日志打印功能
     *
     * @return 是否开启了请求日志打印功能
     */
    public boolean isEnableRequestLog() {
        return enableRequestLog;
    }

    /**
     * 是否开启了响应日志打印功能
     *
     * @return 是否开启了响应日志打印功能
     */
    public boolean isEnableResponseLog() {
        return enableResponseLog;
    }

    /**
     * 获取MimeType，当MimeType为这些类型时，将打印响应体日志<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/xml</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
     * </ui>
     */
    public Set<String> getAllowPrintLogBodyMimeTypes() {
        return allowPrintLogBodyMimeTypes;
    }

    /**
     * 获取打印响应日志的阈值，响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public long getAllowPrintLogBodyMaxLength() {
        return allowPrintLogBodyMaxLength;
    }

    /**
     * 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @return 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    public String getPrintReqLogCondition() {
        return printReqLogCondition;
    }

    /**
     * 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @return 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    public String getPrintRespLogCondition() {
        return printRespLogCondition;
    }
}
