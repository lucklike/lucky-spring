package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.OkHttp3Executor;
import io.github.lucklike.httpclient.config.PoolParamHttpExecutorFactory;

/**
 * 基于OkHttp3实现的Http执行器工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 07:04
 */
public class OkHttp3ExecutorFactory extends PoolParamHttpExecutorFactory {

    @Override
    public HttpExecutor getHttpExecutor() {
        return new OkHttp3Executor(getMaxIdleConnections(), getKeepAliveDuration(), getTimeUnit());
    }
}
