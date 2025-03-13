package io.github.lucklike.httpclient.discovery;

import com.luckyframework.httpclient.proxy.annotations.DomainNameMeta;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HttpClient注解，用于将某个接口声明为httpclient代理接口
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:06
 * @see DomainNameMeta
 * @see HttpClientComponent
 * @see CommonDomainNameGetter
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@DomainNameMeta(getter = @ObjectGenerate(CommonDomainNameGetter.class))
@HttpClientComponent
public @interface HttpClient {

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClientComponent.class, attribute = "name")
    String beanId() default "";

    /**
     * 同url
     */
    String value() default "";

    /**
     * 支持SpEL表达式
     * 用于获取URL的表达式
     */
    @AliasFor("value")
    String url() default "";

    /**
     * 支持SpEL表达式
     * 服务名称，用于注册中心发现服务
     */
    String service() default "";

    /**
     * 支持SpEL表达式
     * path，全局路径前缀，请求时会自动加上
     */
    String path() default "";

    /**
     * 代理模式
     */
    @AliasFor(annotation = HttpClientComponent.class, attribute = "proxyModel")
    ProxyModel proxyModel() default ProxyModel.DEFAULT;

}
