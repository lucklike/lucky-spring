package io.github.lucklike.httpclient.injection;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;


/**
 * 给Bean对象被{@link HttpReference @HttpReference}注解标注的属性或方法注入代理对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/10/12 03:59
 */
public class HttpReferenceAnnotationFieldInjection extends AnnotationFieldInjection<HttpReference> implements BeanFactoryAware {

    /**
     * HttpClient代理对象工厂
     */
    private HttpClientProxyObjectFactory proxyObjectFactory;


    public HttpReferenceAnnotationFieldInjection() {
        super(HttpReference.class);
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory.containsBean(PROXY_FACTORY_BEAN_NAME)) {
            this.proxyObjectFactory = beanFactory.getBean(PROXY_FACTORY_BEAN_NAME, HttpClientProxyObjectFactory.class);
        } else {
            try {
                this.proxyObjectFactory = beanFactory.getBean(HttpClientProxyObjectFactory.class);
            } catch (BeansException e) {
                this.proxyObjectFactory = new HttpClientProxyObjectFactory();
            }
        }
    }


    @Override
    protected Object getInjectObjectByField(Object bean, String beanName, Field field, ResolvableType fieldType, HttpReference annotation) {
        return getHttpClientObject(field.getType(), annotation);
    }

    @Override
    protected Object getInjectObjectByParam(Object bean, String beanName, Parameter param, ResolvableType paramType, HttpReference annotation) {
        return getHttpClientObject(param.getType(), annotation);
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
            return proxyObjectFactory.getCglibProxyObject(clazz);
        }
        if (httpReference.proxyModel() == ProxyModel.JDK) {
            return proxyObjectFactory.getJdkProxyObject(clazz);
        }
        return proxyObjectFactory.getProxyObject(clazz);
    }

}
