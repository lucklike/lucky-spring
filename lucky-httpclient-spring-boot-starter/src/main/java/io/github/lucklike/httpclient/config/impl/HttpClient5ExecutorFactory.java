package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.httpclient.core.executor.HttpClient5Executor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import io.github.lucklike.httpclient.config.PoolParamHttpExecutorFactory;

/**
 * 基于Apache HttpClient 5 实现的Http执行器工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/9/6 03:22
 */
public class HttpClient5ExecutorFactory extends PoolParamHttpExecutorFactory {
    @Override
    public HttpExecutor getHttpExecutor() {
        return new HttpClient5Executor(getMaxIdleConnections(), getKeepAliveDuration(), getTimeUnit());
    }
}
