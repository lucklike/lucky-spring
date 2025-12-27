package io.github.lucklike.httpclient.injection;

import io.github.lucklike.httpclient.SupplierObjectProvider;
import org.springframework.beans.factory.ObjectProvider;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 针对{@link ObjectProvider}的包装类型持有者
 * @author fukang
 * @version 1.0.0
 * @date 2025/12/26 23:54
 */
public class ObjectProviderWrapTypeHolder implements WrapTypeHolder {
    @Override
    public Class<?> getBaseType() {
        return ObjectProvider.class;
    }

    @Override
    public Function<Supplier<?>, Object> wrapFunction() {
        return s -> {
            Object obj = s.get();
            if (obj instanceof ObjectProvider){
                return obj;
            }
            return SupplierObjectProvider.of(s);
        };
    }
}
