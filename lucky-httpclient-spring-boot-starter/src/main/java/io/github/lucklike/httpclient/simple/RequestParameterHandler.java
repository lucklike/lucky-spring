package io.github.lucklike.httpclient.simple;

import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.config.simple.SimpleHttpClientConfiguration;

/**
 * 请求参数处理器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/7 02:32
 */
public interface RequestParameterHandler {

    /**
     * 请求对象刚完成初始化时执行
     *
     * @param mc      方法上下文对象
     * @param request 请求对象
     * @param config  配置信息
     */
    default void requestInit(MethodContext mc, Request request, SimpleHttpClientConfiguration config) {

    }

    /**
     * 请求对象封装完成时调用
     *
     * @param mc      方法上下文对象
     * @param request 请求对象
     * @param config  配置信息
     */
    default void requestCompleted(MethodContext mc, Request request, SimpleHttpClientConfiguration config) {

    }
}
