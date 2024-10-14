package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.annotation.HttpReference;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;

/**
 * 给Bean对象被{@link HttpReference @HttpReference}注解标注的属性或方法注入代理对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/10/12 03:59
 */
public class HttpReferenceAnnotationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    /**
     * HttpClient代理对象工厂
     */
    private HttpClientProxyObjectFactory proxyObjectFactory;

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
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //Bean类型为非JDK原生类型时执行属性注入操作
        if (!ClassUtils.isJdkBasic(bean.getClass())) {
            // 属性注入
            fieldInject(bean);
            // 方法注入
            methodInject(bean);
        }
        return bean;
    }

    /**
     * 属性注入
     * <pre>
     *   1.静态属性直接忽略
     *   2.被{@link HttpReference @HttpReference}注解标注的属性则会通过{@link HttpClientProxyObjectFactory#getProxyObject(Class)}
     *   来获取代理对象，之后将此代理对象赋值给该属性
     * </pre>
     *
     * @param bean Bean实例
     */
    private void fieldInject(Object bean) {
        for (Field field : ClassUtils.getAllFields(bean.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (AnnotationUtils.isAnnotated(field, HttpReference.class)) {
                FieldUtils.setValue(bean, field, getHttpClientObject(field.getType(), AnnotationUtils.findMergedAnnotation(field, HttpReference.class)));
            }
        }
    }

    /**
     * 方法注入
     * <pre>
     *   1.静态方法以及不需要入参的方法直接忽略
     *   2.方法上被{@link HttpReference @HttpReference}注解标注时，会为参数列表所有的参数都生成代理对象，并使用这些代理对象作为参数化调用该方法
     *   3.参数上被{@link HttpReference @HttpReference}注解标注时，只会为该参数生成代理对象
     *   4.方法和参数上均没有被被{@link HttpReference @HttpReference}注解标注时，忽略该方法
     * </pre>
     *
     * @param bean Bean实例
     */
    private void methodInject(Object bean) {
        for (Method method : ClassUtils.getAllMethod(bean.getClass())) {
            int parameterCount = method.getParameterCount();
            if (Modifier.isStatic(method.getModifiers()) || parameterCount == 0) {
                continue;
            }
            Object[] args = new Object[parameterCount];
            Parameter[] parameters = method.getParameters();
            boolean methodIsReference = AnnotationUtils.isAnnotated(method, HttpReference.class);
            boolean invoke = methodIsReference;
            for (int i = 0; i < parameterCount; i++) {
                if (methodIsReference || AnnotationUtils.isAnnotated(parameters[i], HttpReference.class)) {
                    args[i] = getHttpClientObject(parameters[i].getType(), AnnotationUtils.findMergedAnnotation(parameters[i], HttpReference.class));
                    invoke = true;
                }
            }

            if (invoke) {
                MethodUtils.invoke(bean, method, args);
            }
        }
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
