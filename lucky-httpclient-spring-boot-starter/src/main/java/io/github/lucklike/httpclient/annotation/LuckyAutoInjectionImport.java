package io.github.lucklike.httpclient.annotation;

import io.github.lucklike.httpclient.injection.HttpReference;
import io.github.lucklike.httpclient.injection.LuckyAutoInjectionBeanPostProcessor;
import io.github.lucklike.httpclient.injection.Bind;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启Lucky的属于注入功能，支持的注入注解如下
 * <pre>
 *     1.{@link HttpReference @HttpReference}  注入HTTP代理对象
 *     2.{@link Bind @Bind}                    将某段环境值绑定到指定的Java对象上
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 01:45
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LuckyAutoInjectionBeanPostProcessor.class)
public @interface LuckyAutoInjectionImport {

}
