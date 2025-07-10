package io.github.lucklike.httpclient.injection;

import com.luckyframework.exception.LuckyRuntimeException;

public class BindException extends LuckyRuntimeException {
    public BindException(String message) {
        super(message);
    }

    public BindException(Throwable ex) {
        super(ex);
    }

    public BindException(String message, Throwable ex) {
        super(message, ex);
    }

    public BindException(String messageTemplate, Object... args) {
        super(messageTemplate, args);
    }

    public BindException(Throwable ex, String messageTemplate, Object... args) {
        super(ex, messageTemplate, args);
    }
}
