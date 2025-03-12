package io.github.lucklike.httpclient.discovery.eureka;

import com.luckyframework.httpclient.proxy.annotations.DomainNameMeta;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

;

/**
 * 提供对Eureka注册中心的支持, 注：使用此注解需要导入Eureka相关的依赖
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/12 12:07
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@HttpClientComponent
@DomainNameMeta(getter = @ObjectGenerate(EurekaDomainGetter.class))
public @interface EurekaHttpClient {

    /**
     * 同name
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 用于获取{@link com.netflix.discovery.EurekaClient}实例的SpEL表达式
     */
    String eurekaClient() default "";

    /**
     * 请求Eureka服务器使用HTTP还是HTTPS false->HTTP; true->HTTPS
     */
    String secure() default "false";

    /**
     * 支持SpEL表达式
     * 协议（http or https）
     */
    String protocol() default "http";

    /**
     * 支持SpEL表达式
     * 服务名
     */
    @AliasFor("value")
    String name() default "";

    /**
     * 支持SpEL表达式
     * path
     */
    String path() default "";
}
