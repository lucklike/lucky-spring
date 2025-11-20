package io.github.lucklike.httpclient.statics;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.statics.StaticParamAnnContext;
import com.luckyframework.httpclient.proxy.statics.StaticParamResolver;
import com.luckyframework.serializable.SerializationTypeToken;
import io.github.lucklike.httpclient.annotation.CombinableEnvJson;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.injection.BindException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * 从环境变量中提取JSON对象请求体的解析器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/11/19 18:30
 */
public class EnvironmentJsonObjectResolver implements StaticParamResolver {


    @Override
    public List<ParamInfo> parser(StaticParamAnnContext context) {
        CombinableEnvJson combinableEnvJsonAnn = context.toAnnotation(CombinableEnvJson.class);
        String envKey = combinableEnvJsonAnn.value();
        envKey = context.parseExpression(envKey, String.class);

        try {
            // 从环境变量提取参数
            LinkedHashMap<String, Object> envValue = BeanFunction.env(envKey, new SerializationTypeToken<LinkedHashMap<String, Object>>() {
            });

            // 空对象直接返回空集合
            if (ContainerUtils.isEmptyMap(envValue)) {
                return Collections.emptyList();
            }

            // 将Map转成List<ParamInfo>
            List<ParamInfo> paramInfos = new ArrayList<>(envValue.size());
            envValue.forEach((k, v) -> paramInfos.add(new ParamInfo(k, v)));
            return Collections.singletonList(new ParamInfo("envJson", paramInfos));
        } catch (BindException e) {
            if (combinableEnvJsonAnn.allowConfigNotExist()) {
                return Collections.emptyList();
            }
            throw e;
        }

    }

}
