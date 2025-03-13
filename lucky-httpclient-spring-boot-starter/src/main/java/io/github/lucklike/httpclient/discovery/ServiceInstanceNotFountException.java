package io.github.lucklike.httpclient.discovery;

import com.luckyframework.exception.LuckyRuntimeException;

/**
 * 服务实例找不到异常
 */
public class ServiceInstanceNotFountException extends LuckyRuntimeException {
    public ServiceInstanceNotFountException(String message) {
        super(message);
    }

    public ServiceInstanceNotFountException(Throwable ex) {
        super(ex);
    }

    public ServiceInstanceNotFountException(String message, Throwable ex) {
        super(message, ex);
    }

    public ServiceInstanceNotFountException(String messageTemplate, Object... args) {
        super(messageTemplate, args);
    }

    public ServiceInstanceNotFountException(Throwable ex, String messageTemplate, Object... args) {
        super(ex, messageTemplate, args);
    }
}
