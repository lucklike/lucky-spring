package io.github.lucklike.httpclient;

import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.parameter.Bind;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import static io.github.lucklike.httpclient.BeanFunction.env;

/**
 * 给Bean对象被{@link Bind @Bind}注解标注的属性或方法绑定环境变量的配置
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/10/12 03:59
 */
public class BindAnnotationBeanPostProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 属性注入
        fieldInject(bean);
        // 方法注入
        methodInject(bean);
        return bean;
    }

    /**
     * 属性注入
     * <pre>
     *   1.静态属性直接忽略
     *   2.被{@link Bind @Bind}注解标注的属性则会通过{@link BeanFunction#env(String, Object...)}方法来绑定配置
     * </pre>
     *
     * @param bean Bean实例
     */
    private void fieldInject(Object bean) {
        for (Field field : ClassUtils.getAllFields(bean.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Bind bindAnn = AnnotationUtils.findMergedAnnotation(field, Bind.class);
            if (bindAnn != null) {
                FieldUtils.setValue(bean, field, env(bindAnn.value(), ResolvableType.forField(field)));
            }
        }
    }

    /**
     * 方法注入
     * <pre>
     *   1.静态方法以及不需要入参的方法直接忽略
     *   2.方法上被{@link Bind @Bind}注解标注时，会为参数列表所有的参数都绑定配置
     *   3.参数上被{@link Bind @Bind}注解标注时，只会为该参数生成代理对象
     *   4.方法和参数上均没有被被{@link Bind @Bind}注解标注时，忽略该方法
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
            Bind methodBindAnn = AnnotationUtils.findMergedAnnotation(method, Bind.class);
            boolean invoke = methodBindAnn != null;
            for (int i = 0; i < parameterCount; i++) {
                Bind paramBindAnn = AnnotationUtils.findMergedAnnotation(parameters[i], Bind.class);
                if (methodBindAnn != null || paramBindAnn != null) {
                    Bind useBindAnn = paramBindAnn == null ? methodBindAnn : paramBindAnn;
                    args[i] = env(useBindAnn.value(), ResolvableType.forMethodParameter(method, i));
                    invoke = true;
                }
            }

            if (invoke) {
                MethodUtils.invoke(bean, method, args);
            }
        }
    }
}
