package io.github.lucklike.httpclient.mock;

import java.util.Map;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/4/30 00:37
 */
public class CommonMockConfiguration {

    /**
     * Mock总开关
     */
    private boolean enable = true;

    /**
     * 总延时配置，单位毫秒
     */
    private long latency;

    /**
     * 方法模拟配置
     */
    private Map<String, MockResult> methods;

    /**
     * Mock总开关
     *
     * @return Mock总开关
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * 设置Mock总开关
     *
     * @param enable Mock总开关
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 获取总延时配置，单位毫秒
     *
     * @return 总延时配置，单位毫秒
     */
    public long getLatency() {
        return latency;
    }

    /**
     * 设置总延时配置，单位毫秒
     *
     * @param latency 总延时配置，单位毫秒
     */
    public void setLatency(long latency) {
        this.latency = latency;
    }

    /**
     * 方法模拟配置
     *
     * @return 方法模拟配置
     */
    public Map<String, MockResult> getMethods() {
        return methods;
    }

    /**
     * 方法模拟配置
     *
     * @param methods 方法模拟配置
     */
    public void setMethods(Map<String, MockResult> methods) {
        this.methods = methods;
    }
}
