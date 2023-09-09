package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.RequestAfterProcessor;
import com.luckyframework.httpclient.proxy.ResponseAfterProcessor;

import java.util.List;

/**
 * {@link ResponseAfterProcessor 响应处理器工厂}
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 05:07
 */
@FunctionalInterface
public interface ResponseAfterProcessorsFactory {

    /**
     * 获取{@link ResponseAfterProcessor 响应处理器工厂}
     * @return 响应处理器工厂
     */
    List<ResponseAfterProcessor> getResponseAfterProcessors();
}
