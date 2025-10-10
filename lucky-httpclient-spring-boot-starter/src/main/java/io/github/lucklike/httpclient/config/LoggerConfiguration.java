package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.logging.LoggerHandler;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 日志相关的配置
 */
public class LoggerConfiguration {

    /**
     * 处理类
     */
    private Class<LoggerHandler> handlerClass;

    /**
     * 默认的日志处理器配置
     */
    @NestedConfigurationProperty
    private DefaultLoggerConfiguration defaultHandlerConfig;


    /**
     * 获取用于日志处理的处理类
     *
     * @return 用于日志处理的处理类
     */
    public Class<LoggerHandler> getHandlerClass() {
        return handlerClass;
    }


    /**
     * 设置用于日志处理的处理类
     *
     * @param handlerClass 用于日志处理的处理类
     */
    public void setHandlerClass(Class<LoggerHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     * 获取默认日志处理器的相关配置
     *
     * @return 默认日志处理器的相关配置
     */
    public DefaultLoggerConfiguration getDefaultHandlerConfig() {
        return defaultHandlerConfig;
    }

    /**
     * 设置默认日志处理器的相关配置
     *
     * @param defaultHandlerConfig 默认日志处理器的相关配置
     */
    public void setDefaultHandlerConfig(DefaultLoggerConfiguration defaultHandlerConfig) {
        this.defaultHandlerConfig = defaultHandlerConfig;
    }
}
