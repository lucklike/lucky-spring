package io.github.lucklike.httpclient.configcenter;


import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.luckyframework.common.ContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;

import java.net.URI;
import java.util.List;

/**
 * Apollo配置自动刷新监听器
 *
 * @author fk7075
 * @version 3.0.3
 * @since 2026-07-31 06:10:05
 */
public class ApolloConfigAutoRefreshListener implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ApolloConfigAutoRefreshListener.class);
    private final ApplicationContext applicationContext;

    public ApolloConfigAutoRefreshListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        String configPrefix = "spring.config.import";
        String apolloConfigPrefix = "apollo";

        List<String> importConfigList = Binder.get(applicationContext.getEnvironment())
                .bind(ConfigurationPropertyName.adapt(configPrefix, '.'), Bindable.listOf(String.class))
                .orElse(null);

        if (ContainerUtils.isNotEmptyCollection(importConfigList)) {
            for (String importConfig : importConfigList) {
                URI uri = URI.create(importConfig);
                // 是apollo协议字符串
                if (apolloConfigPrefix.equalsIgnoreCase(uri.getScheme())) {
                    String namespace = uri.getHost();
                    Config config = ConfigService.getConfig(namespace);
                    config.addChangeListener(changeEvent -> {
                        // 发布 EnvironmentChangeEvent
                        applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
                    });
                    logger.info("[namespace={}] Apollo configuration listener has been registered successfully.", namespace);
                }
            }
        }
    }
}
