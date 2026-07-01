package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.meta.BodyObject;
import com.luckyframework.httpclient.core.meta.ContentType;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.RequestMethod;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.core.ssl.KeyStoreInfo;
import com.luckyframework.httpclient.core.ssl.SSLSocketFactoryWrap;
import com.luckyframework.httpclient.core.ssl.SSLUtils;
import com.luckyframework.httpclient.core.ssl.TrustAllHostnameVerifier;
import com.luckyframework.httpclient.proxy.configapi.Condition;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationParserException;
import com.luckyframework.httpclient.proxy.configapi.MultipartFormData;
import com.luckyframework.httpclient.proxy.configapi.SSLConf;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.convert.ActivelyThrownException;
import com.luckyframework.httpclient.proxy.function.ResourceFunctions;
import com.luckyframework.httpclient.proxy.handle.DefaultHttpExceptionHandle;
import com.luckyframework.httpclient.proxy.retry.RetryDeciderContext;
import com.luckyframework.httpclient.proxy.retry.RunBeforeRetryContext;
import com.luckyframework.httpclient.proxy.spel.SpELVariate;
import com.luckyframework.httpclient.proxy.ssl.SSLSocketFactoryBuilder;
import io.github.lucklike.httpclient.config.RetryConfiguration;
import io.github.lucklike.httpclient.retry.ConfigurationBackoffWaitingBeforeRetryContext;
import io.github.lucklike.httpclient.retry.ConfigurationRetryDeciderContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.luckyframework.httpclient.core.executor.Constant.HTTPCLIENT_PM_CONNECTION_REQUEST_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.OKHTTP_PM_CALL_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.OKHTTP_PM_WRITE_TIMEOUT;
import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$RETRY_COUNT$__;
import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$RETRY_DECIDER_FUNCTION$__;
import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$RETRY_RUN_BEFORE_RETRY_FUNCTION$__;
import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$RETRY_SWITCH$__;
import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$RETRY_TASK_NAME$__;

/**
 * 标准的生命周期管理器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/9 22:10
 */
public class StandardLifeCycleManager implements LifeCycleManager {

    private final DefaultHttpExceptionHandle exceptionHandle = new DefaultHttpExceptionHandle();

    @Override
    public String buildBaseUrl(MethodContext mc, StandardHttpClientConfiguration config) throws Exception {
        return config.getUrl();
    }

    @Override
    public void methodMetaContentInit(MethodMetaContext mec, StandardApiConfiguration config) {
        // 重试相关配置
        retrySetter(mec, config);
    }

    @Override
    public void requestInitCompleted(MethodContext mc, Request request, StandardApiConfiguration apiConfig) throws Exception {
        // 设置Api信息
        setApiInfo(mc, apiConfig);
        // 设置URL的Path部分
        setUrlPath(request, apiConfig);
        // 设置请求方法
        setRequestMethod(request, apiConfig);
        // 填充固定的请求参数
        fillFixedRequestParameter(mc, request, apiConfig);
        // 填充条件请求参数
        fillConditionRequestParameter(mc, request, apiConfig);
        // 填充请求体参数
        fillRequestBodyParameter(mc, request, apiConfig);
        //设置超时时间
        timeoutSetter(request, apiConfig);
        // SSL参数设置
        sslSetter(mc, request, apiConfig);
    }


    @Override
    public ResolvableType getResponseMetaType(MethodContext mc, StandardApiConfiguration apiConfig) throws Exception {
        for (ConditionMetaType conditionMetaType : apiConfig.getConditionMetaType()) {
            if (mc.parseExpression(conditionMetaType.getCondition(), boolean.class)) {
                return mc.parseExpression(conditionMetaType.getMetaType(), ResolvableType.class);
            }
        }
        String metaType = apiConfig.getMetaType();
        if (StringUtils.hasText(metaType)) {
            return mc.parseExpression(metaType, ResolvableType.class);
        }
        return ResolvableType.forClass(Object.class);
    }

    @Override
    public String mandatoryDesignationResponseContentType(MethodContext mc, StandardApiConfiguration apiConfig) {
        for (ConditionRespContentType conditionRespContentType : apiConfig.getConditionRespContentType()) {
            if (mc.parseExpression(conditionRespContentType.getCondition(), boolean.class)) {
                return mc.parseExpression(conditionRespContentType.getResponseContentType(), String.class);
            }
        }
        String responseContentType = apiConfig.getResponseContentType();
        if (StringUtils.hasText(responseContentType)) {
            return mc.parseExpression(responseContentType, String.class);
        }
        return "";
    }

    @Override
    public Object resultConvert(MethodContext mc, Response response, StandardApiConfiguration apiConfig) throws Exception {
        // 处理条件转换器配置
        for (Condition condition : apiConfig.getConditionConvert()) {
            String assertion = condition.getAssertion();
            if (StringUtils.hasText(assertion) && mc.parseExpression(assertion, boolean.class)) {

                // 响应结果转换
                String result = condition.getResult();
                if (StringUtils.hasText(result)) {
                    return mc.parseExpression(result, mc.getResultType());
                }

                // 异常处理
                String exception = condition.getException();
                if (StringUtils.hasText(exception)) {
                    Object exObj = mc.parseExpression(exception);
                    if (exObj instanceof Throwable) {
                        throw new ActivelyThrownException((Throwable) exObj);
                    }
                    throw new ActivelyThrownException(String.valueOf(exObj));
                }
            }
        }

        // 处理结果表达式
        String resultConvert = apiConfig.getResultConvert();
        if (StringUtils.hasText(resultConvert)) {
            return mc.parseExpression(resultConvert, mc.getResultType());
        }

        // 没有进行任何配置时
        return response.getEntity(mc.getResultType());
    }

    @Override
    public Object exceptionHandler(MethodContext mc, Request request, Throwable th, StandardApiConfiguration apiConfig) throws Throwable {
        for (ExceptionHandlerConfig exceptionHandlerConfig : apiConfig.getExceptionHandlerConfigs()) {
            if (canExHandler(mc, th, exceptionHandlerConfig)) {
                return exHandler(mc, request, th, exceptionHandlerConfig);
            }
        }
        return exceptionHandle.exceptionHandler(mc, request, th);
    }


    private boolean canExHandler(MethodContext mc, Throwable th, ExceptionHandlerConfig exceptionHandlerConfig) {
        String condition = exceptionHandlerConfig.getCondition();
        if (StringUtils.hasText(condition)) {
            return mc.parseExpression(condition, boolean.class);
        }

        Set<Class<? extends Throwable>> exceptionClasses = exceptionHandlerConfig.getExceptionClasses();
        if (ContainerUtils.isNotEmptyCollection(exceptionClasses)) {
            ExceptionHandlerConfig.Compare exceptionCompare = exceptionHandlerConfig.getExceptionCompare();
            if (exceptionCompare == ExceptionHandlerConfig.Compare.EQUALS) {
                for (Class<? extends Throwable> exceptionClass : exceptionClasses) {
                    if (Objects.equals(exceptionClass, th.getClass())) {
                        return true;
                    }
                }
                return false;
            }

            for (Class<? extends Throwable> exceptionClass : exceptionClasses) {
                if (exceptionClass.isInstance(th)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    public Object exHandler(MethodContext mc, Request request, Throwable th, ExceptionHandlerConfig exceptionHandlerConfig) throws Throwable {
        List<String> running = exceptionHandlerConfig.getRunning();
        if (ContainerUtils.isNotEmptyCollection(running)) {
            for (String ex : running) {
                mc.parseExpression(ex);
            }
        }

        String result = exceptionHandlerConfig.getResult();
        if (StringUtils.hasText(result) && !mc.isVoidMethod()) {
            return mc.parseExpression(result, mc.getResultResolvableType());
        }

        return exceptionHandle.exceptionHandler(mc, request, th);
    }

    /**
     * 设置请求方法
     *
     * @param request   请求对象
     * @param apiConfig API配置
     */
    protected void setRequestMethod(Request request, StandardApiConfiguration apiConfig) {
        // 存在 method 配置
        if (apiConfig.getMethod() != null) {
            request.setRequestMethod(apiConfig.getMethod());
        }
        // 不存在 method 配置，且请求对象没有设置 method 时给默认值 POST
        else if (request.getRequestMethod() == RequestMethod.NON) {
            request.setRequestMethod(RequestMethod.POST);
        }
    }

    /**
     * 填充固定请求请求参数
     *
     * @param mc        方法上下文
     * @param request   请求对象
     * @param apiConfig API配置
     */
    protected void fillFixedRequestParameter(MethodContext mc, Request request, StandardApiConfiguration apiConfig) {
        // Query param setter
        setParameter(mc, request, apiConfig.getQueryParams(), req -> req.getRequest().addQueryParameter(req.getName(), req.getValue()));

        // Path param setter
        setParameter(mc, request, apiConfig.getPathParams(), req -> req.getRequest().addPathParameter(req.getName(), req.getValue()));

        // Header param setter
        setParameter(mc, request, apiConfig.getHeaderParams(), req -> req.getRequest().setHeader(req.getName(), req.getValue()));

        // Form param setter
        setParameter(mc, request, apiConfig.getFormParams(), req -> req.getRequest().addFormParameter(req.getName(), req.getValue()));

        // MultipartFormData param setter
        setMultipartFormData(mc, request, apiConfig.getMultipartFormParams());
    }

    /**
     * 填充条件请求参数
     *
     * @param mc        方法上下文
     * @param request   请求对象
     * @param apiConfig API配置
     */
    protected void fillConditionRequestParameter(MethodContext mc, Request request, StandardApiConfiguration apiConfig) {
        // Condition query param setter
        setConditionParameter(mc, request, apiConfig.getConditionQueryParams(), req -> req.getRequest().addQueryParameter(req.getName(), req.getValue()));

        // Condition path param setter
        setConditionParameter(mc, request, apiConfig.getConditionPathParams(), req -> req.getRequest().addPathParameter(req.getName(), req.getValue()));

        // Condition header param setter
        setConditionParameter(mc, request, apiConfig.getConditionHeaderParams(), req -> req.getRequest().setHeader(req.getName(), req.getValue()));

        // Condition form param setter
        setConditionParameter(mc, request, apiConfig.getConditionFormParams(), req -> req.getRequest().addFormParameter(req.getName(), req.getValue()));

        // Condition multipartFormData param setter
        setConditionMultipartFormData(mc, request, apiConfig.getConditionMultipartFormParams());
    }

    /**
     * 填充请求体参数
     *
     * @param mc        方法上下文
     * @param request   请求对象
     * @param apiConfig 配置信息
     */
    protected void fillRequestBodyParameter(MethodContext mc, Request request, StandardApiConfiguration apiConfig) {

        // 条件 Body
        List<ConditionBody> conditionBodyList = apiConfig.getConditionBody();
        for (ConditionBody conditionBody : conditionBodyList) {
            String condition = conditionBody.getCondition();
            if (StringUtils.hasText(condition) && mc.parseExpression(condition, boolean.class)) {
                setRequestBody(mc, request, conditionBody.getBody());
                return;
            }
        }

        // 通用 Body
        setRequestBody(mc, request, apiConfig.getBody());
    }


    /**
     * SSL相关配置
     *
     * @param context   方法上下文实例
     * @param request   当前请求实例
     * @param apiConfig 当前API配置
     */
    private void sslSetter(MethodContext context, Request request, StandardApiConfiguration apiConfig) {
        SSLConf ssl = apiConfig.getSslConfig();
        if (ssl != null && Objects.equals(Boolean.TRUE, ssl.getEnable())) {

            // HostnameVerifier
            HostnameVerifier hostnameVerifier = StringUtils.hasText(ssl.getHostnameVerifier()) ? context.parseExpression(ssl.getHostnameVerifier(), HostnameVerifier.class) : TrustAllHostnameVerifier.DEFAULT_INSTANCE;

            // SSLSocketFactory
            SSLSocketFactory sslSocketFactory;
            if (StringUtils.hasText(ssl.getSslSocketFactory())) {
                sslSocketFactory = context.parseExpression(ssl.getSslSocketFactory(), SSLSocketFactory.class);
            } else {
                KeyStoreInfo keyStoreInfo = ssl.getKeyStoreInfo();
                KeyStoreInfo trustStoreInfo = ssl.getTrustStoreInfo();

                String keyStore = ssl.getKeyStore();
                String trustStore = ssl.getTrustStore();
                if (keyStoreInfo == null) {
                    keyStoreInfo = SSLSocketFactoryBuilder.getKeyStoreInfo(context, keyStore);
                }
                if (trustStoreInfo == null) {
                    trustStoreInfo = SSLSocketFactoryBuilder.getKeyStoreInfo(context, trustStore);
                }
                sslSocketFactory = new SSLSocketFactoryWrap(SSLUtils.createSSLContext(ssl.getProtocol(), keyStoreInfo, trustStoreInfo));
            }
            request.setHostnameVerifier(hostnameVerifier);
            request.setSSLSocketFactory(sslSocketFactory);
        }
    }


    /**
     * 设置重试相关的配置
     *
     * @param context   方法上下文实例
     * @param apiConfig 当前API配置
     */
    private void retrySetter(MethodMetaContext context, StandardApiConfiguration apiConfig) {
        RetryConfiguration retryConfig = apiConfig.getRetryConfig();
        if (retryConfig != null && Objects.equals(Boolean.TRUE, retryConfig.isEnable())) {
            SpELVariate contextVar = context.getContextVar();

            contextVar.addVariable(__$RETRY_SWITCH$__, true);

            String taskName = retryConfig.getTaskNameFormat();
            if (StringUtils.hasText(taskName)) {
                contextVar.addVariable(__$RETRY_TASK_NAME$__, taskName);
            }

            contextVar.addVariable(__$RETRY_COUNT$__, retryConfig.getCount());
            Function<MethodContext, RunBeforeRetryContext<?>> beforeRetryFunction = c -> new ConfigurationBackoffWaitingBeforeRetryContext(retryConfig);
            Function<MethodContext, RetryDeciderContext<?>> deciderFunction = c -> new ConfigurationRetryDeciderContext(retryConfig);

            contextVar.addVariable(__$RETRY_RUN_BEFORE_RETRY_FUNCTION$__, beforeRetryFunction);
            contextVar.addVariable(__$RETRY_DECIDER_FUNCTION$__, deciderFunction);
        }
    }

    /**
     * 设置API描述信息
     *
     * @param mc        方法上下文
     * @param apiConfig 配置信息
     */
    private void setApiInfo(MethodContext mc, StandardApiConfiguration apiConfig) {
        if (StringUtils.hasText(apiConfig.getDesc())) {
            mc.getApiDescribe().setName(apiConfig.getDesc());
        }
    }

    /**
     * 设置URL的Path部分
     *
     * @param request   请求对象
     * @param apiConfig 配置信息
     */
    protected void setUrlPath(Request request, StandardApiConfiguration apiConfig) {
        request.setPath(apiConfig.getPath());
    }

    /**
     * 超时时间设置
     *
     * @param request   请求对象
     * @param apiConfig 配置信息
     */
    protected void timeoutSetter(Request request, StandardApiConfiguration apiConfig) {
        // 通用
        setTimeout(apiConfig.getConnectTimeout(), request::setConnectTimeout);
        setTimeout(apiConfig.getReadTimeout(), request::setReadTimeout);

        // OkHttp
        setTimeout(apiConfig.getWriteTimeout(), timeout -> request.addAdditionalParameter(OKHTTP_PM_WRITE_TIMEOUT, timeout));
        setTimeout(apiConfig.getCallTimeout(), timeout -> request.addAdditionalParameter(OKHTTP_PM_CALL_TIMEOUT, timeout));

        // HttpClient
        setTimeout(apiConfig.getConnectionRequestTimeout(), timeout -> request.addAdditionalParameter(HTTPCLIENT_PM_CONNECTION_REQUEST_TIMEOUT, timeout));
    }

    /**
     * 设置超时时间，当且仅当超时时间大于0时才会进行设置
     *
     * @param timeout    超时时间
     * @param timeSetter 超时时间设置逻辑
     */
    protected void setTimeout(Integer timeout, Consumer<Integer> timeSetter) {
        if (timeout != null) {
            timeSetter.accept(timeout);
        }
    }

    /**
     * 设置条件参数
     *
     * @param mc               方法上下文
     * @param request          请求对象
     * @param conditionConfigs 条件配置
     * @param requestConsumer  请求消费者
     */
    protected void setConditionParameter(MethodContext mc, Request request, List<ConditionConfig> conditionConfigs, Consumer<RequestParameter> requestConsumer) {
        if (ContainerUtils.isEmptyCollection(conditionConfigs)) {
            return;
        }
        for (ConditionConfig conditionConfig : conditionConfigs) {
            String condition = conditionConfig.getCondition();
            if (StringUtils.hasText(condition) && mc.parseExpression(condition, boolean.class)) {
                setParameter(mc, request, conditionConfig.getConfigs(), requestConsumer);
            }
        }
    }

    /**
     * 设置MultipartFormData类型的参数
     *
     * @param mc                方法上下文
     * @param request           请求对象
     * @param multipartFormData MultipartFormData配置
     */
    protected void setConditionMultipartFormData(MethodContext mc, Request request, List<ConditionMultipartFormData> multipartFormData) {
        if (ContainerUtils.isEmptyCollection(multipartFormData)) {
            return;
        }
        for (ConditionMultipartFormData conditionMultipartFormDatum : multipartFormData) {
            String condition = conditionMultipartFormDatum.getCondition();
            if (StringUtils.hasText(condition) && mc.parseExpression(condition, boolean.class)) {
                // Txt param setter
                setParameter(mc, request, conditionMultipartFormDatum.getTxt(), req -> req.getRequest().addMultipartFormParameter(req.getName(), req.getValue()));

                // File param setter
                setParameter(mc, request, conditionMultipartFormDatum.getFile(),  req -> {
                    Request httpReq = req.getRequest();
                    String name = req.getName();
                    Object value = req.getValue();

                    if (HttpExecutor.isResourceParam(value)) {
                        httpReq.addMultipartFormParameter(name, value);
                    } else if (value instanceof String) {
                        httpReq.addResources(name, ResourceFunctions.resources((String) value));
                    } else {
                        throw new ConfigurationParserException("['multipart/form-data'] format parameter parsing exception: '{}' is not a resource class.", name);
                    }

                });
            }
        }


    }

    /**
     * 设置参数
     *
     * @param mc              方法上下文
     * @param request         请求对象
     * @param configMap       配置 Map
     * @param requestConsumer 请求消费者
     */
    protected void setParameter(MethodContext mc, Request request, Map<String, Object> configMap, Consumer<RequestParameter> requestConsumer) {
        if (ContainerUtils.isEmptyMap(configMap)) {
            return;
        }
        configMap.forEach((name, value) -> {
            String pName = mc.parseExpression(name, String.class);
            if (ContainerUtils.isIterable(value)) {
                ContainerUtils.getIterable(value).forEach(e -> {
                    requestConsumer.accept(RequestParameter.of(pName, mc.parseExpression(String.valueOf(e)), request));
                });
            } else {
                requestConsumer.accept(RequestParameter.of(pName, mc.parseExpression(String.valueOf(value)), request));
            }
        });
    }


    /**
     * 设置MultipartFormData类型的参数
     *
     * @param mc                方法上下文
     * @param request           请求对象
     * @param multipartFormData MultipartFormData配置
     */
    protected void setMultipartFormData(MethodContext mc, Request request, MultipartFormData multipartFormData) {
        // Txt param setter
        setParameter(mc, request, multipartFormData.getTxt(), req -> req.getRequest().addMultipartFormParameter(req.getName(), req.getValue()));

        // File param setter
        setParameter(mc, request, multipartFormData.getFile(), req -> {
            Request httpReq = req.getRequest();
            String name = req.getName();
            Object value = req.getValue();

            if (HttpExecutor.isResourceParam(value)) {
                httpReq.addMultipartFormParameter(name, value);
            } else if (value instanceof String) {
                httpReq.addResources(name, ResourceFunctions.resources((String) value));
            } else {
                throw new ConfigurationParserException("['multipart/form-data'] format parameter parsing exception: '{}' is not a resource class.", name);
            }

        });
    }

    /**
     * 设置请求体
     *
     * @param mc      方法上下文
     * @param request 请求对象
     * @param body    请求体配置
     */
    protected void setRequestBody(MethodContext mc, Request request, String body) {
        if (!StringUtils.hasText(body)) {
            return;
        }

        Object bodyResult = mc.parseExpression(body);
        if (request instanceof Resource) {
            request.setBody(BodyObject.binaryBody((Resource) bodyResult));
        } else {
            String strBody = String.valueOf(bodyResult);
            ContentType contentType = request.getContentType();
            contentType = contentType == ContentType.NON ? ContentType.TEXT_PLAIN : contentType;
            request.setBody(BodyObject.builder(contentType, strBody));
        }
    }

    /**
     * 请求参数
     */
    public static class RequestParameter {
        private final String name;
        private final Object value;
        private final Request request;

        private RequestParameter(String name, Object value, Request request) {
            this.name = name;
            this.value = value;
            this.request = request;
        }

        public static RequestParameter of(String name, Object value, Request request) {
            return new RequestParameter(name, value, request);
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public Request getRequest() {
            return request;
        }
    }
}
