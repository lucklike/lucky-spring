package io.github.lucklike.httpclient.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明式Http客户端的注解，被标记的接口将会被Lucky自动发现并代理
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:06
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface HttpClientComponent {

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String name() default "";

}
