package io.github.lucklike.httpclient.simple;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationParserException;
import com.luckyframework.httpclient.proxy.configapi.MultipartFormData;
import com.luckyframework.httpclient.proxy.context.ClassContext;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.function.ResourceFunctions;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.Rar;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import com.luckyframework.httpclient.proxy.spel.hook.Lifecycle;
import com.luckyframework.httpclient.proxy.spel.hook.callback.Callback;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.config.simple.SimpleHttpClientConfiguration;
import io.github.lucklike.httpclient.discovery.HttpClient;
import io.github.lucklike.httpclient.mock.AutoIdentifyDefaultMockConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_CONFIG_BEAN_NAME;

/**
 * 简单HTTP客户端
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@HttpClient(func = "get_http_server_url")
@AutoIdentifyDefaultMockConfiguration
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
     * 请求参数处理器，用户可使用该接口扩展自己的参数设置逻辑
     */
    Class<? extends RequestParameterHandler> reqHandler() default RequestParameterHandler.class;

    /**
     * 简单HTTP客户端实现相关的工具函数和回调函数
     */
    class SimpleHttpClientFunctionAndCallback {

        public static final String CLASS_CONFIG_NAME = "$SimpleHttpClientConfig";
        public static final String CLASS_HANDLER_NAME = "$RequestParameterHandler";

        /**
         * 将当前Http客户端相关的配置加载到上下文中
         *
         * @param cc                   类上下文
         * @param factoryConfiguration 全局配置
         * @return 当前Http客户端相关的配置
         */
        @Callback(lifecycle = Lifecycle.CLASS, storeOrNot = true, unfold = true)
        public static Map<String, Object> loadSimpleHttpClientConfig(ClassContext cc,
                                                                     @Qualifier(PROXY_FACTORY_CONFIG_BEAN_NAME) HttpClientProxyObjectFactoryConfiguration factoryConfiguration) {
            Map<String, Object> resultMap = new HashMap<>(2);

            // 初始化SimpleHttpClientConfiguration配置
            Map<String, SimpleHttpClientConfiguration> simpleHttpClientConfigs = factoryConfiguration.getSimpleClientConfigs();
            resultMap.put(CLASS_CONFIG_NAME, simpleHttpClientConfigs.get(getConfigId(cc)));

            // 初始化RequestParameterHandler配置
            SimpleHttpClient ann = cc.getMergedAnnotation(SimpleHttpClient.class);
            if (ann.reqHandler() != RequestParameterHandler.class) {
                resultMap.put(CLASS_HANDLER_NAME, cc.generateObject(ann.reqHandler(), Scope.SINGLETON));
            }

            return resultMap;
        }

        /**
         *
         * @param mc                      方法上下文
         * @param request                 请求对象
         * @param config                  当前HTTP客户端的配置
         * @param requestParameterHandler 参数处理器扩展对象
         */
        @Callback(lifecycle = Lifecycle.REQUEST_INIT)
        public static void requestInit(MethodContext mc,
                                       Request request,
                                       @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config,
                                       @Rar(CLASS_HANDLER_NAME) RequestParameterHandler requestParameterHandler) {
            if (requestParameterHandler != null) {
                requestParameterHandler.requestInit(mc, request, config);
            }
        }

        /**
         * 填充请求参数的回调函数
         *
         * @param mc                      方法上下文
         * @param request                 请求对象
         * @param config                  当前HTTP客户端的配置
         * @param requestParameterHandler 参数处理器扩展对象
         */
        @Callback(lifecycle = Lifecycle.REQUEST)
        public static void fillHttpRequestParameter(MethodContext mc,
                                                    Request request,
                                                    @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config,
                                                    @Rar(CLASS_HANDLER_NAME) RequestParameterHandler requestParameterHandler) {
            // Query param setter
            setParameter(mc, request, config.getQueryParams(), req -> req.getRequest().addQueryParameter(req.getName(), req.getValue()));

            // Path param setter
            setParameter(mc, request, config.getPathParams(), req -> req.getRequest().addPathParameter(req.getName(), req.getValue()));

            // Header param setter
            setParameter(mc, request, config.getHeaderParams(), req -> req.getRequest().setHeader(req.getName(), req.getValue()));

            // Form param setter
            setParameter(mc, request, config.getFormParams(), req -> req.getRequest().addFormParameter(req.getName(), req.getValue()));

            // MultipartFormData param setter
            setMultipartFormData(mc, request, config.getMultipartFormParams());

            // 执行扩展方法
            if (requestParameterHandler != null) {
                requestParameterHandler.requestCompleted(mc, request, config);
            }
        }

        /**
         * 用于获取HTTP服务地址的函数
         *
         * @param cc     类上下文
         * @param config 当前HTTP客户端的配置
         * @return 目标HTTP服务地址
         */
        @FunctionAlias("get_http_server_url")
        public static String getHttpServerUrl(ClassContext cc,
                                              @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config) {
            if (config == null || !StringUtils.hasText(config.getUrl())) {
                throw new ConfigurationParserException("Missing necessary configuration: ['lucky.http-client.simple-client-configs.{}.url']", getConfigId(cc));
            }
            return config.getUrl();
        }

        /**
         * 设置参数
         *
         * @param mc              方法上下文
         * @param request         请求对象
         * @param configMap       配置 Map
         * @param requestConsumer 请求消费者
         */
        private static void setParameter(MethodContext mc, Request request, Map<String, Object> configMap, Consumer<RequestParameter> requestConsumer) {
            if (ContainerUtils.isEmptyMap(configMap)) {
                return;
            }
            configMap.forEach((name, value) -> {
                String pName = mc.parseExpression(name, String.class);
                if (ContainerUtils.isIterable(value)) {
                    ContainerUtils.getIterable(value).forEach(e -> {
                        requestConsumer.accept(RequestParameter.of(pName, mc.parseExpression(String.valueOf(e)), request));
                    });
                } else {
                    requestConsumer.accept(RequestParameter.of(pName, mc.parseExpression(String.valueOf(value)), request));
                }
            });
        }

        /**
         * 设置MultipartFormData类型的参数
         *
         * @param mc                方法上下文
         * @param request           请求对象
         * @param multipartFormData MultipartFormData配置
         */
        private static void setMultipartFormData(MethodContext mc, Request request, MultipartFormData multipartFormData) {
            // Txt param setter
            setParameter(mc, request, multipartFormData.getTxt(), req -> req.getRequest().addMultipartFormParameter(req.getName(), req.getValue()));

            // File param setter
            setParameter(mc, request, multipartFormData.getFile(), req -> req.getRequest().addResources(req.getName(), ResourceFunctions.resource(String.valueOf(req.getValue()))));
        }

        /**
         * 获取当前API的ConfigId
         *
         * @param cc 类上下文
         * @return 当前API的ConfigId
         */
        public static String getConfigId(ClassContext cc) {
            Class<?> proxyObjectClass = cc.getCurrentAnnotatedElement();
            Component componentAnn = AnnotationUtils.findMergedAnnotation(proxyObjectClass, Component.class);
            if (componentAnn != null && StringUtils.hasText(componentAnn.value())) {
                return componentAnn.value();
            }
            String beanClassName = proxyObjectClass.getName();
            String shortClassName = org.springframework.util.ClassUtils.getShortName(beanClassName);
            return Introspector.decapitalize(shortClassName);
        }
    }

    /**
     * 请求参数
     */
    class RequestParameter {
        private final String name;
        private final Object value;
        private final Request request;

        private RequestParameter(String name, Object value, Request request) {
            this.name = name;
            this.value = value;
            this.request = request;
        }

        public static RequestParameter of(String name, Object value, Request request) {
            return new RequestParameter(name, value, request);
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public Request getRequest() {
            return request;
        }
    }
}
