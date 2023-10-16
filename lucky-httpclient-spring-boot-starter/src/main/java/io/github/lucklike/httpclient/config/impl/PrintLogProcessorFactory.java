package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.httpclient.proxy.RequestAfterProcessor;
import com.luckyframework.httpclient.proxy.ResponseAfterProcessor;
import com.luckyframework.httpclient.proxy.impl.PrintLogProcessor;
import io.github.lucklike.httpclient.config.RequestAfterProcessorsFactory;
import io.github.lucklike.httpclient.config.ResponseAfterProcessorsFactory;

import java.util.Collections;
import java.util.List;

/**
 * 打印日志的响应处理器工厂
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 05:15
 */
public class PrintLogProcessorFactory implements RequestAfterProcessorsFactory, ResponseAfterProcessorsFactory {

    private final PrintLogProcessor processor = new PrintLogProcessor();

    @Override
    public List<RequestAfterProcessor> getRequestAfterProcessors() {
        return Collections.singletonList(processor);
    }

    @Override
    public List<ResponseAfterProcessor> getResponseAfterProcessors() {
        return Collections.singletonList(processor);
    }
}
