package io.github.lucklike.httpclient.dbclient.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 02:31
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Column
public @interface Id {

    @AliasFor(annotation = Column.class, attribute = "value")
    String value() default "";

    Type type() default Type.MANUAL_SETTINGS;

    enum Type {
        AUTO_INCREMENT,
        MANUAL_SETTINGS
    }
}
