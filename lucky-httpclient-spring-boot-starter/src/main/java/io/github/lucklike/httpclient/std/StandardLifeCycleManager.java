package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.FontUtil;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.RequestMethod;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.configapi.Condition;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.convert.ActivelyThrownException;
import com.luckyframework.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static com.luckyframework.httpclient.core.executor.Constant.HTTPCLIENT_PM_CONNECTION_REQUEST_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.OKHTTP_PM_CALL_TIMEOUT;
import static com.luckyframework.httpclient.core.executor.Constant.OKHTTP_PM_WRITE_TIMEOUT;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.retrySetter;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setConditionFormParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setConditionHeaderParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setConditionMultipartFormData;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setConditionPathParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setConditionQueryParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setConditionRequestBody;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setFormParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setHeaderParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setMultipartFormData;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setPathParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.setQueryParams;
import static com.luckyframework.httpclient.proxy.configapi.parse.RequestParameterUtils.sslSetter;

/**
 * 标准的生命周期管理器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/9 22:10
 */
public class StandardLifeCycleManager implements LifeCycleManager {

    private static final Logger logger = LoggerFactory.getLogger(StandardLifeCycleManager.class);

    @Override
    public String buildBaseUrl(MethodContext mc, StandardHttpClientConfiguration config) throws Exception {
        return config.getUrl();
    }

    @Override
    public void methodMetaContentInit(MethodMetaContext mec, StandardApiConfiguration config) {
        // 重试相关配置
        retrySetter(mec, config.getRetryConfig());
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
        sslSetter(mc, request, apiConfig.getSslConfig());
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
        for (ConditionExceptionHandlerConfig exceptionHandlerConfig : apiConfig.getConditionExceptionHandler()) {
            if (canExHandler(mc, th, exceptionHandlerConfig)) {
                return exHandler(mc, request, th, exceptionHandlerConfig);
            }
        }

        ExceptionHandlerConfig exceptionHandlerConfig = apiConfig.getExceptionHandler();
        if (exceptionHandlerConfig != null && exceptionHandlerConfig.effective()) {
            return exHandler(mc, request, th, exceptionHandlerConfig);
        }

        return getRootCause(mc, th);
    }


    private boolean canExHandler(MethodContext mc, Throwable th, ConditionExceptionHandlerConfig exceptionHandlerConfig) {
        String condition = exceptionHandlerConfig.getCondition();
        if (StringUtils.hasText(condition)) {
            return mc.parseExpression(condition, boolean.class);
        }

        Set<Class<? extends Throwable>> exceptionClasses = exceptionHandlerConfig.getExceptionClasses();
        if (ContainerUtils.isNotEmptyCollection(exceptionClasses)) {
            ConditionExceptionHandlerConfig.Compare exceptionCompare = exceptionHandlerConfig.getExceptionCompare();
            if (exceptionCompare == ConditionExceptionHandlerConfig.Compare.EQUALS) {
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

        String exception = exceptionHandlerConfig.getException();
        if (StringUtils.hasText(exception)) {
            Object exObj = mc.parseExpression(exception);
            if (exObj instanceof Throwable) {
                return exObj;
            }

            return new ActivelyThrownException(String.valueOf(exObj));
        }

        return getRootCause(mc, th);
    }

    public Throwable getRootCause(MethodContext mc, Throwable th) {
        Throwable rootCause = ActivelyThrownException.getRootCause(th);
        logger.error("HTTP proxy method ['{}'] execution failed.", FontUtil.getBlueUnderline(MethodUtils.getLocation(mc.getCurrentAnnotatedElement())), rootCause);
        return rootCause;
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
        // Header param setter
        setHeaderParams(mc, request, apiConfig.getHeaderParams());

        // Query param setter
        setQueryParams(mc, request, apiConfig.getQueryParams());

        // Path param setter
        setPathParams(mc, request, apiConfig.getPathParams());

        // Form param setter
        setFormParams(mc, request, apiConfig.getFormParams());

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
        // Condition header param setter
        setConditionHeaderParams(mc, request, apiConfig.getConditionHeaderParams());

        // Condition query param setter
        setConditionQueryParams(mc, request, apiConfig.getConditionQueryParams());

        // Condition path param setter
        setConditionPathParams(mc, request, apiConfig.getConditionPathParams());

        // Condition form param setter
        setConditionFormParams(mc, request, apiConfig.getConditionFormParams());

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
        setConditionRequestBody(mc, request, apiConfig.getConditionBody(), apiConfig.getBody());
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
}
