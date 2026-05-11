package io.github.lucklike.httpclient.retry;

import com.luckyframework.httpclient.proxy.retry.RunBeforeRetryContext;
import com.luckyframework.retry.BackoffWaitBeforeRetry;
import com.luckyframework.retry.TaskResult;
import io.github.lucklike.httpclient.config.RetryConfiguration;

/**
 * 基于配置的重试等待器
 */
public class ConfigurationBackoffWaitingBeforeRetryContext extends RunBeforeRetryContext<Object> {

    private final BackoffWaitBeforeRetry backoffWaitBeforeRetry;

    public ConfigurationBackoffWaitingBeforeRetryContext(RetryConfiguration retryConfig) {
        this.backoffWaitBeforeRetry = new BackoffWaitBeforeRetry(retryConfig.getWaitMillis(), retryConfig.getMultiplier(), retryConfig.getMaxWaitMillis(), retryConfig.getMinWaitMillis());
    }

    @Override
    protected void doBeforeRetry(TaskResult<Object> taskResult) {
        backoffWaitBeforeRetry.beforeRetry(taskResult);
    }
}
