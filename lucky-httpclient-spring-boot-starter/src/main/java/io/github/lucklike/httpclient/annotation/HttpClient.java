package io.github.lucklike.httpclient.annotation;

import com.luckyframework.httpclient.proxy.annotations.DomainName;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * httpclient注解，用于将某个接口声明为httpclient代理接口
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:06
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DomainName
@HttpClientComponent
public @interface HttpClient {

    /**
     * 域名配置，同{@link DomainName#value()}
     */
    @AliasFor(annotation = DomainName.class, attribute = "value")
    String value() default "";

    /**
     * 域名配置，同{@link DomainName#value()}
     */
    @AliasFor(annotation = DomainName.class, attribute = "value")
    String domainName() default "";

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClientComponent.class, attribute = "name")
    String name() default "";

}
