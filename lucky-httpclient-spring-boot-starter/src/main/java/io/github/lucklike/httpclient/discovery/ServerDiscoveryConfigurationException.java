package io.github.lucklike.httpclient.discovery;

import com.luckyframework.exception.LuckyRuntimeException;

/**
 * 服务发现相关配置异常
 */
public class ServerDiscoveryConfigurationException extends LuckyRuntimeException {
    public ServerDiscoveryConfigurationException(String message) {
        super(message);
    }

    public ServerDiscoveryConfigurationException(Throwable ex) {
        super(ex);
    }

    public ServerDiscoveryConfigurationException(String message, Throwable ex) {
        super(message, ex);
    }

    public ServerDiscoveryConfigurationException(String messageTemplate, Object... args) {
        super(messageTemplate, args);
    }

    public ServerDiscoveryConfigurationException(Throwable ex, String messageTemplate, Object... args) {
        super(ex, messageTemplate, args);
    }
}
