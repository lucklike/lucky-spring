package io.github.lucklike.httpclient.configapi;

import com.luckyframework.httpclient.proxy.configapi.EnableConfigurationParser;
import com.luckyframework.httpclient.proxy.interceptor.PriorityConstant;
import com.luckyframework.reflect.Combination;
import io.github.lucklike.httpclient.annotation.LuckyComponent;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.luckyframework.httpclient.proxy.configapi.Source.RESOURCE;


/**
 * <b>如下所有配置均支持松散绑定</b><br/><br/>
 * 声明式Http客户端的注解，支持从本地文件中获取请求与响应转化的相关配置<br/>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:06
 * @see EnableConfigurationParser
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@LuckyComponent
@EnableConfigurationParser(sourceType = RESOURCE)
@Combination({EnableConfigurationParser.class})
public @interface ResourceHttpClient {

    /**
     * 配置源信息
     */
    @AliasFor(annotation = EnableConfigurationParser.class, attribute = "source")
    String value() default "classpath:/api/#{$class$.getSimpleName()}.yml";

    /**
     * 定义配置前缀
     */
    @AliasFor(annotation = EnableConfigurationParser.class, attribute = "prefix")
    String prefix() default "";

    /**
     * 配置源信息
     */
    @AliasFor(annotation = EnableConfigurationParser.class, attribute = "source")
    String source() default "classpath:/api/#{$class$.getSimpleName()}.yml";


    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = LuckyComponent.class, attribute = "name")
    String name() default "";


    /**
     * 拦截器优先级，数值越高优先级越低
     */
    @AliasFor(annotation = EnableConfigurationParser.class, attribute = "priority")
    int priority() default PriorityConstant.CONFIG_API_PRIORITY;

    /**
     * 代理模式
     */
    @AliasFor(annotation = LuckyComponent.class, attribute = "proxyModel")
    ProxyModel proxyModel() default ProxyModel.AUTO;
}
