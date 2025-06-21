package io.github.lucklike.httpclient.configapi;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationSource;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * 基于Spring环境变量实现的配置源
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/7/5 00:16
 */
public class SpringEnvironmentConfigurationSource implements ConfigurationSource {

    @Override
    @SuppressWarnings("all")
    public ConfigurationMap getConfigMap(String source, String prefix) {
        ConfigurationMap envMap = Binder
                .get(ApplicationContextUtils.getEnvironment())
                .bind(ConfigurationPropertyName.adapt(prefix, '.'), Bindable.of(ConfigurationMap.class))
                .orElseGet(ConfigurationMap::new);

        ConfigurationMap configMap = new ConfigurationMap();
        configMap.addProperty(prefix, envMap);
        return configMap;
    }

}
