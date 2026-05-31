package io.github.lucklike.httpclient.dbclient.annotation;

import io.github.lucklike.httpclient.dbclient.function.Condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列描述注解
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 02:31
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Column {

    /**
     * 列名
     */
    String value() default "";

    /**
     * 是否是数据库字段
     */
    boolean exist() default true;

    /**
     * 条件拼接策略
     */
    Class<? extends Condition> condition() default Condition.Eq.class;

}
