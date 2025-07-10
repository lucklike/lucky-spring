package io.github.lucklike.httpclient.injection;


import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import com.luckyframework.reflect.MethodUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动注入扩展接口，为Bean对象自动注入属性
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/10/12 03:59
 * @see PropertyInjection
 */
public class LuckyAutoInjectionBeanPostProcessor implements BeanPostProcessor {

    private static final List<PropertyInjection> PROPERTY_INJECTIONS = new ArrayList<>();

    static {
        PROPERTY_INJECTIONS.add(new HttpReferenceAnnotationPropertyInjection());
        PROPERTY_INJECTIONS.add(new BindAnnotationPropertyInjection());
    }

    /**
     * 添加一个属性注册器
     *
     * @param propertyInjection 属性注册器
     */
    public static void addPropertyInjection(PropertyInjection propertyInjection) {
        PROPERTY_INJECTIONS.add(propertyInjection);
    }

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


    /**
     * 通过属性注入
     *
     * @param bean     当前Bean实例
     * @param beanName 当前Bean名称
     */
    private void fieldInject(Object bean, String beanName) {
        for (Field field : ClassUtils.getAllFields(bean.getClass())) {

            // 静态属性直接过滤
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            PropertyInfo propertyInfo = PropertyInfo.of(field, ResolvableType.forField(field));

            // 遍历属性注入器进行属性过滤
            for (PropertyInjection propertyInjection : PROPERTY_INJECTIONS) {
                if (propertyInjection.canInject(bean, beanName, propertyInfo)) {
                    FieldUtils.setValue(bean, field, propertyInjection.getInjectObject(bean, beanName, propertyInfo));
                }
            }
        }
    }

    /**
     * 通过方法参数注入
     *
     * @param bean     当前Bean实例
     * @param beanName 当前Bean名称
     */
    private void methodInject(Object bean, String beanName) {
        for (Method method : ClassUtils.getAllMethod(bean.getClass())) {
            int parameterCount = method.getParameterCount();
            if (Modifier.isStatic(method.getModifiers()) || parameterCount == 0) {
                continue;
            }
            Object[] args = new Object[parameterCount];
            Parameter[] parameters = method.getParameters();
            boolean invoke = false;
            out:
            for (int i = 0; i < parameterCount; i++) {
                PropertyInfo propertyInfo = PropertyInfo.of(parameters[i], ResolvableType.forMethodParameter(method, i));

                // 遍历属性注入器进行属性过滤
                for (PropertyInjection propertyInjection : PROPERTY_INJECTIONS) {
                    if (propertyInjection.canInject(bean, beanName, propertyInfo)) {
                        args[i] = propertyInjection.getInjectObject(bean, beanName, propertyInfo);
                        if (args[i] != null) {
                            invoke = true;
                            continue out;
                        }
                    }
                }
            }

            if (invoke) {
                MethodUtils.invoke(bean, method, args);
            }
        }
    }
}
