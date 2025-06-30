package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ResolvableType;

/**
 * 支持{@link Qualifier @Qualifier}注解功能的参数实例工厂
 */
public class QualifierParameterInstanceFactory extends AnnotationParameterInstanceFactory<Qualifier> {


    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        Qualifier annotation = getAnnotation(parameterInfo);
        return annotation != null && StringUtils.hasText(annotation.value());
    }

    @Override
    protected Object doCreateInstance(ParameterInfo parameterInfo, ResolvableType realType, Qualifier annotation) {
        return ApplicationContextUtils.getBean(annotation.value());
    }
}
