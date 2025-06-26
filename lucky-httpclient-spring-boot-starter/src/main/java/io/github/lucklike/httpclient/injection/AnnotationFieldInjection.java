package io.github.lucklike.httpclient.injection;

import com.luckyframework.reflect.AnnotationUtils;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public abstract class AnnotationFieldInjection<A extends Annotation> extends AbstractFieldInjection {

    private final Class<A> annotationType;

    public AnnotationFieldInjection(Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    protected boolean doCanInject(Object bean, String beanName, Field field, ResolvableType fieldType) {
        return AnnotationUtils.isAnnotated(field, annotationType);
    }

    @Override
    protected boolean doCanInject(Object bean, String beanName, Parameter param, ResolvableType paramType) {
        return AnnotationUtils.isAnnotated(param, annotationType) || AnnotationUtils.isAnnotated(param.getDeclaringExecutable(), annotationType);
    }

    @Override
    protected Object doGetInjectObject(Object bean, String beanName, Field field, ResolvableType fieldType) {
        return getInjectObjectByField(bean, beanName, field, fieldType, AnnotationUtils.findMergedAnnotation(field, annotationType));
    }

    @Override
    protected Object doGetInjectObject(Object bean, String beanName, Parameter param, ResolvableType paramType) {
        A paramAnn = AnnotationUtils.findMergedAnnotation(param, annotationType);
        A methodAnn = AnnotationUtils.findMergedAnnotation(param.getDeclaringExecutable(), annotationType);

        A ann = paramAnn != null ? paramAnn : methodAnn;

        return getInjectObjectByParam(bean, beanName, param, paramType, ann);
    }



    protected abstract Object getInjectObjectByField(Object bean, String beanName, Field field, ResolvableType fieldType, A annotation);

    protected abstract Object getInjectObjectByParam(Object bean, String beanName, Parameter param, ResolvableType paramType, A annotation);
}
