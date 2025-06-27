package io.github.lucklike.httpclient.injection;

import com.luckyframework.reflect.AnnotationUtils;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;


/**
 * 基于注解实现的属性注入器
 * <pre>
 *     关于条件判断：
 *     判断是否可以进行属性注入的条件是：对象属性、方法、方法参数是否被指定的注解给标注了
 *
 *     关于值获取
 *     在获取注入值时会此类会提取出指定注解的实例
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/06/27 13:18
 */
public abstract class AnnotationPropertyInjection<A extends Annotation> extends AbstractPropertyInjection {

    /**
     * 注解类型
     */
    private final Class<A> annotationType;

    /**
     * 构造方法
     * 子类在进行构造时，会自动获取子中指定的注解泛型的类型
     */
    @SuppressWarnings("unchecked")
    public AnnotationPropertyInjection() {
        this.annotationType = (Class<A>) ResolvableType.forClass(AnnotationPropertyInjection.class, getClass()).getGeneric(0).resolve();
    }

    @Override
    protected boolean fieldCanInject(Object bean, String beanName, Field field, ResolvableType fieldType) {
        return AnnotationUtils.isAnnotated(field, annotationType);
    }

    @Override
    protected boolean paramCanInject(Object bean, String beanName, Parameter param, ResolvableType paramType) {
        return AnnotationUtils.isAnnotated(param, annotationType) || AnnotationUtils.isAnnotated(param.getDeclaringExecutable(), annotationType);
    }

    @Override
    protected Object getFieldInjectObject(Object bean, String beanName, Field field, ResolvableType fieldType) {
        return getInjectObjectByField(bean, beanName, field, fieldType, AnnotationUtils.findMergedAnnotation(field, annotationType));
    }

    @Override
    protected Object getParamInjectObject(Object bean, String beanName, Parameter param, ResolvableType paramType) {
        A paramAnn = AnnotationUtils.findMergedAnnotation(param, annotationType);
        A methodAnn = AnnotationUtils.findMergedAnnotation(param.getDeclaringExecutable(), annotationType);

        A ann = paramAnn != null ? paramAnn : methodAnn;

        return getInjectObjectByParam(bean, beanName, param, paramType, ann);
    }


    /**
     * 获取注入属性的值
     *
     * @param bean       当前Bean对象
     * @param beanName   当前Bean对象名称
     * @param field      要注入的属性
     * @param fieldType  要注入的属性类型
     * @param annotation 注解实例
     * @return 需要注入的值
     */
    protected abstract Object getInjectObjectByField(Object bean, String beanName, Field field, ResolvableType fieldType, A annotation);


    /**
     * 获取注入方法参数的值
     *
     * @param bean       当前Bean对象
     * @param beanName   当前Bean对象名称
     * @param param      要注入的方法参数
     * @param paramType  要注入的方法参数类型
     * @param annotation 注解实例
     * @return 需要注入的值
     */
    protected abstract Object getInjectObjectByParam(Object bean, String beanName, Parameter param, ResolvableType paramType, A annotation);
}
