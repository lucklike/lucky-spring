package io.github.lucklike.httpclient.config.impl;

import java.net.HttpURLConnection;

import static io.github.lucklike.httpclient.Constant.DEFAULT_HTTP_CLIENT_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_HTTP_CLIENT_V5_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_JDK_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_OKHTTP_EXECUTOR_BEAN_NAME;

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
    JDK(DEFAULT_JDK_EXECUTOR_BEAN_NAME),

    /**
     * 基于Okhttp3实现的执行器枚举配置
     */
    OKHTTP(DEFAULT_OKHTTP_EXECUTOR_BEAN_NAME),

    /**
     * 基于Apache HttpClient实现的执行器枚举配置
     */
    HTTP_CLIENT(DEFAULT_HTTP_CLIENT_EXECUTOR_BEAN_NAME),

    /**
     * 基于Apache HttpClient5实现的执行器枚举配置
     */
    HTTP_CLIENT5(DEFAULT_HTTP_CLIENT_V5_EXECUTOR_BEAN_NAME);

    private final String httpExecutorBean;

    HttpExecutorEnum(String httpExecutorBean) {
        this.httpExecutorBean = httpExecutorBean;
    }

    public String getHttpExecutorBean() {
        return httpExecutorBean;
    }
}
