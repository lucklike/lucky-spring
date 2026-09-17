package io.github.lucklike.httpclient.token;


import java.io.Serializable;

/**
 * RedisToken接口
 * <pre>
 *     1.使用{@link UseRedisTokenManager}注解时，方法的返回值类型必须要实现该接口，
 *       用于定义Token数据以及Token的过期时间
 *     2.刷新Token后整个Token对象将会被序列化后缓存到Redis中，缓存的Token未过期时将会一直复用，
 *       不会调用真实方法重新获取Token
 * </pre>
 *
 * @author fk7075
 * @version 3.0.3
 * @since 2026-09-18 01:01:08
 */
public interface RedisToken extends Serializable {

    /**
     * 获取Token的过期时间（单位毫秒）
     * <pre>
     *     1.返回值大于0时，该值将会直接作为Key的过期时间(TTL)写入Redis，过期后Redis将会自动清除缓存的Token
     *     2.返回值小于等于0时，该Token将不会被缓存到Redis中
     * </pre>
     *
     * @return Token的过期时间（单位毫秒）
     */
    long expires();
}
