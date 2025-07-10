package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;

public abstract class AnnotationParameterInstanceFactory<A extends Annotation> extends AbstractParameterInstanceFactory {

    /**
     * 注解类型
     */
    private final Class<A> annotationType;

    /**
     * 构造方法
     * 子类在进行构造时，会自动获取子中指定的注解泛型的类型
     */
    @SuppressWarnings("unchecked")
    public AnnotationParameterInstanceFactory() {
        this.annotationType = (Class<A>) ResolvableType.forClass(AnnotationParameterInstanceFactory.class, getClass()).getGeneric(0).resolve();
    }


    @Override
    public boolean doCanCreateInstance(ParameterInfo parameterInfo, ResolvableType realType) {
        // 空跑
        return false;
    }

    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        return AnnotationUtils.isAnnotated(parameterInfo.getParameter(), annotationType);
    }

    @Override
    public Object doCreateInstance(ParameterInfo parameterInfo, ResolvableType realType) {
        A annotation = AnnotationUtils.findMergedAnnotation(parameterInfo.getParameter(), annotationType);
        return doCreateInstance(parameterInfo, realType, annotation);
    }

    protected A getAnnotation(ParameterInfo parameterInfo) {
        return AnnotationUtils.getMergedAnnotation(parameterInfo.getParameter(), annotationType);
    }


    protected abstract Object doCreateInstance(ParameterInfo parameterInfo, ResolvableType realType, A annotation);
}
