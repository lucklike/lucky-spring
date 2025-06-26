package io.github.lucklike.httpclient.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.BeanFunction;

/**
 * 支持{@link Bind @Bind}注解功能的参数实例工厂
 */
public class BindParameterInstanceFactory implements ParameterInstanceFactory {
    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        return AnnotationUtils.isAnnotated(parameterInfo.getParameter(), Bind.class);
    }

    @Override
    public Object createInstance(ParameterInfo parameterInfo) {
        Bind bindAnn = AnnotationUtils.getMergedAnnotation(parameterInfo.getParameter(), Bind.class);
        return BeanFunction.env(bindAnn.value(), parameterInfo.getResolvableType());
    }
}
