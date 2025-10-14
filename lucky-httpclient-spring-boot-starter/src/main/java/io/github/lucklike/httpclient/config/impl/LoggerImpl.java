package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.httpclient.proxy.logging.BeautifulLoggerPrintHandler;
import com.luckyframework.httpclient.proxy.logging.LoggerHandler;
import com.luckyframework.httpclient.proxy.logging.SimpleLoggerPrintHandler;

/**
 * 日志打印实现
 */
public enum LoggerImpl {
    /**
     * 简单格式的日志打印
     */
    SIMPLE(new SimpleLoggerPrintHandler()),

    /**
     * 漂亮格式的日志打印
     */
    BEAUTIFUL(new BeautifulLoggerPrintHandler());

    private final LoggerHandler loggerHandler;

    LoggerImpl(LoggerHandler loggerHandler) {
        this.loggerHandler = loggerHandler;
    }

    public LoggerHandler getLoggerHandler() {
        return loggerHandler;
    }
}
