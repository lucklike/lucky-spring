package io.github.lucklike.httpclient.discovery.nacos;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.url.DomainNameContext;
import com.luckyframework.httpclient.proxy.url.DomainNameGetter;
import io.github.lucklike.httpclient.discovery.RegistryConfigurationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Nacos域名获取器，从Nacos获取对应服务的ip地址和端口
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/12 12:07
 */
public class NacosDomainGetter implements DomainNameGetter {

    @NacosInjected
    private NamingService namingService;

    @Override
    public String getDomainName(DomainNameContext context) throws Exception {

        // 获取注解信息
        NacosHttpClient nacosAnn = context.toAnnotation(NacosHttpClient.class);

        // 校验必填配置项
        requiredConfigCheck(nacosAnn);

        // 获取并解析配置项目
        String namingServiceConfig = nacosAnn.namingService();
        String protocol = context.parseExpression(nacosAnn.protocol(), String.class);
        String name = context.parseExpression(nacosAnn.name(), String.class);
        String group = context.parseExpression(nacosAnn.group(), String.class);
        String[] clusters = nacosAnn.clusters();
        List<String> clusterList = ContainerUtils.isEmptyArray(clusters) ? Collections.emptyList() : Arrays.asList(clusters);


        // 获取命名服务实例，并通过服务名和组信息获取对应服务的IP和端口
        NamingService namingService = getNamingService(context, namingServiceConfig);
        Instance instance = namingService.selectOneHealthyInstance(name, group, clusterList);

        // 拼接URL
        String baseUrl = StringUtils.format("{}://{}:{}", protocol, instance.getIp(), instance.getPort());
        String path = context.parseExpression(nacosAnn.path(), String.class);
        if (StringUtils.hasText(path)) {
            return StringUtils.joinUrlPath(baseUrl, path);
        }
        return baseUrl;
    }

    private NamingService getNamingService(DomainNameContext context, String namingServiceConfig) throws NacosException {
        if (StringUtils.hasText(namingServiceConfig)) {
            return context.parseExpression(namingServiceConfig, NamingService.class);
        }
        if (namingService == null) {
            throw new RegistryConfigurationException("It is detected that the current Nacos environment is not available and service information cannot be obtained");
        }
        return this.namingService;
    }

    private void requiredConfigCheck(NacosHttpClient nacosHttpClient) {
        requiredConfigCheck("protocol", nacosHttpClient.protocol());
        requiredConfigCheck("name", nacosHttpClient.name());
        requiredConfigCheck("group", nacosHttpClient.group());
    }

    private void requiredConfigCheck(String configName, String configValue) {
        if (!StringUtils.hasText(configValue)) {
            throw new RegistryConfigurationException("The @NacosHttpClient annotation configuration is error, '{}' is not configured.", configName);
        }
    }
}
