package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.executor.HttpExecutor;

/**
 * HTTP请求执行器工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/9 09:08
 */
@FunctionalInterface
public interface HttpExecutorFactory {

    HttpExecutor getHttpExecutor();
}
