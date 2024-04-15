package io.github.lucklike.httpclient.annotation;

import com.luckyframework.httpclient.proxy.spel.FunctionPrefix;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将一个工具类声明为一个SpEL函数
 * @author fukang
 * @version 1.0.0
 * @date 2024/4/1514 15:23
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@FunctionPrefix
public @interface SpELFunction {

    /**
     * 方法前缀
     */
    @AliasFor(annotation = FunctionPrefix.class, attribute = "prefix")
    String value() default "";

    /**
     * 方法前缀
     */
    @AliasFor(annotation = FunctionPrefix.class, attribute = "prefix")
    String prefix() default "";
}
