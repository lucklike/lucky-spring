package io.github.lucklike.httpclient.discovery;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.url.BaseURLGetter;
import com.luckyframework.httpclient.proxy.url.DomainNameContext;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.discovery.cloud.SpringCloudBaseUrlGetter;

import static com.luckyframework.httpclient.proxy.url.SpELURLGetter.autoInjectParamExecuteUrlFunction;
import static io.github.lucklike.httpclient.discovery.Constant.SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME;

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

        // 存在url函数时
        if (StringUtils.hasText(func)) {
            String _url = autoInjectParamExecuteUrlFunction(context.getContext(), func);
            return StringUtils.joinUrlPath(_url, path);
        }

        // url和service均为配置时返回空字符串
        if (!StringUtils.hasText(serviceName)) {
            return path;
        }

        // 尝试使用server配置进行解析，server解析需要依赖SpringCloud环境，如果不在SprigCloud环境时将无法解析，会直接返回空字符串
        if (!ApplicationContextUtils.containsBean(SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME)) {
            return path;
        }
        return ApplicationContextUtils
                .getBean(SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME, SpringCloudBaseUrlGetter.class)
                .getBaseUrl(serviceName, path);
    }

}
