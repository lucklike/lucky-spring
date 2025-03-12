package io.github.lucklike.httpclient.discovery.eureka;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.url.DomainNameContext;
import com.luckyframework.httpclient.proxy.url.DomainNameGetter;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.discovery.RegistryConfigurationException;


/**
 * Eureka域名获取器，从Eureka获取对应服务的ip地址和端口
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/12 12:07
 */
public class EurekaDomainGetter implements DomainNameGetter {



    @Override
    public String getDomainName(DomainNameContext context) throws Exception {
        // 获取注解实例并检验配置
        EurekaHttpClient eurekaAnn = context.toAnnotation(EurekaHttpClient.class);
        requiredConfigCheck(eurekaAnn);

        // 解析注解配置
        EurekaClient eurekaClient = getEurekaClient(context, eurekaAnn.eurekaClient());
        String name = context.parseExpression(eurekaAnn.name(), String.class);
        boolean secure = context.parseExpression(eurekaAnn.secure(), boolean.class);
        String protocol = context.parseExpression(eurekaAnn.protocol(), String.class);
        String path = context.parseExpression(eurekaAnn.path(), String.class);

        // 拼接URL
        InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka(name, secure);
        String baseUrl = StringUtils.format("{}://{}:{}", protocol, instanceInfo.getIPAddr(), instanceInfo.getPort());
        if (StringUtils.hasText(path)) {
            return StringUtils.joinUrlPath(baseUrl, path);
        }
        return baseUrl;
    }

    private EurekaClient getEurekaClient(DomainNameContext context, String eurekaClientConfig) {
        if (StringUtils.hasText(eurekaClientConfig)) {
            return context.parseExpression(eurekaClientConfig, EurekaClient.class);
        }
        return ApplicationContextUtils.getBean(EurekaClient.class);
    }

    private void requiredConfigCheck(EurekaHttpClient eurekaHttpClient) {
        requiredConfigCheck("protocol", eurekaHttpClient.protocol());
        requiredConfigCheck("name", eurekaHttpClient.name());
    }

    private void requiredConfigCheck(String configName, String configValue) {
        if (!StringUtils.hasText(configValue)) {
            throw new RegistryConfigurationException("The @EurekaHttpClient annotation configuration is error, '{}' is not configured.", configName);
        }
    }
}
