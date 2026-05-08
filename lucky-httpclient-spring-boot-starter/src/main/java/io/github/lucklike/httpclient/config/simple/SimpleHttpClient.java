package io.github.lucklike.httpclient.config.simple;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerateUtil;
import com.luckyframework.httpclient.proxy.annotations.RespConvert;
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
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.config.GenerateEntry;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.discovery.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AliasFor;
import org.springframework.lang.Nullable;
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
@HttpClient(func = "__get_http_server_url__")
@RespConvert(metaTypeFunc = "__get_response_meta_type__", resultFunc = "__result_convert__")
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
     * 生命周期管理器对象的 Class
     */
    Class<? extends LifeCycleManager> lifecycle() default LifeCycleManager.class;

    /**
     * 生命周期管理器对象生成器对象
     */
    ObjectGenerate lifecycleGenerate() default @ObjectGenerate(LifeCycleManager.class);

    /**
     * 简单HTTP客户端实现相关的工具函数和回调函数
     */
    class SimpleHttpClientFunctionAndCallback {

        // 配置对象名称
        public static final String CLASS_CONFIG_NAME = "$SimpleHttpClientConfig";
        // 生命周期管理器对象名称
        public static final String CLASS_LIFE_CYCLE_MANAGER_NAME = "$LifeCycleManager";

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

            // 初始化LifeCycleManager和SimpleHttpClientConfiguration配置
            Map<String, SimpleHttpClientConfiguration> simpleHttpClientConfigs = factoryConfiguration.getSimpleClientConfigs();
            SimpleHttpClientConfiguration config = simpleHttpClientConfigs.get(getConfigId(cc));

            // 封装成 Map 后返回
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put(CLASS_CONFIG_NAME, config);
            resultMap.put(CLASS_LIFE_CYCLE_MANAGER_NAME, createLifeCycleManager(cc, config));

            return resultMap;
        }

        /**
         * 创建{@link LifeCycleManager}对象
         *
         * @param cc     类上下文
         * @param config 当前HTTP客户端的配置
         * @return {@link LifeCycleManager}对象
         */
        @Nullable
        @SuppressWarnings("unchecked")
        private static LifeCycleManager createLifeCycleManager(ClassContext cc, SimpleHttpClientConfiguration config) {
            // 优先从配置中进行初始化
            if (config != null && config.getLifecycleManager() != null) {
                GenerateEntry<LifeCycleManager> generate = config.getLifecycleManager();
                return cc.generateObject(
                        generate.getType() == null ? LifeCycleManager.class : generate.getType(),
                        generate.getBeanName(),
                        generate.getScope(),
                        (Consumer<LifeCycleManager>) ClassUtils.newObject(generate.getConsumerClass())
                );
            }

            // 其次从注解中进行初始化
            SimpleHttpClient ann = cc.getMergedAnnotation(SimpleHttpClient.class);
            ObjectGenerate generate = ann.lifecycleGenerate();

            // 优先使用生成器对象
            if (ObjectGenerateUtil.isEffectiveObjectGenerate(generate, LifeCycleManager.class)) {
                return cc.generateObject(generate);
            }

            // 其实使用 Class
            if (ann.lifecycle() != LifeCycleManager.class) {
                return cc.generateObject(ann.lifecycle(), Scope.SINGLETON);
            }

            return null;
        }

        /**
         * 请求对象初始化完成时调用
         *
         * @param mc               方法上下文
         * @param request          请求对象
         * @param config           当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         */
        @Callback(lifecycle = Lifecycle.REQUEST_INIT)
        public static void requestInit(MethodContext mc,
                                       Request request,
                                       @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config,
                                       @Rar(CLASS_LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            if (lifeCycleManager != null) {
                lifeCycleManager.requestInit(mc, request, config);
            }
        }

        /**
         * 请求对象封装完成时调用
         *
         * @param mc               方法上下文
         * @param request          请求对象
         * @param config           当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         */
        @Callback(lifecycle = Lifecycle.REQUEST)
        public static void requestCompleted(MethodContext mc,
                                            Request request,
                                            @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config,
                                            @Rar(CLASS_LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
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
            if (lifeCycleManager != null) {
                lifeCycleManager.requestCompleted(mc, request, config);
            }
        }


        /**
         * 响应对象返回完成时调用
         *
         * @param mc               方法上下文
         * @param response         响应对象
         * @param config           当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         */
        @Callback(lifecycle = Lifecycle.RESPONSE)
        public static void responseCompleted(MethodContext mc,
                                             Response response,
                                             @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config,
                                             @Rar(CLASS_LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            if (lifeCycleManager != null) {
                lifeCycleManager.responseCompleted(mc, response, config);
            }
        }

        /**
         * 用于获取HTTP服务地址的函数
         *
         * @param cc     类上下文
         * @param config 当前HTTP客户端的配置
         * @return 目标HTTP服务地址
         */
        @FunctionAlias("__get_http_server_url__")
        public static String getHttpServerUrl(ClassContext cc,
                                              @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config) {
            if (config == null || !StringUtils.hasText(config.getUrl())) {
                throw new ConfigurationParserException("[@SimpleHttpClient('{0}')] Missing necessary configuration: 'lucky.http-client.simple-client-configs.{0}.url'", getConfigId(cc));
            }
            return config.getUrl();
        }

        /**
         * 获取响应元类型
         *
         * @param mc 方法上下文对象
         * @return 响应元类型
         */
        @FunctionAlias("__get_response_meta_type__")
        public static Object getResponseMetaType(MethodContext mc,
                                                 @Rar(CLASS_LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            if (lifeCycleManager != null) {
                return lifeCycleManager.getResponseMetaType(mc);
            }
            return Object.class;
        }

        /**
         * 结果转换
         *
         * @param mc               方法上下文
         * @param response         响应对象
         * @param config           当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         * @return 最终的响应结果
         */
        @FunctionAlias("__result_convert__")
        public static Object resultConvert(MethodContext mc,
                                           Response response,
                                           @Rar(CLASS_CONFIG_NAME) SimpleHttpClientConfiguration config,
                                           @Rar(CLASS_LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            if (lifeCycleManager != null) {
                return lifeCycleManager.resultConvert(mc, response, config);
            }
            return response.getEntity(mc.getResultType());
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
