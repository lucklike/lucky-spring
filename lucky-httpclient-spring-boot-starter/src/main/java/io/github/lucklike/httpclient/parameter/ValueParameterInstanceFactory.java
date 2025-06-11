package io.github.lucklike.httpclient.parameter;

import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;


/**
 * 支持{@link Value @Value}注解功能的参数实例工厂
 */
public class ValueParameterInstanceFactory implements ParameterInstanceFactory {

    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        return AnnotationUtils.isAnnotated(parameterInfo.getParameter(), Value.class);
    }

    @Override
    public Object createInstance(ParameterInfo parameterInfo) {
        Value valueAnn = AnnotationUtils.findMergedAnnotation(parameterInfo.getParameter(), Value.class);
        Environment env = ApplicationContextUtils.getEnvironment();
        return ConversionUtils.conversion(env.resolveRequiredPlaceholders(valueAnn.value()), parameterInfo.getResolvableType());
    }
}
