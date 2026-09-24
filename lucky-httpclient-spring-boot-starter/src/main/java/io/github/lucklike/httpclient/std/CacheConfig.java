package io.github.lucklike.httpclient.std;

/**
 * {@link StdHttpClient @StdHttpClient}缓存相关配置
 *
 * <p>支持类级别和方法级别两种维度的配置，方法级别的配置优先级更高。
 * 配置路径（类级别）：
 * <pre>
 *     lucky.http-client.standard-client-configs.{configId}.cache-config.*
 * </pre>
 *
 * <p>方法级别的配置路径：
 * <pre>
 *     lucky.http-client.standard-client-configs.{configId}.method-configs.{apiId}.cache-config.*
 * </pre>
 *
 * <p>配置示例：
 * <pre>
 *     lucky:
 *       http-client:
 *         standard-client-configs:
 *           GiteeApi:
 *             url: https://gitee.com/api/v5
 *             cache-config:
 *               enable: true
 *               type: memory
 *               key: "gitee:user:#{#id}"
 *               expires: "60000"
 *               # 【MEMORY类型缓存专属配置】
 *               memory-capacity: "1000"
 *               memory-save-dir: "D:/cache/gitee"
 *               # 【REDIS类型缓存专属配置】
 *               redis-template-bean-name: "giteeRedisTemplate"
 *             method-configs:
 *               getUsers:
 *                 cache-config:
 *                   enable: false
 * </pre>
 *
 * @author fukang
 * @version 3.0.3
 * @since 2026-09-23
 */
public class CacheConfig {

    /**
     * 是否开启缓存功能，只有值为{@code true}时才会启用缓存
     */
    private Boolean enable;

    /**
     * 缓存类型，未配置时默认使用{@link CacheType#MEMORY}
     */
    private CacheType type;

    /**
     * 缓存key（支持SpEL表达式），未配置时会导致缓存功能不可用
     */
    private String key;

    /**
     * 缓存过期时间，单位：毫秒（支持SpEL表达式），小于0时表示永不过期
     */
    private String expires;

    /**
     * {@link CacheType#REDIS}类型缓存使用的{@code RedisTemplate}的Bean名称（支持SpEL表达式），
     * 未配置时使用默认规则解析，详见{@link StdCacheAdapter}的说明
     */
    private String redisTemplateBeanName;

    /**
     * {@link CacheType#MEMORY}类型缓存的最大容量（支持SpEL表达式），
     * 小于等于0时表示不限制容量，缓存条目数超过容量时会按最近最少使用(LRU)策略淘汰数据
     */
    private String memoryCapacity;

    /**
     * {@link CacheType#MEMORY}类型缓存数据保存的目录（支持SpEL表达式），
     * 未配置时表示不保存到磁盘；配置该目录后，缓存数据会被持久化到该目录中，
     * 应用启动后首次访问时会自动从磁盘恢复数据
     */
    private String memorySaveDir;

    /**
     * 是否开启缓存功能
     *
     * @return 是否开启缓存功能
     */
    public Boolean getEnable() {
        return enable;
    }

    /**
     * 设置是否开启缓存功能
     *
     * @param enable 是否开启缓存功能
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * 缓存类型，未配置时默认使用{@link CacheType#MEMORY}
     *
     * @return 缓存类型
     */
    public CacheType getType() {
        return type;
    }

    /**
     * 设置缓存类型
     *
     * @param type 缓存类型
     */
    public void setType(CacheType type) {
        this.type = type;
    }

    /**
     * 缓存key（支持SpEL表达式）
     *
     * @return 缓存key
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置缓存key（支持SpEL表达式）
     *
     * @param key 缓存key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 缓存过期时间，单位：毫秒（支持SpEL表达式），小于0时表示永不过期
     *
     * @return 缓存过期时间
     */
    public String getExpires() {
        return expires;
    }

    /**
     * 设置缓存过期时间，单位：毫秒（支持SpEL表达式），小于0时表示永不过期
     *
     * @param expires 缓存过期时间
     */
    public void setExpires(String expires) {
        this.expires = expires;
    }

    /**
     * {@link CacheType#REDIS}类型缓存使用的{@code RedisTemplate}的Bean名称（支持SpEL表达式）
     *
     * @return {@code RedisTemplate}的Bean名称
     */
    public String getRedisTemplateBeanName() {
        return redisTemplateBeanName;
    }

    /**
     * 设置{@link CacheType#REDIS}类型缓存使用的{@code RedisTemplate}的Bean名称（支持SpEL表达式）
     *
     * @param redisTemplateBeanName {@code RedisTemplate}的Bean名称
     */
    public void setRedisTemplateBeanName(String redisTemplateBeanName) {
        this.redisTemplateBeanName = redisTemplateBeanName;
    }

    /**
     * {@link CacheType#MEMORY}类型缓存的最大容量（支持SpEL表达式），
     * 小于等于0时表示不限制容量
     *
     * @return 内存缓存的最大容量
     */
    public String getMemoryCapacity() {
        return memoryCapacity;
    }

    /**
     * 设置{@link CacheType#MEMORY}类型缓存的最大容量（支持SpEL表达式），
     * 小于等于0时表示不限制容量
     *
     * @param memoryCapacity 内存缓存的最大容量
     */
    public void setMemoryCapacity(String memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    /**
     * {@link CacheType#MEMORY}类型缓存数据保存的目录（支持SpEL表达式），
     * 未配置时表示不保存到磁盘
     *
     * @return 内存缓存数据保存的目录
     */
    public String getMemorySaveDir() {
        return memorySaveDir;
    }

    /**
     * 设置{@link CacheType#MEMORY}类型缓存数据保存的目录（支持SpEL表达式），
     * 未配置时表示不保存到磁盘
     *
     * @param memorySaveDir 内存缓存数据保存的目录
     */
    public void setMemorySaveDir(String memorySaveDir) {
        this.memorySaveDir = memorySaveDir;
    }
}
