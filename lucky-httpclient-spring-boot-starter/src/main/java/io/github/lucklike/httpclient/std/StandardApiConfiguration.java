package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.core.meta.RequestMethod;
import com.luckyframework.httpclient.proxy.configapi.Condition;
import com.luckyframework.httpclient.proxy.configapi.MultipartFormData;
import com.luckyframework.httpclient.proxy.configapi.SSLConf;
import com.luckyframework.httpclient.proxy.configapi.SpELImportConf;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.httpclient.proxy.generator.GeneratedJavaCodeConfiguration;
import io.github.lucklike.httpclient.config.RetryConfiguration;
import io.github.lucklike.httpclient.config.mock.MockResult;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 标准 API 配置
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/9 23:38
 */
public class StandardApiConfiguration {

    /**
     * 接口路径
     */
    private String path;

    /**
     * 接口表述信息
     */
    private String desc;

    /**
     * 请求方法，默认 POST
     */
    private RequestMethod method;

    /**
     * 连接超时时间
     */
    private Integer connectTimeout;

    /**
     * 读取超时时间
     */
    private Integer readTimeout;

    /**
     * 写入超时时间，OkHttp执行器特有
     */
    private Integer writeTimeout;

    /**
     * 整体超时时间，OkHttp执行器特有
     */
    private Integer callTimeout;

    /**
     * 获取链接的超时时间，HttpClient执行器特有
     */
    private Integer connectionRequestTimeout;

    /**
     * 请求头参数
     */
    private Map<String, Object> headerParams = new LinkedHashMap<>();

    /**
     * 路径请求参数
     */
    private Map<String, Object> pathParams = new LinkedHashMap<>();

    /**
     * Query请求参数
     */
    private Map<String, Object> queryParams = new LinkedHashMap<>();

    /**
     * application/x-www-form-urlencoded请求参数
     */
    private Map<String, Object> formParams = new LinkedHashMap<>();

    /**
     * multipart/form-data类型请求参数
     */
    @NestedConfigurationProperty
    private MultipartFormData multipartFormParams = new MultipartFormData();

    /**
     * 请求体
     */
    private String body;

    /**
     * 支持条件的请求头参数
     */
    @NestedConfigurationProperty
    private List<ConditionConfig> conditionHeaderParams = new ArrayList<>();

    /**
     * 支持条件的路径请求参数
     */
    @NestedConfigurationProperty
    private List<ConditionConfig> conditionPathParams = new ArrayList<>();

    /**
     * 支持条件的Query请求参数
     */
    @NestedConfigurationProperty
    private List<ConditionConfig> conditionQueryParams = new ArrayList<>();

    /**
     * 支持条件的application/x-www-form-urlencoded请求参数
     */
    @NestedConfigurationProperty
    private List<ConditionConfig> conditionFormParams = new ArrayList<>();

    /**
     * 支持条件的application/x-www-form-urlencoded请求参数
     */
    @NestedConfigurationProperty
    private List<ConditionMultipartFormData> conditionMultipartFormParams = new ArrayList<>();

    /**
     * 支持条件的请求体
     */
    @NestedConfigurationProperty
    private List<ConditionBody> conditionBody = new ArrayList<>();

    /**
     * 初始化参数
     */
    @NestedConfigurationProperty
    private InitBindParams initBindParams = new InitBindParams();

    /**
     * 额外的自定义请求参数
     */
    @NestedConfigurationProperty
    private AdditionalParams additionalParams = new AdditionalParams();

    /**
     * 转化元类型表达式，表达式结果必须为{@link ResolvableType}类型
     *
     * @see CommonFunctions#typeOf(Object, Object...)
     */
    private String metaType;

    /**
     * 强制指定响应体的Content-Type
     */
    private String responseContentType;

    /**
     * 支持条件表达式的强制指定响应体的Content-Type
     */
    @NestedConfigurationProperty
    private List<ConditionRespContentType> conditionRespContentType = new ArrayList<>();

    /**
     * 支持条件表达式的转化元类型表达式
     */
    @NestedConfigurationProperty
    private List<ConditionMetaType> conditionMetaType = new ArrayList<>();

    /**
     * 响应转换表达式
     */
    private String resultConvert;

    /**
     * 条件转换配置
     */
    @NestedConfigurationProperty
    private List<Condition> conditionConvert = new ArrayList<>();

    /**
     * 重试相关配置
     */
    @NestedConfigurationProperty
    private RetryConfiguration retryConfig;

    /**
     * SSL相关的配置
     */
    @NestedConfigurationProperty
    private SSLConf sslConfig;

    /**
     * Mock相关配置
     */
    @NestedConfigurationProperty
    private MockResult mockConfig;

    /**
     * 当前上下文级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    @NestedConfigurationProperty
    private SpELImportConf spelImport;

    /**
     * {@link MethodMetaContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    @NestedConfigurationProperty
    private SpELImportConf methodMetaSpelImport;

    /**
     * 用于生成响应对象对应的JavaBean的配置
     */
    @NestedConfigurationProperty
    private GeneratedJavaCodeConfiguration generateResponseJavaBean;

    /**
     * 接口路径
     *
     * @return 接口路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 接口路径
     *
     * @param path 接口路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 设置的请求头参数
     *
     * @param headerParams 公共的请求头参数
     */
    public void setHeaderParams(Map<String, Object> headerParams) {
        this.headerParams = headerParams;
    }

    /**
     * 设置的路径参数
     *
     * @param pathParams 公共的路径参数
     */
    public void setPathParams(Map<String, Object> pathParams) {
        this.pathParams = pathParams;
    }

    /**
     * 设置的URL参数
     *
     * @param queryParams 公共的URL参数
     */
    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
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
     * 获取接口描述信息
     *
     * @return 接口描述信息
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 设置接口描述信息
     *
     * @param desc 接口描述信息
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 请求方法，默认 POST
     *
     * @return 请求方法
     */
    public RequestMethod getMethod() {
        return method;
    }

    /**
     * 设置请求方法
     *
     * @param method 请求方法
     */
    public void setMethod(RequestMethod method) {
        this.method = method;
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
        this.formParams = formParams;
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
    public InitBindParams getInitBindParams() {
        return initBindParams;
    }

    /**
     * 设置初始化参数
     *
     * @param initBindParams 初始化参数
     */
    public void setInitBindParams(InitBindParams initBindParams) {
        this.initBindParams = initBindParams;
    }

    /**
     * 额外的自定义请求参数
     *
     * @return 额外的自定义请求参数
     */
    public AdditionalParams getAdditionalParams() {
        return additionalParams;
    }

    /**
     * 设置额外的自定义请求参数
     *
     * @param additionalParams 额外的自定义请求参数
     */
    public void setAdditionalParams(AdditionalParams additionalParams) {
        this.additionalParams = additionalParams;
    }

    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeout 连接超时时间
     */
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 获取读取超时时间
     *
     * @return 读取超时时间
     */
    public Integer getReadTimeout() {
        return readTimeout;
    }

    /**
     * 设置读取超时时间
     *
     * @param readTimeout 读取超时时间
     */
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * 获取写入超时时间，OkHttp执行器特有
     *
     * @return 写入超时时间，OkHttp执行器特有
     */
    public Integer getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * 设置写入超时时间，OkHttp执行器特有
     *
     * @param writeTimeout 写入超时时间，OkHttp执行器特有
     */
    public void setWriteTimeout(Integer writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     * 获取整体超时时间，OkHttp执行器特有
     *
     * @return 整体超时时间，OkHttp执行器特有
     */
    public Integer getCallTimeout() {
        return callTimeout;
    }

    /**
     * 设置整体超时时间，OkHttp执行器特有
     *
     * @param callTimeout 整体超时时间，OkHttp执行器特有
     */
    public void setCallTimeout(Integer callTimeout) {
        this.callTimeout = callTimeout;
    }

    /**
     * 获取链接的超时时间，HttpClient执行器特有
     *
     * @return 获取链接的超时时间，HttpClient执行器特有
     */
    public Integer getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * 设置获取链接的超时时间，HttpClient执行器特有
     *
     * @param connectionRequestTimeout 获取链接的超时时间，HttpClient执行器特有
     */
    public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    /**
     * 获取请求体
     *
     * @return 请求体
     */
    public String getBody() {
        return body;
    }

    /**
     * 设置请求体
     *
     * @param body 请求体
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 强制指定响应体的Content-Type
     *
     * @return 强制指定响应体的Content-Type
     */
    public String getResponseContentType() {
        return responseContentType;
    }

    /**
     * 设置强制指定响应体的Content-Type
     *
     * @param responseContentType 强制指定响应体的Content-Type
     */
    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    /**
     * 支持条件表达式的强制指定响应体的Content-Type
     *
     * @return 支持条件表达式的强制指定响应体的Content-Type
     */
    public List<ConditionRespContentType> getConditionRespContentType() {
        return conditionRespContentType;
    }

    /**
     * 设置支持条件表达式的强制指定响应体的Content-Type
     *
     * @param conditionRespContentType 支持条件表达式的强制指定响应体的Content-Type
     */
    public void setConditionRespContentType(List<ConditionRespContentType> conditionRespContentType) {
        this.conditionRespContentType = conditionRespContentType;
    }

    /**
     * 获取转化元类型表达式，表达式结果必须为{@link ResolvableType}类型
     *
     * @return 转化元类型表达式
     * @see CommonFunctions#typeOf(Object, Object...)
     */
    public String getMetaType() {
        return metaType;
    }

    /**
     * 设置转化元类型表达式，表达式结果必须为{@link ResolvableType}类型
     *
     * @param metaType 转化元类型表达式
     * @see CommonFunctions#typeOf(Object, Object...)
     */
    public void setMetaType(String metaType) {
        this.metaType = metaType;
    }

    /**
     * 获取支持条件表达式的转化元类型表达式
     *
     * @return 支持条件表达式的转化元类型表达式
     */
    public List<ConditionMetaType> getConditionMetaType() {
        return conditionMetaType;
    }

    /**
     * 设置支持条件表达式的转化元类型表达式
     *
     * @param conditionMetaType 支持条件表达式的转化元类型表达式
     */
    public void setConditionMetaType(List<ConditionMetaType> conditionMetaType) {
        this.conditionMetaType = conditionMetaType;
    }

    /**
     * 获取响应转换表达式
     *
     * @return 响应转换表达式
     */
    public String getResultConvert() {
        return resultConvert;
    }

    /**
     * 设置响应转换表达式
     *
     * @param resultConvert 响应转换表达式
     */
    public void setResultConvert(String resultConvert) {
        this.resultConvert = resultConvert;
    }

    /**
     * 获取条件转换配置
     *
     * @return 条件转换配置
     */
    public List<Condition> getConditionConvert() {
        return conditionConvert;
    }

    /**
     * 设置条件转换配置
     *
     * @param conditionConvert 条件转换配置
     */
    public void setConditionConvert(List<Condition> conditionConvert) {
        this.conditionConvert = conditionConvert;
    }

    /**
     * 获取支持条件的请求头参数
     *
     * @return 支持条件的请求头参数
     */
    public List<ConditionConfig> getConditionHeaderParams() {
        return conditionHeaderParams;
    }

    /**
     * 设置支持条件的请求头参数
     *
     * @param conditionHeaderParams 支持条件的请求头参数
     */
    public void setConditionHeaderParams(List<ConditionConfig> conditionHeaderParams) {
        this.conditionHeaderParams = conditionHeaderParams;
    }

    /**
     * 获取支持条件的路径请求参数
     *
     * @return 支持条件的路径请求参数
     */
    public List<ConditionConfig> getConditionPathParams() {
        return conditionPathParams;
    }

    /**
     * 设置支持条件的路径请求参数
     *
     * @param conditionPathParams 支持条件的路径请求参数
     */
    public void setConditionPathParams(List<ConditionConfig> conditionPathParams) {
        this.conditionPathParams = conditionPathParams;
    }

    /**
     * 获取支持条件的Query请求参数
     *
     * @return 支持条件的Query请求参数
     */
    public List<ConditionConfig> getConditionQueryParams() {
        return conditionQueryParams;
    }

    /**
     * 设置支持条件的Query请求参数
     *
     * @param conditionQueryParams 支持条件的Query请求参数
     */
    public void setConditionQueryParams(List<ConditionConfig> conditionQueryParams) {
        this.conditionQueryParams = conditionQueryParams;
    }

    /**
     * 获取支持条件的application/x-www-form-urlencoded请求参数
     *
     * @return 支持条件的application/x-www-form-urlencoded请求参数
     */
    public List<ConditionConfig> getConditionFormParams() {
        return conditionFormParams;
    }

    /**
     * 设置支持条件的application/x-www-form-urlencoded请求参数
     *
     * @param conditionFormParams 支持条件的application/x-www-form-urlencoded请求参数
     */
    public void setConditionFormParams(List<ConditionConfig> conditionFormParams) {
        this.conditionFormParams = conditionFormParams;
    }

    /**
     * 获取支持条件的application/x-www-form-urlencoded请求参数
     *
     * @return 支持条件的application/x-www-form-urlencoded请求参数
     */
    public List<ConditionMultipartFormData> getConditionMultipartFormParams() {
        return conditionMultipartFormParams;
    }

    /**
     * 设置支持条件的application/x-www-form-urlencoded请求参数
     *
     * @param conditionMultipartFormParams 支持条件的application/x-www-form-urlencoded请求参数
     */
    public void setConditionMultipartFormParams(List<ConditionMultipartFormData> conditionMultipartFormParams) {
        this.conditionMultipartFormParams = conditionMultipartFormParams;
    }

    /**
     * 获取支持条件的请求体
     *
     * @return 支持条件的请求体
     */
    public List<ConditionBody> getConditionBody() {
        return conditionBody;
    }

    /**
     * 设置支持条件的请求体
     *
     * @param conditionBody 支持条件的请求体
     */
    public void setConditionBody(List<ConditionBody> conditionBody) {
        this.conditionBody = conditionBody;
    }

    /**
     * 重试相关配置
     *
     * @return 重试相关配置
     */
    public RetryConfiguration getRetryConfig() {
        return retryConfig;
    }

    /**
     * 设置重试相关配置
     *
     * @param retryConfig 重试相关配置
     */
    public void setRetryConfig(RetryConfiguration retryConfig) {
        this.retryConfig = retryConfig;
    }

    /**
     * 获取SSL相关的配置
     *
     * @return SSL相关的配置
     */
    public SSLConf getSslConfig() {
        return sslConfig;
    }

    /**
     * 设置SSL相关的配置
     *
     * @param sslConfig SSL相关的配置
     */
    public void setSslConfig(SSLConf sslConfig) {
        this.sslConfig = sslConfig;
    }

    /**
     * 获取模拟相关配置
     *
     * @return 模拟相关配置
     */
    public MockResult getMockConfig() {
        return mockConfig;
    }

    /**
     * 设置模拟相关配置
     *
     * @param mockConfig 模拟相关配置
     */
    public void setMockConfig(MockResult mockConfig) {
        this.mockConfig = mockConfig;
    }

    /**
     * 当前上下文级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     *
     * @return 当前上下文级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    public SpELImportConf getSpelImport() {
        return spelImport;
    }

    /**
     * 设置当前上下文级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     *
     * @param spelImport 当前上下文级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    public void setSpelImport(SpELImportConf spelImport) {
        this.spelImport = spelImport;
    }

    /**
     * {@link MethodMetaContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     *
     * @return {@link MethodMetaContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    public SpELImportConf getMethodMetaSpelImport() {
        return methodMetaSpelImport;
    }

    /**
     * 设置{@link MethodMetaContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     *
     * @param methodMetaSpelImport {@link MethodMetaContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    public void setMethodMetaSpelImport(SpELImportConf methodMetaSpelImport) {
        this.methodMetaSpelImport = methodMetaSpelImport;
    }

    /**
     * 获取用于生成响应对象对应的JavaBean的配置
     *
     * @return 用于生成响应对象对应的JavaBean的配置
     */
    public GeneratedJavaCodeConfiguration getGenerateResponseJavaBean() {
        return generateResponseJavaBean;
    }

    /**
     * 设置用于生成响应对象对应的JavaBean的配置
     *
     * @param generateResponseJavaBean 用于生成响应对象对应的JavaBean的配置
     */
    public void setGenerateResponseJavaBean(GeneratedJavaCodeConfiguration generateResponseJavaBean) {
        this.generateResponseJavaBean = generateResponseJavaBean;
    }

    /**
     * 移除无效配置
     */
    public void removeNonEffectiveConfig() {
        conditionHeaderParams.removeIf(config -> !config.effective());
        conditionPathParams.removeIf(config -> !config.effective());
        conditionQueryParams.removeIf(config -> !config.effective());
        conditionFormParams.removeIf(config -> !config.effective());
        conditionMultipartFormParams.removeIf(config -> !config.effective());
        conditionBody.removeIf(config -> !config.effective());
        conditionConvert.removeIf(config -> !config.effective());
        conditionMetaType.removeIf(config -> !config.effective());
        conditionRespContentType.removeIf(config -> !config.effective());
    }
}
