package io.github.lucklike.httpclient.injection;

import io.github.lucklike.httpclient.function.BeanFunction;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * 给Bean对象被{@link Bind @Bind}注解标注的属性或方法绑定一段配置
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/06/27 15:27
 */
public class BindAnnotationPropertyInjection extends AnnotationPropertyInjection<Bind> {

    @Override
    protected Object getInjectObjectByField(Object bean, String beanName, Field field, ResolvableType fieldType, Bind bindAnn) {
        return BeanFunction.env(bindAnn.value(), fieldType);
    }

    @Override
    protected Object getInjectObjectByParam(Object bean, String beanName, Parameter param, ResolvableType paramType, Bind bindAnn) {
        return BeanFunction.env(bindAnn.value(), paramType);
    }
}
