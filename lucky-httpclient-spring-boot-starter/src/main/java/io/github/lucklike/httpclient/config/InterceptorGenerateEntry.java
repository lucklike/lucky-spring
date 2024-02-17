package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.interceptor.Interceptor;

/**
 * 拦截器生成对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/2/17 20:46
 */
public class InterceptorGenerateEntry extends GenerateEntry<Interceptor> {
    private Integer priority;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
