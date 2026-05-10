package io.github.lucklike.httpclient.std;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.annotations.HttpRequest;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerateUtil;
import com.luckyframework.httpclient.proxy.annotations.RespConvert;
import com.luckyframework.httpclient.proxy.configapi.Api;
import com.luckyframework.httpclient.proxy.configapi.ApiConfig;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationParserException;
import com.luckyframework.httpclient.proxy.configapi.MultipartFormData;
import com.luckyframework.httpclient.proxy.context.ClassContext;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.Rar;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import com.luckyframework.httpclient.proxy.spel.hook.Lifecycle;
import com.luckyframework.httpclient.proxy.spel.hook.callback.Callback;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.spel.LazyValue;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.config.GenerateEntry;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.discovery.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AliasFor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.luckyframework.common.ContainerUtils.mergeList;
import static com.luckyframework.common.ContainerUtils.mergeMap;
import static com.luckyframework.common.StringUtils.blankReturnDefault;
import static com.luckyframework.common.StringUtils.nullReturnDefault;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_CONFIG_BEAN_NAME;

/**
 * 标准的HTTP客户端
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiConfig
@HttpRequest(func = "__get_http_server_path__")
@HttpClient(func = "__get_http_server_url__")
@RespConvert(metaTypeFunc = "__get_response_meta_type__", resultFunc = "__result_convert__")
@SpELImport(StdHttpClient.SimpleHttpClientFunctionAndCallback.class)
public @interface StdHttpClient {

    /**
     * 配置ID，配置ID相同的接口共享同一套配置
     */
    @AliasFor(annotation = ApiConfig.class, attribute = "value")
    String configId() default "";

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
    Class<? extends LifeCycleManager> lifecycle() default StandardLifeCycleManager.class;

    /**
     * 生命周期管理器对象的 Bean 名称
     */
    String lifecycleBean() default "";

    /**
     * 生命周期管理器对象生成器对象
     */
    ObjectGenerate lifecycleGenerate() default @ObjectGenerate(LifeCycleManager.class);

    /**
     * 简单HTTP客户端实现相关的工具函数和回调函数
     */
    class SimpleHttpClientFunctionAndCallback {

        // 存储标准客户端配置对象的变量名
        public static final String STANDARD_HTTP_CLIENT_CONFIG_NAME = "$StandardHttpClientConfiguration";
        // 存储标准API配置对象的变量名
        public static final String STANDARD_API_CONFIG_NAME = "$StandardApiConfiguration";
        // 存储生命周期管理器对象变量名
        public static final String LIFE_CYCLE_MANAGER_NAME = "$LifeCycleManager";

        /**
         * 将当前Http客户端相关的配置加载到上下文中
         *
         * @param cc                   类上下文
         * @param factoryConfiguration 全局配置
         * @return 当前Http客户端相关的配置
         */
        @Callback(lifecycle = Lifecycle.CLASS, storeOrNot = true, unfold = true)
        public static Map<String, Object> loadSimpleHttpClientConfig(
                ClassContext cc,
                @Qualifier(PROXY_FACTORY_CONFIG_BEAN_NAME) HttpClientProxyObjectFactoryConfiguration factoryConfiguration
        ) {

            // 初始化LifeCycleManager和SimpleHttpClientConfiguration配置
            Map<String, StandardHttpClientConfiguration> simpleHttpClientConfigs = factoryConfiguration.getStandardClientConfigs();
            StandardHttpClientConfiguration config = simpleHttpClientConfigs.get(CommonFunctions.getApiConfigId(cc));
            config.removeNonEffectiveConfig();

            // 封装成 Map 后返回
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put(STANDARD_HTTP_CLIENT_CONFIG_NAME, config);
            resultMap.put(LIFE_CYCLE_MANAGER_NAME, LazyValue.of(() -> createLifeCycleManager(cc, config)));
            return resultMap;
        }

        /**
         * 创建标准 API 配置对象
         *
         * @param mec    方法元上下文
         * @param config 当前HTTP客户端的配置
         * @return 当前标准 API 的配置
         */
        @Callback(lifecycle = Lifecycle.METHOD_META, storeOrNot = true, storeName = STANDARD_API_CONFIG_NAME)
        public static StandardApiConfiguration createStandardApiConfig(
                MethodMetaContext mec,
                @Rar(STANDARD_HTTP_CLIENT_CONFIG_NAME) StandardHttpClientConfiguration config
        ) {
            return mergeConfig(mec, config);
        }

        /**
         * 创建{@link LifeCycleManager}对象
         *
         * @param cc     类上下文
         * @param config 当前HTTP客户端的配置
         * @return {@link LifeCycleManager}对象
         */
        @NonNull
        @SuppressWarnings("unchecked")
        private static LifeCycleManager createLifeCycleManager(ClassContext cc, StandardHttpClientConfiguration config) {
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

            // 从注解中进行初始化
            StdHttpClient ann = cc.getMergedAnnotation(StdHttpClient.class);
            ObjectGenerate generate = ann.lifecycleGenerate();

            // 使用生成器对象
            if (ObjectGenerateUtil.isEffectiveObjectGenerate(generate, LifeCycleManager.class)) {
                return cc.generateObject(generate);
            }

            // 尝试从 Spring 容器中获取
            if (StringUtils.hasText(ann.lifecycleBean())) {
                return ApplicationContextUtils.getBean(ann.lifecycleBean(), LifeCycleManager.class);
            }

            // 最后使用 Class
            if (ann.lifecycle() != LifeCycleManager.class) {
                return cc.generateObject(ann.lifecycle(), Scope.SINGLETON);
            }

            // 没有配置生命周期管理器时直接报错
            throw new ConfigurationParserException("@StdHttpClient('{}')[{}] Lifecycle manager configuration is missing", CommonFunctions.getApiConfigId(cc), cc.getCurrentAnnotatedElement().getName());
        }

        /**
         * 请求对象初始化完成时调用
         *
         * @param mc               方法上下文
         * @param request          请求对象
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         */
        @Callback(lifecycle = Lifecycle.REQUEST_INIT)
        public static void requestInit(MethodContext mc,
                                       Request request,
                                       @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                                       @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            lifeCycleManager.requestInit(mc, request, apiConfig);
        }

        /**
         * 请求对象封装完成时调用
         *
         * @param mc               方法上下文
         * @param request          请求对象
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         */
        @Callback(lifecycle = Lifecycle.REQUEST)
        public static void requestCompleted(MethodContext mc,
                                            Request request,
                                            @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                                            @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            lifeCycleManager.requestCompleted(mc, request, apiConfig);
        }


        /**
         * 响应对象返回完成时调用
         *
         * @param mc               方法上下文
         * @param response         响应对象
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         */
        @Callback(lifecycle = Lifecycle.RESPONSE)
        public static void responseCompleted(MethodContext mc,
                                             Response response,
                                             @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                                             @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            if (lifeCycleManager != null) {
                lifeCycleManager.responseCompleted(mc, response, apiConfig);
            }
        }

        /**
         * 用于获取HTTP服务地址的函数
         *
         * @param mc               方法上下文
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         * @return 目标HTTP服务地址
         */
        @FunctionAlias("__get_http_server_url__")
        public static String getHttpServerUrl(MethodContext mc,
                                              @Rar(STANDARD_HTTP_CLIENT_CONFIG_NAME) StandardApiConfiguration apiConfig,
                                              @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            return lifeCycleManager.buildBaseUrl(mc, apiConfig);
        }

        /**
         * 用于获取HTTP接口Path的函数
         *
         * @param mc               方法上下文
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         * @return HTTP接口Path
         */
        @FunctionAlias("__get_http_server_path__")
        public static String getHttpServerPath(MethodContext mc,
                                               @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                                               @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            return lifeCycleManager.buildApiPath(mc, apiConfig);
        }

        /**
         * 获取响应元类型
         *
         * @param mc               方法上下文对象
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         * @return 响应元类型
         */
        @FunctionAlias("__get_response_meta_type__")
        public static Object getResponseMetaType(MethodContext mc,
                                                 @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                                                 @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            return lifeCycleManager.getResponseMetaType(mc, apiConfig);
        }

        /**
         * 结果转换
         *
         * @param mc               方法上下文
         * @param response         响应对象
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         * @return 最终的响应结果
         */
        @FunctionAlias("__result_convert__")
        public static Object resultConvert(MethodContext mc,
                                           Response response,
                                           @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                                           @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager) throws Exception {
            return lifeCycleManager.resultConvert(mc, response, apiConfig);
        }

        /**
         * 合并配置
         *
         * @param mec    元方法上下文
         * @param config 标准 API 配置
         * @return 合并后的配置
         */
        @SuppressWarnings("unchecked")
        private static StandardApiConfiguration mergeConfig(MethodMetaContext mec, StandardHttpClientConfiguration config) {
            String apiId = getApiId(mec);
            StandardApiConfiguration methodConfig = config.getMethodConfigs().get(apiId);
            if (methodConfig == null) {
                return config;
            }

            StandardApiConfiguration apiConfig = new StandardApiConfiguration();
            apiConfig.setUrl(methodConfig.getUrl());
            apiConfig.setMethod(nullReturnDefault(config.getMethod(), methodConfig.getMethod()));
            apiConfig.setConnectTimeout(nullReturnDefault(config.getConnectTimeout(), methodConfig.getConnectTimeout()));
            apiConfig.setReadTimeout(nullReturnDefault(config.getReadTimeout(), methodConfig.getReadTimeout()));
            apiConfig.setWriteTimeout(nullReturnDefault(config.getWriteTimeout(), methodConfig.getWriteTimeout()));
            apiConfig.setCallTimeout(nullReturnDefault(config.getCallTimeout(), methodConfig.getCallTimeout()));
            apiConfig.setConnectionRequestTimeout(nullReturnDefault(config.getConnectionRequestTimeout(), methodConfig.getConnectionRequestTimeout()));

            apiConfig.setHeaderParams(mergeMap(config.getHeaderParams(), methodConfig.getHeaderParams()));
            apiConfig.setPathParams(mergeMap(config.getPathParams(), methodConfig.getPathParams()));
            apiConfig.setQueryParams(mergeMap(config.getQueryParams(), methodConfig.getQueryParams()));
            apiConfig.setFormParams(mergeMap(config.getFormParams(), methodConfig.getFormParams()));
            apiConfig.setMultipartFormParams(mergeMultipartFormData(config.getMultipartFormParams(), methodConfig.getMultipartFormParams()));
            apiConfig.setBody(blankReturnDefault(config.getBody(), methodConfig.getBody()));

            apiConfig.setConditionHeaderParams(mergeList(config.getConditionHeaderParams(), methodConfig.getConditionHeaderParams()));
            apiConfig.setConditionPathParams(mergeList(config.getConditionPathParams(), methodConfig.getConditionPathParams()));
            apiConfig.setConditionQueryParams(mergeList(config.getConditionQueryParams(), methodConfig.getConditionQueryParams()));
            apiConfig.setConditionFormParams(mergeList(config.getConditionFormParams(), methodConfig.getConditionFormParams()));
            apiConfig.setConditionMultipartFormParams(mergeList(config.getConditionMultipartFormParams(), methodConfig.getConditionMultipartFormParams()));
            apiConfig.setConditionBody(mergeList(config.getConditionBody(), methodConfig.getConditionBody()));
            apiConfig.setConditionConvert(mergeList(config.getConditionConvert(), methodConfig.getConditionConvert()));

            apiConfig.setInitParams(mergeMap(config.getInitParams(), methodConfig.getInitParams()));
            apiConfig.setAdditionalParams(mergeMap(config.getAdditionalParams(), methodConfig.getAdditionalParams()));

            apiConfig.setMetaType(blankReturnDefault(config.getMetaType(), methodConfig.getMetaType()));
            apiConfig.setResultConvert(blankReturnDefault(config.getResultConvert(), methodConfig.getResultConvert()));

            return apiConfig;
        }

        /**
         * 获取配置APIID
         *
         * @param mc 方法上下文
         * @return 配置APIID
         */
        private static String getApiId(MethodMetaContext mc) {
            Api api = mc.getMergedAnnotation(Api.class);
            return api == null ? mc.getCurrentAnnotatedElement().getName() : api.value();
        }

        /**
         * 合并MultipartFormData配置
         *
         * @param cf Class 级别配置
         * @param mf Method 级别配置
         * @return 合并后的配置
         */
        @SuppressWarnings("unchecked")
        private static MultipartFormData mergeMultipartFormData(MultipartFormData cf, MultipartFormData mf) {
            MultipartFormData multipartFormData = new MultipartFormData();
            multipartFormData.setTxt(mergeMap(cf.getTxt(), mf.getTxt()));
            multipartFormData.setFile(mergeMap(cf.getFile(), mf.getFile()));
            return multipartFormData;
        }
    }
}
