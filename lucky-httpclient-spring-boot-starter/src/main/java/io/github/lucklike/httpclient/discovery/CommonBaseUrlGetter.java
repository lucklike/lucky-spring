package io.github.lucklike.httpclient.discovery;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.url.BaseURLGetter;
import com.luckyframework.httpclient.proxy.url.DomainNameContext;
import io.github.lucklike.httpclient.ApplicationContextUtils;

import static com.luckyframework.httpclient.proxy.url.SpELURLGetter.autoInjectParamExecuteUrlFunction;
import static io.github.lucklike.httpclient.discovery.Constant.LOAD_BALANCER_CLIENT_URL_GETTER_BEAN_NAME;

/**
 * 通用的域名获取器，目前支持SpringCloud环境和原生环境的域名获取
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/13 14:58
 */
public class CommonBaseUrlGetter implements BaseURLGetter {

    @Override
    public String getBaseUrl(DomainNameContext context) {
        HttpClient httpClientAnn = context.toAnnotation(HttpClient.class);

        String url = context.parseExpression(httpClientAnn.url(), String.class);
        String serviceName = context.parseExpression(httpClientAnn.service(), String.class);
        String path = context.parseExpression(httpClientAnn.path(), String.class);
        String func = context.parseExpression(httpClientAnn.func(), String.class);

        // 存在url配置时优先使用url配置
        if (StringUtils.hasText(url)) {
            return StringUtils.joinUrlPath(url, path);
        }

        // 存在url函数时，而且url函数计算结果不为空或者空字符时
        if (StringUtils.hasText(func)) {
            String _url = autoInjectParamExecuteUrlFunction(context.getContext(), func);
            if (StringUtils.hasText(_url)) {
                return StringUtils.joinUrlPath(_url, path);
            }
        }

        // url和service均为配置时返回空字符串
        if (!StringUtils.hasText(serviceName)) {
            return path;
        }

        // 尝试使用server配置进行解析，server解析需要依赖SpringCloud环境，如果不在SprigCloud环境时将无法解析
        if (!ApplicationContextUtils.containsBean(LOAD_BALANCER_CLIENT_URL_GETTER_BEAN_NAME)) {
            throw new ServerDiscoveryConfigurationException("LoadBalancerClient bean not found in Spring container, unable to get service information for: ['{}']", serviceName);
        }
        return ApplicationContextUtils
                .getBean(LOAD_BALANCER_CLIENT_URL_GETTER_BEAN_NAME, LoadBalancerClientUrlGetter.class)
                .getBaseUrl(serviceName, path);
    }

}
