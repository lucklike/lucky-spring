package io.github.lucklike.httpclient.statics;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.statics.StaticParamAnnContext;
import com.luckyframework.httpclient.proxy.statics.StaticParamResolver;
import io.github.lucklike.httpclient.annotation.CombinableEnvJsonArray;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.injection.BindException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.luckyframework.httpclient.proxy.CommonFunctions.typeOf;

public class EnvironmentJsonArrayResolver implements StaticParamResolver {


    @Override
    public List<ParamInfo> parser(StaticParamAnnContext context) {
        CombinableEnvJsonArray envArrayAnn = context.toAnnotation(CombinableEnvJsonArray.class);
        String arrayKey = envArrayAnn.prefix();
        String envKey = context.parseExpression(envArrayAnn.value(), String.class);
        try {
            // 从环境变量提取参数
            List<Object> envValue = BeanFunction.env(envKey, typeOf(List.class, envArrayAnn.elementClass()));

            // 空对象直接返回空集合
            if (ContainerUtils.isEmptyCollection(envValue)) {
                return Collections.emptyList();
            }

            // 统一封装成Map
            Map<String, Object> rootMap = new LinkedHashMap<>();
            rootMap.put(arrayKey, envValue);

            return Collections.singletonList(new ParamInfo(arrayKey, rootMap));
        } catch (BindException e) {
            if (envArrayAnn.allowConfigNotExist()) {
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
