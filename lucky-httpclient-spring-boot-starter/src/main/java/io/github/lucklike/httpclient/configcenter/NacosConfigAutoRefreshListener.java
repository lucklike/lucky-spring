package io.github.lucklike.httpclient.configcenter;


import com.alibaba.boot.nacos.config.properties.NacosConfigProperties;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import com.luckyframework.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * Apollo配置自动刷新监听器
 *
 * @author fk7075
 * @version 3.0.3
 * @since 2026-08-03 00:46:01
 */
public class NacosConfigAutoRefreshListener extends AbstractConfigChangeListener implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(NacosConfigAutoRefreshListener.class);

    private final ApplicationContext applicationContext;

    @NacosInjected
    private ConfigService configService;

    @Resource
    private NacosConfigProperties nacosConfigProperties;

    public NacosConfigAutoRefreshListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String group = nacosConfigProperties.getGroup();

        // 注册配置的 data-id
        registerNacosListener(group, nacosConfigProperties.getDataId());

        // 注册配置的 data-ids
        String dataIds = nacosConfigProperties.getDataIds();
        if (StringUtils.hasText(dataIds)) {
            for (String dataId : dataIds.split(",")) {
                registerNacosListener(group, dataId);
            }
        }

        // 注册扩展配置中的 data-id 和 data-ids
        for (NacosConfigProperties.Config exConfig : nacosConfigProperties.getExtConfig()) {
            String exGroup = exConfig.getGroup();
            registerNacosListener(exGroup, exConfig.getDataId());
            // 注册配置的 data-ids
            String exDataIds = exConfig.getDataIds();
            if (StringUtils.hasText(exDataIds)) {
                for (String exDataId : exDataIds.split(",")) {
                    registerNacosListener(exGroup, exDataId);
                }
            }
        }
    }

    @Override
    public void receiveConfigChange(ConfigChangeEvent event) {
        applicationContext.publishEvent(new EnvironmentChangeEvent(event.getChangeItems().stream().map(ConfigChangeItem::getKey).collect(Collectors.toSet())));
    }

    /**
     * 注册 Nacos 配置监听器
     *
     * @param group  Group
     * @param dataId dataId
     * @throws NacosException 可能出现的异常
     */
    private void registerNacosListener(String group, String dataId) throws NacosException {
        if (StringUtils.hasText(dataId)) {
            configService.addListener(dataId, group, this);
            logger.info("[🎧] Nacos['{}?group={}'] configuration listener has been registered successfully.", dataId, group);
        }
    }
}
