package io.github.lucklike.httpclient.discovery.nacos;

import com.alibaba.nacos.api.common.Constants;
import com.luckyframework.httpclient.proxy.annotations.DomainNameMeta;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.lucklike.httpclient.discovery.Constant.NACOS_DOMAIN_GETTER_BEAN_NAME;

/**
 * 提供对Nacos注册中心的支持, 注：使用此注解需要导入Nacos相关的依赖
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/12 12:07
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@HttpClientComponent
@DomainNameMeta(getter = @ObjectGenerate(msg = NACOS_DOMAIN_GETTER_BEAN_NAME))
public @interface NacosHttpClient {

    /**
     * 集群
     */
    String[] clusters() default {};

    /**
     * 同name
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 用于获取{@link com.alibaba.nacos.api.naming.NamingService}实例的SpEL表达式
     */
    String namingService() default "";

    /**
     * 支持SpEL表达式
     * 协议（http or https）
     */
    String protocol() default "http";

    /**
     * 支持SpEL表达式
     * 服务所在的Group
     */
    String group() default Constants.DEFAULT_GROUP;

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
