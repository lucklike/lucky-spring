package io.github.lucklike.httpclient.configcenter;


import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.factory.DualProxyObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

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
    private final Environment environment;

    /**
     * 构造函数
     *
     * @param environment            环境变量
     * @param dualProxyObjectFactory 代理对象工厂类
     */
    public GlobalConfigRefreshApplicationListener(Environment environment, DualProxyObjectFactory dualProxyObjectFactory) {
        this.environment = environment;
        this.dualProxyObjectFactory = dualProxyObjectFactory;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> changeKeys = event.getKeys();

        // HttpClient相关配置变更
        boolean needShutdown = false;
        Set<String> httpClientKeyValueSet = new HashSet<>();
        for (String changeKey : changeKeys) {
            if (changeKey.startsWith(LUCKY_CONFIG_PREFIX)) {
                httpClientKeyValueSet.add(String.format("%s=%s", changeKey, environment.getProperty(changeKey)));
            }
            if (changeKey.startsWith(LUCKY_CONFIG_THREAD_POOL_PREFIX)) {
                needShutdown = true;
            }
        }

        // Refresh proxy objects if needed
        if (ContainerUtils.isNotEmptyCollection(httpClientKeyValueSet)) {
            logger.info("[🔄] Refreshing HttpClientProxyObjectFactory due to config changes: {}, need-shutdown={}", httpClientKeyValueSet, needShutdown);
            dualProxyObjectFactory.clearHttpClientProxyObjectFactoryInstance(needShutdown);
            logger.info("[✅] HttpClientProxyObjectFactory refresh successful.");
        }
    }
}
