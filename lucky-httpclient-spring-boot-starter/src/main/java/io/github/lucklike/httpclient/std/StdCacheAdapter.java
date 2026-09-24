package io.github.lucklike.httpclient.std;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.generalapi.plugin.cache.CachePluginMeta;
import com.luckyframework.httpclient.generalapi.plugin.cache.ICache;
import com.luckyframework.httpclient.generalapi.plugin.cache.MemoryCacheImpl;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.cache.RedisCacheException;
import io.github.lucklike.httpclient.cache.RedisCacheImpl;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.luckyframework.common.StringUtils.blankReturnDefault;

/**
 * {@link StdHttpClient @StdHttpClient}的缓存适配器，用于兼容内置的两种{@link ICache}实现
 *
 * <p>缓存插件是否需要注册由{@link CachePluginMeta CachePluginMeta#enable()}控制，缓存功能未启用时本适配器不会被调用；
 * 适配器通过{@link CacheConfig @StdHttpClient缓存配置}来决定使用哪种缓存实现：
 * <pre>
 *     1.{@link CacheType#MEMORY}：委托给{@link StdMemoryCache}实现（默认）
 *     2.{@link CacheType#REDIS} ：委托给{@link RedisCacheImpl}实现
 * </pre>
 *
 * <p>每次读写缓存前都会重新读取最新的缓存配置，因此可以配合配置刷新机制动态调整缓存行为；
 * 缓存key为空时不会执行真正的缓存读写操作
 *
 * <p>{@link CacheType#MEMORY}类型缓存基于{@link MemoryCacheImpl}实现，容量以及保存目录通过
 * {@link CacheConfig#getMemoryCapacity()}与{@link CacheConfig#getMemorySaveDir()}配置（均支持SpEL表达式），
 * 内存缓存配置相同的接口会共享同一个缓存实例
 *
 * <p>{@link CacheType#REDIS}类型缓存使用的{@code RedisTemplate}的解析规则：
 * <pre>
 *     1.通过{@link CacheConfig#getRedisTemplateBeanName()}配置的Bean名称（支持SpEL表达式）
 *     2.Spring容器中名称为"redisTemplate"的Bean
 *     3.Spring容器中唯一的类型为{@link RedisTemplate}的Bean
 * </pre>
 *
 * @author fukang
 * @version 3.0.3
 * @since 2026-09-23
 */
public class StdCacheAdapter implements ICache {

    /**
     * 使用默认规则解析{@code RedisTemplate}时的Redis缓存实例标识
     */
    private static final String DEFAULT_REDIS_CACHE_KEY = "DEFAULT";

    /**
     * 内存缓存实现的集合，key由容量以及保存目录的配置值生成，
     * 内存缓存配置相同的接口共享同一个缓存实例
     */
    private final Map<String, StdMemoryCache> memoryCacheMap = new ConcurrentHashMap<>(2);

    /**
     * Redis缓存实现集合，key为配置的{@code RedisTemplate}的Bean名称，
     * 使用默认规则解析时key为{@value #DEFAULT_REDIS_CACHE_KEY}
     */
    private final Map<String, RedisCacheImpl> redisCacheMap = new ConcurrentHashMap<>(2);

    /**
     * 获取缓存数据，缓存key为空时直接返回{@code null}（视为未命中）
     *
     * @param mc  当前API方法对应的方法上下文
     * @param key 缓存key
     * @return 缓存数据
     */
    @Override
    public Object get(MethodContext mc, String key) {
        return StringUtils.hasText(key) ? getDelegate(mc).get(mc, key) : null;
    }

    /**
     * 写入缓存，缓存key为空时不做任何处理
     *
     * @param mc    当前API方法对应的方法上下文
     * @param key   缓存key
     * @param value 缓存数据
     */
    @Override
    public void put(MethodContext mc, String key, Object value) {
        if (StringUtils.hasText(key)) {
            getDelegate(mc).put(mc, key, value);
        }
    }

    /**
     * 写入缓存，并指定过期时间，缓存key为空时不做任何处理
     *
     * @param mc      当前API方法对应的方法上下文
     * @param key     缓存key
     * @param value   缓存数据
     * @param expires 过期时间，单位：毫秒，小于0时表示永不过期
     */
    @Override
    public void put(MethodContext mc, String key, Object value, long expires) {
        if (StringUtils.hasText(key)) {
            getDelegate(mc).put(mc, key, value, expires);
        }
    }

    /**
     * 移除缓存，缓存key为空时不做任何处理
     *
     * @param mc  当前API方法对应的方法上下文
     * @param key 缓存key
     */
    @Override
    public void remove(MethodContext mc, String key) {
        if (StringUtils.hasText(key)) {
            getDelegate(mc).remove(mc, key);
        }
    }

    /**
     * 获取实际使用的缓存实现
     *
     * @param mc 方法上下文
     * @return 实际使用的缓存实现
     */
    private ICache getDelegate(MethodContext mc) {
        CacheConfig cacheConfig = StdHttpClientFunction.getCacheConfig(mc);
        return cacheConfig != null && cacheConfig.getType() == CacheType.REDIS
                ? getRedisCache(mc, cacheConfig)
                : getMemoryCache(mc, cacheConfig);
    }

    /**
     * 获取{@link CacheType#MEMORY}类型缓存对应的缓存实现，
     * 使用容量以及保存目录的配置值作为缓存实例的标识，配置相同的接口共享同一个缓存实例
     *
     * @param mc          方法上下文
     * @param cacheConfig 缓存配置，未配置时使用默认配置（不限制容量、不保存到磁盘）
     * @return 内存缓存实现
     */
    private ICache getMemoryCache(MethodContext mc, CacheConfig cacheConfig) {
        String capacityExpression = cacheConfig == null ? null : cacheConfig.getMemoryCapacity();
        String saveDirExpression = cacheConfig == null ? null : cacheConfig.getMemorySaveDir();

        long capacity = StringUtils.hasText(capacityExpression)
                ? mc.parseExpression(capacityExpression, long.class)
                : -1L;
        String saveDir = StringUtils.hasText(saveDirExpression)
                ? blankReturnDefault(mc.parseExpression(saveDirExpression, String.class), "")
                : "";

        String cacheKey = capacity + "|" + saveDir;
        return memoryCacheMap.computeIfAbsent(cacheKey, _k -> new StdMemoryCache(capacity, saveDir));
    }

    /**
     * 获取{@link CacheType#REDIS}类型缓存对应的缓存实现，
     * 同一个{@code RedisTemplate}的Bean名称只会生成一个缓存实例
     *
     * @param mc          方法上下文
     * @param cacheConfig 缓存配置
     * @return Redis缓存实现
     */
    private RedisCacheImpl getRedisCache(MethodContext mc, CacheConfig cacheConfig) {
        String beanNameExpression = cacheConfig.getRedisTemplateBeanName();
        String beanName = StringUtils.hasText(beanNameExpression)
                ? mc.parseExpression(beanNameExpression, String.class)
                : null;

        String cacheKey = blankReturnDefault(beanName, DEFAULT_REDIS_CACHE_KEY);
        return redisCacheMap.computeIfAbsent(cacheKey, _k -> StringUtils.hasText(beanName)
                ? new RedisCacheImpl(getRedisTemplateByBeanName(beanName))
                : new RedisCacheImpl());
    }

    /**
     * 根据Bean名称从Spring容器中获取{@link RedisTemplate}
     *
     * @param beanName Bean名称
     * @return {@link RedisTemplate}
     */
    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> getRedisTemplateByBeanName(String beanName) {
        if (ApplicationContextUtils.getApplicationContext() == null) {
            throw new RedisCacheException("The Spring ApplicationContext has not been initialized yet, and the RedisTemplate named ['{}'] cannot be obtained!", beanName);
        }
        if (!ApplicationContextUtils.containsBean(beanName)) {
            throw new RedisCacheException("The RedisTemplate bean named ['{}'] does not exist in the Spring container! Please check the 'redisTemplateBeanName' configuration of the cache", beanName);
        }
        return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(beanName, RedisTemplate.class);
    }
}
