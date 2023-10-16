package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import io.github.lucklike.httpclient.config.HttpExecutorFactory;

/**
 * 基于JDK实现的Http执行器工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 07:04
 */
public class JdkHttpExecutorFactory implements HttpExecutorFactory {

    @Override
    public HttpExecutor getHttpExecutor() {
        return new JdkHttpExecutor();
    }
}
