package io.github.lucklike.httpclient.injection;

import io.github.lucklike.httpclient.BeanFunction;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class BindAnnotationFieldInjection extends AnnotationFieldInjection<Bind> {

    public BindAnnotationFieldInjection() {
        super(Bind.class);
    }

    @Override
    protected Object getInjectObjectByField(Object bean, String beanName, Field field, ResolvableType fieldType, Bind annotation) {
        return BeanFunction.env(annotation.value(), fieldType);
    }

    @Override
    protected Object getInjectObjectByParam(Object bean, String beanName, Parameter param, ResolvableType paramType, Bind annotation) {
        return BeanFunction.env(annotation.value(), paramType);
    }
}
