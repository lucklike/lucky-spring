package io.github.lucklike.httpclient.config;

import io.github.lucklike.httpclient.discovery.RetryableHttpClient;

/**
 * 重试相关的配置，需要结合{@link RetryableHttpClient @RetryableHttpClient}注解一起使用
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/9/17 01:04
 * @see RetryableHttpClient
 */
public class RetryConfiguration {

    /**
     * 是否开启重试功能
     */
    private boolean enable = true;

    /**
     * 任务名称
     */
    private String nameFormat = "[#{T(Thread).currentThread().getName()}]<#{$unique_id$}>-#{$api$.name}";

    /**
     * 最大重试次数，默认 3 次
     */
    private int count = 3;

    /**
     * 重试等待时长，默认 1 秒
     */
    private long waitMillis = 1000L;

    /**
     * 最大的重试等待时间，默认 10 秒
     */
    private long maxWaitMillis = 10000L;

    /**
     * 最小的重试等待时间，默认 0.5 秒
     */
    private long minWaitMillis = 500L;

    /**
     * 延时倍数，下一次等待时间与上一次等待时间的比值
     */
    private double multiplier = 0D;

    /**
     * 重试表达式，当该表达式返回true时才有可能进行重试
     */
    private String condition = "";

    /**
     * 是否开启重试功能
     *
     * @return 是否开启重试功能
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * 设置是否开启重试功能
     *
     * @param enable 是否开启重试功能
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    public String getNameFormat() {
        return nameFormat;
    }

    /**
     * 设置任务名称，支持 SpEL 表达式
     *
     * @param nameFormat 任务名称
     */
    public void setNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
    }

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public int getCount() {
        return count;
    }

    /**
     * 设置最大重试次数，默认 3 次
     *
     * @param count 最大重试次数
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 获取重试等待时长
     *
     * @return 重试等待时长
     */
    public long getWaitMillis() {
        return waitMillis;
    }

    /**
     * 设置重试等待时长，默认 1 秒
     *
     * @param waitMillis 重试等待时长
     */
    public void setWaitMillis(long waitMillis) {
        this.waitMillis = waitMillis;
    }

    /**
     * 获取最大的重试等待时间
     *
     * @return 最大的重试等待时间
     */
    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    /**
     * 设置最大的重试等待时间，默认 10 秒
     *
     * @param maxWaitMillis 最大的重试等待时间
     */
    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    /**
     * 获取最小的重试等待时间
     *
     * @return 最小的重试等待时间
     */
    public long getMinWaitMillis() {
        return minWaitMillis;
    }

    /**
     * 设置最小的重试等待时间，默认 0.5 秒
     *
     * @param minWaitMillis 最小的重试等待时间
     */
    public void setMinWaitMillis(long minWaitMillis) {
        this.minWaitMillis = minWaitMillis;
    }

    /**
     * 获取延时倍数，下一次等待时间与上一次等待时间的比值
     *
     * @return 延时倍数
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * 设置延时倍数，下一次等待时间与上一次等待时间的比值
     *
     * @param multiplier 延时倍数
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * 重试条件表达式，当该表达式返回true时才有可能进行重试
     *
     * @return 重试条件表达式
     */
    public String getCondition() {
        return condition;
    }

    /**
     * 重试条件表达式，当该表达式返回true时才有可能进行重试
     *
     * @param condition 重试条件表达式
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
