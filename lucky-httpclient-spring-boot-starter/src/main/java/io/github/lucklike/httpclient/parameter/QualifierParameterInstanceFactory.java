package io.github.lucklike.httpclient.parameter;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 支持{@link Qualifier @Qualifier}注解功能的参数实例工厂
 */
public class QualifierParameterInstanceFactory implements ParameterInstanceFactory {

    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        Qualifier qualifierAnn = AnnotationUtils.findMergedAnnotation(parameterInfo.getParameter(), Qualifier.class);
        return qualifierAnn != null && StringUtils.hasText(qualifierAnn.value());
    }

    @Override
    public Object createInstance(ParameterInfo parameterInfo) {
        Qualifier qualifierAnn = AnnotationUtils.findMergedAnnotation(parameterInfo.getParameter(), Qualifier.class);
        return ApplicationContextUtils.getBean(qualifierAnn.value());
    }
}
