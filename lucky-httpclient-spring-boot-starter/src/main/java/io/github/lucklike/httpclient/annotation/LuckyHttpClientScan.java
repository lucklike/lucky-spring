package io.github.lucklike.httpclient.annotation;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import io.github.lucklike.httpclient.LuckyHttpClientImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;

/**
 * lucky-httpclient自动导入注解
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 01:45
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LuckyHttpClientImportBeanDefinitionRegistrar.class)
public @interface LuckyHttpClientScan {

    /**
     * 配置需要扫描的包
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 配置需要扫描的包
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * 配置一些基本类，使用这些类的包名作为扫描包进行扫描
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Http接口代理对象是依赖{@link HttpClientProxyObjectFactory}来生成的，这里需要配置这个Bean的名称
     */
    String proxyFactoryName() default PROXY_FACTORY_BEAN_NAME;

    /**
     * 是否启用Cglib代码方法，默认使用Jdk的代码方式
     */
    boolean useCglibProxy() default false;
}
