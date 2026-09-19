package io.github.lucklike.httpclient.cache;

import com.luckyframework.common.FontUtil;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.generalapi.token.TokenCacheException;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.plugin.ProxyDecorator;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.luckyframework.httpclient.core.serialization.SerializationConstant.JSON_SCHEME;

/**
 * 用于实现Redis缓存的代理插件
 * <pre>
 *     1.注：使用该插件的方法必须要通过{@link RedisCache}注解来配置缓存信息
 *     2.先从Redis中获取缓存，获取到则反序列化后直接返回；获取不到时调用真实方法
 *     3.调用真实方法获取到的返回值不允许为空，不为空时会使用JSON序列化后写入Redis
 *     4.缓存的Key必须要通过{@link RedisCache#key()}属性手动指定
 *     5.缓存的过期时间由{@link RedisCache#expires()}属性指定（单位毫秒），
 *       未指定（或者配置的值不大于0）时缓存将会被永久存储在Redis中
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/9/18 10:00
 */
public final class RedisCacheProxyPlugin implements ProxyPlugin {

    /**
     * 默认的RedisTemplate Bean名称
     */
    private static final String DEFAULT_REDIS_TEMPLATE_BEAN_NAME = "redisTemplate";

    private static final Logger log = LoggerFactory.getLogger(RedisCacheProxyPlugin.class);

    @Override
    public Object decorate(ProxyDecorator decorator) {
        MethodMetaContext mc = decorator.getMeta().getMethodMetaContext();
        ResolvableType returnResolvableType = mc.getMethodConvertReturnResolvableType();

        // 获取当前方法的位置信息
        String methodLocation = FontUtil.getYellowUnderline(MethodUtils.getLocation(mc.getCurrentAnnotatedElement()));

        // 获取注解配置信息
        RedisCache ann = mc.getMergedAnnotation(RedisCache.class);
        String key = mc.parseExpression(ann.key(), String.class);

        // Key必须要手动指定
        if (!StringUtils.hasText(key)) {
            throw new TokenCacheException("The 'key' attribute of @RedisCache cannot be empty! decorate method is ['{}']", methodLocation);
        }

        String redisTemplateBeanName = mc.parseExpression(ann.redisTemplate(), String.class);
        Long expires = parseExpires(mc, ann, methodLocation);

        RedisTemplate<String, Object> redisTemplate = getRedisTemplate(redisTemplateBeanName);

        // 先从Redis中获取缓存
        Object cachedValue;
        try {
            cachedValue = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new TokenCacheException(e, "Failed to read the cache from redis! key: ['{}']", key);
        }

        // 缓存命中时直接反序列化后返回，反序列化失败时视为未命中
        if (cachedValue != null) {
            try {
                return JSON_SCHEME.deserialization(cachedValue.toString(), returnResolvableType.getType());
            } catch (Exception e) {
                log.warn("Failed to deserialize the cached value, it will be treated as a cache miss! key: [{}], decorate method is ['{}']", key, methodLocation, e);
            }
        }

        // 缓存未命中时调用真实方法获取返回值
        Object returnValue;
        try {
            returnValue = decorator.proceed();
        } catch (Throwable e) {
            throw new TokenCacheException(e, "RedisCacheProxyPlugin failed to obtain the cache value! decorate method is ['{}']", methodLocation);
        }

        // 获取到的返回值不允许为空
        if (returnValue == null) {
            throw new TokenCacheException("The return value obtained by the decorate method cannot be null! decorate method is ['{}']", methodLocation);
        }

        // 将返回值序列化后写入Redis，expires大于0时设置过期时间，否则永久存储
        try {
            String serializedValue = JSON_SCHEME.serialization(returnValue);
            if (expires != null && expires > 0) {
                redisTemplate.opsForValue().set(key, serializedValue, expires, TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(key, serializedValue);
            }
        } catch (Exception e) {
            throw new TokenCacheException(e, "Failed to save the cache to redis! key: ['{}']", key);
        }

        return returnValue;
    }

    /**
     * 解析{@link RedisCache#expires()}属性的值（单位毫秒）
     *
     * @param mc             方法元数据上下文
     * @param ann            RedisCache注解
     * @param methodLocation 方法位置信息
     * @return 过期时间（单位毫秒）
     */
    private static Long parseExpires(MethodMetaContext mc, RedisCache ann, String methodLocation) {
        try {
            return mc.parseExpression(ann.expires(), Long.class);
        } catch (Exception e) {
            throw new TokenCacheException(e, "Failed to resolve the 'expires' attribute of @RedisCache! expires: '{}', decorate method is ['{}']", ann.expires(), methodLocation);
        }
    }

    /**
     * 获取用于读写缓存数据的RedisTemplate
     *
     * @param redisTemplateBeanName RedisTemplate Bean名称
     * @return RedisTemplate
     */
    private static RedisTemplate<String, Object> getRedisTemplate(String redisTemplateBeanName) {
        return StringUtils.hasText(redisTemplateBeanName)
                ? getRedisTemplateByBeanName(redisTemplateBeanName)
                : getDefaultRedisTemplate();
    }

    /**
     * 根据Bean名称从Spring容器中获取RedisTemplate
     *
     * @param beanName Bean名称
     * @return RedisTemplate
     */
    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> getRedisTemplateByBeanName(String beanName) {
        if (!ApplicationContextUtils.containsBean(beanName)) {
            throw new TokenCacheException("The RedisTemplate bean named ['{}'] does not exist in the Spring container", beanName);
        }
        return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(beanName, RedisTemplate.class);
    }

    /**
     * 获取默认的RedisTemplate
     * <pre>
     *     1.优先使用名称为"redisTemplate"的Bean
     *     2.不存在时按照类型匹配Spring容器中唯一的RedisTemplate Bean
     * </pre>
     *
     * @return RedisTemplate
     */
    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> getDefaultRedisTemplate() {
        if (ApplicationContextUtils.containsBean(DEFAULT_REDIS_TEMPLATE_BEAN_NAME)) {
            return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(DEFAULT_REDIS_TEMPLATE_BEAN_NAME, RedisTemplate.class);
        }

        String[] beanNames = ApplicationContextUtils.getBeanNamesForType(RedisTemplate.class);
        if (beanNames.length == 0) {
            throw new TokenCacheException("No RedisTemplate bean found in the Spring container! Please define a RedisTemplate bean named '{}', or specify the Bean name to use through the 'redisTemplate' attribute of @RedisCache", DEFAULT_REDIS_TEMPLATE_BEAN_NAME);
        }
        if (beanNames.length > 1) {
            throw new TokenCacheException("Multiple RedisTemplate beans found in the Spring container: {}, please specify the Bean name to use through the 'redisTemplate' attribute of @RedisCache", Arrays.toString(beanNames));
        }
        return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(beanNames[0], RedisTemplate.class);
    }
}
