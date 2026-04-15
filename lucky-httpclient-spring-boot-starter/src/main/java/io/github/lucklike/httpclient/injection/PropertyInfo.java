package io.github.lucklike.httpclient.injection;


import com.luckyframework.httpclient.proxy.spel.WrapType;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.function.Supplier;

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
     * 包装类型
     */
    private final WrapType wrapType;

    /**
     * 私有构造器
     *
     * @param element 元素信息
     * @param type    元素的类型
     */
    private PropertyInfo(AnnotatedElement element, ResolvableType type) {
        this.element = element;
        this.type = type;
        this.wrapType = WrapType.of(type);
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


    /**
     * 获取包装类型
     *
     * @return 包装类型
     */
    @NonNull
    public WrapType getWrapType() {
        return wrapType;
    }

    /**
     * 获取真实类型{@link ResolvableType}
     *
     * @return 真实类型{@link ResolvableType}
     */
    @NonNull
    public ResolvableType getTargetResolvableType() {
        return wrapType.getTargetType(type);
    }

    /**
     * 获取真实类型{@link Class}
     *
     * @return 真实类型{@link Class}
     */
    @NonNull
    public Class<?> getTargetClass() {
        ResolvableType targetResolvableType = getTargetResolvableType();
        return getTargetResolvableType().toClass();
    }

    /**
     * 对Value进行包装
     *
     * @param value 待包装的值
     * @return 包装后的值
     */
    public Object wrapValue(Object value) {
        return wrapType.wrap(() -> value);
    }

    /**
     * 对Value进行包装
     *
     * @param valueSupplier 待包装的值
     * @return 包装后的值
     */
    public Object wrapValue(Supplier<?> valueSupplier) {
        return wrapType.wrap(valueSupplier);
    }
}
