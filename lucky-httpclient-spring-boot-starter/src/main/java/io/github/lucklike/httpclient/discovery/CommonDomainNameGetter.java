package io.github.lucklike.httpclient.discovery;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.annotations.DomainNameMeta;
import com.luckyframework.httpclient.proxy.url.DomainNameContext;
import com.luckyframework.httpclient.proxy.url.DomainNameGetter;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.discovery.cloud.SpringCloudDomainNameGetter;

import static io.github.lucklike.httpclient.discovery.Constant.SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME;

/**
 * 通用的域名获取器，目前支持SpringCloud环境和原生环境的域名获取
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/13 14:58
 */
public class CommonDomainNameGetter implements DomainNameGetter {

    @Override
    public String getDomainName(DomainNameContext context) throws Exception {
        HttpClient httpClientAnn = context.toAnnotation(HttpClient.class);


        String url = context.parseExpression(httpClientAnn.url(), String.class);
        String serviceName = context.parseExpression(httpClientAnn.service(), String.class);
        String path = context.parseExpression(httpClientAnn.path(), String.class);

        // 存在url配置时优先使用url配置
        if (StringUtils.hasText(url)) {
            return StringUtils.joinUrlPath(url, path);
        }

        // url和service均为配置时报错提示
        if (!StringUtils.hasText(serviceName)) {
            throw new ServerDiscoveryConfigurationException("Missing url configuration and service name configuration");
        }

        // 尝试使用server配置进行解析，server解析需要依赖SpringCloud环境，如果不在SprigCloud环境时将无法解析，会直接返回空字符串
        if (!ApplicationContextUtils.containsBean(SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME)) {
            return DomainNameMeta.EMPTY;
        }
        return ApplicationContextUtils
                .getBean(SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME, SpringCloudDomainNameGetter.class)
                .getDomainName(serviceName, path);
    }

}
