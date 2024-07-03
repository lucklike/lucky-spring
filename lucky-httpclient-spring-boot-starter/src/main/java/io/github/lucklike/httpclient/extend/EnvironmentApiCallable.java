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
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/30 21:06
 */
public class EnvironmentApiCallable extends AbstractSpELResponseConvert implements StaticParamResolver, Interceptor {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentApiCallable.class);

    private final AtomicBoolean init = new AtomicBoolean(false);
    private final Map<String, EnvApi> envApiMap = new ConcurrentHashMap<>(16);

    private Api api;
    private ConfigurationMap configMap;

    @Override
    public List<ParamInfo> parser(StaticParamAnnContext context) {
        return Collections.singletonList(new ParamInfo("envApi", new EnvContextApi(createApi(context), context.getContext())));
    }

    @Override
    public <T> T convert(Response response, ConvertContext context) throws Throwable {
        EnvApi envApi = getEnvApi(context.getContext());
        Convert convert = envApi.getRespConvert();
        Class<?> metaType = convert.getMetaType();
        if (Object.class != metaType) {
            context.getResponseVar().addRootVariable(RESPONSE_BODY, LazyValue.of(() -> getResponseBody(response, metaType)));
        }

        for (Condition condition : convert.getCondition()) {
            boolean assertion = context.parseExpression(condition.getAssertion(), boolean.class);
            if (assertion) {
                String result = condition.getResult();
                if (StringUtils.hasText(result)) {
                    return context.parseExpression(
                            result,
                            context.getRealMethodReturnType()
                    );
                }
                String exception = condition.getException();
                if (StringUtils.hasText(exception)) {
                    throwException(context, exception);
                }
                throw new ConditionalSelectionException("The 'result' and 'exception' in the conversion configuration cannot be null at the same time");
            }
        }

        String result = convert.getResult();
        if (StringUtils.hasText(result)) {
            return context.parseExpression(
                    result,
                    context.getRealMethodReturnType()
            );
        }

        String exception = convert.getException();
        if (StringUtils.hasText(exception)) {
            throwException(context, exception);
        }
        return response.getEntity(context.getRealMethodReturnType());
    }

    @Override
    public void doBeforeExecute(Request request, InterceptorContext context) {
        MethodContext methodContext = context.getContext();
        EnvApi envApi = getEnvApi(methodContext);
        for (InterceptorConf conf : envApi.getInterceptor()) {
            Interceptor interceptor = createInterceptor(methodContext, conf);
            interceptor.beforeExecute(request, context);
        }
    }

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
