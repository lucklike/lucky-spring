package io.github.lucklike.httpclient.injection;

import com.luckyframework.common.StringUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.spel.LazyValue;
import io.github.lucklike.httpclient.SupplierObjectProvider;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.function.Supplier;


/**
 * 1. 属性注入器的基本实现，该抽象类将属性注入拆分为如下两种具体的情况
 * <pre>
 *     1.通过对象属性注入
 *     2.通过对象方法注入
 * </pre>
 * <p>
 * 2. 并提供懒加载功能，支持注入懒加载类型
 * <pre>
 *     {@link LazyValue}
 *     {@link Supplier}
 *     {@link ObjectProvider}
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/06/27 13:18
 */
public abstract class AbstractPropertyInjection implements PropertyInjection {


    @Override
    public boolean canInject(Object bean, String beanName, PropertyInfo propertyInfo) {
        AnnotatedElement element = propertyInfo.getElement();
        if (element instanceof Field) {
            return fieldCanInject(bean, beanName, (Field) element, propertyInfo.getType());
        }
        if (element instanceof Parameter) {
            return paramCanInject(bean, beanName, (Parameter) element, propertyInfo.getType());
        }
        throw new IllegalArgumentException("Unsupported field type: " + propertyInfo.getType());
    }

    @Override
    public Object getInjectObject(Object bean, String beanName, PropertyInfo propertyInfo) {
        try {
            AnnotatedElement element = propertyInfo.getElement();
            ResolvableType sourceType = propertyInfo.getType();

            // 懒加载值的情况
            boolean isObjectProvider = sourceType.resolve() == ObjectProvider.class && sourceType.hasGenerics();
            boolean isLazyValue = sourceType.resolve() == LazyValue.class && sourceType.hasGenerics();
            boolean isSupplier = sourceType.resolve() == Supplier.class && sourceType.hasGenerics();


            // 确定真实的参数类型
            ResolvableType realType;
            if (isObjectProvider || isLazyValue || isSupplier) {
                realType = sourceType.getGeneric(0);
            } else {
                realType = sourceType;
            }

            // 将注入值获取的过程封装成Supplier对象
            Supplier<?> objectSupplier;
            if (element instanceof Field) {
                objectSupplier = () -> getFieldInjectObject(bean, beanName, (Field) element, realType);
            } else if (element instanceof Parameter) {
                objectSupplier = () -> getParamInjectObject(bean, beanName, (Parameter) element, realType);
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + propertyInfo.getType());
            }

            // 处理ObjectProvider的情况
            if (isObjectProvider) {
                return SupplierObjectProvider.of(objectSupplier);
            }

            // 处理LazyValue的情况
            if (isLazyValue) {
                return LazyValue.of(objectSupplier);
            }

            // 处理Supplier的情况
            if (isSupplier) {
                return objectSupplier;
            }

            // 一般情况
            return objectSupplier.get();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            String info;
            AnnotatedElement element = propertyInfo.getElement();
            if (element instanceof Field) {
                info = StringUtils.format("[Class: {}, Field: '{}']", ClassUtils.getClassName(bean), ((Field) element).getName());
            } else {
                info = StringUtils.format("[Class: {}, Method: '{}']", ClassUtils.getClassName(bean), ((Parameter) element).getDeclaringExecutable().getName());
            }
            String msg = StringUtils.format("Attribute injection failed, injection approach: {}", info);
            throw new BeanCreationException(beanName, msg, e);
        }
    }

    //------------------------------------------------------------------------------------------------------------
    //                                          field Injection
    //------------------------------------------------------------------------------------------------------------

    /**
     * 是否可以通过属性来给当前的Bean对象来注入值
     *
     * @param bean      当前Bean对象
     * @param beanName  当前Bean对象名称
     * @param field     要注入的属性
     * @param fieldType 要注入的属性类型
     * @return 是否可以注入
     */
    protected abstract boolean fieldCanInject(Object bean, String beanName, Field field, ResolvableType fieldType);

    /**
     * 获取注入属性的值
     *
     * @param bean      当前Bean对象
     * @param beanName  当前Bean对象名称
     * @param field     要注入的属性
     * @param fieldType 要注入的属性类型
     * @return 需要注入的值
     */
    protected abstract Object getFieldInjectObject(Object bean, String beanName, Field field, ResolvableType fieldType);

    //------------------------------------------------------------------------------------------------------------
    //                                          Method Parameter Injection
    //------------------------------------------------------------------------------------------------------------

    /**
     * 是否可以通过方法来给当前的Bean对象来注入值
     *
     * @param bean      当前Bean对象
     * @param beanName  当前Bean对象名称
     * @param param     要注入的方法参数
     * @param paramType 要注入的方法参数类型
     * @return 是否可以注入
     */
    protected abstract boolean paramCanInject(Object bean, String beanName, Parameter param, ResolvableType paramType);

    /**
     * 获取注入方法参数的值
     *
     * @param bean      当前Bean对象
     * @param beanName  当前Bean对象名称
     * @param param     要注入的方法参数
     * @param paramType 要注入的方法参数类型
     * @return 需要注入的值
     */
    protected abstract Object getParamInjectObject(Object bean, String beanName, Parameter param, ResolvableType paramType);
}
