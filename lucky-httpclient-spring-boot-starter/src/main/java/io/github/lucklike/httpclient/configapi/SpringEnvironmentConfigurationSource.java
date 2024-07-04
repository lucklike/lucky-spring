package io.github.lucklike.httpclient.configapi;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationSource;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

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
        String keyPrefix = prefix + ".";
        AbstractEnvironment env = (AbstractEnvironment) ApplicationContextUtils.getEnvironment();
        Properties properties = new Properties();
        for (PropertySource<?> propertySource : env.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                    if (propertyName.startsWith(keyPrefix)) {
                        properties.put(propertyName, env.getProperty(propertyName));
                    }
                }
            }
        }
        return ConfigurationMap.create(properties);
    }

}
