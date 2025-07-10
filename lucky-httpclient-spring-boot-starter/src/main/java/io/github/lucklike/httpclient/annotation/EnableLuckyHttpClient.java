package io.github.lucklike.httpclient.annotation;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import io.github.lucklike.httpclient.injection.HttpReference;
import io.github.lucklike.httpclient.injection.LuckyAutoInjectionBeanPostProcessor;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;

/**
 * 开启lucky-http-client声明式编程功能
 * <pre>
 *     包括:
 *     1.基于扩展点{@link LuckyAutoInjectionBeanPostProcessor}实现的属性注入功能
 *     2.基于{@link HttpClientComponent @HttpClientComponent}注解的自动扫描与注册功能
 *
 *     相当于以下两个注解的组合：
 *     {@link EnableLuckyAutoInjection @EnableLuckyAutoInjection} + {@link EnableLuckyHttpAutoScan @EnableLuckyHttpAutoScan}
 * </pre>
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 01:45
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@LuckyHttpClientScan
@LuckyAutoInjectionImport
@EnableLuckyHttpClientAutoConfiguration
public @interface EnableLuckyHttpClient {

    /**
     * 配置需要扫描的包
     */
    @AliasFor(annotation = LuckyHttpClientScan.class, attribute = "value")
    String[] value() default {};

    /**
     * 配置需要扫描的包
     */
    @AliasFor(annotation = LuckyHttpClientScan.class, attribute = "basePackages")
    String[] basePackages() default {};

    /**
     * 配置一些基本类，使用这些类的包名作为扫描包进行扫描
     */
    @AliasFor(annotation = LuckyHttpClientScan.class, attribute = "basePackageClasses")
    Class<?>[] basePackageClasses() default {};

    /**
     * Http接口代理对象是依赖{@link HttpClientProxyObjectFactory}来生成的，这里需要配置这个Bean的名称
     */
    @AliasFor(annotation = LuckyHttpClientScan.class, attribute = "proxyFactoryName")
    String proxyFactoryName() default PROXY_FACTORY_BEAN_NAME;

    /**
     * 代理模式，默认使用自适应代理方式
     */
    @AliasFor(annotation = LuckyHttpClientScan.class, attribute = "proxyModel")
    ProxyModel proxyModel() default ProxyModel.AUTO;
}
