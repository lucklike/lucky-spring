package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.generalapi.plugin.cache.MemoryCache;
import com.luckyframework.httpclient.generalapi.plugin.cache.MemoryCacheConfig;
import com.luckyframework.httpclient.generalapi.plugin.cache.MemoryCacheImpl;
import com.luckyframework.httpclient.proxy.context.MethodContext;

/**
 * {@link StdCacheAdapter @StdHttpClient缓存适配器}使用的进程内缓存实现，继承自{@link MemoryCacheImpl}
 *
 * <p>与{@link MemoryCacheImpl}的区别在于缓存配置的来源：{@link MemoryCacheImpl}默认从
 * {@link MemoryCache @MemoryCache}注解中解析缓存配置，本实现则重写了{@link MemoryCacheImpl#getConfig(MethodContext)}
 * 方法，直接使用随构造器传入的缓存配置，该配置来源于{@link CacheConfig @StdHttpClient缓存配置}：
 * <pre>
 *     1.容量控制：通过{@link CacheConfig#getMemoryCapacity()}配置最大容量，容量超限时按最近最少使用(LRU)策略淘汰数据
 *     2.磁盘持久化：通过{@link CacheConfig#getMemorySaveDir()}配置保存目录后，缓存数据会以JSON格式持久化到该目录中，
 *       应用启动后首次访问时会自动从磁盘恢复数据
 * </pre>
 *
 * @author fukang
 * @version 3.0.3
 * @since 2026-09-24
 */
public class StdMemoryCache extends MemoryCacheImpl {

    /**
     * 缓存的最大容量，小于等于0时表示不限制容量
     */
    private final long capacity;

    /**
     * 缓存数据保存的目录，空白字符串表示不保存到磁盘
     */
    private final String saveDir;

    /**
     * 构造方法
     *
     * @param capacity 缓存的最大容量，小于等于0时表示不限制容量
     * @param saveDir  缓存数据保存的目录，空白字符串表示不保存到磁盘
     */
    public StdMemoryCache(long capacity, String saveDir) {
        this.capacity = capacity;
        this.saveDir = saveDir;
    }

    /**
     * 获取缓存配置，直接返回随构造器传入的缓存配置
     *
     * @param mc 当前API方法对应的方法上下文
     * @return 缓存配置
     */
    @Override
    protected MemoryCacheConfig getConfig(MethodContext mc) {
        return new MemoryCacheConfig(capacity, saveDir);
    }
}
