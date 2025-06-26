package io.github.lucklike.httpclient.injection;


import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * 自动注入扩展接口，为Bean对象自动注入属性
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/10/12 03:59
 */
public class LuckyAutoInjectionBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //Bean类型为非JDK原生类型时执行属性注入操作
        if (!ClassUtils.isJdkBasic(bean.getClass())) {
            // 属性注入
            fieldInject(bean, beanName);
            // 方法注入
            methodInject(bean, beanName);
        }
        return bean;
    }


    private void fieldInject(Object bean, String beanName) {
        ObjectProvider<FieldInjection> beanProvider = ApplicationContextUtils.getBeanProvider(FieldInjection.class);
        for (Field field : ClassUtils.getAllFields(bean.getClass())) {

            // 静态属性直接过滤
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            FieldInfo fieldInfo = FieldInfo.of(field, ResolvableType.forField(field));

            // 遍历属性注入器进行属性过滤
            for (FieldInjection fieldInjection : beanProvider) {
                if (fieldInjection.canInject(bean, beanName, fieldInfo)) {
                    FieldUtils.setValue(bean, field, fieldInjection.getInjectObject(bean, beanName, fieldInfo));
                }
            }

        }
    }


    private void methodInject(Object bean, String beanName) {
        ObjectProvider<FieldInjection> beanProvider = ApplicationContextUtils.getBeanProvider(FieldInjection.class);
        for (Method method : ClassUtils.getAllMethod(bean.getClass())) {
            int parameterCount = method.getParameterCount();
            if (Modifier.isStatic(method.getModifiers()) || parameterCount == 0) {
                continue;
            }
            Object[] args = new Object[parameterCount];
            Parameter[] parameters = method.getParameters();
            boolean invoke = false;
            for (int i = 0; i < parameterCount; i++) {
                FieldInfo fieldInfo = FieldInfo.of(parameters[i], ResolvableType.forMethodParameter(method, i));

                // 遍历属性注入器进行属性过滤
                for (FieldInjection fieldInjection : beanProvider) {
                    if (fieldInjection.canInject(bean, beanName, fieldInfo)) {
                        args[i] = fieldInjection.getInjectObject(bean, beanName, fieldInfo);
                        invoke = true;
                    }
                }
            }

            if (invoke) {
                MethodUtils.invoke(bean, method, args);
            }
        }
    }
}
