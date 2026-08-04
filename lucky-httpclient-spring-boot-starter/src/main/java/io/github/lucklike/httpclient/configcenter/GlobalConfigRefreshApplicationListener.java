package io.github.lucklike.httpclient.configcenter;


import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.factory.DualProxyObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Set;

/**
 * 监听{@link EnvironmentChangeEvent}事件刷新全局 Lucky HttpClient 配置
 *
 * @author fk7075
 * @version 3.0.3
 * @since 2026-07-31 02:51:42
 */
public class GlobalConfigRefreshApplicationListener implements ApplicationListener<EnvironmentChangeEvent> {


    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigRefreshApplicationListener.class);

    private static final String LUCKY_CONFIG_PREFIX = "lucky.http-client.";
    private static final String LUCKY_CONFIG_THREAD_POOL_PREFIX = "lucky.http-client.thread-pool";

    private final DualProxyObjectFactory dualProxyObjectFactory;

    /**
     * 构造函数
     *
     * @param dualProxyObjectFactory 代理对象工厂类
     */
    public GlobalConfigRefreshApplicationListener(DualProxyObjectFactory dualProxyObjectFactory) {
        this.dualProxyObjectFactory = dualProxyObjectFactory;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> changeKeys = event.getKeys();

        // HttpClient相关配置变更
        boolean needShutdown = false;
        Set<String> httpClientKeySet = new HashSet<>();
        for (String changeKey : changeKeys) {
            if (changeKey.startsWith(LUCKY_CONFIG_PREFIX)) {
                httpClientKeySet.add(changeKey);
            }
            if (changeKey.startsWith(LUCKY_CONFIG_THREAD_POOL_PREFIX)) {
                needShutdown = true;
            }
        }

        // Refresh proxy objects if needed
        if (ContainerUtils.isNotEmptyCollection(httpClientKeySet)) {
            logger.info("[🔄] Refreshing HttpClientProxyObjectFactory due to config changes: {}, need-shutdown={}", httpClientKeySet, needShutdown);
            dualProxyObjectFactory.clearHttpClientProxyObjectFactoryInstance(needShutdown);
            logger.info("[✅] HttpClientProxyObjectFactory refresh successful.");
        }
    }
}
