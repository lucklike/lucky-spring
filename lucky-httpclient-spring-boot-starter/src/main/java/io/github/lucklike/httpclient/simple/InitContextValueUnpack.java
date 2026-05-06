package io.github.lucklike.httpclient.simple;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.core.util.BeanUtils;
import com.luckyframework.httpclient.proxy.context.Context;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.ValueContext;
import com.luckyframework.httpclient.proxy.mock.config.MockConfigFunction;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpack;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpackException;
import com.luckyframework.httpclient.proxy.unpack.ValueUnpackContext;
import io.github.lucklike.httpclient.config.simple.SimpleHttpClientConfiguration;
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
        ValueContext valueContext = unpackContext.getContext();

        //  获取配置
        SimpleHttpClientConfiguration config = unpackContext.getRootVar(SimpleHttpClient.SimpleHttpClientFunctionAndCallback.CLASS_CONFIG_NAME, SimpleHttpClientConfiguration.class);
        Map<String, Object> initParams = config.getInitParams();
        Object apiInitParams = initParams.get(MockConfigFunction.getApiName(valueContext.lookupContext(MethodContext.class)));

        spelInitBind(valueContext, wrapperValue, initParams);
        if ((apiInitParams instanceof Map) && ContainerUtils.isNotEmptyMap((Map<?, ?>)apiInitParams)) {
            spelInitBind(valueContext, wrapperValue, (Map<String, Object>) apiInitParams);
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
