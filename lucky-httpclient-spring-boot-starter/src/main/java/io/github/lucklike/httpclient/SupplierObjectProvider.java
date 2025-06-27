package io.github.lucklike.httpclient;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 基于{@link Supplier}来实现的${@link ObjectProvider}
 *
 * @param <T> 懒加载对象的类型
 */
public class SupplierObjectProvider<T> implements ObjectProvider<T>, Serializable {

    private final Supplier<T> supplier;

    private SupplierObjectProvider(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> SupplierObjectProvider<T> of(Supplier<T> supplier) {
        return new SupplierObjectProvider<>(supplier);
    }

    @Override
    public T getObject(Object... args) throws BeansException {
        return supplier.get();
    }

    @Override
    public T getIfAvailable() throws BeansException {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public T getIfUnique() throws BeansException {
        return getIfAvailable();
    }

    @Override
    public T getObject() throws BeansException {
        return supplier.get();
    }
}
