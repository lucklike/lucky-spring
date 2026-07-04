package io.github.lucklike.httpclient.configcenter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.properties.ConfigurationPropertiesBeans;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * 配置变更时自动刷新{@link ConfigurationProperties}配置的自动配置
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/7/5 02:03
 */
@Configuration
public class ConfigurationPropertiesAutoRefreshConfiguration {


    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(type = {
            "org.springframework.cloud.context.properties.ConfigurationPropertiesBeans",
            "org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder"
    })
    static class ConfigurationPropertiesAutoRefreshConfig {

        @Bean
        public ConfigurationPropertiesBeans configurationPropertiesBeans() {
            return new ConfigurationPropertiesBeans();
        }

        @Bean
        public ConfigurationPropertiesRebinder configurationPropertiesRebinder(ConfigurationPropertiesBeans beans) {
            return new ConfigurationPropertiesRebinder(beans);
        }
    }

}
