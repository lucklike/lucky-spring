package io.github.lucklike.httpclient.annotation;

import com.luckyframework.httpclient.proxy.spel.FunctionNamespace;
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
@FunctionNamespace
public @interface SpELFunction {

    /**
     * 命名空间名称
     */
    @AliasFor(annotation = FunctionNamespace.class, attribute = "value")
    String value() default "";

}
