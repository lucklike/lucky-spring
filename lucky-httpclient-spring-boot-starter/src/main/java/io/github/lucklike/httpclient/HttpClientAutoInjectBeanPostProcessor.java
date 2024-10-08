package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.annotation.HttpReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;

public class HttpClientAutoInjectBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private HttpClientProxyObjectFactory proxyObjectFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext.containsBean(PROXY_FACTORY_BEAN_NAME)) {
            this.proxyObjectFactory = applicationContext.getBean(PROXY_FACTORY_BEAN_NAME, HttpClientProxyObjectFactory.class);
        } else {
            try {
                this.proxyObjectFactory = applicationContext.getBean(HttpClientProxyObjectFactory.class);
            } catch (BeansException e) {
                this.proxyObjectFactory = new HttpClientProxyObjectFactory();
            }
        }

    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (ClassUtils.isSimpleBaseType(bean.getClass())) {
            return bean;
        }
        fieldInject(bean);
        methodInject(bean);
        return bean;
    }

    private void fieldInject(Object bean) {
        for (Field field : ClassUtils.getAllFields(bean.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (AnnotationUtils.isAnnotated(field, HttpReference.class)) {
                FieldUtils.setValue(bean, field, getHttpClientObject(field.getType()));
            }
        }
    }

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
                    args[i] = getHttpClientObject(parameters[i].getType());
                    invoke = true;
                }
            }

            if (invoke) {
                MethodUtils.invoke(bean, method, args);
            }
        }
    }

    private <T> T getHttpClientObject(Class<T> clazz) {
        return proxyObjectFactory.getProxyObject(clazz);
    }

}
