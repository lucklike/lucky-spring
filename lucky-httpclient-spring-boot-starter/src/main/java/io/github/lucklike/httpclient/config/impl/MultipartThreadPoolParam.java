package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.threadpool.ThreadPoolParam;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多线程池参数
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/26 00:46
 */
public class MultipartThreadPoolParam extends ThreadPoolParam {

    /**
     * 备用线程池参数
     */
    @NestedConfigurationProperty
    private Map<String, ThreadPoolParam> alternative = new ConcurrentHashMap<>();

    /**
     * 获取备用线程池参数
     *
     * @return 备用线程池参数
     */
    public Map<String, ThreadPoolParam> getAlternative() {
        return alternative;
    }


    /**
     * 设置备用线程池参数
     *
     * @param alternative 备用线程池参数
     */
    public void setAlternative(Map<String, ThreadPoolParam> alternative) {
        this.alternative = alternative;
    }
}
