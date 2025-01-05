package io.github.lucklike.httpclient.config.impl;

import io.github.lucklike.httpclient.config.HttpExecutorFactory;

import java.net.HttpURLConnection;

/**
 * HTTP执行器枚举
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/10/7 01:20
 */
public enum HttpExecutorEnum {

    /**
     * 基于JDK{@link HttpURLConnection}实现的执行器枚举配置
     */
    JDK(new JdkHttpExecutorFactory()),

    /**
     * 基于Okhttp3实现的执行器枚举配置
     */
    OKHTTP(new OkHttpExecutorFactory()),

    /**
     * 基于Apache HttpClient实现的执行器枚举配置
     */
    HTTP_CLIENT(new HttpClientExecutorFactory());

    private final HttpExecutorFactory factory;

    HttpExecutorEnum(HttpExecutorFactory factory) {
        this.factory = factory;
    }

    public HttpExecutorFactory HttpExecutorFactory() {
        return factory;
    }
}
