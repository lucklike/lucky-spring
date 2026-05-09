package io.github.lucklike.httpclient.std;

import io.github.lucklike.httpclient.config.GenerateEntry;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 标准 HTTP 客户端配置
 */
public class StandardHttpClientConfiguration extends StandardApiConfiguration {

    /**
     * 生命周期管理器
     */
    @NestedConfigurationProperty
    private GenerateEntry<LifeCycleManager> lifecycleManager;

    /**
     * 标准客户端中单个方法的个性化配置
     */
    private Map<String, StandardApiConfiguration> methodConfigs = new LinkedHashMap<>();

    /**
     * 获取生命周期管理器对象
     *
     * @return 生命周期管理器对象
     */
    public GenerateEntry<LifeCycleManager> getLifecycleManager() {
        return lifecycleManager;
    }

    /**
     * 设置生命周期管理器对象
     *
     * @param lifecycleManager 生命周期管理器对象
     */
    public void setLifecycleManager(GenerateEntry<LifeCycleManager> lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    /**
     * 获取标准客户端中单个方法的个性化配置
     *
     * @return 标准客户端中单个方法的个性化配置
     */
    public Map<String, StandardApiConfiguration> getMethodConfigs() {
        return methodConfigs;
    }

    /**
     * 设置标准客户端中单个方法的个性化配置
     *
     * @param methodConfigs 标准客户端中单个方法的个性化配置
     */
    public void setMethodConfigs(Map<String, StandardApiConfiguration> methodConfigs) {
        this.methodConfigs = methodConfigs;
    }
}

