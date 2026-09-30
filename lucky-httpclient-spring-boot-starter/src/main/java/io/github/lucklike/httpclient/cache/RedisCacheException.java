package io.github.lucklike.httpclient.cache;

import com.luckyframework.exception.LuckyRuntimeException;

/**
 * Redis缓存异常
 */
public class RedisCacheException extends LuckyRuntimeException {

    public RedisCacheException(String message) {
        super(message);
    }

    public RedisCacheException(Throwable ex) {
        super(ex);
    }

    public RedisCacheException(String message, Throwable ex) {
        super(message, ex);
    }

    public RedisCacheException(String messageTemplate, Object... args) {
        super(messageTemplate, args);
    }

    public RedisCacheException(Throwable ex, String messageTemplate, Object... args) {
        super(ex, messageTemplate, args);
    }
}
