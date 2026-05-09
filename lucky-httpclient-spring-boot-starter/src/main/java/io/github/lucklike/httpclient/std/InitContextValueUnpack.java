package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.core.util.BeanUtils;
import com.luckyframework.httpclient.proxy.context.Context;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.ParameterContext;
import com.luckyframework.httpclient.proxy.context.ValueContext;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpack;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpackException;
import com.luckyframework.httpclient.proxy.unpack.ValueUnpackContext;
import io.github.lucklike.httpclient.function.SpELPropertyCopyConvert;

import java.util.Map;

import static com.luckyframework.httpclient.core.util.BeanUtils.copyProperties;

/**
 * 初始化参数解包器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/7 01:22
 * @see Init
 */
public class InitContextValueUnpack implements ContextValueUnpack {

    @Override
    @SuppressWarnings("unchecked")
    public Object getRealValue(ValueUnpackContext unpackContext, Object wrapperValue) throws ContextValueUnpackException {
        // 只有参数类型的上下文才进行绑定
        ValueContext valueContext = unpackContext.getContext();
        if (!(valueContext instanceof ParameterContext)) {
            return wrapperValue;
        }

        // 尝试直接绑定整个 initParams
        StandardApiConfiguration config = unpackContext.getRootVar(StdHttpClient.SimpleHttpClientFunctionAndCallback.STANDARD_API_CONFIG_NAME, StandardApiConfiguration.class);
        Map<String, Object> initParams = config.getInitParams();
        spelInitBind(valueContext, wrapperValue, initParams);

        // 绑定@Init 注解中指定的配置
        Init initAnn = unpackContext.toAnnotation(Init.class);
        for (String initConfigKey : initAnn.value()) {
            Object initConfigParams = initParams.get(valueContext.parseExpression(initConfigKey, String.class));
            if ((initConfigParams instanceof Map) && ContainerUtils.isNotEmptyMap((Map<?, ?>)initConfigParams)) {
                spelInitBind(valueContext, wrapperValue, (Map<String, Object>) initConfigParams);
            }
        }
        return wrapperValue;
    }

    /**
     * SpEL 初始化绑定
     *
     * @param context      上下文对象
     * @param targetObject 真实对象
     * @param initParams   初始化绑定参数
     */
    public static void spelInitBind(Context context, Object targetObject, Map<String, Object> initParams) {
        Object configObj = ConversionUtils.conversion(initParams, targetObject.getClass());
        BeanUtils.TargetPropertyIsDefValueExecuteCopy filter = new BeanUtils.TargetPropertyIsDefValueExecuteCopy();
        copyProperties(configObj, targetObject, filter, new SpELPropertyCopyConvert(context, filter));
    }
}
