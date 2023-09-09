package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.httpclient.proxy.HttpExceptionHandle;
import com.luckyframework.httpclient.proxy.impl.DefaultHttpExceptionHandle;
import io.github.lucklike.httpclient.config.HttpExceptionHandleFactory;

/**
 * 默认异常处理器工厂
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 07:07
 */
public class DefaultHttpExceptionHandleFactory implements HttpExceptionHandleFactory {
    @Override
    public HttpExceptionHandle getHttpExceptionHandle() {
        return new DefaultHttpExceptionHandle();
    }
}
