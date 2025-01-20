package io.github.lucklike.httpclient.config.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多线程池参数
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/26 00:46
 */
public class MultipartThreadPoolParam extends LazyThreadPoolParam {

    /**
     * 备用线程池参数
     */
    private Map<String, LazyThreadPoolParam> alternative = new ConcurrentHashMap<>();

    /**
     * 获取备用线程池参数
     *
     * @return 备用线程池参数
     */
    public Map<String, LazyThreadPoolParam> getAlternative() {
        return alternative;
    }


    /**
     * 设置备用线程池参数
     *
     * @param alternative 备用线程池参数
     */
    public void setAlternative(Map<String, LazyThreadPoolParam> alternative) {
        this.alternative = alternative;
    }
}
