package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.configapi.ApiConfig;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.factory.DualProxyObjectFactory;
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
 * 监听{@link EnvironmentChangeEvent}事件刷新@StdHttpClient客户端配置的监听器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/7/2 23:54
 */
public class StdConfigRefreshApplicationListener implements ApplicationListener<EnvironmentChangeEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StdConfigRefreshApplicationListener.class);

    private static final String STD_CONFIG_PREFIX = "lucky.http-client.standard-client-configs.";

    private final ApplicationContext applicationContext;
    private final DualProxyObjectFactory dualProxyObjectFactory;
    private final Map<String, Class<?>> stdConfigMap;

    /**
     * 构造函数
     *
     * @param applicationContext     Spring 上下文对象
     * @param dualProxyObjectFactory 代理对象工厂类
     */
    public StdConfigRefreshApplicationListener(ApplicationContext applicationContext, DualProxyObjectFactory dualProxyObjectFactory) {
        this.applicationContext = applicationContext;
        this.dualProxyObjectFactory = dualProxyObjectFactory;
        this.stdConfigMap = loadAllStdConfigPrefix();
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> changeKeys = event.getKeys();
        Set<Class<?>> needRefreshStdBeanClasses = new HashSet<>();
        Set<String> needRefreshStdConfigKeys = new HashSet<>();

        // Detect changed keys
        for (String changeKey : changeKeys) {
            if (!changeKey.startsWith(STD_CONFIG_PREFIX)) {
                continue;
            }
            stdConfigMap.forEach((k, v) -> {
                if (changeKey.startsWith(k)) {
                    needRefreshStdConfigKeys.add(changeKey);
                    needRefreshStdBeanClasses.add(v);
                }
            });
        }

        // Refresh proxy objects if needed
        if (ContainerUtils.isNotEmptyCollection(needRefreshStdBeanClasses)) {
            logger.info("[🔄] Refreshing {} @StdHttpClient proxy(s) due to config changes: {}", needRefreshStdBeanClasses.size(),  needRefreshStdBeanClasses.stream().map(Class::getSimpleName).collect(Collectors.toList()));

            HttpClientProxyObjectFactory httpClientProxyObjectFactory = dualProxyObjectFactory.getHttpClientProxyObjectFactory();
            httpClientProxyObjectFactory.clearCacheProxyObject(needRefreshStdBeanClasses.toArray(new Class[0]));

            logger.info("[✅] StdHttpClient proxy cache cleared successfully");
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
}
