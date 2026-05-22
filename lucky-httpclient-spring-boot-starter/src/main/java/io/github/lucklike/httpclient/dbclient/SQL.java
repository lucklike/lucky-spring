package io.github.lucklike.httpclient.dbclient;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义 SQL 模板的注解
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 03:16
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SQL {

    String sql();

    SQLType type();
}
