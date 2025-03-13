package io.github.lucklike.httpclient.discovery.cloud;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.url.DomainNameContext;
import com.luckyframework.httpclient.proxy.url.DomainNameGetter;
import io.github.lucklike.httpclient.discovery.HttpClient;
import io.github.lucklike.httpclient.discovery.ServerDiscoveryConfigurationException;
import io.github.lucklike.httpclient.discovery.ServiceInstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

/**
 * 基于{@link LoadBalancerClient SpringCloud LoadBalancerClient} 组件实现的域名获取器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/13 14:58
 */
public class SpringCloudDomainNameGetter implements DomainNameGetter {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Override
    public String getDomainName(DomainNameContext context) throws Exception {
        // 获取注解实例并检验配置
        HttpClient httpclientAnn = context.toAnnotation(HttpClient.class);

        String serviceName = context.parseExpression(httpclientAnn.service(), String.class);
        String path = context.parseExpression(httpclientAnn.path(), String.class);

        return getDomainName(serviceName, path);
    }

    public String getDomainName(String serviceName, String path) {
        if (loadBalancerClient == null) {
            throw new ServerDiscoveryConfigurationException("It is detected that the current Spring Cloud environment is not available and service information cannot be obtained");
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
        String baseUrl = instance.getUri().toString();
        if (StringUtils.hasText(path)) {
            return StringUtils.joinUrlPath(baseUrl, path);
        }
        return baseUrl;
    }
}
