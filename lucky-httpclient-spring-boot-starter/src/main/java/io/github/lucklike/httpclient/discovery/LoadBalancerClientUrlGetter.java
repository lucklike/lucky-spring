package io.github.lucklike.httpclient.discovery;

import com.luckyframework.common.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

/**
 * 基于{@link LoadBalancerClient SpringCloud LoadBalancerClient} 组件实现的域名获取器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/13 14:58
 */
public class LoadBalancerClientUrlGetter {

    private final LoadBalancerClient loadBalancerClient;

    public LoadBalancerClientUrlGetter(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }

    public String getBaseUrl(String serviceName, String path) {
        if (loadBalancerClient == null) {
            throw new ServerDiscoveryConfigurationException("LoadBalancerClient bean not found in Spring container, unable to get service information for: ['{}']", serviceName);
        }

        // 解析注解配置
        if (!StringUtils.hasText(serviceName)) {
            throw new ServerDiscoveryConfigurationException("The service name is not configured");
        }

        // 获取服务实例，并检验服务实例是否存在
        ServiceInstance instance = loadBalancerClient.choose(serviceName);
        if (instance == null) {
            throw new ServiceInstanceNotFoundException("No service instance named '{}' was found", serviceName);
        }

        // 拼接URL返回
        return StringUtils.joinUrlPath(instance.getUri().toString(), path);
    }
}
