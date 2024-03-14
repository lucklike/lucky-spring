package io.github.lucklike.httpclient.config;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.httpclient.core.CookieStore;
import com.luckyframework.httpclient.proxy.handle.HttpExceptionHandle;
import com.luckyframework.threadpool.ThreadPoolParam;
import io.github.lucklike.httpclient.config.impl.HttpExecutorEnum;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
     * 指定使用的HTTP执行器Bean的名称
     */
    private String httpExecutorBean;

    /**
     * 连接池最大连接数
     */
    private Integer maxIdleConnections = 10;

    /**
     * 连接池空闲连接的保活时间
     */
    private Long keepAliveDuration = 5L;

    /**
     * 连接池空闲连接的保活时间单位
     */
    private TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;

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
     * HTTP异常处理器生成器信息
     */
    private GenerateEntry<HttpExceptionHandle> exceptionHandleGenerate;

    /**
     * 拦截器生成器数组
     */
    private InterceptorGenerateEntry[] interceptorGenerates;

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
     * 公共multipart/form-data参数(简单参数)
     */
    private ConfigurationMap multipartFormSimpleParams = new ConfigurationMap();

    /**
     * 公共multipart/form-data参数(资源参数)
     */
    private ConfigurationMap multipartFormResourceParams = new ConfigurationMap();

    /**
     * SpEL表达式参数
     */
    private ConfigurationMap expressionParams = new ConfigurationMap();

    /**
     * 指定需要打印日志的包
     */
    private Set<String> printLogPackages = new HashSet<>();

    /**
     * 是否开启请求日志，默认开启（只有在{@link #printLogPackages}不为{@code null}时才生效）
     */
    private boolean enableRequestLog = true;

    /**
     * 是否开启响应日志，默认开启（只有在{@link #printLogPackages}不为{@code null}时才生效）
     */
    private boolean enableResponseLog = true;

    /**
     * 是否开启打印注解信息功能，默认关闭
     */
    private boolean enablePrintAnnotationInfo = false;

    /**
     * 是否开启打印参数信息功能，默认关闭
     */
    private boolean enablePrintArgsInfo = false;

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

    /**
     * 是否开启自动重定向
     */
    private boolean autoRedirect;

    /**
     * 是否忽略SSL证书认证
     */
    private boolean ignoreSSLVerify;

    /**
     * 使用的SSL协议，默认为TLS
     */
    private String sslProtocol;

    /**
     * 需要重定向的状态码，默认重定向状态码：301, 302, 303, 304, 307, 308
     */
    private Integer[] redirectStatus;

    /**
     * 需要重定向的条件，此处支持SpEL表达式
     */
    private String redirectCondition;

    /**
     * 重定向地址表达式，此处支持SpEL表达式，默认值为：#{$respHeader$.Location}
     */
    private String redirectLocation;

    /**
     * 是否开启Cookie管理功能
     */
    private boolean enableCookieManage;

    /**
     * CookieStore生成器
     */
    private SimpleGenerateEntry<CookieStore> cookieStoreGenerate;

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
     * {@link HttpExecutorEnum#OKHTTP3 OK_HTTP}: 使用OkHttp3实现的执行器。<br/>
     * {@link HttpExecutorEnum#HTTP_CLIENT HTTP_CLIENT}: 使用Apache HttpClient实现的执行器。<br/>
     *
     * @param httpExecutor 执行器枚举
     */
    public void setHttpExecutor(HttpExecutorEnum httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    /**
     * 设置异常处理器生成器
     *
     * @param exceptionHandleGenerate 异常处理器生成器
     */
    public void setExceptionHandleGenerate(GenerateEntry<HttpExceptionHandle> exceptionHandleGenerate) {
        this.exceptionHandleGenerate = exceptionHandleGenerate;
    }

    /**
     * 设置拦截器生成器（数组）
     *
     * @param interceptorGenerates 拦截器生成器（数组）
     */
    public void setInterceptorGenerates(InterceptorGenerateEntry[] interceptorGenerates) {
        this.interceptorGenerates = interceptorGenerates;
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
     * 设置公共的multipart/form-data参数(简单参数)
     *
     * @param multipartFormSimpleParams 公共的multipart/form-data参数(简单参数)
     */
    public void setMultipartFormSimpleParams(ConfigurationMap multipartFormSimpleParams) {
        this.multipartFormSimpleParams = multipartFormSimpleParams;
    }

    /**
     * 设置公共的multipart/form-data参数(资源参数)
     *
     * @param multipartFormResourceParams 公共的multipart/form-data参数(资源参数)
     */
    public void setMultipartFormResourceParams(ConfigurationMap multipartFormResourceParams) {
        this.multipartFormResourceParams = multipartFormResourceParams;
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

    /**
     * 设置是否开启打印注解信息功能
     *
     * @param enablePrintAnnotationInfo 是否开启打印注解信息功能
     */
    public void setEnablePrintAnnotationInfo(boolean enablePrintAnnotationInfo) {
        this.enablePrintAnnotationInfo = enablePrintAnnotationInfo;
    }

    /**
     * 设置是否开启打印参数信息功能
     *
     * @param enablePrintArgsInfo 是否开启打印参数信息功能
     */
    public void setEnablePrintArgsInfo(boolean enablePrintArgsInfo) {
        this.enablePrintArgsInfo = enablePrintArgsInfo;
    }


    /**
     * 设置是否开启自动重定向功能
     *
     * @param autoRedirect 是否开启自动重定向功能
     */
    public void setAutoRedirect(boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }

    /**
     * 是否忽略SSL证书认证
     *
     * @param ignoreSSLVerify 是否忽略SSL证书认证
     */
    public void setIgnoreSSLVerify(boolean ignoreSSLVerify) {
        this.ignoreSSLVerify = ignoreSSLVerify;
    }

    /**
     * 设置SSL协议
     *
     * @param sslProtocol SSL协议，默认TLS
     */
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    /**
     * 设置需要重定向的状态码
     *
     * @param redirectStatus 需要重定向的状态码
     */
    public void setRedirectStatus(Integer[] redirectStatus) {
        this.redirectStatus = redirectStatus;
    }

    /**
     * 设置需要重定向的条件，此处支持SpEL表达式
     *
     * @param redirectCondition 需要重定向的条件，此处支持SpEL表达式
     */
    public void setRedirectCondition(String redirectCondition) {
        this.redirectCondition = redirectCondition;
    }

    /**
     * 设置重定向地址获取表达式
     *
     * @param redirectLocation 重定向地址获取表达式
     */
    public void setRedirectLocation(String redirectLocation) {
        this.redirectLocation = redirectLocation;
    }

    /**
     * 设置连接池的最大连接数
     *
     * @param maxIdleConnections 最大连接数
     */
    public void setMaxIdleConnections(Integer maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    /**
     * 设置连接池空闲连接的保活时间
     *
     * @param keepAliveDuration 连接池空闲连接的保活时间
     */
    public void setKeepAliveDuration(Long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    /**
     * 设置连接池空闲连接的保活时间单位
     *
     * @param keepAliveTimeUnit 连接池空闲连接的保活时间单位
     */
    public void setKeepAliveTimeUnit(TimeUnit keepAliveTimeUnit) {
        this.keepAliveTimeUnit = keepAliveTimeUnit;
    }

    /**
     * 设置使用HTTP执行器的SpringBean的名称
     *
     * @param httpExecutorBean HTTP执行器的SpringBean的名称
     */
    public void setHttpExecutorBean(String httpExecutorBean) {
        this.httpExecutorBean = httpExecutorBean;
    }

    /**
     * 设置是否开启Cookie管理功能
     *
     * @param enableCookieManage 是否开启Cookie管理功能
     */
    public void setEnableCookieManage(boolean enableCookieManage) {
        this.enableCookieManage = enableCookieManage;
    }

    /**
     * 设置CookieStore对象生成器
     *
     * @param cookieStoreGenerate CookieStore对象生成器
     */
    public void setCookieStoreGenerate(SimpleGenerateEntry<CookieStore> cookieStoreGenerate) {
        this.cookieStoreGenerate = cookieStoreGenerate;
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
     * 获取拦截器生成器（数组）
     *
     * @return 拦截器生成器（数组）
     */
    public InterceptorGenerateEntry[] getInterceptorGenerates() {
        return interceptorGenerates;
    }

    /**
     * 获取异常处理器生成器
     *
     * @return 异常处理器生成器
     */
    public GenerateEntry<HttpExceptionHandle> getExceptionHandleGenerate() {
        return exceptionHandleGenerate;
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
     * 获取公共的multipart/form-data参数(简单参数)
     *
     * @return 公共的multipart/form-data参数(简单参数)
     */
    public ConfigurationMap getMultipartFormSimpleParams() {
        return multipartFormSimpleParams;
    }

    /**
     * 获取公共的multipart/form-data参数(资源参数)
     *
     * @return 公共的multipart/form-data参数(资源参数)
     */
    public ConfigurationMap getMultipartFormResourceParams() {
        return multipartFormResourceParams;
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
     * 是否开启了打印注解信息的功能
     *
     * @return 是否开启了打印注解信息的功能
     */
    public boolean isEnablePrintAnnotationInfo() {
        return enablePrintAnnotationInfo;
    }

    /**
     * 是否开启了打印参数信息的功能
     *
     * @return 是否开启了打印参数信息的功能
     */
    public boolean isEnablePrintArgsInfo() {
        return enablePrintArgsInfo;
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

    /**
     * 是否开启了自动重定向功能
     *
     * @return 是否开启了自动重定向功能
     */
    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    /**
     * 是否忽略SSL证书认证功能
     *
     * @return 是否忽略SSL证书认证功能
     */
    public boolean isIgnoreSSLVerify() {
        return ignoreSSLVerify;
    }

    /**
     * 获取SSL协议
     *
     * @return SSL协议
     */
    public String getSslProtocol() {
        return sslProtocol;
    }

    /**
     * 获取需要重定向的状态码
     *
     * @return 需要重定向的状态码
     */
    public Integer[] getRedirectStatus() {
        return redirectStatus;
    }

    /**
     * 获取需要重定向的条件表达式
     *
     * @return 需要重定向的条件表达式
     */
    public String getRedirectCondition() {
        return redirectCondition;
    }

    /**
     * 获取需要重定向的地址表达式
     *
     * @return 需要重定向的地址表达式
     */
    public String getRedirectLocation() {
        return redirectLocation;
    }

    /**
     * 连接池的最大连接数
     *
     * @return 连接池的最大连接数
     */
    public Integer getMaxIdleConnections() {
        return maxIdleConnections;
    }

    /**
     * 连接池空闲连接的保活时间
     *
     * @return 连接池空闲连接的保活时间
     */
    public Long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * 连接池空闲连接的保活时间单位
     *
     * @return 连接池空闲连接的保活时间单位
     */
    public TimeUnit getKeepAliveTimeUnit() {
        return keepAliveTimeUnit;
    }

    /**
     * HTTP执行器的SpringBean的名称
     *
     * @return HTTP执行器的SpringBean的名称
     */
    public String getHttpExecutorBean() {
        return httpExecutorBean;
    }

    /**
     * 是否开启Cookie管理功能
     *
     * @return 是否开启Cookie管理功能
     */
    public boolean isEnableCookieManage() {
        return enableCookieManage;
    }

    /**
     * 获取CookieStore对象生成器
     *
     * @return CookieStore对象生成器
     */
    public SimpleGenerateEntry<CookieStore> getCookieStoreGenerate() {
        return cookieStoreGenerate;
    }
}
