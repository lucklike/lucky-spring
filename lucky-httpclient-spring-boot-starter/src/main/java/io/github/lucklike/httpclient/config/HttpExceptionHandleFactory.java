package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.handle.HttpExceptionHandle;

/**
 * HTTP异常处理器工厂
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/9 09:12
 */
@FunctionalInterface
public interface HttpExceptionHandleFactory {

    HttpExceptionHandle getHttpExceptionHandle();
}
