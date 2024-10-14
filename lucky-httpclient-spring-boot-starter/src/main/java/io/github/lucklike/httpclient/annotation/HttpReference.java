package io.github.lucklike.httpclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于支持属性/方法注入的注解
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/10/09 14:50
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpReference {

    /**
     * 代理模式
     */
    ProxyModel proxyModel() default ProxyModel.AUTO;
}
