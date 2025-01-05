package io.github.lucklike.httpclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注允许为null的注解
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/1/5 03:36
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AllowNull {
    boolean value() default true;
}
