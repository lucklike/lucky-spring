package io.github.lucklike.httpclient.extend;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.common.StringUtils;
import com.luckyframework.exception.LuckyRuntimeException;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.convert.AbstractSpELResponseConvert;
import com.luckyframework.httpclient.proxy.convert.ConditionalSelectionException;
import com.luckyframework.httpclient.proxy.convert.ConvertContext;
import com.luckyframework.httpclient.proxy.creator.ObjectCreator;
import com.luckyframework.httpclient.proxy.interceptor.Interceptor;
import com.luckyframework.httpclient.proxy.interceptor.InterceptorContext;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.statics.StaticParamAnnContext;
import com.luckyframework.httpclient.proxy.statics.StaticParamResolver;
import com.luckyframework.spel.LazyValue;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.luckyframework.httpclient.proxy.ParameterNameConstant.RESPONSE_BODY;
import static com.luckyframework.httpclient.proxy.spel.DefaultSpELVarManager.getResponseBody;


/**
 * 对环境变量API提供支持的类
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/30 21:06
 */
public class EnvironmentApiFunctionalSupport extends AbstractSpELResponseConvert implements StaticParamResolver, Interceptor {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentApiFunctionalSupport.class);

    /**
     * 初始化标识
     */
    private final AtomicBoolean init = new AtomicBoolean(false);

    /**
     * 环境变量API相关的缓存
     */
    private final Map<String, EnvApi> envApiMap = new ConcurrentHashMap<>(16);

    /**
     * 所有接口方法公用的请求参数
     */
    private Api api;

    /**
     * 用于存储于接口相关的环变量的容器
     */
    private ConfigurationMap configMap;

    /**
     * 静态参数解析器相关的实现
     *
     * @param context 静态注解上下文信息
     * @return 解析得到的参数对象
     */
    @Override
    public List<ParamInfo> parser(StaticParamAnnContext context) {
        return Collections.singletonList(new ParamInfo("envApi", new EnvContextApi(createApi(context), context.getContext())));
    }

    /**
     * 响应转换相关的实现
     *
     * @param response 响应实体
     * @param context  转化器注解上下文
     * @param <T>      返回值泛型
     * @return 方法返回值类型相对应的实例
     * @throws Throwable 转换过程中可能会抛出的异常
     */
    @Override
    public <T> T convert(Response response, ConvertContext context) throws Throwable {
        EnvApi envApi = getEnvApi(context.getContext());
        Convert convert = envApi.getRespConvert();
        Class<?> metaType = convert.getMetaType();

        // 将响应体懒加载值替换为元类型的实例
        if (Object.class != metaType) {
            context.getResponseVar().addRootVariable(RESPONSE_BODY, LazyValue.of(() -> getResponseBody(response, metaType)));
        }

        // 条件判断，满足不同的条件时执行不同的逻辑
        for (Condition condition : convert.getCondition()) {
            boolean assertion = context.parseExpression(condition.getAssertion(), boolean.class);
            if (assertion) {

                // 响应结果转换
                String result = condition.getResult();
                if (StringUtils.hasText(result)) {
                    return context.parseExpression(
                            result,
                            context.getRealMethodReturnType()
                    );
                }

                // 异常处理
                String exception = condition.getException();
                if (StringUtils.hasText(exception)) {
                    throwException(context, exception);
                }
                throw new ConditionalSelectionException("The 'result' and 'exception' in the conversion configuration cannot be null at the same time");
            }
        }


        // 所有条件均不满足时，执行默认的响应结果转换
        String result = convert.getResult();
        if (StringUtils.hasText(result)) {
            return context.parseExpression(
                    result,
                    context.getRealMethodReturnType()
            );
        }

        // 所有条件均不满足时，执行默认的异常处理
        String exception = convert.getException();
        if (StringUtils.hasText(exception)) {
            throwException(context, exception);
        }

        // 未配置响应转化时直接将响应体转为方法返回值类型
        return response.getEntity(context.getRealMethodReturnType());
    }

    /**
     * 请求拦截相关的实现
     *
     * @param request 请求对象
     * @param context 拦截器上下文
     */
    @Override
    public void doBeforeExecute(Request request, InterceptorContext context) {
        MethodContext methodContext = context.getContext();
        EnvApi envApi = getEnvApi(methodContext);
        for (InterceptorConf conf : envApi.getInterceptor()) {
            Interceptor interceptor = createInterceptor(methodContext, conf);
            interceptor.beforeExecute(request, context);
        }
    }

    /**
     * 响应拦截相关的实现
     *
     * @param response 响应对象
     * @param context  拦截器上下文
     */
    @Override
    public Response doAfterExecute(Response response, InterceptorContext context) {
        MethodContext methodContext = context.getContext();
        EnvApi envApi = getEnvApi(methodContext);
        for (InterceptorConf conf : envApi.getInterceptor()) {
            Interceptor interceptor = createInterceptor(methodContext, conf);
            response = interceptor.afterExecute(response, context);
        }
        return response;
    }

    @SuppressWarnings("all")
    private EnvApi createApi(StaticParamAnnContext context) {
        MethodContext methodContext = context.getContext();
        EnableEnvironmentClient ann = context.toAnnotation(EnableEnvironmentClient.class);
        String methodName = methodContext.getCurrentAnnotatedElement().getName();

        String prefix = StringUtils.hasText(ann.prefix()) ? ann.prefix() : methodContext.getClassContext().getCurrentAnnotatedElement().getName();
        String keyProfix = prefix + ".";

        if (init.compareAndSet(false, true)) {
            AbstractEnvironment env = (AbstractEnvironment) ApplicationContextUtils.getEnvironment();
            Properties properties = new Properties();
            for (PropertySource<?> propertySource : env.getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                    for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                        if (propertyName.startsWith(keyProfix)) {
                            properties.put(propertyName, env.getProperty(propertyName));
                        }
                    }
                }
            }
            configMap = ConfigurationMap.create(properties);
            api = configMap.getEntry(prefix, Api.class);
        }
        String apiKey = keyProfix + methodName;
        return envApiMap.computeIfAbsent(methodName, k -> {
            EnvApi envApi = configMap.getEntry(apiKey, EnvApi.class);
            envApi.setApi(api);
            return envApi;
        });
    }

    private EnvApi getEnvApi(MethodContext context) {
        String methodName = context.getCurrentAnnotatedElement().getName();
        return envApiMap.get(methodName);
    }

    /**
     * 使用拦截器配置创建一个拦截器实例
     *
     * <pre>
     *     1.如果配置了beanName，则通过beanName到Spring容器中获取拦截器实例
     *     2.如果配置了class，则通过对象创建器{@link ObjectCreator}来创建拦截器实例
     *     3.如果以上两个都没有配置，则抛出异常
     * </pre>
     *
     * @param context 当前方法上下文实例对象
     * @param conf    拦截器配置
     * @return 拦截器实例
     */
    private Interceptor createInterceptor(MethodContext context, InterceptorConf conf) {
        if (StringUtils.hasText(conf.getBeanName())) {
            return ApplicationContextUtils.getBean(conf.getBeanName(), Interceptor.class);
        }

        if (conf.getClazz() != null) {
            return (Interceptor) context.getHttpProxyFactory().getObjectCreator().newObject(conf.getClazz(), "", context, conf.getScope());
        }
        throw new LuckyRuntimeException("@EnableEnvironmentClient the interceptor configuration of 'bean-name' and 'class-name' must be configured at least one.").printException(log);
    }
}
