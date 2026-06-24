package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.core.util.BeanUtils;
import com.luckyframework.httpclient.proxy.annotations.HttpRequest;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerateUtil;
import com.luckyframework.httpclient.proxy.annotations.RespConvert;
import com.luckyframework.httpclient.proxy.configapi.ApiConfig;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationParserException;
import com.luckyframework.httpclient.proxy.configapi.MultipartFormData;
import com.luckyframework.httpclient.proxy.configapi.SpELImportConf;
import com.luckyframework.httpclient.proxy.context.ClassContext;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.httpclient.proxy.generator.GeneratedJavaCodeConfiguration;
import com.luckyframework.httpclient.proxy.generator.GeneratedJavaCodeUtils;
import com.luckyframework.httpclient.proxy.generator.GeneratedResponseJavaBeanFunction;
import com.luckyframework.httpclient.proxy.mock.Mock;
import com.luckyframework.httpclient.proxy.mock.MockResponse;
import com.luckyframework.httpclient.proxy.mock.config.MockBody;
import com.luckyframework.httpclient.proxy.mock.config.MockConfigFunction;
import com.luckyframework.httpclient.proxy.mock.config.MockConfiguration;
import com.luckyframework.httpclient.proxy.mock.config.WhenMockResult;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.Rar;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import com.luckyframework.httpclient.proxy.spel.hook.AsyncHook;
import com.luckyframework.httpclient.proxy.spel.hook.Lifecycle;
import com.luckyframework.httpclient.proxy.spel.hook.callback.Callback;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpack;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.spel.LazyValue;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.config.GenerateEntry;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.config.mock.MockResult;
import io.github.lucklike.httpclient.discovery.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AliasFor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.luckyframework.common.ContainerUtils.mergeCollection;
import static com.luckyframework.common.ContainerUtils.mergeMap;
import static com.luckyframework.common.StringUtils.blankReturnDefault;
import static com.luckyframework.common.StringUtils.nullReturnDefault;
import static com.luckyframework.httpclient.proxy.function.CommonFunctions.getApiId;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_CONFIG_BEAN_NAME;

/**
 * 标准的HTTP客户端
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiConfig
@HttpRequest
@HttpClient(func = "__get_http_server_url__", service = "#{__get_http_service_name__($mc$)}")
@Mock(enable = "#{__std_mock_enable__($mc$)}", mockFunc = "__std_mock_result__")
@RespConvert(metaTypeFunc = "__get_response_meta_type__", resultFunc = "__result_convert__", respContentType = "#{__mandatory_designation_response_content_type__($mc$)}")
@SpELImport({GeneratedResponseJavaBeanFunction.class, StdHttpClient.StandardHttpClientFunctionAndCallback.class})
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
     * 标准HTTP客户端实现相关的工具函数和回调函数
     */
    class StandardHttpClientFunctionAndCallback {
        private static final Logger logger = LoggerFactory.getLogger(StandardHttpClientFunctionAndCallback.class);

        // 存储标准客户端配置对象的变量名
        public static final String STANDARD_HTTP_CLIENT_CONFIG_NAME = "$StandardHttpClientConfiguration";
        // 存储标准API配置对象的变量名
        public static final String STANDARD_API_CONFIG_NAME = "$StandardApiConfiguration";
        // 存储生命周期管理器对象变量名
        public static final String LIFE_CYCLE_MANAGER_NAME = "$LifeCycleManager";
        // 存储标准客户端 Mock 相关配置的变量名
        public static final String STANDARD_MOCK_CONFIG = "$StandardMockConfiguration";

        static {
            ContextValueUnpack.addParameterConvert(new StdInitBindParameterConvert());
        }


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
            StandardHttpClientConfiguration config = loadStandardHttpClientConfiguration(cc, factoryConfiguration);
            config.removeNonEffectiveConfig();

            // 注册ClassContent级别SpEL变量、函数、Hooks、包
            SpELImportConf springElImport = config.getSpelImport();
            if (springElImport != null) {
                springElImport.importSpELRuntime(cc);
            }

            // 封装成 Map 后返回
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put(STANDARD_HTTP_CLIENT_CONFIG_NAME, config);
            resultMap.put(LIFE_CYCLE_MANAGER_NAME, LazyValue.of(() -> createLifeCycleManager(cc, config)));
            return resultMap;
        }

        /**
         * 创建标准 API 配置对象
         *
         * @param mec              方法元上下文
         * @param config           当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         * @return 当前标准 API 的配置
         */
        @Callback(lifecycle = Lifecycle.METHOD_META, storeOrNot = true, unfold = true)
        public static Map<String, Object> methodMetaContentInit(
                MethodMetaContext mec,
                @Rar(STANDARD_HTTP_CLIENT_CONFIG_NAME) StandardHttpClientConfiguration config,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) {
            Map<String, Object> resultMap = new HashMap<>(2);
            StandardApiConfiguration methodConfig = mergeConfig(mec, config);
            if (methodConfig.getRetryConfig() != null) {
                methodConfig.getRetryConfig().init();
            }
            resultMap.put(STANDARD_API_CONFIG_NAME, methodConfig);
            resultMap.put(STANDARD_MOCK_CONFIG, createMockConfiguration(mec, config));

            // 注册MethodMetaCOntent级别SpEL变量、函数、Hooks、包
            SpELImportConf methodMetaSpringElImport = config.getMethodMetaSpelImport();
            if (methodMetaSpringElImport != null) {
                methodMetaSpringElImport.importSpELRuntime(mec);
            }
            lifeCycleManager.methodMetaContentInit(mec, methodConfig);
            return resultMap;
        }

        /**
         * 方法上下文初始化时执行
         *
         * @param mc               方法元上下文
         * @param apiConfig        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         */
        @Callback(lifecycle = Lifecycle.METHOD)
        public static void methodContentInit(
                MethodContext mc,
                @Rar(STANDARD_HTTP_CLIENT_CONFIG_NAME) StandardHttpClientConfiguration config,
                @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) {
            // 注册MethodContent级别的SpEL变量、函数、Hooks、包
            SpELImportConf commonMethodSpringElImport = config.getMethodSpelImport();
            if (commonMethodSpringElImport != null) {
                commonMethodSpringElImport.importSpELRuntime(mc);
            }
            SpELImportConf methodSpELImportConf = apiConfig.getSpelImport();
            if (methodSpELImportConf != null) {
                methodSpELImportConf.importSpELRuntime(mc);
            }
            lifeCycleManager.methodContentInit(mc, apiConfig);
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
        public static void requestInit(
                MethodContext mc,
                Request request,
                @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) throws Exception {
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
        public static void requestCompleted(
                MethodContext mc,
                Request request,
                @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) throws Exception {
            lifeCycleManager.requestInitCompleted(mc, request, apiConfig);
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
        public static void responseCompleted(
                MethodContext mc,
                Response response,
                @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) throws Exception {
            if (lifeCycleManager != null) {
                lifeCycleManager.responseCompleted(mc, response, apiConfig);
            }
        }

        /**
         * 异步钩子方法，在响应生命周期阶段触发生成Java代码。
         * 此方法由框架自动调用，负责将注解配置转换为内部配置对象，
         * 并委托给 {@link GeneratedJavaCodeUtils} 执行实际的代码生成逻辑。
         *
         * @param mc       方法上下文
         * @param response HTTP响应对象
         * @throws IOException 文件写入失败时抛出
         */
        @AsyncHook
        @Callback(enable = "#{__std_generated_java_code_enable__($mc$)}", lifecycle = Lifecycle.RESPONSE, errorInterrupt = false)
        public static void generatedJavaCode(
                MethodContext mc,
                Response response,
                @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig
        ) throws Exception {
            GeneratedJavaCodeConfiguration codeConfig = apiConfig.getGenerateResponseJavaBean();
            if (codeConfig != null && Objects.equals(Boolean.TRUE, codeConfig.getEnable())) {
                GeneratedJavaCodeUtils.generatedJavaCode(mc, response, codeConfig);
            }
        }

        @FunctionAlias("__std_generated_java_code_enable__")
        public static boolean stdGeneratedJavaCodeEnable(MethodContext mc) {
            StandardApiConfiguration apiConfig = mc.getRootVar(STANDARD_API_CONFIG_NAME, StandardApiConfiguration.class);
            GeneratedJavaCodeConfiguration codeConfig = apiConfig.getGenerateResponseJavaBean();
            return codeConfig != null && Objects.equals(Boolean.TRUE, codeConfig.getEnable());
        }

        /**
         * 用于获取HTTP服务地址的函数
         *
         * @param mc               方法上下文
         * @param config        当前HTTP客户端的配置
         * @param lifeCycleManager 生命周期管理器对象
         * @return 目标HTTP服务地址
         */
        @FunctionAlias("__get_http_server_url__")
        public static String getHttpServerUrl(
                MethodContext mc,
                @Rar(STANDARD_HTTP_CLIENT_CONFIG_NAME) StandardHttpClientConfiguration config,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) throws Exception {
            return lifeCycleManager.buildBaseUrl(mc, config);
        }

        /**
         * 获取服务名，用于从注册中心获取URL
         *
         * @param mc 方法上下文
         * @return 服务名
         */
        @FunctionAlias("__get_http_service_name__")
        public static String getHttpServiceName(MethodContext mc) {
            StandardHttpClientConfiguration config = mc.getRootVar(STANDARD_HTTP_CLIENT_CONFIG_NAME, StandardHttpClientConfiguration.class);
            return config.getService();
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
        public static Object getResponseMetaType(
                MethodContext mc,
                @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) throws Exception {
            return lifeCycleManager.getResponseMetaType(mc, apiConfig);
        }


        /**
         * 强制指定响应体的Content-Type
         *
         * @param mc 方法上下文对象
         * @return 强制指定的响应体Content-Type
         */
        @FunctionAlias("__mandatory_designation_response_content_type__")
        public static String mandatoryDesignationResponseContentType(MethodContext mc) {
            StandardApiConfiguration apiConfig = mc.getRootVar(STANDARD_API_CONFIG_NAME, StandardApiConfiguration.class);
            LifeCycleManager lifeCycleManager = mc.getRootVar(LIFE_CYCLE_MANAGER_NAME, LifeCycleManager.class);
            return lifeCycleManager.mandatoryDesignationResponseContentType(mc, apiConfig);
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
        public static Object resultConvert(
                MethodContext mc,
                Response response,
                @Rar(STANDARD_API_CONFIG_NAME) StandardApiConfiguration apiConfig,
                @Rar(LIFE_CYCLE_MANAGER_NAME) LifeCycleManager lifeCycleManager
        ) throws Exception {
            return lifeCycleManager.resultConvert(mc, response, apiConfig);
        }

        /**
         * 是否启用 Mock 功能
         *
         * @param mc 方法上下文
         * @return 是否启用 Mock 文件
         */
        @FunctionAlias("__std_mock_enable__")
        public static boolean stdMockEnable(MethodContext mc) {
            MockConfiguration mockConfig = mc.getRootVar(STANDARD_MOCK_CONFIG, MockConfiguration.class);
            return MockConfigFunction.mockEnable(mc, mockConfig);
        }

        /**
         * 返回 Mock 结果
         *
         * @param mc         方法上下文
         * @param mockConfig Mock 配置
         * @return Mock 结果
         * @throws InterruptedException 可能出现的异常
         */
        @FunctionAlias("__std_mock_result__")
        public static MockResponse stdMockResult(
                MethodContext mc,
                @Rar(STANDARD_MOCK_CONFIG) MockConfiguration mockConfig
        ) throws InterruptedException {

            // 将Mock配置转化为MockResponse对象
            MockResponse mockResponse = MockConfigFunction.mockResult(mc, mockConfig);

            // 设置特殊Mock响应头
            mockResponse.header("Mock-Annotation", "@StdHttpClient");
            mockResponse.header("Mock-Environment-Prefix", StringUtils.format("lucky.http-client.standard-client-configs.{}", CommonFunctions.getApiConfigId(mc.getClassContext())));
            mockResponse.header("Mock-Environment-Property", CommonFunctions.getApiId(mc));

            //return
            return mockResponse;
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
         * 加载{@link StandardHttpClientConfiguration}配置
         *
         * @param cc                   类上下文
         * @param factoryConfiguration 全局配置
         * @return {@link StandardHttpClientConfiguration}配置
         */
        private static StandardHttpClientConfiguration loadStandardHttpClientConfiguration(ClassContext cc, HttpClientProxyObjectFactoryConfiguration factoryConfiguration) {
            // 初始化LifeCycleManager和SimpleHttpClientConfiguration配置
            String apiConfigId = CommonFunctions.getApiConfigId(cc);
            Map<String, StandardHttpClientConfiguration> simpleHttpClientConfigs = factoryConfiguration.getStandardClientConfigs();
            StandardHttpClientConfiguration config = simpleHttpClientConfigs.get(apiConfigId);
            if (config == null) {
                // 是否开启标准客户端初始化配置校验
                if (factoryConfiguration.isEnableStdClientInitCheck()) {
                    throw new ConfigurationParserException(
                            "@StdHttpClient('{0}')[{1}] Missing the necessary configuration: 'lucky.http-client.standard-client-configs.{0}'",
                            apiConfigId,
                            cc.getCurrentAnnotatedElement().getName()
                    );
                } else {
                    logger.warn(
                            "[⛔] Unavailable standard http client: @StdHttpClient('{}')[{}] -- Missing the necessary configuration: 'lucky.http-client.standard-client-configs.{}'",
                            apiConfigId,
                            cc.getCurrentAnnotatedElement().getName(),
                            apiConfigId
                    );
                    config = new StandardHttpClientConfiguration();
                }
            }
            return config;
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

            // 检查URL和Service配置
            if (!StringUtils.hasText(config.getUrl())  && !StringUtils.hasText(config.getService())) {
                throw new ConfigurationParserException(
                        "@StdHttpClient('{0}')[{1}] Missing the necessary configuration, at least one of the following configurations should be configured：['lucky.http-client.standard-client-configs.{0}.url'] or ['lucky.http-client.standard-client-configs.{0}.service'] ",
                        CommonFunctions.getApiConfigId(mec.getParentContext()),
                        mec.getParentContext().getCurrentAnnotatedElement().getName(),
                        apiId
                ).error(logger);
            }

            // 只有类配置，没有方法配置
            if (methodConfig == null) {
                StandardHttpClientConfiguration apiConfig = new StandardHttpClientConfiguration();
                BeanUtils.copyProperties(config, apiConfig);
                apiConfig.getAdditionalParams().setContext(mec);

                // 将GeneratedJavaCodeConfiguration的extractExpression设置默认值为resultConvert
                GeneratedJavaCodeConfiguration codeConfiguration = apiConfig.getGenerateResponseJavaBean();
                if (codeConfiguration != null && !StringUtils.hasText(codeConfiguration.getExtractExpression())) {
                    codeConfiguration.setExtractExpression(apiConfig.getResultConvert());
                }
                return apiConfig;
            }

            StandardApiConfiguration apiConfig = new StandardApiConfiguration();
            apiConfig.setPath(StringUtils.joinUrlPath(config.getPath(), methodConfig.getPath()));
            apiConfig.setDesc(blankReturnDefault(config.getDesc(), "") + blankReturnDefault(methodConfig.getDesc(), ""));
            apiConfig.setMethod(nullReturnDefault(methodConfig.getMethod(), config.getMethod()));
            apiConfig.setConnectTimeout(nullReturnDefault(methodConfig.getConnectTimeout(), config.getConnectTimeout()));
            apiConfig.setReadTimeout(nullReturnDefault(methodConfig.getReadTimeout(), config.getReadTimeout()));
            apiConfig.setWriteTimeout(nullReturnDefault(methodConfig.getWriteTimeout(), config.getWriteTimeout()));
            apiConfig.setCallTimeout(nullReturnDefault(methodConfig.getCallTimeout(), config.getCallTimeout()));
            apiConfig.setConnectionRequestTimeout(nullReturnDefault(methodConfig.getConnectionRequestTimeout(), config.getConnectionRequestTimeout()));

            apiConfig.setHeaderParams(mergeMap(config.getHeaderParams(), methodConfig.getHeaderParams()));
            apiConfig.setPathParams(mergeMap(config.getPathParams(), methodConfig.getPathParams()));
            apiConfig.setQueryParams(mergeMap(config.getQueryParams(), methodConfig.getQueryParams()));
            apiConfig.setFormParams(mergeMap(config.getFormParams(), methodConfig.getFormParams()));
            apiConfig.setMultipartFormParams(mergeMultipartFormData(config.getMultipartFormParams(), methodConfig.getMultipartFormParams()));
            apiConfig.setBody(blankReturnDefault(methodConfig.getBody(), config.getBody()));

            apiConfig.setConditionHeaderParams(mergeCollection(config.getConditionHeaderParams(), methodConfig.getConditionHeaderParams()));
            apiConfig.setConditionPathParams(mergeCollection(config.getConditionPathParams(), methodConfig.getConditionPathParams()));
            apiConfig.setConditionQueryParams(mergeCollection(config.getConditionQueryParams(), methodConfig.getConditionQueryParams()));
            apiConfig.setConditionFormParams(mergeCollection(config.getConditionFormParams(), methodConfig.getConditionFormParams()));
            apiConfig.setConditionMultipartFormParams(mergeCollection(config.getConditionMultipartFormParams(), methodConfig.getConditionMultipartFormParams()));
            apiConfig.setConditionBody(mergeCollection(methodConfig.getConditionBody(), config.getConditionBody()));
            apiConfig.setConditionConvert(mergeCollection( methodConfig.getConditionConvert(), config.getConditionConvert()));
            apiConfig.setConditionMetaType(mergeCollection(methodConfig.getConditionMetaType(), config.getConditionMetaType()));
            apiConfig.setConditionRespContentType(mergeCollection(methodConfig.getConditionRespContentType(), config.getConditionRespContentType()));

            apiConfig.setInitBindParams(mergeInitBindParams(config.getInitBindParams(), methodConfig.getInitBindParams()));
            apiConfig.setAdditionalParams(mergeAdditionalParams(mec, config.getAdditionalParams(), methodConfig.getAdditionalParams()));

            apiConfig.setMetaType(blankReturnDefault(methodConfig.getMetaType(), config.getMetaType()));
            apiConfig.setResponseContentType(blankReturnDefault(methodConfig.getResponseContentType(), config.getResponseContentType()));
            apiConfig.setResultConvert(blankReturnDefault(methodConfig.getResultConvert(), config.getResultConvert()));
            apiConfig.setSslConfig(nullReturnDefault(methodConfig.getSslConfig(), config.getSslConfig()));
            apiConfig.setRetryConfig(nullReturnDefault(methodConfig.getRetryConfig(), config.getRetryConfig()));
            apiConfig.setGenerateResponseJavaBean(nullReturnDefault(methodConfig.getGenerateResponseJavaBean(), config.getGenerateResponseJavaBean()));
            apiConfig.setSpelImport(methodConfig.getSpelImport());
            apiConfig.setMethodMetaSpelImport(methodConfig.getMethodMetaSpelImport());


            // 将GeneratedJavaCodeConfiguration的extractExpression设置默认值为resultConvert
            GeneratedJavaCodeConfiguration codeConfiguration = apiConfig.getGenerateResponseJavaBean();
            if (codeConfiguration != null && !StringUtils.hasText(codeConfiguration.getExtractExpression())) {
                codeConfiguration.setExtractExpression(apiConfig.getResultConvert());
            }

            return apiConfig;
        }

        @SuppressWarnings("unchecked")
        private static MockConfiguration createMockConfiguration(MethodMetaContext mec, StandardHttpClientConfiguration stdConfig) {
            MockResult classMockResult = stdConfig.getMockConfig();
            String apiId = getApiId(mec);
            StandardApiConfiguration methodConfig = stdConfig.getMethodConfigs().get(apiId);
            MockResult methodMockResult = methodConfig == null ? null : methodConfig.getMockConfig();

            // 未配置时返回 null
            if (classMockResult == null && methodMockResult == null) {
                return null;
            }
            MockConfiguration mockConfig = new MockConfiguration();

            // 设置类级别配置
            if (classMockResult != null) {
                mockConfig.setEnable(classMockResult.isEnable());
                mockConfig.setLatency(classMockResult.getLatency());
            }

            // 设置方法级别配置
            if (methodMockResult != null) {
                Map<String, com.luckyframework.httpclient.proxy.mock.config.MockResult> methodConfigs = new LinkedHashMap<>();
                com.luckyframework.httpclient.proxy.mock.config.MockResult mMockConfig = new com.luckyframework.httpclient.proxy.mock.config.MockResult();
                mMockConfig.setEnable(methodMockResult.isEnable());
                mMockConfig.setLatency(methodMockResult.getLatency());
                mMockConfig.setStatus(methodMockResult.getStatus());

                boolean hasClassConfig = classMockResult != null;
                if (hasClassConfig) {
                    mMockConfig.setHeaders(mergeMap(classMockResult.getHeaders(), methodMockResult.getHeaders()));
                    mMockConfig.setMatch(convertToWhenMockResults(mergeCollection(classMockResult.getMatch(), methodMockResult.getMatch())));

                    // set mock body
                    mMockConfig.setBody(convertToMockBody(nullReturnDefault(methodMockResult.getBody(), classMockResult.getBody())));
                } else {
                    mMockConfig.setHeaders(methodMockResult.getHeaders());
                    mMockConfig.setMatch(convertToWhenMockResults(methodMockResult.getMatch()));

                    // set mock body
                    mMockConfig.setBody(convertToMockBody(methodMockResult.getBody()));
                }

                methodConfigs.put(apiId, mMockConfig);
                mockConfig.setMethodConfigs(methodConfigs);
            }
            return mockConfig;

        }

        private static List<WhenMockResult> convertToWhenMockResults(List<io.github.lucklike.httpclient.config.mock.WhenMockResult> _whenMockResults) {
            if (ContainerUtils.isEmptyCollection(_whenMockResults)) {
                return Collections.emptyList();
            }
            List<WhenMockResult> listResult = new ArrayList<>(_whenMockResults.size());
            for (io.github.lucklike.httpclient.config.mock.WhenMockResult whenMockResult : _whenMockResults) {
                WhenMockResult when = new WhenMockResult();
                BeanUtils.copyProperties(whenMockResult, when);
                listResult.add(when);
            }
            return listResult;
        }

        private static MockBody convertToMockBody(io.github.lucklike.httpclient.config.mock.MockBody _mockBody) {
            MockBody mockBody = new MockBody();
            BeanUtils.copyProperties(_mockBody, mockBody);
            return mockBody;
        }

        /**
         * 合并初始化绑定参数
         *
         * @param cibp Class 级别配置
         * @param mibp Method 级别配置
         * @return 合并后的配置
         */
        @SuppressWarnings("unchecked")
        private static InitBindParams mergeInitBindParams(InitBindParams cibp, InitBindParams mibp) {
            InitBindParams initBindParams = new InitBindParams();
            initBindParams.setBindClasses(mergeCollection(cibp.getBindClasses(), mibp.getBindClasses()));
            initBindParams.setBindParams(mergeMap(cibp.getBindParams(), mibp.getBindParams()));
            return initBindParams;
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

        /**
         * 合并额外参数
         *
         * @param mec 元方法上下文
         * @param cap 类级别的额外参数
         * @param map 方法级别的额外参数
         * @return 合并后的额外参数
         */
        private static AdditionalParams mergeAdditionalParams(MethodMetaContext mec, AdditionalParams cap, AdditionalParams map) {
            AdditionalParams additionalParams = new AdditionalParams();
            additionalParams.putAll(cap);
            additionalParams.putAll(map);
            additionalParams.setContext(mec);
            return additionalParams;
        }
    }
}
