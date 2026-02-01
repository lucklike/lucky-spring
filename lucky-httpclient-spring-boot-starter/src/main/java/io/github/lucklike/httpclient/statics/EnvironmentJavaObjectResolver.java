package io.github.lucklike.httpclient.statics;

import com.luckyframework.common.FlatBean;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.statics.StaticParamAnnContext;
import com.luckyframework.httpclient.proxy.statics.StaticParamResolver;
import io.github.lucklike.httpclient.annotation.EnvironmentJava;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.injection.BindException;
import org.springframework.core.ResolvableType;

import java.util.Collections;
import java.util.List;


/**
 * 从环境变量中提取JAVA对象请求体的解析器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/11/19 18:30
 */
public class EnvironmentJavaObjectResolver implements StaticParamResolver {


    @Override
    public List<ParamInfo> parser(StaticParamAnnContext context) {
        EnvironmentJava envAnn = context.toAnnotation(EnvironmentJava.class);
        String envKey = context.parseExpression(envAnn.value(), String.class);
        ResolvableType type = context.parseExpression(envAnn.type(), ResolvableType.class);
        try {
            return Collections.singletonList(new ParamInfo("", FlatBean.of(BeanFunction.env(envKey, type))));
        } catch (BindException e) {
            if (envAnn.allowConfigNotExist()) {
                return Collections.emptyList();
            }
            throw e;
        }

    }

}
