package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.injection.Bind;

/**
 * 支持{@link Bind @Bind}注解功能的参数实例工厂
 */
public class BindParameterInstanceFactory extends AnnotationParameterInstanceFactory<Bind> {

    @Override
    protected Object doCreateInstance(ParameterInfo parameterInfo, Bind bindAnn) {
        return BeanFunction.env(bindAnn.value(), parameterInfo.getTargetResolvableType());
    }
}
