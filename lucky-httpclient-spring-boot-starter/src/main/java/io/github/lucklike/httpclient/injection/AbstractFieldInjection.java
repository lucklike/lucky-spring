package io.github.lucklike.httpclient.injection;

import org.springframework.core.ResolvableType;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public abstract class AbstractFieldInjection implements FieldInjection {


    @Override
    public boolean canInject(Object bean, String beanName, FieldInfo fieldInfo) {
        AnnotatedElement element = fieldInfo.getElement();
        if (element instanceof Field) {
            return doCanInject(bean, beanName, (Field) element, fieldInfo.getType());
        }
        if (element instanceof Parameter) {
            return doCanInject(bean, beanName, (Parameter) element, fieldInfo.getType());
        }
        throw new IllegalArgumentException("Unsupported field type: " + fieldInfo.getType());
    }

    @Override
    public Object getInjectObject(Object bean, String beanName, FieldInfo fieldInfo) {
        AnnotatedElement element = fieldInfo.getElement();
        if (element instanceof Field) {
            return doGetInjectObject(bean, beanName, (Field) element, fieldInfo.getType());
        }
        if (element instanceof Parameter) {
            return doGetInjectObject(bean, beanName, (Parameter) element, fieldInfo.getType());
        }
        throw new IllegalArgumentException("Unsupported field type: " + fieldInfo.getType());
    }

    protected abstract boolean doCanInject(Object bean, String beanName, Field field, ResolvableType fieldType);

    protected abstract boolean doCanInject(Object bean, String beanName, Parameter param, ResolvableType paramType);

    protected abstract Object doGetInjectObject(Object bean, String beanName, Field field, ResolvableType fieldType);

    protected abstract Object doGetInjectObject(Object bean, String beanName, Parameter param, ResolvableType paramType);
}
