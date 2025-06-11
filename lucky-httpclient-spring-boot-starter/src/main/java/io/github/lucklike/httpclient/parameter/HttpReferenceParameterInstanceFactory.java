package io.github.lucklike.httpclient.parameter;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.annotation.HttpReference;
import io.github.lucklike.httpclient.annotation.ProxyModel;

import java.lang.reflect.Parameter;


/**
 * 支持{@link HttpReference @HttpReference}注解功能的参数实例工厂
 */
public class HttpReferenceParameterInstanceFactory implements ParameterInstanceFactory {
    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        return AnnotationUtils.isAnnotated(parameterInfo.getParameter(), HttpReference.class);
    }

    @Override
    public Object createInstance(ParameterInfo parameterInfo) {
        Parameter parameter = parameterInfo.getParameter();
        Class<?> parameterType = parameter.getType();
        HttpReference httpReferenceAnn = AnnotationUtils.findMergedAnnotation(parameter, HttpReference.class);
        HttpClientProxyObjectFactory factory = ApplicationContextUtils.getBean(HttpClientProxyObjectFactory.class);
        ProxyModel proxyModel = httpReferenceAnn.proxyModel();
        switch (proxyModel) {
            case JDK:
                return factory.getJdkProxyObject(parameterType);
            case CGLIB:
                return factory.getCglibProxyObject(parameterType);
            default:
                return factory.getProxyObject(parameterType);
        }
    }
}
