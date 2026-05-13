package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.context.ParameterContext;
import com.luckyframework.httpclient.proxy.context.ValueContext;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpack;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpackException;
import com.luckyframework.httpclient.proxy.unpack.ValueUnpackContext;

import java.util.Map;

import static com.luckyframework.httpclient.proxy.function.CommonFunctions.spelInitCopy;

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
    public Object getRealValue(ValueUnpackContext unpackContext, Object wrapperValue) throws ContextValueUnpackException {
        // 只有参数类型的上下文才进行绑定
        ValueContext valueContext = unpackContext.getContext();
        if (!(valueContext instanceof ParameterContext)) {
            return wrapperValue;
        }

        // 获取初始化配置
        StandardApiConfiguration config = unpackContext.getRootVar(StdHttpClient.StandardHttpClientFunctionAndCallback.STANDARD_API_CONFIG_NAME, StandardApiConfiguration.class);
        Map<String, Object> initParams = config.getInitParams();

        // 初始化配置不存在时直接返回
        if (ContainerUtils.isEmptyMap(initParams)) {
            return wrapperValue;
        }

        // 尝试直接绑定整个 initParams
        spelInitCopy(valueContext, wrapperValue, initParams);

        // 绑定@Init 注解中指定的配置
        Init initAnn = unpackContext.toAnnotation(Init.class);
        for (String initConfigKey : initAnn.value()) {
            Object initConfigParams = initParams.get(valueContext.parseExpression(initConfigKey, String.class));
            if ((initConfigParams instanceof Map) && ContainerUtils.isNotEmptyMap((Map<?, ?>)initConfigParams)) {
                spelInitCopy(valueContext, wrapperValue, initConfigParams);
            }
        }
        return wrapperValue;
    }
}
