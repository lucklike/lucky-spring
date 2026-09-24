package io.github.lucklike.httpclient.std;

import io.github.lucklike.httpclient.cache.RedisCacheImpl;

/**
 * 缓存类型，用于指定{@link CacheConfig @StdHttpClient缓存配置}使用哪种缓存实现
 *
 * <pre>
 *     MEMORY：进程内缓存，对应{@link StdMemoryCache}实现，也是默认使用的缓存类型
 *     REDIS ：分布式缓存，对应{@link RedisCacheImpl}实现
 * </pre>
 *
 * @author fukang
 * @version 3.0.3
 * @since 2026-09-23
 */
public enum CacheType {

    /**
     * 进程内缓存，对应{@link StdMemoryCache}实现，也是默认使用的缓存类型
     */
    MEMORY,

    /**
     * 分布式缓存，对应{@link RedisCacheImpl}实现
     */
    REDIS
}
