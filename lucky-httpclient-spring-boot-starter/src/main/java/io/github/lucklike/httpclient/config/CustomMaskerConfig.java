package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.logging.CustomMasker;

import java.util.Set;

public class CustomMaskerConfig {

    /**
     * 脱敏处理器
     */
    private Class<? extends CustomMasker> clazz;

    /**
     * 脱敏处理器作用的关键字
     */
    private Set<String> keys;

    /**
     * 获取脱敏处理器
     *
     * @return 脱敏处理器
     */
    public Class<? extends CustomMasker> getClazz() {
        return clazz;
    }

    /**
     * 设置脱敏处理器
     *
     * @param clazz 脱敏处理器
     */
    public void setClazz(Class<? extends CustomMasker> clazz) {
        this.clazz = clazz;
    }

    /**
     * 获取脱敏处理器作用的关键字
     *
     * @return 脱敏处理器作用的关键字
     */
    public Set<String> getKeys() {
        return keys;
    }

    /**
     * 设置脱敏处理器作用的关键字
     *
     * @param keys 脱敏处理器作用的关键字
     */
    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }
}
