package io.github.lucklike.httpclient.cache;

import com.luckyframework.httpclient.generalapi.describe.TokenApi;
import com.luckyframework.httpclient.proxy.plugin.Plugin;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用基于Redis的缓存功能
 * <pre>
 *     1.注：使用该注解的方法必须要通过{@link #key()}属性手动指定缓存的Redis Key
 *     2.缓存读写机制：
 *        a.先从Redis中获取缓存，获取到则反序列化后直接返回，不会调用真实方法
 *        b.未获取到缓存时调用真实方法，将方法的返回值使用JSON序列化后作为值写入Redis
 *     3.方法的返回值不允许为空，为空时将会抛出异常
 *     4.可以在{@link #expires()}中指定缓存的过期时间（单位毫秒），未指定时缓存将会被永久存储
 *     5.底层使用Spring的{@link RedisTemplate}来读写Redis，可以通过{@link #redisTemplate()}属性指定要使用的RedisTemplate
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/9/18 10:00
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@TokenApi
@Plugin(pluginClass = RedisCacheProxyPlugin.class)
public @interface RedisCache {

    /**
     * 用于缓存的Redis Key，必须要手动指定，支持SpEL表达式
     * <pre>
     *     未指定或者解析结果为空时将会抛出异常
     * </pre>
     */
    String key();

    /**
     * 指定缓存的过期时间（单位毫秒），支持SpEL表达式
     * <pre>
     *     1.配置的值大于0时，该值将会作为Key的过期时间(TTL)写入Redis，过期后Redis将会自动清除缓存
     *     2.未配置（默认值为-1）或者配置的值不大于0时，缓存将会被永久存储在Redis中
     * </pre>
     */
    String expires() default "-1";

    /**
     * 指定要使用的{@link RedisTemplate}Bean的名称，支持SpEL表达式
     * <pre>
     *     1.配置了该属性时，使用指定名称的Bean
     *     2.未配置该属性时，优先使用名称为"redisTemplate"的Bean
     *     3.不存在名称为"redisTemplate"的Bean时，按照类型匹配Spring容器中唯一的RedisTemplate Bean
     * </pre>
     */
    String redisTemplate() default "";

}
