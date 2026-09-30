package io.github.lucklike.httpclient.configcenter;


import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.configapi.ApiConfig;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.factory.DualProxyObjectFactory;
import io.github.lucklike.httpclient.std.StdHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 监听{@link EnvironmentChangeEvent}事件刷新全局 Lucky HttpClient 配置
 *
 * @author fk7075
 * @version 3.0.3
 * @since 2026-07-31 02:51:42
 */
public class GlobalConfigRefreshApplicationListener implements ApplicationListener<EnvironmentChangeEvent> {


    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigRefreshApplicationListener.class);

    /**
     * 全局配置前缀
     */
    private static final String LUCKY_CONFIG_PREFIX = "lucky.http-client.";

    /**
     * Std 配置前缀
     */
    private static final String STD_CONFIG_PREFIX = "lucky.http-client.standard-client-configs.";

    /**
     * 全局线程池相关的配置前缀
     */
    private static final String LUCKY_CONFIG_THREAD_POOL_PREFIX = "lucky.http-client.thread-pool.";

    /**
     * 代理对象工厂
     */
    private final DualProxyObjectFactory dualProxyObjectFactory;

    /**
     * Spring上下文
     */
    private final ApplicationContext applicationContext;

    /**
     * Std 配置相关信息
     */
    private final Map<String, Class<?>> stdConfigMap;

    /**
     * 构造函数
     *
     * @param applicationContext     Spring 上下文对象
     * @param dualProxyObjectFactory 代理对象工厂类
     */
    public GlobalConfigRefreshApplicationListener(ApplicationContext applicationContext, DualProxyObjectFactory dualProxyObjectFactory) {
        this.applicationContext = applicationContext;
        this.dualProxyObjectFactory = dualProxyObjectFactory;
        this.stdConfigMap = loadAllStdConfigPrefix();
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {

        // Configuration changes related to HttpClient
        Set<String> changeKeys = event.getKeys();
        boolean needShutdown = false;
        Set<String> needRefreshHttpClientKeySet = new HashSet<>();
        Set<String> changeStdKeySet = new HashSet<>();

        for (String changeKey : changeKeys) {

            //Collect Std configuration
            if (changeKey.startsWith(STD_CONFIG_PREFIX)) {
                changeStdKeySet.add(changeKey);
            }

            // Collect global lucky parameters
            else if (changeKey.startsWith(LUCKY_CONFIG_PREFIX)) {
                needRefreshHttpClientKeySet.add(changeKey);
            }

            // There are changes to the thread pool parameters
            if (changeKey.startsWith(LUCKY_CONFIG_THREAD_POOL_PREFIX)) {
                needShutdown = true;
            }
        }

        // There are no other configuration changes other than std
        if (ContainerUtils.isEmptyCollection(needRefreshHttpClientKeySet)) {
            // Collect the Std classes and configurations that need to be changed
            Set<Class<?>> needRefreshStdBeanClasses = new HashSet<>();
            Set<String> changeKeySet = new HashSet<>();

            for (String stdKey : changeStdKeySet) {
                stdConfigMap.forEach((k, v) -> {
                    if (stdKey.startsWith(k)) {
                        needRefreshStdBeanClasses.add(v);
                        changeKeySet.add(stdKey);
                    }
                });
            }

            // Refresh proxy objects if needed
            if (ContainerUtils.isNotEmptyCollection(needRefreshStdBeanClasses)) {
                logger.info("[🔄][🎯] Refreshing [{}] @StdHttpClient proxy(s) due to config changes: {}", needRefreshStdBeanClasses.size(), changeKeySet);

                HttpClientProxyObjectFactory httpClientProxyObjectFactory = dualProxyObjectFactory.getHttpClientProxyObjectFactory();
                Set<Class<?>> refreshedProxyClasses = httpClientProxyObjectFactory.refreshProxyObjectCache(needRefreshStdBeanClasses);
                if (refreshedProxyClasses.isEmpty()) {
                    logger.info("[❌] No @StdHttpClient that needs to be refreshed was found.");
                } else {
                    logger.info("[✅][🎯] @StdHttpClient cache object refresh successful: {}", refreshedProxyClasses.stream().map(Class::getSimpleName).collect(Collectors.toList()));
                }


            }
        }
        // Refresh the global proxy object
        else {
            needRefreshHttpClientKeySet.addAll(changeStdKeySet);
            logger.info("[🔄] Refreshing HttpClientProxyObjectFactory due to config changes: {}, executor-shutdown={}", needRefreshHttpClientKeySet, needShutdown);
            dualProxyObjectFactory.initHttpClientProxyObjectFactoryLoadProxyCache(needShutdown);
            logger.info("[✅] HttpClientProxyObjectFactory object reconstruction successful.");
        }

    }

    /**
     * 加载 Std 客户端对应的配置前缀和 Class 组成的 Map
     *
     * @return Std 客户端对应的配置前缀和 Class 组成的 Map
     */
    private Map<String, Class<?>> loadAllStdConfigPrefix() {
        String[] stdHttpClientBeanNames = applicationContext.getBeanNamesForAnnotation(StdHttpClient.class);
        Map<String, Class<?>> stdConfigMap = new HashMap<>(stdHttpClientBeanNames.length);
        BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        for (String stdHttpClientBeanName : stdHttpClientBeanNames) {
            RootBeanDefinition beanDefinition = (RootBeanDefinition) definitionRegistry.getBeanDefinition(stdHttpClientBeanName);
            Class<?> beanClass = beanDefinition.getBeanClass();
            String apiConfigId = getApiConfigId(beanClass);
            stdConfigMap.put(String.format("%s%s.", STD_CONFIG_PREFIX, apiConfigId), beanClass);

        }
        return stdConfigMap;
    }

    /**
     * 根据 Class 获取 ApiConfigId
     *
     * @param clazz Std 客户端对应的 Class
     * @return ApiConfigId
     */
    private String getApiConfigId(Class<?> clazz) {
        ApiConfig api = AnnotationUtils.findMergedAnnotation(clazz, ApiConfig.class);
        if (api != null && StringUtils.hasText(api.value())) {
            return api.value();
        }
        return clazz.getSimpleName();
    }

    public synchronized void preInitProxyObjectCache() {

    }
}
