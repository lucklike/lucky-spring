package io.github.lucklike.httpclient.annotation;

import io.github.lucklike.httpclient.HttpReferenceAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启{@link HttpReference @HttpReference}注解注入功能
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 01:45
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HttpReferenceAnnotationBeanPostProcessor.class)
public @interface HttpReferenceAutoImport {

}
