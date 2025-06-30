package io.github.lucklike.httpclient.injection;

import com.luckyframework.spel.LazyValue;
import io.github.lucklike.httpclient.SupplierObjectProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 类型转换工具
 */
public class TypeConvertUtils {

    public static final int TYPE_SOURCE = -1;
    public static final int TYPE_OBJECT_PROVIDER = 0;
    public static final int TYPE_LAZY_VALUE = 1;
    public static final int TYPE_SUPPLIER = 2;
    public static final int TYPE_OPTIONAL = 3;


    public static int getTypeType(ResolvableType sourceType) {
        if (!sourceType.hasGenerics()) {
            return TYPE_SOURCE;
        }
        Class<?> resolve = sourceType.resolve();
        if (ObjectProvider.class == resolve) {
            return TYPE_OBJECT_PROVIDER;
        }
        if (LazyValue.class == resolve) {
            return TYPE_LAZY_VALUE;
        }
        if (Supplier.class == resolve) {
            return TYPE_SUPPLIER;
        }
        if (Optional.class == resolve) {
            return TYPE_OPTIONAL;
        }
        return TYPE_SOURCE;
    }

    /**
     * 获取真实的转化类型
     *
     * @param sourceType 原始类型
     * @return 真实的转化类型
     */
    public static ResolvableType getConvertType(ResolvableType sourceType) {
        return getConvertType(getTypeType(sourceType), sourceType);
    }

    /**
     * 获取真实的转化类型
     *
     * @param typeType   类型的类型
     * @param sourceType 原始类型
     * @return 真实的转化类型
     */
    public static ResolvableType getConvertType(int typeType, ResolvableType sourceType) {
        return typeType == TYPE_SOURCE ? sourceType : sourceType.getGeneric(0);
    }

    /**
     * 获取包装值对象
     * @param typeType 类型的类型
     * @param objectSupplier 获取对象的Supplier
     * @return 对象的包装对象
     */
    public static Object getWapperObject(int typeType, Supplier<?> objectSupplier) {
        switch (typeType) {
            case TYPE_SUPPLIER: return objectSupplier;
            case TYPE_OBJECT_PROVIDER: return SupplierObjectProvider.of(objectSupplier);
            case TYPE_LAZY_VALUE: return LazyValue.of(objectSupplier);
            case TYPE_OPTIONAL: return Optional.ofNullable(objectSupplier.get());
            default: return objectSupplier.get();
        }
    }

}
