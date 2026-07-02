package io.github.lucklike.httpclient.injection;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.Constant;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import io.github.lucklike.httpclient.factory.DefaultProxyObjectFactory;
import io.github.lucklike.httpclient.factory.LuckyComponentProxyObjectFactory;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import static io.github.lucklike.httpclient.Constant.LUCKY_COMPONENT_PROXY_OBJECT_FACTORY_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;


/**
 * 给Bean对象被{@link HttpReference @HttpReference}注解标注的属性或方法注入代理对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/06/27 12:06
 */
public class HttpReferenceAnnotationPropertyInjection extends AnnotationPropertyInjection<HttpReference> {

    /**
     * HttpClient代理对象工厂
     */
    private LuckyComponentProxyObjectFactory luckyComponentProxyObjectFactory;

    @Override
    protected Object getInjectObjectByField(Object bean, String beanName, Field field, ResolvableType fieldType, HttpReference httpReferenceAnn) {
        return getHttpClientObject(fieldType.toClass(), httpReferenceAnn);
    }

    @Override
    protected Object getInjectObjectByParam(Object bean, String beanName, Parameter param, ResolvableType paramType, HttpReference httpReferenceAnn) {
        return getHttpClientObject(paramType.toClass(), httpReferenceAnn);
    }


    /**
     * 获取代理对象
     *
     * @param clazz         代理类的Class
     * @param httpReference {@link HttpReference @HttpReference}注解实例
     * @param <T>           代理对象泛型
     * @return 代理对象
     */
    private <T> T getHttpClientObject(Class<T> clazz, HttpReference httpReference) {
        if (httpReference.proxyModel() == ProxyModel.CGLIB) {
            return getLuckyComponentProxyObjectFactory().getCglibProxyObject(clazz);
        }
        if (httpReference.proxyModel() == ProxyModel.JDK) {
            return getLuckyComponentProxyObjectFactory().getJdkProxyObject(clazz);
        }
        return getLuckyComponentProxyObjectFactory().getProxyObject(clazz);
    }

    /**
     * 获取{@link LuckyComponentProxyObjectFactory}对象实例
     * <pre>
     *     1.Spring容器中存在名称为{@linkplain Constant#LUCKY_COMPONENT_PROXY_OBJECT_FACTORY_BEAN_NAME}时优先使用该Bean
     *     2.尝试使用类型查找实例
     *     3.直接构造（new）
     * </pre>
     *
     * @return HttpClientProxyObjectFactory对象实例
     */
    private synchronized LuckyComponentProxyObjectFactory getLuckyComponentProxyObjectFactory() {
        if (luckyComponentProxyObjectFactory == null) {
            if (ApplicationContextUtils.containsBean(LUCKY_COMPONENT_PROXY_OBJECT_FACTORY_BEAN_NAME)) {
                this.luckyComponentProxyObjectFactory = ApplicationContextUtils.getBean(LUCKY_COMPONENT_PROXY_OBJECT_FACTORY_BEAN_NAME, LuckyComponentProxyObjectFactory.class);
            } else if (ApplicationContextUtils.containsBean(PROXY_FACTORY_BEAN_NAME)) {
                this.luckyComponentProxyObjectFactory = new DefaultProxyObjectFactory(ApplicationContextUtils.getBean(PROXY_FACTORY_BEAN_NAME, HttpClientProxyObjectFactory.class));
            } else {
                HttpClientProxyObjectFactory httpClientProxyObjectFactory = ApplicationContextUtils.getBeanProvider(HttpClientProxyObjectFactory.class).getIfUnique(HttpClientProxyObjectFactory::new);
                this.luckyComponentProxyObjectFactory = new DefaultProxyObjectFactory(httpClientProxyObjectFactory);
            }
        }
        return luckyComponentProxyObjectFactory;
    }

}
