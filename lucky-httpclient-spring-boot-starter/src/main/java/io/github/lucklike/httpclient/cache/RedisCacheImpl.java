package io.github.lucklike.httpclient.cache;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.generalapi.plugin.cache.ICache;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.luckyframework.httpclient.core.serialization.SerializationConstant.JSON_SCHEME;

/**
 * 基于Spring的{@link RedisTemplate}实现的{@link ICache}，
 * 作为{@link RedisCache @RedisCache}注解的默认缓存实现
 *
 * <p>序列化机制：使用JSON_SCHEME（Jackson）进行JSON序列化，写入缓存时将方法的返回值
 * 序列化为JSON字符串；读取缓存时使用方法上下文{@link MethodContext}中的方法返回类型
 * （{@link MethodContext#getRealMethodReturnType()}）将JSON字符串反序列化为目标类型
 *
 * <p>RedisTemplate的获取规则（优先级从高到低）：
 * <pre>
 *     1.通过{@link RedisCache#redisTemplateBeanName()}属性指定的Bean（支持SpEL表达式）
 *     2.通过构造器或者{@link #setRedisTemplate(RedisTemplate)}方法显式指定的RedisTemplate，
 *       通常用于注册自定义的{@link RedisCacheImpl} Bean的场景
 *     3.Spring容器中名称为"redisTemplate"的Bean
 *     4.Spring容器中唯一的类型为{@link RedisTemplate}的Bean
 * </pre>
 *
 * <p>写入缓存时的expires参数为毫秒数，表示存入后多久过期，不大于0时表示永不过期
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/9/20 10:00
 */
public class RedisCacheImpl implements ICache {

    /**
     * 默认的RedisTemplate Bean名称
     */
    private static final String DEFAULT_REDIS_TEMPLATE_BEAN_NAME = "redisTemplate";

    private static final Logger log = LoggerFactory.getLogger(RedisCacheImpl.class);

    /**
     * 通过构造器或者setter方法显式指定的RedisTemplate
     */
    private volatile RedisTemplate<String, Object> redisTemplate;

    /**
     * 使用默认规则解析出的RedisTemplate
     */
    private volatile RedisTemplate<String, Object> defaultRedisTemplate;

    public RedisCacheImpl() {
    }

    public RedisCacheImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object get(MethodContext mc, String key) {
        checkKey(key);

        String cachedValue = readCacheValue(getRedisTemplate(mc), key);

        // 缓存未命中
        if (cachedValue == null) {
            return null;
        }

        // 缓存命中时按照方法返回类型反序列化后返回，反序列化失败时视为未命中
        try {
            return JSON_SCHEME.deserialization(cachedValue, mc.getRealMethodReturnType());
        } catch (Exception e) {
            log.warn("Failed to deserialize the cached value from Redis, it will be treated as a cache miss! key: ['{}']", key, e);
            return null;
        }
    }

    @Override
    public void put(MethodContext mc, String key, Object value) {
        put(mc, key, value, -1L);
    }

    @Override
    public void put(MethodContext mc, String key, Object value, long expires) {
        checkKey(key);
        if (value == null) {
            throw new RedisCacheException("The cached value cannot be null! key: ['{}']", key);
        }

        // 序列化
        String jsonValue;
        try {
            jsonValue = JSON_SCHEME.serialization(value);
        } catch (Exception e) {
            throw new RedisCacheException(e, "Failed to serialize the cached value! key: ['{}']", key);
        }

        // 写入Redis，expires大于0时设置过期时间，否则永久存储
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate(mc);
        try {
            if (expires > 0) {
                redisTemplate.opsForValue().set(key, jsonValue, expires, TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(key, jsonValue);
            }
        } catch (Exception e) {
            throw new RedisCacheException(e, "Failed to save the cache to Redis! key: ['{}']", key);
        }
    }

    @Override
    public void remove(MethodContext mc, String key) {
        checkKey(key);

        RedisTemplate<String, Object> redisTemplate = getRedisTemplate(mc);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RedisCacheException(e, "Failed to remove the cache from Redis! key: ['{}']", key);
        }
    }

    /**
     * 设置用于读写缓存的RedisTemplate
     *
     * @param redisTemplate RedisTemplate
     */
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取用于读写缓存的RedisTemplate
     * <pre>
     *     1.通过{@link RedisCache#redisTemplateBeanName()}属性指定的Bean
     *     2.显式指定（构造器/setter）的RedisTemplate
     *     3.使用默认规则解析出的RedisTemplate
     * </pre>
     *
     * @param mc 当前API方法对应的方法上下文
     * @return RedisTemplate
     */
    private RedisTemplate<String, Object> getRedisTemplate(MethodContext mc) {

        // 优先使用@RedisCache注解的redisTemplateBeanName属性指定的Bean
        String redisTemplateBeanName = parseRedisTemplateBeanName(mc);
        if (StringUtils.hasText(redisTemplateBeanName)) {
            return getRedisTemplateByBeanName(redisTemplateBeanName);
        }

        // 其次使用显式指定的RedisTemplate
        RedisTemplate<String, Object> template = redisTemplate;
        if (template != null) {
            return template;
        }

        // 最后使用默认规则解析出的RedisTemplate
        return getDefaultRedisTemplate();
    }

    /**
     * 解析{@link RedisCache#redisTemplateBeanName()}属性，获取指定的RedisTemplate的Bean名称
     *
     * @param mc 当前API方法对应的方法上下文
     * @return RedisTemplate的Bean名称，未指定时返回{@code null}
     */
    private static String parseRedisTemplateBeanName(MethodContext mc) {
        RedisCache redisCacheAnn = mc.getMergedAnnotationCheckParent(RedisCache.class);
        return redisCacheAnn == null
                ? null
                : mc.parseExpression(redisCacheAnn.redisTemplateBeanName(), String.class);
    }

    /**
     * 获取默认规则解析出的RedisTemplate，解析结果会被缓存下来
     *
     * @return RedisTemplate
     */
    private RedisTemplate<String, Object> getDefaultRedisTemplate() {
        RedisTemplate<String, Object> template = defaultRedisTemplate;
        if (template == null) {
            synchronized (this) {
                template = defaultRedisTemplate;
                if (template == null) {
                    defaultRedisTemplate = template = parseDefaultRedisTemplate();
                }
            }
        }
        return template;
    }

    /**
     * 校验缓存Key
     *
     * @param key 缓存Key
     */
    private static void checkKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new RedisCacheException("The Redis cache key cannot be empty! Please specify a valid key through the 'key' attribute of @RedisCache");
        }
    }

    /**
     * 从Redis中读取缓存值
     *
     * @param redisTemplate 用于读写缓存的RedisTemplate
     * @param key           缓存Key
     * @return 缓存值
     */
    private static String readCacheValue(RedisTemplate<String, Object> redisTemplate, String key) {
        Object cachedValue;
        try {
            cachedValue = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new RedisCacheException(e, "Failed to read the cache from Redis! key: ['{}']", key);
        }
        return cachedValue == null ? null : cachedValue.toString();
    }

    /**
     * 使用默认规则解析RedisTemplate
     * <pre>
     *     1.名称为"redisTemplate"的Bean
     *     2.Spring容器中唯一的类型为{@link RedisTemplate}的Bean
     * </pre>
     *
     * @return RedisTemplate
     */
    private static RedisTemplate<String, Object> parseDefaultRedisTemplate() {
        if (ApplicationContextUtils.getApplicationContext() == null) {
            throw new RedisCacheException("The Spring ApplicationContext has not been initialized yet, and the RedisTemplate cannot be obtained!");
        }

        if (ApplicationContextUtils.containsBean(DEFAULT_REDIS_TEMPLATE_BEAN_NAME)) {
            return getRedisTemplateByBeanName(DEFAULT_REDIS_TEMPLATE_BEAN_NAME);
        }

        String[] beanNames = ApplicationContextUtils.getBeanNamesForType(RedisTemplate.class);
        if (beanNames.length == 0) {
            throw new RedisCacheException("No RedisTemplate bean was found in the Spring container! Please define a RedisTemplate bean named '{}', or specify the Bean name to use through the 'redisTemplateBeanName' attribute of @RedisCache", DEFAULT_REDIS_TEMPLATE_BEAN_NAME);
        }
        if (beanNames.length > 1) {
            throw new RedisCacheException("Multiple RedisTemplate beans were found in the Spring container: {}, please specify the Bean name to use through the 'redisTemplateBeanName' attribute of @RedisCache", Arrays.toString(beanNames));
        }
        return getRedisTemplateByBeanName(beanNames[0]);
    }

    /**
     * 根据Bean名称从Spring容器中获取RedisTemplate
     *
     * @param beanName Bean名称
     * @return RedisTemplate
     */
    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> getRedisTemplateByBeanName(String beanName) {
        if (ApplicationContextUtils.getApplicationContext() == null) {
            throw new RedisCacheException("The Spring ApplicationContext has not been initialized yet, and the RedisTemplate named ['{}'] cannot be obtained!", beanName);
        }
        if (!ApplicationContextUtils.containsBean(beanName)) {
            throw new RedisCacheException("The RedisTemplate bean named ['{}'] does not exist in the Spring container! Please check the 'redisTemplateBeanName' attribute of @RedisCache", beanName);
        }
        return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(beanName, RedisTemplate.class);
    }
}
