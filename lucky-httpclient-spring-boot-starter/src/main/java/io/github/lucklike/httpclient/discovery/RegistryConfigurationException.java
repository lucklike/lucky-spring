package io.github.lucklike.httpclient.discovery;

import com.luckyframework.exception.LuckyRuntimeException;

public class RegistryConfigurationException extends LuckyRuntimeException {
    public RegistryConfigurationException(String message) {
        super(message);
    }

    public RegistryConfigurationException(Throwable ex) {
        super(ex);
    }

    public RegistryConfigurationException(String message, Throwable ex) {
        super(message, ex);
    }

    public RegistryConfigurationException(String messageTemplate, Object... args) {
        super(messageTemplate, args);
    }

    public RegistryConfigurationException(Throwable ex, String messageTemplate, Object... args) {
        super(ex, messageTemplate, args);
    }
}
