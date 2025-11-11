package io.github.lucklike.httpclient.config;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.httpclient.core.meta.Version;
import com.luckyframework.httpclient.proxy.handle.HttpExceptionHandle;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

/**
 * HttpClientProxyObjectFactory配置类
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/8 11:35
 */
public class HttpClientProxyObjectFactoryConfiguration {


    /**
     * 对象创建器工厂
     */
    private ObjectCreatorFactory objectCreatorFactory;

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
     * HTTP版本
     */
    private Version httpVersion;

    /**
     * 公共请求头参数
     * <pre>
     *     1.直接配置的k-v为全局公用的参数
     *       header-params:
     *         key: value
     *     2.为某个接口或者接口系列配置特有参数
     *       例如：为io.github.lucklike.springboothttp.api.gitee.GiteeApi接口配置特有的header参数
     *       header-params:
     *         "[io.github.lucklike.springboothttp.api.gitee.GiteeApi]":
     *            key: value
     * </pre>
     */
    private final Map<String, Object> headerParams = new ConfigurationMap();

    /**
     * 公共路径请求参数
     * <pre>
     *     1.直接配置的k-v为全局公用的参数
     *       path-params:
     *         key: value
     *     2.为某个接口或者接口系列配置特有参数
     *       例如：为io.github.lucklike.springboothttp.api.gitee.GiteeApi接口配置特有的path参数
     *       path-params:
     *         "[io.github.lucklike.springboothttp.api.gitee.GiteeApi]":
     *            key: value
     * </pre>
     */
    private final Map<String, Object> pathParams = new ConfigurationMap();

    /**
     * 公共Query请求参数
     * <pre>
     *     1.直接配置的k-v为全局公用的参数
     *       query-params:
     *         key: value
     *     2.为某个接口或者接口系列配置特有参数
     *       例如：为io.github.lucklike.springboothttp.api.gitee.GiteeApi接口配置特有的query参数
     *       query-params:
     *         "[io.github.lucklike.springboothttp.api.gitee.GiteeApi]":
     *            key: value
     * </pre>
     */
    private final Map<String, Object> queryParams = new ConfigurationMap();

    /**
     * Http执行器相关额配置
     */
    @NestedConfigurationProperty
    private HttpExecutorConfiguration httpExecutor = new HttpExecutorConfiguration();

    /**
     * HTTP线程池配置
     */
    @NestedConfigurationProperty
    private HttpAsyncThreadPoolConfiguration asyncThreadPool = new HttpAsyncThreadPoolConfiguration();


    /**
     * SSL协议相关配置
     */
    @NestedConfigurationProperty
    private SSLConfiguration ssl = new SSLConfiguration();

    /**
     * HTTP异常处理器生成器信息
     */
    @NestedConfigurationProperty
    private GenerateEntry<HttpExceptionHandle> exceptionHandleGenerate;

    /**
     * 日志打印相关配置
     */
    @NestedConfigurationProperty
    private LoggerConfiguration logger = new LoggerConfiguration();

    /**
     * SpEL表达式相关的配置
     */
    @NestedConfigurationProperty
    private SpELConfiguration springEl = new SpELConfiguration();

    /**
     * 重定向相关的配置
     */
    @NestedConfigurationProperty
    private RedirectConfiguration redirect = new RedirectConfiguration();

    /**
     * 重试相关的配置
     */
    @NestedConfigurationProperty
    private RetryConfiguration retry = new RetryConfiguration();

    /**
     * Cookie管理器相关配置
     */
    @NestedConfigurationProperty
    private CookieManageConfiguration cookieManage = new CookieManageConfiguration();

    /**
     * 响应结果转换相关的配置
     */
    @NestedConfigurationProperty
    private ResponseConvertConfiguration responseConvert = new ResponseConvertConfiguration();

    /**
     * 插件相关配置
     */
    private Class<? extends ProxyPlugin>[] plugins;

    /**
     * 参数转换器相关配置
     */
    private ParameterConvertConfig[] parameterConverts;

    //------------------------------------------------------------------------------------------------
    //                                Setter methods
    //------------------------------------------------------------------------------------------------

    /**
     * 设置{@link ObjectCreatorFactory 对象创建器工厂}
     *
     * @param objectCreatorFactory 对象创建器工厂
     */
    public void setObjectCreatorFactory(ObjectCreatorFactory objectCreatorFactory) {
        this.objectCreatorFactory = objectCreatorFactory;
    }

    /**
     * Http执行器相关的设置
     *
     * @param httpExecutor Http执行器配置
     */
    public void setHttpExecutor(HttpExecutorConfiguration httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    /**
     * 设置HTTP异步线程池相关的参数
     *
     * @param asyncThreadPool HTTP异步线程池相关的参数
     */
    public void setAsyncThreadPool(HttpAsyncThreadPoolConfiguration asyncThreadPool) {
        this.asyncThreadPool = asyncThreadPool;
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
     * 设置 HTTP 版本
     *
     * @param httpVersion HTTP 版本
     */
    public void setHttpVersion(Version httpVersion) {
        this.httpVersion = httpVersion;
    }

    /**
     * 设置公共的请求头参数
     * <pre>
     *     1.直接配置的k-v为全局公用的参数
     *     2.为某个接口或者接口系列配置特有参数
     *     {@code
     *      例如：为io.github.lucklike.springboothttp.api.gitee.GiteeApi接口配置特有的header参数
     *      lucky:
     *        httpclient:
     *           header-params:
     *              "[io.github.lucklike.springboothttp.api.gitee.GiteeApi]":
     *                   key: value
     *
     *     }
     * </pre>
     *
     * @param headerParams 公共的请求头参数
     */
    public void setHeaderParams(Map<String, Object> headerParams) {
        this.headerParams.putAll(headerParams);
    }

    /**
     * 设置公共的路径参数
     *
     * @param pathParams 公共的路径参数
     */
    public void setPathParams(Map<String, Object> pathParams) {
        this.pathParams.putAll(pathParams);
    }

    /**
     * 设置公共的URL参数
     *
     * @param queryParams 公共的URL参数
     */
    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams.putAll(queryParams);
    }

    /**
     * 设置SSL协议相关的配置
     *
     * @param ssl SSL协议相关的配置
     */
    public void setSsl(SSLConfiguration ssl) {
        this.ssl = ssl;
    }


    /**
     * 设置日志相关的配置
     *
     * @param logger 日志相关的配置
     */
    public void setLogger(LoggerConfiguration logger) {
        this.logger = logger;
    }

    /**
     * 设置SpEL相关的配置
     *
     * @param springEl SpEL相关的配置
     */
    public void setSpringEl(SpELConfiguration springEl) {
        this.springEl = springEl;
    }

    /**
     * 设置重定向相关的配置
     *
     * @param redirect 重定向相关的配置
     */
    public void setRedirect(RedirectConfiguration redirect) {
        this.redirect = redirect;
    }

    /**
     * 设置重试相关的配置
     *
     * @param retry 重试相关的配置
     */
    public void setRetry(RetryConfiguration retry) {
        this.retry = retry;
    }

    /**
     * 设置Cookie管理器相关配置
     *
     * @param cookieManage Cookie管理器相关配置
     */
    public void setCookieManage(CookieManageConfiguration cookieManage) {
        this.cookieManage = cookieManage;
    }

    /**
     * 设置响应结果转换相关的配置
     *
     * @param responseConvert 响应结果转换相关的配置
     */
    public void setResponseConvert(ResponseConvertConfiguration responseConvert) {
        this.responseConvert = responseConvert;
    }

    /**
     * 设置插件
     *
     * @param plugins 插件集合
     */
    public void setPlugins(Class<? extends ProxyPlugin>[] plugins) {
        this.plugins = plugins;
    }

    /**
     * 设置参数转换器
     *
     * @param parameterConverts 参数转换器
     */
    public void setParameterConverts(ParameterConvertConfig[] parameterConverts) {
        this.parameterConverts = parameterConverts;
    }

    //------------------------------------------------------------------------------------------------
    //                                Getter methods
    //------------------------------------------------------------------------------------------------

    /**
     * 获取{@link ObjectCreatorFactory 对象创建器工厂}
     *
     * @return 对象创建器工厂
     */
    public ObjectCreatorFactory getObjectCreatorFactory() {
        return objectCreatorFactory;
    }

    /**
     * 获取HTTP执行器相关的配置
     *
     * @return HTTP执行器相关的配置
     */
    public HttpExecutorConfiguration getHttpExecutor() {
        return httpExecutor;
    }

    /**
     * 获取HTTP异步线程池相关的参数
     *
     * @return HTTP异步线程池相关的参数
     */
    public HttpAsyncThreadPoolConfiguration getAsyncThreadPool() {
        return asyncThreadPool;
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
     * 获取 HTTP 版本
     *
     * @return HTTP 版本
     */
    public Version getHttpVersion() {
        return httpVersion;
    }

    /**
     * 获取公共的请求头参数
     *
     * @return 公共的请求头参数
     */
    public Map<String, Object> getHeaderParams() {
        return headerParams;
    }

    /**
     * 获取公共的路径参数
     *
     * @return 公共的路径参数
     */
    public Map<String, Object> getPathParams() {
        return pathParams;
    }

    /**
     * 获取公共的URL参数
     *
     * @return 公共的URL参数
     */
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    /**
     * 获取SSL相关的配置
     *
     * @return SSL相关的配置
     */
    public SSLConfiguration getSsl() {
        return ssl;
    }

    /**
     * 获取日志相关的配置
     *
     * @return 日志相关的配置
     */
    public LoggerConfiguration getLogger() {
        return logger;
    }

    /**
     * 获取SpEL相关的配置
     *
     * @return SpEL相关的配置
     */
    public SpELConfiguration getSpringEl() {
        return springEl;
    }

    /**
     * 获取重定向相关的配置
     *
     * @return 重定向相关的配置
     */
    public RedirectConfiguration getRedirect() {
        return redirect;
    }

    /**
     * 获取重试相关的配置
     *
     * @return 重试相关的配置
     */
    public RetryConfiguration getRetry() {
        return retry;
    }


    /**
     * 获取Cookie管理器相关配置
     *
     * @return Cookie管理器相关配置
     */
    public CookieManageConfiguration getCookieManage() {
        return cookieManage;
    }

    /**
     * 获取响应结果转换相关的配置
     *
     * @return 响应结果转换相关的配置
     */
    public ResponseConvertConfiguration getResponseConvert() {
        return responseConvert;
    }

    /**
     * 获取所有的插件配置
     *
     * @return 所有的插件配置
     */
    public Class<? extends ProxyPlugin>[] getPlugins() {
        return plugins;
    }

    /**
     * 获取参数转换器
     *
     * @return 参数转换器
     */
    public ParameterConvertConfig[] getParameterConverts() {
        return parameterConverts;
    }
}
