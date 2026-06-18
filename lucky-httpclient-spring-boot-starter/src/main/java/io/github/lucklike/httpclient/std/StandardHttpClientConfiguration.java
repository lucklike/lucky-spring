package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.proxy.configapi.SpELImportConf;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.config.GenerateEntry;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 标准 HTTP 客户端配置
 */
public class StandardHttpClientConfiguration extends StandardApiConfiguration {

    /**
     * 服务地址
     */
    private String url;

    /**
     * 服务名称，用于从注册中心获取服务地址
     */
    private String service;

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
     * {@link MethodContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    @NestedConfigurationProperty
    private SpELImportConf methodSpelImport;

    /**
     * 获取URL地址
     *
     * @return URL地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置URL地址
     *
     * @param url URL地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 服务名称，用于从注册中心获取服务地址
     *
     * @return 服务名
     */
    public String getService() {
        return service;
    }

    /**
     * 服务名称，用于从注册中心获取服务地址
     *
     * @param service 服务名
     */
    public void setService(String service) {
        this.service = service;
    }

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

    /**
     * {@link MethodContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     *
     * @return {@link MethodContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    public SpELImportConf getMethodSpelImport() {
        return methodSpelImport;
    }

    /**
     * 设置{@link MethodContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     *
     * @param methodSpelImport {@link MethodContext}级别SpEL配置，通过此配置可以向上下文中导入变量、函数、Hooks、包
     */
    public void setMethodSpelImport(SpELImportConf methodSpelImport) {
        this.methodSpelImport = methodSpelImport;
    }

    @Override
    public void removeNonEffectiveConfig() {
        super.removeNonEffectiveConfig();
        methodConfigs.forEach((cc, config) -> {
            config.removeNonEffectiveConfig();
        });
    }
}

