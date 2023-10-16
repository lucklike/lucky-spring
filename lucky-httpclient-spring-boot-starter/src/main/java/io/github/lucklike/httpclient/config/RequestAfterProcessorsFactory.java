package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.RequestAfterProcessor;

import java.util.List;

/**
 * {@link RequestAfterProcessor 请求处理器工厂}
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 05:07
 */
@FunctionalInterface
public interface RequestAfterProcessorsFactory {

    /**
     * 获取{@link RequestAfterProcessor 请求处理器工厂}
     * @return 请求处理器工厂
     */
    List<RequestAfterProcessor> getRequestAfterProcessors();
}
