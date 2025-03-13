package io.github.lucklike.httpclient.discovery;

import com.luckyframework.exception.LuckyRuntimeException;

/**
 * 服务实例找不到异常
 */
public class ServiceInstanceNotFoundException extends LuckyRuntimeException {
    public ServiceInstanceNotFoundException(String message) {
        super(message);
    }

    public ServiceInstanceNotFoundException(Throwable ex) {
        super(ex);
    }

    public ServiceInstanceNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

    public ServiceInstanceNotFoundException(String messageTemplate, Object... args) {
        super(messageTemplate, args);
    }

    public ServiceInstanceNotFoundException(Throwable ex, String messageTemplate, Object... args) {
        super(ex, messageTemplate, args);
    }
}
