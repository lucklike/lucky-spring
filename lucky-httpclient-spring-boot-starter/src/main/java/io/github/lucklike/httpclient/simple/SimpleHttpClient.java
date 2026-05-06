package io.github.lucklike.httpclient.simple;

import com.luckyframework.httpclient.proxy.context.ClassContext;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.Rar;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import com.luckyframework.httpclient.proxy.spel.hook.Lifecycle;
import com.luckyframework.httpclient.proxy.spel.hook.callback.Callback;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.config.simple.SimpleHttpClientConfiguration;
import io.github.lucklike.httpclient.discovery.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_CONFIG_BEAN_NAME;

/**
 * 简单HTTP客户端
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@HttpClient(func = "get_http_server_url")
@SpELImport(SimpleHttpClient.SimpleHttpClientFunctionAndCallback.class)
public @interface SimpleHttpClient {

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClient.class, attribute = "beanId")
    String beanId() default "";

    /**
     * 支持SpEL表达式
     * path，全局路径前缀，请求时会自动加上
     */
    @AliasFor(annotation = HttpClient.class, attribute = "path")
    String path() default "";


    /**
     * 简单HTTP客户端实现相关的工具函数和回调函数
     */
    class SimpleHttpClientFunctionAndCallback {

        public static final String CLASS_CONFIG_NAME = "$SimpleHttpClientConfig";

        /**
         * 将当前Http客户端相关的配置加载到上下文中
         *
         * @param cc                   类上下文
         * @param factoryConfiguration 全局配置
         * @return 当前Http客户端相关的配置
         */
        @Callback(lifecycle = Lifecycle.CLASS, storeOrNot = true, storeName = CLASS_CONFIG_NAME)
        public static SimpleHttpClientConfiguration loadSimpleHttpClientConfig(ClassContext cc,
                                                                               @Qualifier(PROXY_FACTORY_CONFIG_BEAN_NAME) HttpClientProxyObjectFactoryConfiguration factoryConfiguration) {
            Map<String, SimpleHttpClientConfiguration> simpleHttpClientConfigs = factoryConfiguration.getSimpleClientConfigs();
            return simpleHttpClientConfigs.get(getConfigId(cc));
        }

        /**
         * 用于获取HTTP服务地址的函数
         *
         * @param config 当前HTTP客户端的配置
         * @return 目标HTTP服务地址
         */
        @FunctionAlias("get_http_server_url")
        public static String getHttpServerUrl(ClassContext cc,
                                              @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config) {
            String url = config.getUrl();
            Assert.hasText(url, "Missing necessary configuration : 'lucky.http-client.simple-client-configs." + getConfigId(cc) + ".url'");
            return url;
        }


        /**
         * 获取当前API的ConfigId
         *
         * @param cc 类上下文
         * @return 当前API的ConfigId
         */
        private static String getConfigId(ClassContext cc) {
            Class<?> proxyObjectClass = cc.getProxyObject().getClass();
            return ApplicationContextUtils.getBeanNamesForType(proxyObjectClass)[0];
        }
    }
}
