package io.github.lucklike.httpclient.cache;

import com.luckyframework.httpclient.generalapi.plugin.cache.CachePluginMeta;
import com.luckyframework.httpclient.generalapi.plugin.cache.CachePluginProhibition;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用基于Redis的缓存功能，使用{@link RedisCacheImpl}作为缓存实现，
 * key与expires的配置同{@link CachePluginMeta @CachePluginMeta}
 * <pre>
 *     1.使用该注解的方法必须要通过{@link #key()}属性手动指定缓存的Redis Key，未指定或者解析结果为空时将会抛出异常
 *     2.缓存读写机制：
 *        a.先从Redis中获取缓存，获取到则反序列化后直接返回，不会调用真实方法
 *        b.未获取到缓存或者反序列化失败时调用真实方法，将方法的返回值序列化后写入Redis
 *        c.方法的返回值为空时不会被缓存，直接返回null
 *     3.可以在{@link #expires()}中指定缓存的过期时间（单位毫秒），
 *       未指定（默认值为-1）或者配置的值不大于0时缓存将会被永久存储在Redis中
 *     4.底层使用Spring的{@link RedisTemplate}来读写Redis，
 *       可以通过{@link #redisTemplateBeanName()}属性指定要使用的RedisTemplate Bean，
 *       未指定时的获取规则详见{@link RedisCacheImpl}说明
 *     5.可以标注{@link CachePluginProhibition @CachePluginProhibition}来禁止使用当前缓存功能
 *     6.需要更细粒度的配置（如指定特定的缓存实现类）时可以直接使用{@link CachePluginMeta @CachePluginMeta}注解
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/9/20 10:00
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@CachePluginMeta(cache = RedisCacheImpl.class)
public @interface RedisCache {

    /**
     * 用于缓存的Redis Key，必须要手动指定，支持SpEL表达式
     * <pre>
     *     未指定或者解析结果为空时将会抛出异常
     * </pre>
     */
    @AliasFor(annotation = CachePluginMeta.class, attribute = "key")
    String key() default "";

    /**
     * 指定缓存的过期时间（单位毫秒），支持SpEL表达式
     * <pre>
     *     1.配置的值大于0时，该值将会作为Key的过期时间(TTL)写入Redis，过期后Redis将会自动清除缓存
     *     2.未配置（默认值为-1）或者配置的值不大于0时，缓存将会被永久存储在Redis中
     * </pre>
     */
    @AliasFor(annotation = CachePluginMeta.class, attribute = "expires")
    String expires() default "-1";

    /**
     * 指定要使用的{@link RedisTemplate}Bean的名称，支持SpEL表达式
     * <pre>
     *     1.配置了该属性时，使用Spring容器中指定名称的RedisTemplate Bean
     *     2.未配置该属性时，使用{@link RedisCacheImpl}的默认规则来获取RedisTemplate
     * </pre>
     */
    String redisTemplateBeanName() default "";

}
