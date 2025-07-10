package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.injection.HttpReference;
import org.springframework.core.ResolvableType;

import java.util.Objects;


/**
 * 支持{@link HttpReference @HttpReference}注解功能的参数实例工厂
 */
public class HttpReferenceParameterInstanceFactory extends AnnotationParameterInstanceFactory<HttpReference> {

    @Override
    protected Object doCreateInstance(ParameterInfo parameterInfo, ResolvableType realType, HttpReference httpReferenceAnn) {
        HttpClientProxyObjectFactory factory = ApplicationContextUtils.getBean(HttpClientProxyObjectFactory.class);
        Class<?> parameterType = Objects.requireNonNull(realType.resolve());
        switch (httpReferenceAnn.proxyModel()) {
            case JDK:
                return factory.getJdkProxyObject(parameterType);
            case CGLIB:
                return factory.getCglibProxyObject(parameterType);
            default:
                return factory.getProxyObject(parameterType);
        }
    }

}
