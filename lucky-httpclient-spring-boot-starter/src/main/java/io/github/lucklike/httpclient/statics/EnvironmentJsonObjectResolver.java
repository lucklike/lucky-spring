package io.github.lucklike.httpclient.statics;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.statics.StaticParamAnnContext;
import com.luckyframework.httpclient.proxy.statics.StaticParamResolver;
import io.github.lucklike.httpclient.annotation.CombinableEnvJson;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.injection.BindException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static com.luckyframework.httpclient.proxy.CommonFunctions.typeOf;


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
        String envKey = context.parseExpression(combinableEnvJsonAnn.value(), String.class);
        try {
            // 从环境变量提取参数
            LinkedHashMap<String, Object> envValue = BeanFunction.env(envKey, typeOf(LinkedHashMap.class, String.class, Object.class));

            // 空对象直接返回空集合
            if (ContainerUtils.isEmptyMap(envValue)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(new ParamInfo("", envValue));
        } catch (BindException e) {
            if (combinableEnvJsonAnn.allowConfigNotExist()) {
                return Collections.emptyList();
            }
            throw e;
        }

    }

}
