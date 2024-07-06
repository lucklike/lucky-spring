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

    /**
     * 拦截器的优先级，数字越小优先级越高
     */
    private Integer priority;

    /**
     * 获取优先级
     * @return 优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 设置优先级
     * @param priority 优先级
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
