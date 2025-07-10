package io.github.lucklike.httpclient.injection;


import org.springframework.core.ResolvableType;

import java.lang.reflect.AnnotatedElement;

/**
 * 属性信息
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/06/27 12:06
 */
public class PropertyInfo {

    /**
     * 属性元素信息{@link java.lang.reflect.Field} Or {@link java.lang.reflect.Parameter}
     */
    private final AnnotatedElement element;

    /**
     * 属性元素的类型
     */
    private final ResolvableType type;

    /**
     * 私有构造器
     *
     * @param element 元素信息
     * @param type    元素的类型
     */
    private PropertyInfo(AnnotatedElement element, ResolvableType type) {
        this.element = element;
        this.type = type;
    }

    /**
     * 获取属性信息实例
     *
     * @param element 元素信息
     * @param type    元素的类型
     * @return 属性信息
     */
    public static PropertyInfo of(AnnotatedElement element, ResolvableType type) {
        return new PropertyInfo(element, type);
    }

    /**
     * 获取属性元素信息
     *
     * @return 属性元素信息
     */
    public AnnotatedElement getElement() {
        return element;
    }

    /**
     * 获取属性元素类型
     *
     * @return 属性元素类型
     */
    public ResolvableType getType() {
        return type;
    }
}
