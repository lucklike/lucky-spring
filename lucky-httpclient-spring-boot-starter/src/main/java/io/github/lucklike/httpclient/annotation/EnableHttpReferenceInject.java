package io.github.lucklike.httpclient.annotation;

import io.github.lucklike.httpclient.HttpReferenceAnnotationBeanPostProcessor;
import io.github.lucklike.httpclient.LuckyHttpAutoConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启{@link HttpReference @HttpReference}注解注入功能
 * <pre>
 *     1.向Spring容器中注入一个自动配置类{@link LuckyHttpAutoConfiguration}，来完成重要组件的初始化工作
 *     2.向Spring容器中注入一个用于支持{@link HttpReference @HttpReference}注解导入的Bean后置处理器{@link HttpReferenceAnnotationBeanPostProcessor}
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 01:45
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@HttpReferenceAutoImport
@EnableLuckyHttpClientAutoConfiguration
public @interface EnableHttpReferenceInject {

}
