package io.github.lucklike.httpclient.config.simple;

import com.luckyframework.httpclient.proxy.annotations.ValueUnpack;
import com.luckyframework.reflect.Combination;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 初始化参数绑定注解，需要配合{@link SimpleHttpClient}注解一起使用
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/7 00:58
 * @see InitContextValueUnpack
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ValueUnpack(unpackClass = InitContextValueUnpack.class)
@Combination(ValueUnpack.class)
public @interface Init {

    /**
     * 指定需要绑定的参数配置项
     */
    String[] value() default {};
}
