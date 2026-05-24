package io.github.lucklike.httpclient.dbclient.annotation;

import io.github.lucklike.httpclient.dbclient.SQLType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询类 SQL
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 05:00
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SQL(type = SQLType.UPDATE)
public @interface Delete {

    @AliasFor(annotation = SQL.class, attribute = "sql")
    String value();
}
