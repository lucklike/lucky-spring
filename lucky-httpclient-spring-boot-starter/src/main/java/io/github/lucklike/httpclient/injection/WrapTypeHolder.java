package io.github.lucklike.httpclient.injection;

import com.luckyframework.httpclient.proxy.spel.WrapType;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link WrapType}持有者
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/12/26 23:48
 */
public interface WrapTypeHolder {

    /**
     * 获取基本类型
     *
     * @return 基本类型
     */
    Class<?> getBaseType();

    /**
     * 对象包装函数
     *
     * @return 对象包装函数
     */
    Function<Supplier<?>, Object> wrapFunction();
}
