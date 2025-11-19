package io.github.lucklike.httpclient.statics;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.statics.StaticParamAnnContext;
import com.luckyframework.httpclient.proxy.statics.StaticParamResolver;
import com.luckyframework.serializable.SerializationTypeToken;
import io.github.lucklike.httpclient.annotation.EnvJsonArray;
import io.github.lucklike.httpclient.function.BeanFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class EnvironmentJsonArrayResolver implements StaticParamResolver {


    @Override
    public List<ParamInfo> parser(StaticParamAnnContext context) {
        EnvJsonArray envArrayAnn = context.toAnnotation(EnvJsonArray.class);
        String arrayKey = getPrefix(envArrayAnn.prefix());

        String envKey = envArrayAnn.value();
        envKey = context.parseExpression(envKey, String.class);

        // 从环境变量提取参数
        List<Object> envValue = BeanFunction.env(envKey, new SerializationTypeToken<List<Object>>() {});

        // 空对象直接返回空集合
        if (ContainerUtils.isEmptyCollection(envValue)) {
            return Collections.emptyList();
        }

        // 将Map转成List<ParamInfo>
        List<ParamInfo> paramInfos = new ArrayList<>(envValue.size());
//        envValue.forEach((k, v) -> paramInfos.add(new ParamInfo(k, v)));
        return Collections.singletonList(new ParamInfo("envJson", paramInfos));

    }

    private String getPrefix(String prefix) {
        return prefix.startsWith("\\") ? prefix.substring(1) : prefix;
    }
}
