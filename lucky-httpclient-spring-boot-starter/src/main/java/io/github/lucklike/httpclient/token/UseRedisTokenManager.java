package io.github.lucklike.httpclient.token;

import com.luckyframework.httpclient.generalapi.describe.TokenApi;
import com.luckyframework.httpclient.proxy.plugin.Plugin;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用基于Redis的Token管理器功能
 * <pre>
 *     1.注：使用该插件的方法返回值类型必须要实现{@link RedisToken}接口
 *     2.底层使用Spring的{@link RedisTemplate}来读写Redis，调用真实方法刷新Token后：
 *        a.将方法返回的Token对象序列化后作为值写入Redis
 *        b.使用{@link RedisToken#expires()}（过期时间，单位毫秒）直接作为过期时间(TTL)写入Redis，
 *          过期后Redis将会自动清除缓存的Token
 *     3.缓存的Token未过期时将会一直使用Redis中缓存的Token，不会调用真实方法重新获取Token
 *     4.缓存的Token不存在或者已经过期时，将会调用真实方法重新获取Token
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
@Plugin(pluginClass = RedisTokenCacheProxyPlugin.class)
public @interface UseRedisTokenManager {

    /**
     * 同 key
     */
    @AliasFor("key")
    String value() default "";

    /**
     * 用于缓存Token的Redis Key，支持SpEL表达式
     * <pre>
     *     该属性可以不配置，不配置时将会使用当前方法的位置信息作为Key，
     *     格式为：全限定类名#方法名(参数)，例如：com.example.TokenApi#getToken()
     * </pre>
     */
    @AliasFor("value")
    String key() default "";

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
