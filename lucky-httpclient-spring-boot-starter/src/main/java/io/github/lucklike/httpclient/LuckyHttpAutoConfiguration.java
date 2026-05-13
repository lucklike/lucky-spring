package io.github.lucklike.httpclient;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.FontUtil;
import com.luckyframework.common.ScanUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.exception.LuckyRuntimeException;
import com.luckyframework.httpclient.core.convert.ProtobufAutoConvert;
import com.luckyframework.httpclient.core.convert.SpringMultipartFileAutoConvert;
import com.luckyframework.httpclient.core.encoder.BrotliContentEncodingConvertor;
import com.luckyframework.httpclient.core.encoder.ContentEncodingConvertor;
import com.luckyframework.httpclient.core.encoder.ZstdContentEncodingConvertor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.meta.CookieStore;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.core.processor.AbstractSaveResultResponseProcessor;
import com.luckyframework.httpclient.core.ssl.HostnameVerifierFactory;
import com.luckyframework.httpclient.core.ssl.KeyStoreInfo;
import com.luckyframework.httpclient.core.ssl.SSLException;
import com.luckyframework.httpclient.core.ssl.SSLSocketFactoryFactory;
import com.luckyframework.httpclient.core.ssl.SSLUtils;
import com.luckyframework.httpclient.core.ssl.TrustAllHostnameVerifier;
import com.luckyframework.httpclient.generalapi.plugin.ValidationPlugin;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.async.Model;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationApiFunctionalSupport;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationSource;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.creator.ObjectCreator;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.handle.HttpExceptionHandle;
import com.luckyframework.httpclient.proxy.interceptor.CookieManagerInterceptor;
import com.luckyframework.httpclient.proxy.interceptor.Interceptor;
import com.luckyframework.httpclient.proxy.interceptor.RedirectInterceptor;
import com.luckyframework.httpclient.proxy.logging.CustomMasker;
import com.luckyframework.httpclient.proxy.logging.LoggerHandler;
import com.luckyframework.httpclient.proxy.logging.MaskType;
import com.luckyframework.httpclient.proxy.logging.PrintLogAnnotationContextLoggerHandler;
import com.luckyframework.httpclient.proxy.plugin.PluginGenerate;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import com.luckyframework.httpclient.proxy.retry.ExceptionModel;
import com.luckyframework.httpclient.proxy.retry.RetryActuator;
import com.luckyframework.httpclient.proxy.retry.RetryDeciderContext;
import com.luckyframework.httpclient.proxy.retry.RunBeforeRetryContext;
import com.luckyframework.httpclient.proxy.slow.AbstractSlowResponseHandler;
import com.luckyframework.httpclient.proxy.slow.ResponseTimeSpent;
import com.luckyframework.httpclient.proxy.spel.ClassStaticElement;
import com.luckyframework.httpclient.proxy.spel.MethodSpaceConstant;
import com.luckyframework.httpclient.proxy.spel.SpELConvert;
import com.luckyframework.httpclient.proxy.spel.StaticMethodEntry;
import com.luckyframework.httpclient.proxy.spel.ValueSpaceConstant;
import com.luckyframework.httpclient.proxy.spel.WrapType;
import com.luckyframework.httpclient.proxy.typeparser.FluxMethodPackTypeParser;
import com.luckyframework.httpclient.proxy.typeparser.MonoMethodPackTypeParser;
import com.luckyframework.httpclient.proxy.typeparser.PackTypeParser;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpack;
import com.luckyframework.httpclient.proxy.unpack.ParameterConvert;
import com.luckyframework.httpclient.proxy.unpack.SpringMultipartFileParameterConvert;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.retry.BackoffWaitBeforeRetry;
import com.luckyframework.retry.TaskResult;
import com.luckyframework.spel.ParamWrapper;
import com.luckyframework.spel.SpELRuntime;
import com.luckyframework.threadpool.ThreadPoolFactory;
import com.luckyframework.threadpool.ThreadPoolParam;
import io.github.lucklike.httpclient.config.AutoConvertConfig;
import io.github.lucklike.httpclient.config.CookieManageConfiguration;
import io.github.lucklike.httpclient.config.CustomMaskerConfig;
import io.github.lucklike.httpclient.config.GenerateEntry;
import io.github.lucklike.httpclient.config.HttpAsyncThreadPoolConfiguration;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.config.HttpExecutorConfiguration;
import io.github.lucklike.httpclient.config.InterceptorGenerateEntry;
import io.github.lucklike.httpclient.config.Locator;
import io.github.lucklike.httpclient.config.LocatorAutoConvert;
import io.github.lucklike.httpclient.config.LocatorParameterConvert;
import io.github.lucklike.httpclient.config.LoggerConfiguration;
import io.github.lucklike.httpclient.config.LoggerMaskerConfig;
import io.github.lucklike.httpclient.config.ObjectCreatorFactory;
import io.github.lucklike.httpclient.config.ParameterConvertConfig;
import io.github.lucklike.httpclient.config.RType;
import io.github.lucklike.httpclient.config.RedirectConfiguration;
import io.github.lucklike.httpclient.config.ResponseConvertConfiguration;
import io.github.lucklike.httpclient.config.RetryConfiguration;
import io.github.lucklike.httpclient.config.SSLConfiguration;
import io.github.lucklike.httpclient.config.SimpleGenerateEntry;
import io.github.lucklike.httpclient.config.SlowResponseHandlerConfiguration;
import io.github.lucklike.httpclient.config.SpELConfiguration;
import io.github.lucklike.httpclient.config.SpELRuntimeFactory;
import io.github.lucklike.httpclient.config.impl.BeanSpELRuntimeFactoryFactory;
import io.github.lucklike.httpclient.config.impl.LazyThreadPoolParam;
import io.github.lucklike.httpclient.config.impl.SpecifiedInterfaceLoggerHandler;
import io.github.lucklike.httpclient.configapi.SpringEnvironmentConfigurationSource;
import io.github.lucklike.httpclient.convert.HttpExecutorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.InitBindParameterConvert;
import io.github.lucklike.httpclient.convert.ObjectCreatorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.SpELRuntimeFactoryInstanceConverter;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.function.SimpleHttpExecutorFunction;
import io.github.lucklike.httpclient.injection.WrapTypeHolder;
import io.github.lucklike.httpclient.masker.BindingKeyMasker;
import io.github.lucklike.httpclient.plugin.HttpPlugin;
import io.github.lucklike.httpclient.plugin.ValidationPluginProvider;
import io.github.lucklike.httpclient.retry.ConfigurationBackoffWaitingBeforeRetryContext;
import io.github.lucklike.httpclient.retry.ConfigurationRetryDeciderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.lucklike.httpclient.Constant.DEFAULT_HTTP_CLIENT_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_HTTP_CLIENT_V5_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_JDK_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_OKHTTP_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_VALIDATION_PLUGIN_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DESTROY_METHOD;
import static io.github.lucklike.httpclient.Constant.INIT_BIND_PARAMETER_CONVERT;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_CONFIG_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.SIMPLE_HTTP_EXECUTOR;
import static io.github.lucklike.httpclient.Constant.SPRING_ENV_CONFIG_SOURCE;
import static io.github.lucklike.httpclient.Constant.SPRING_FUNCTION_SPACE;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * <pre>
 * lucky-httpclient自动配置类，主要是为了生成{@link HttpClientProxyObjectFactory}对象实例
 * 并将实例放到Spring容器中
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:35
 */
@Configuration
@Role(ROLE_INFRASTRUCTURE)
public class LuckyHttpAutoConfiguration implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(LuckyHttpAutoConfiguration.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        ApplicationContextUtils.setApplicationContext(applicationContext);
    }

    @Bean(INIT_BIND_PARAMETER_CONVERT)
    @Role(ROLE_INFRASTRUCTURE)
    public ParameterConvert initBindParameterConvert() {
        return new InitBindParameterConvert();
    }

    /**
     * 转换器相关的配置
     */
    @Bean("conversionService")
    @Role(ROLE_INFRASTRUCTURE)
    public ConversionServiceFactoryBean conversionServiceFactoryBean() {
        ConversionServiceFactoryBean factoryBean = new ConversionServiceFactoryBean();
        factoryBean.setConverters(new HashSet<>(Arrays.asList(
                new SpELRuntimeFactoryInstanceConverter(),
                new ObjectCreatorFactoryInstanceConverter(),
                new HttpExecutorFactoryInstanceConverter()
        )));
        return factoryBean;
    }

    @Bean(SPRING_ENV_CONFIG_SOURCE)
    @Role(ROLE_INFRASTRUCTURE)
    public ConfigurationSource springEnvConfigSource() {
        return new SpringEnvironmentConfigurationSource();
    }

    /**
     * 从环境变量获取必要的配置
     */
    @Role(ROLE_INFRASTRUCTURE)
    @Bean(PROXY_FACTORY_CONFIG_BEAN_NAME)
    @ConfigurationProperties("lucky.http-client")
    public HttpClientProxyObjectFactoryConfiguration httpClientProxyObjectFactoryConfiguration() {
        return new HttpClientProxyObjectFactoryConfiguration();
    }

    /**
     * 生成并配置一个{@link HttpClientProxyObjectFactory}对象实例
     *
     * @param factoryConfig 配置实例
     */
    @Primary
    @Role(ROLE_INFRASTRUCTURE)
    @Bean(name = PROXY_FACTORY_BEAN_NAME, destroyMethod = DESTROY_METHOD)
    public HttpClientProxyObjectFactory luckyHttpClientProxyFactory(@Qualifier(PROXY_FACTORY_CONFIG_BEAN_NAME) HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        HttpClientProxyObjectFactory factory = new HttpClientProxyObjectFactory();
        registeredSpace(factoryConfig);
        registeredWapType(factoryConfig);
        registeredUniversalFunction(factory);
        registeredPackTypeParser(factory);
        objectCreateSetting(factory, factoryConfig);
        factorySpELConvertSetting(factory, factoryConfig);
        factoryExpressionParamSetting(factory, factoryConfig);
        asyncExecuteSetting(factory, factoryConfig);
        httpExecuteSetting(factory, factoryConfig);
        exceptionHandlerSetting(factory, factoryConfig);
        httpParamSetting(factory, factoryConfig);
        slowResponseHandlerSetting(factory, factoryConfig);
        loggerSetting(factory, factoryConfig);
        retryActuatorSetting(factory, factoryConfig);
        interceptorSetting(factory, factoryConfig);
        sslSetting(factory, factoryConfig);
        responseConvertSetting(factory, factoryConfig);
        pluginSetting(factory, factoryConfig);
        configApiSourceSetting();
        return factory;
    }

    /**
     * 注册命名空间
     *
     * @param factoryConfig 工厂配置
     */
    private void registeredSpace(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        // 注册Spring函数命名空间
        MethodSpaceConstant.addExternalSpace(SPRING_FUNCTION_SPACE);
        // 注册简单HTTP执行器函数命名空间
        MethodSpaceConstant.addExternalSpace(SIMPLE_HTTP_EXECUTOR);

        // 注册配置中的命名空间
        SpELConfiguration springEl = factoryConfig.getSpringEl();
        List<String> defaultNameSpaces = springEl.getDefaultNameSpaces();
        if (ContainerUtils.isNotEmptyCollection(defaultNameSpaces)) {
            for (String nameSpace : defaultNameSpaces) {
                MethodSpaceConstant.addExternalSpace(nameSpace);
                ValueSpaceConstant.addExternalSpace(nameSpace);
            }
        }
    }

    /**
     * 注册 WapType
     *
     * @param factoryConfig 工厂配置
     */
    private void registeredWapType(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        ObjectProvider<WrapTypeHolder> beanProvider = applicationContext.getBeanProvider(WrapTypeHolder.class);
        beanProvider.stream().forEach(wth -> WrapType.registerWrapType(wth.getBaseType(), wth.wrapFunction()));
    }

    /**
     * 注册通用函数
     *
     * @param factory 工厂实例
     */
    private void registeredUniversalFunction(HttpClientProxyObjectFactory factory) {
        factory.addSpringElFunctionClass(BeanFunction.class);
        factory.addSpringElFunctionClass(SimpleHttpExecutorFunction.class);
    }


    /**
     * 注册包装类型解析器
     *
     * @param factory 工厂实例
     */
    private void registeredPackTypeParser(HttpClientProxyObjectFactory factory) {
        applicationContext.getBeanProvider(PackTypeParser.class).forEach(factory::addPackTypeParser);
    }

    /**
     * 设置{@link SpELConvert SPEL表达式转换器}，首先尝试从配置中读取用户配置的{@link SpELRuntimeFactory},
     * 如果存在则采用该工厂创建，否则使用默认实例
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void factorySpELConvertSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        SpELConfiguration springEl = factoryConfig.getSpringEl();

        // 使用工厂构建一个SpELRuntime对象
        SpELRuntimeFactory spELRuntimeFactory = springEl.getRuntimeFactory();
        spELRuntimeFactory = spELRuntimeFactory == null ? new BeanSpELRuntimeFactoryFactory() : spELRuntimeFactory;
        SpELRuntime spELRuntime = spELRuntimeFactory.getSpELRuntime();

        // 获取嵌套表达式的前缀和后缀
        String prefix = springEl.getNestExpressionPrefix();
        String suffix = springEl.getNestExpressionSuffix();

        // 使用SpELRuntime对象构建一个SpELConvert对象
        SpELConvert spELConvert = new SpringSpELConvert(spELRuntime, applicationContext.getEnvironment(), prefix, suffix);
        factory.setSpELConverter(spELConvert);
    }

    /**
     * 设置工厂SpEL表达式配置参数
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void factoryExpressionParamSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {

        SpELConfiguration springElConfig = factoryConfig.getSpringEl();

        // 设置类型限制模型和比较算法
        factory.setTypeRestrictionModel(springElConfig.getTypeRestrictedModel());
        factory.setTypeRestrictionCompare(springElConfig.getTypeRestrictedCompare());

        // 导入SpEL依赖包
        List<String> springElPackageImports = springElConfig.getImportPackages();
        if (ContainerUtils.isNotEmptyCollection(springElPackageImports)) {
            springElPackageImports.forEach(factory::importPackage);
        }

        // 导入类型别名配置
        Map<String, Class<?>> typeAlias = springElConfig.getTypeAlias();
        if (ContainerUtils.isNotEmptyMap(typeAlias)) {
            typeAlias.forEach(factory::addTypeAlias);
        }

        // 设置类型白名单
        List<Class<?>> typeWhiteList = springElConfig.getTypeWhiteList();
        if (ContainerUtils.isNotEmptyCollection(typeWhiteList)) {
            typeWhiteList.forEach(factory::addTypeWhiteList);
        }

        // 设置类型黑名单
        List<Class<?>> typeBlackList = springElConfig.getTypeBlackList();
        if (ContainerUtils.isNotEmptyCollection(typeBlackList)) {
            typeBlackList.forEach(factory::addTypeBlackList);
        }

        // 注册SpELRoot变量
        ConfigurationMap springElRootVariables = springElConfig.getRootVariables();
        if (ContainerUtils.isNotEmptyMap(springElRootVariables)) {
            factory.addSpringElRootVariables(springElRootVariables);
        }

        // 注册SpEL普通变量
        ConfigurationMap springElVariables = springElConfig.getVariables();
        if (ContainerUtils.isNotEmptyMap(springElVariables)) {
            factory.addSpringElVariables(springElVariables);
        }

        // SpEL函数自动扫描与注册
        if (ContainerUtils.isNotEmptyCollection(springElConfig.getFunctionPackages())) {
            final String SPEL_FUNCTION_ANN = springElConfig.getFunctionAnnotation().getName();
            String[] packages = ScanUtils.getPackages(ContainerUtils.setToArray(springElConfig.getFunctionPackages(), String.class));
            ScanUtils.resourceHandle(packages, resource -> {
                AnnotationMetadata annotationMetadata = ScanUtils.resourceToAnnotationMetadata(resource);
                if (annotationMetadata.isAnnotated(SPEL_FUNCTION_ANN) && !annotationMetadata.isAnnotation()) {
                    factory.addSpringElFunctionClass(ClassUtils.getClass(annotationMetadata.getClassName()));
                    if (log.isDebugEnabled()) {
                        log.debug("@SpELFunction '{}' is registered", annotationMetadata.getClassName());
                    }
                }
            });
        }

        // 注册配置文件中的SpEL函数
        ClassStaticElement[] springElFunctionClasses = springElConfig.getFunctionClasses();
        if (ContainerUtils.isNotEmptyArray(springElFunctionClasses)) {
            for (ClassStaticElement springElFunctionClass : springElFunctionClasses) {
                factory.addSpringElFunctionClass(springElFunctionClass.getNamespace(), springElFunctionClass.getClazz());
            }
        }

        StaticMethodEntry[] springElFunctions = springElConfig.getFunctions();
        if (ContainerUtils.isNotEmptyArray(springElFunctions)) {
            for (StaticMethodEntry springElFunction : springElFunctions) {
                factory.addSpringElFunction(springElFunction);
            }
        }
    }


    /**
     * 设置{@link HttpExecutor HTTP请求执行器}：
     * <pre>
     *  1.如果配置了httpExecutorFactory，则使用httpExecutorFactory来创建执行器
     *  2.如果配置了 httpExecutorBean，则直接使用该Bean名称对应的执行器
     *  3.如果配置了HttpExecutorEnum，则使用枚举中指定的httpExecutorBean来创建执行器
     *  4.在Spring容器中按类型查找，将找到的Bean设置为执行器(优先级：HttpClient > HttpClient5 > OkHttp > URLConnection)
     * </pre>
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void httpExecuteSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {

        HttpExecutorConfiguration httpExecutorConfig = factoryConfig.getHttpExecutor();

        // 全局Http执行器设置
        HttpExecutorConfiguration.GlobalConfiguration globalConfig = httpExecutorConfig.getGlobal();
        if (globalConfig.getExecutorFactory() != null) {
            factory.setHttpExecutor(globalConfig.getExecutorFactory().getHttpExecutor());
        } else if (StringUtils.hasText(globalConfig.getExecutorBean())) {
            factory.setHttpExecutor(applicationContext.getBean(globalConfig.getExecutorBean(), HttpExecutor.class));
        } else if (globalConfig.getExecutor() != null) {
            factory.setHttpExecutor(applicationContext.getBean(globalConfig.getExecutor().getHttpExecutorBean(), HttpExecutor.class));
        } else {
            factory.setHttpExecutor(applicationContext.getBeanProvider(HttpExecutor.class).stream().findFirst().get());
        }

        // 导入Spring容器中配置的HttpExecutor
        String[] executorBeanNames = applicationContext.getBeanNamesForType(HttpExecutor.class);
        if (ContainerUtils.isNotEmptyArray(executorBeanNames)) {
            for (String executorBeanName : executorBeanNames) {
                factory.addAlternativeHttpExecutor(executorBeanName, () -> applicationContext.getBean(HttpExecutor.class));
            }
        }

        // 导入备用Http执行器设置
        Map<String, HttpExecutorConfiguration.AlternativeConfiguration> alternativeConfig = httpExecutorConfig.getAlternative();
        if (ContainerUtils.isNotEmptyMap(alternativeConfig)) {
            alternativeConfig.forEach((k, v) -> {
                if (v.isLazy()) {
                    factory.addAlternativeHttpExecutor(k, v::createExecutor);
                } else {
                    factory.addAlternativeHttpExecutor(k, v.createExecutor());
                }
            });
        }
    }

    /**
     * 设置{@link ObjectCreator 对象创建器}，首先尝试从配置中读取用户配置的{@link ObjectCreatorFactory},
     * 如果存在则采用该工厂创建，否则使用默认对象
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void objectCreateSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        ObjectCreatorFactory objectCreatorFactory = factoryConfig.getObjectCreatorFactory();
        if (objectCreatorFactory == null) {
            factory.setObjectCreator(new BeanObjectCreator(applicationContext));
        } else {
            factory.setObjectCreator(objectCreatorFactory.getObjectCreator());
        }
    }

    /**
     * 设置用于异步执行Http请求的线程池，首先尝试从配置中读取用户配置的{@link ThreadPoolParam},
     * 如果存在则采用该线程池参数来创建线程池，否则使用默认线程池
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void asyncExecuteSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {

        // 获取异步线程池相关的配置
        HttpAsyncThreadPoolConfiguration asyncThreadPoolConfig = factoryConfig.getThreadPool();

        // 设置异步模型
        Model asyncModel = asyncThreadPoolConfig.getAsyncModel();
        if (asyncModel != null) {
            factory.setAsyncModel(asyncModel);
        }

        // 设置默认执行器的并发数
        factory.setDefaultExecutorConcurrency(asyncThreadPoolConfig.getDefaultExecutorConcurrency());

        // 导入Spring容器中配置的Executor
        String[] executorBeanNames = applicationContext.getBeanNamesForType(Executor.class);
        if (ContainerUtils.isNotEmptyArray(executorBeanNames)) {
            for (String executorBeanName : executorBeanNames) {
                factory.addAlternativeAsyncExecutor(executorBeanName, () -> applicationContext.getBean(Executor.class));
            }
        }

        // 导入用户配置的默认Executor
        LazyThreadPoolParam defaultPoolParam = asyncThreadPoolConfig.getGlobal();
        if (defaultPoolParam != null) {
            if (defaultPoolParam.isLazy()) {
                factory.setAsyncExecutor(() -> ThreadPoolFactory.createThreadPool(defaultPoolParam));
            } else {
                factory.setAsyncExecutor(ThreadPoolFactory.createThreadPool(defaultPoolParam));
            }
        }

        // 导入用户配置的备选Executor
        Map<String, LazyThreadPoolParam> alternativePoolParamMap = asyncThreadPoolConfig.getAlternative();
        if (ContainerUtils.isNotEmptyMap(alternativePoolParamMap)) {
            alternativePoolParamMap.forEach((name, poolParam) -> {
                if (poolParam.isLazy()) {
                    factory.addAlternativeAsyncExecutor(name, () -> ThreadPoolFactory.createThreadPool(poolParam));
                } else {
                    factory.addAlternativeAsyncExecutor(name, ThreadPoolFactory.createThreadPool(poolParam));
                }
            });
        }
    }

    /**
     * 设置公用的{@link HttpExceptionHandle 异常处理器}
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    @SuppressWarnings("unchecked")
    private void exceptionHandlerSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        GenerateEntry<HttpExceptionHandle> generate = factoryConfig.getExceptionHandleGenerate();
        if (generate != null) {
            factory.setExceptionHandle(generate.getType(), generate.getBeanName(), generate.getScope(), (Consumer<HttpExceptionHandle>) ClassUtils.newObject(generate.getConsumerClass()));
        }
    }

    /**
     * 慢响应处理相关配置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void slowResponseHandlerSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        SlowResponseHandlerConfiguration slowResponseConfig = factoryConfig.getSlowResponseConfig();
        if (slowResponseConfig != null && slowResponseConfig.getSlowTime() > 0 ) {
            AbstractSlowResponseHandler slowResponseHandler;
            if (slowResponseConfig.getHandler() != null){
                SimpleGenerateEntry<? extends AbstractSlowResponseHandler> handler = slowResponseConfig.getHandler();
                slowResponseHandler = createObject("lucky.http-client.slow-response-config.handler", handler);
            } else {
                slowResponseHandler = new AbstractSlowResponseHandler() {
                    @Override
                    public void handleSlowResponse(MethodContext context, Response response, ResponseTimeSpent responseTimeSpent) {

                    }
                };
            }
            slowResponseHandler.setSlowTime(slowResponseConfig.getSlowTime());
            factory.setSlowResponseHandler(slowResponseHandler);
        }
    }

    /**
     * 日志处理器设置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void loggerSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        // 获取日志处理相关的配置
        LoggerConfiguration loggerConfig = factoryConfig.getLogger();


        // 功能未开启或者为配置日志打印的包时直接结束
        if (!loggerConfig.isEnable() || ContainerUtils.isEmptyCollection(loggerConfig.getPackages())) {
            return;
        }

        // 获取日志处理器实现类
        LoggerHandler loggerHandler = null;
        SimpleGenerateEntry<LoggerHandler> logHandlerEntry = loggerConfig.getHandlerClass();
        if (logHandlerEntry != null) {
            loggerHandler = createObject("lucky.http-client.logger.handler-class", logHandlerEntry);
        }
        if (loggerHandler == null) {
            loggerHandler = loggerConfig.getType().getLoggerHandler();
        }

        // PrintLogAnnotationContextLoggerHandler类型的日志处理器需要另外设置参数
        if (loggerHandler instanceof PrintLogAnnotationContextLoggerHandler) {
            PrintLogAnnotationContextLoggerHandler plaLoggerHandler = (PrintLogAnnotationContextLoggerHandler) loggerHandler;
            plaLoggerHandler.setReqCondition(loggerConfig.getReqLogCondition());
            plaLoggerHandler.setLogErrorWithDetails(loggerConfig.isLogErrorWithDetails());
            plaLoggerHandler.setRespCondition(loggerConfig.getRespLogCondition());
            plaLoggerHandler.setPrintRespHeader(loggerConfig.getEnableRespHeaderLog());
            Set<String> allowPrintLogBodyMimeTypes = loggerConfig.getSetAllowMimeTypes();
            if (ContainerUtils.isNotEmptyCollection(allowPrintLogBodyMimeTypes)) {
                plaLoggerHandler.setAllowPrintLogBodyMimeTypes(allowPrintLogBodyMimeTypes);
            }
            Set<String> addAllowPrintLogBodyMimeTypes = loggerConfig.getAddAllowMimeTypes();
            if (ContainerUtils.isNotEmptyCollection(addAllowPrintLogBodyMimeTypes)) {
                plaLoggerHandler.addAllowPrintLogBodyMimeTypes(addAllowPrintLogBodyMimeTypes);
            }
            plaLoggerHandler.setAllowPrintLogReqBodyMaxLength(loggerConfig.getReqBodyMaxLength());
            plaLoggerHandler.setAllowPrintLogRespBodyMaxLength(loggerConfig.getRespBodyMaxLength());

            // 日志脱敏相关配置
            LoggerMaskerConfig maskers = loggerConfig.getMaskers();
            plaLoggerHandler.setEnableRequestMask(String.valueOf(maskers.isMaskRequest()));
            plaLoggerHandler.setEnableResponseMask(String.valueOf(maskers.isMaskResponse()));

            Map<CustomMasker, Set<String>> maskerSetMap = new LinkedHashMap<>();

            // 注册 Spring 容器中的脱敏处理器
            applicationContext.getBeanProvider(BindingKeyMasker.class).forEach(bkm -> maskerSetMap.put(bkm, bkm.getKeys()));

            // 注册预定义的脱敏处理器
            Map<MaskType, Set<String>> generalConfig = maskers.getPredefined();
            if (ContainerUtils.isNotEmptyMap(generalConfig)) {
                maskerSetMap.putAll(generalConfig);
            }

            // 注册自定扩展的脱敏处理器
            List<CustomMaskerConfig> customConfig = maskers.getExtended();
            if (ContainerUtils.isNotEmptyCollection(customConfig)) {
                for (CustomMaskerConfig maskerConfig : customConfig) {
                    CustomMasker customMasker;
                    String beanName = maskerConfig.getBean();
                    if (StringUtils.hasText(beanName)) {
                        customMasker = applicationContext.getBean(beanName, CustomMasker.class);
                    } else {
                        customMasker = ClassUtils.newObject(maskerConfig.getClazz());
                    }
                    maskerSetMap.put(customMasker, maskerConfig.getKeys());
                }
            }
            plaLoggerHandler.addCommonMaskers(maskerSetMap);
        }

        SpecifiedInterfaceLoggerHandler specifiedInterfaceLoggerHandler = new SpecifiedInterfaceLoggerHandler(loggerHandler);
        specifiedInterfaceLoggerHandler.setPrintLogPackageSet(loggerConfig.getPackages());
        specifiedInterfaceLoggerHandler.setPrintRequestLog(loggerConfig.isEnableReqLog());
        specifiedInterfaceLoggerHandler.setPrintResponseLog(loggerConfig.isEnableRespLog());
        factory.setLoggerHandler(specifiedInterfaceLoggerHandler);
    }

    /**
     * 重试执行器设置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void retryActuatorSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        RetryConfiguration retryConfig = factoryConfig.getRetry();
        if (!retryConfig.isEnable() || retryConfig.getCount() < 0) {
            return;
        }

        RetryActuator retryActuator = new RetryActuator(
                retryConfig.getTaskNameFormat(),
                retryConfig.getCount(),
                c -> new ConfigurationBackoffWaitingBeforeRetryContext(retryConfig),
                c -> new ConfigurationRetryDeciderContext(retryConfig),
                retryConfig.isStrictModel(),
                null
        );

        factory.setRetryActuator(retryActuator);
    }

    /**
     * 拦截器设置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    @SuppressWarnings("unchecked")
    private void interceptorSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        RedirectConfiguration redirectConfig = factoryConfig.getRedirect();

        // 检查是否需要注册支持自动重定向功能的拦截器
        if (redirectConfig.isEnable()) {
            factory.addInterceptor(RedirectInterceptor.class, Scope.METHOD, interceptor -> {
                if (ContainerUtils.isNotEmptyArray(redirectConfig.getStatus())) {
                    interceptor.setRedirectStatus(redirectConfig.getStatus());
                }
                if (StringUtils.hasText(redirectConfig.getCondition())) {
                    interceptor.setRedirectCondition(redirectConfig.getCondition());
                }
                if (StringUtils.hasText(redirectConfig.getLocation())) {
                    interceptor.setRedirectLocationExp(redirectConfig.getLocation());
                }
                interceptor.setMaxRedirectCount(redirectConfig.getMaxCount());
            }, redirectConfig.getPriority());
        }

        // 检查是否开启了Cookie管理功能，开启则注入相关的拦截器
        CookieManageConfiguration cookieManageConfig = factoryConfig.getCookieManage();
        if (cookieManageConfig.isEnable()) {
            SimpleGenerateEntry<CookieStore> cookieStoreGenerate = cookieManageConfig.getCookieStore();
            Integer priority = cookieManageConfig.getPriority();
            if (cookieStoreGenerate != null) {
                factory.addInterceptor(CookieManagerInterceptor.class, Scope.SINGLETON, cmi -> cmi.setCookieStore(createObject("lucky.http-client.cookie-manage.cookie-store",cookieStoreGenerate)), priority);
            } else {
                factory.addInterceptor(CookieManagerInterceptor.class, Scope.SINGLETON, priority);
            }
        }

        InterceptorGenerateEntry[] interceptorGenerates = factoryConfig.getInterceptorGenerates();
        if (ContainerUtils.isNotEmptyArray(interceptorGenerates)) {
            for (InterceptorGenerateEntry interceptorGenerate : interceptorGenerates) {

                factory.addInterceptor(
                        interceptorGenerate.getType(),
                        interceptorGenerate.getBeanName(),
                        interceptorGenerate.getScope(),
                        (Consumer<Interceptor>) ClassUtils.newObject(interceptorGenerate.getConsumerClass()),
                        interceptorGenerate.getPriority()
                );
            }
        }
    }

    /**
     * SSL证书相关的配置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void sslSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        SSLConfiguration sslConfig = factoryConfig.getSsl();

        // 注册SSLContext
        Map<String, KeyStoreInfo> keyStoreMap = sslConfig.getKeyStores();
        if (ContainerUtils.isNotEmptyMap(keyStoreMap)) {
            keyStoreMap.forEach(factory::addKeyStoreInfo);
        }

        // 开启全局SSL配置
        if (Objects.equals(Boolean.TRUE, sslConfig.getGlobalEnable())) {

            // HostnameVerifier
            HostnameVerifier hostnameVerifier = TrustAllHostnameVerifier.DEFAULT_INSTANCE;
            SimpleGenerateEntry<HostnameVerifierFactory> hvbFactory = sslConfig.getHostnameVerifier();
            if (hvbFactory != null) {
                hostnameVerifier = createObject("lucky.http-client.ssl.hostname-verifier", hvbFactory).getHostnameVerifier();
            } else if (StringUtils.hasText(sslConfig.getHostnameVerifierExpression())) {
                hostnameVerifier = factory.getSpELConverter().parseExpression(new ParamWrapper(sslConfig.getHostnameVerifierExpression()).setExpectedResultType(HostnameVerifier.class));
            }
            factory.setHostnameVerifier(hostnameVerifier);

            // SSLSocketFactory
            SimpleGenerateEntry<SSLSocketFactoryFactory> sslFactoryConfig = sslConfig.getSslSocketFactory();
            if (sslFactoryConfig != null) {
                factory.setSslSocketFactory(createObject("lucky.http-client.ssl.ssl-socket-factory", sslFactoryConfig).getSSLSocketFactory());
            } else if (StringUtils.hasText(sslConfig.getSslSocketFactoryExpression())) {
                factory.setSslSocketFactory(factory.getSpELConverter().parseExpression(new ParamWrapper(sslConfig.getSslSocketFactoryExpression()).setExpectedResultType(SSLSocketFactory.class)));
            } else {
                KeyStoreInfo keyStoreInfo = null;
                KeyStoreInfo trustStoreInfo = null;

                String keyStoreId = sslConfig.getGlobalKeyStore();
                String trustStoreId = sslConfig.getGlobalTrustStore();
                if (StringUtils.hasText(keyStoreId)) {
                    keyStoreInfo = factory.getKeyStoreInfo(keyStoreId);
                    if (keyStoreInfo == null) {
                        throw new SSLException("Not found in the HttpClientProxyObjectFactory KeyStoreInfo object called {}", keyStoreId);
                    }
                }

                if (StringUtils.hasText(trustStoreId)) {
                    trustStoreInfo = factory.getKeyStoreInfo(trustStoreId);
                    if (trustStoreInfo == null) {
                        throw new SSLException("Not found in the HttpClientProxyObjectFactory KeyStoreInfo object called {}", keyStoreId);
                    }
                }

                factory.setSslSocketFactory(SSLUtils.createSSLContext(sslConfig.getGlobalProtocol(), keyStoreInfo, trustStoreInfo).getSslContext().getSocketFactory());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parameterConvertSetting(HttpClientProxyObjectFactoryConfiguration factoryConfig) {

        // 收集Spring容器中的ParameterConvert
        String[] beanNamesForType = applicationContext.getBeanNamesForType(ParameterConvert.class);

        List<Locator<ParameterConvert>> locators = new ArrayList<>();
        List<ParameterConvert> parameterConverts = new ArrayList<>();

        for (String parameterConvertName : beanNamesForType) {
            ParameterConvert parameterConvert = applicationContext.getBean(parameterConvertName, ParameterConvert.class);
            if (parameterConvert instanceof Locator) {
                locators.add((Locator<ParameterConvert>) parameterConvert);
            } else {
                parameterConverts.add(parameterConvert);
            }
        }

        // 注册配置文件中配置的ParameterConvert
        ParameterConvertConfig[] parameterConvertConfigs = factoryConfig.getParameterConverts();
        if (ContainerUtils.isNotEmptyArray(parameterConvertConfigs)) {
            for (ParameterConvertConfig config : parameterConvertConfigs) {
                Class<? extends ParameterConvert> clazz = config.getClazz();
                ParameterConvert parameterConvert = ClassUtils.newObject(clazz);
                locators.add(LocatorParameterConvert.of(parameterConvert, config.getType(), config.getIndex(), config.getIndexClass()));
            }
        }


        parameterConverts.forEach(ContextValueUnpack::addParameterConvert);
        locators.forEach(this::addLocatorParameterConvert);

    }

    /**
     * 设置响应结果自动转换器
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void responseConvertSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        autoConvertSetting(factoryConfig);
        contentEncodingConvertorSetting(factory, factoryConfig);
    }

    /**
     * 注册Response.AutoConvert
     *
     * @param factoryConfig 工厂配置
     */
    @SuppressWarnings("unchecked")
    private void autoConvertSetting(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        // 收集Spring容器中的Response.AutoConvert
        String[] autoConvertNames = applicationContext.getBeanNamesForType(Response.AutoConvert.class);

        List<Locator<Response.AutoConvert>> locators = new ArrayList<>();
        List<Response.AutoConvert> autoConverts = new ArrayList<>();

        for (String autoConvertName : autoConvertNames) {
            Response.AutoConvert autoConvert = applicationContext.getBean(autoConvertName, Response.AutoConvert.class);
            if (autoConvert instanceof Locator) {
                locators.add((Locator<Response.AutoConvert>) autoConvert);
            } else {
                autoConverts.add(autoConvert);
            }
        }

        // 注册配置文件中配置的Response.AutoConvert
        ResponseConvertConfiguration responseConvertConfig = factoryConfig.getResponseConvert();
        AutoConvertConfig[] responseAutoConverts = responseConvertConfig.getAutoConverts();
        if (ContainerUtils.isNotEmptyArray(responseAutoConverts)) {
            for (AutoConvertConfig config : responseAutoConverts) {
                Class<? extends Response.AutoConvert> clazz = config.getClazz();
                Response.AutoConvert autoConvert = ClassUtils.newObject(clazz);
                locators.add(LocatorAutoConvert.of(autoConvert, config.getType(), config.getIndex(), config.getIndexClass()));
            }
        }

        autoConverts.forEach(Response::addAutoConvert);
        locators.forEach(this::addLocatorAutoConvert);

    }

    private void addLocatorParameterConvert(Locator<ParameterConvert> locator) {
        addLocatorObject(
                locator,
                ContextValueUnpack::addParameterConvert,
                ContextValueUnpack::addParameterConvert,
                ContextValueUnpack::setParameterConvert,
                cz -> ContextValueUnpack.getParameterConvertIndex(cz)
        );
    }

    private void addLocatorAutoConvert(Locator<Response.AutoConvert> locator) {
        addLocatorObject(
                locator,
                Response::addAutoConvert,
                Response::addAutoConvert,
                Response::setAutoConvert,
                cz -> Response.getAutoConvertIndex(cz)
        );
    }

    @SuppressWarnings("unchecked")
    private <T> void addLocatorObject(Locator<T> locator,
                                      Consumer<T> addLast,
                                      BiConsumer<Integer, T> addIndex,
                                      BiConsumer<Integer, T> set,
                                      Function<Class<? extends T>, Integer> indexFun) {

        Integer index = locator.index();
        Class<? extends T> indexClass = locator.indexClass();
        RType rType = locator.rType();
        T element = (T) locator;

        if (index == null && indexClass == null) {
            addLast.accept(element);
            return;
        }

        int _index;
        if (index != null) {
            _index = index;
        } else {
            _index = indexFun.apply(indexClass);
            if (_index == -1) {
                throw new LuckyRuntimeException("The element of type '{}' does not exist", indexClass.getName());
            }
        }

        switch (rType) {
            case ADD:
                addIndex.accept(_index, element);
                break;
            case COVER:
                set.accept(_index, element);
                break;
        }
    }

    /**
     * 注册ContentEncodingConvertor
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void contentEncodingConvertorSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {

        // 注册Spring容器中的ContentEncodingConvertor
        applicationContext.getBeanProvider(ContentEncodingConvertor.class)
                .forEach(AbstractSaveResultResponseProcessor::addContentEncodingConvertor);

        ResponseConvertConfiguration responseConvertConfig = factoryConfig.getResponseConvert();

        // 注册配置文件中配置的ContentEncodingConvertor
        Class<? extends ContentEncodingConvertor>[] contentEncodingDecoders = responseConvertConfig.getContentEncodingDecoder();
        if (ContainerUtils.isNotEmptyArray(contentEncodingDecoders)) {
            Stream.of(contentEncodingDecoders).forEach(cedClass -> AbstractSaveResultResponseProcessor.addContentEncodingConvertor(ClassUtils.newObject(cedClass)));
        }

        // 根据ContentEncodingConvertor解码器自动生成Accept-Encoding
        if (responseConvertConfig.isEnableContentCompress()) {
            String acceptEncoding;
            String encodeConfig = responseConvertConfig.getAcceptEncoding();
            if (StringUtils.hasText(encodeConfig)) {
                acceptEncoding = encodeConfig;
            } else {
                final StringBuilder sb = new StringBuilder();
                AbstractSaveResultResponseProcessor.getContentEncodingConvertors().forEach(cec -> sb.append(cec.contentEncoding()).append(", "));
                acceptEncoding = sb.substring(0, sb.length() - 2);
            }
            factory.addHeader("Accept-Encoding", acceptEncoding);
        }
    }

    /**
     * 设置插件相关的配置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void pluginSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        // 注册Spring容器中的插件
        applicationContext.getBeanProvider(ProxyPlugin.class).forEach(factory::addPlugin);

        // 注册Spring容器中由@HttpPlugin注解声明的插件
        String[] annPluginBeanNames = applicationContext.getBeanNamesForAnnotation(HttpPlugin.class);
        for (String pluginBeanName : annPluginBeanNames) {
            Object bean = applicationContext.getBean(pluginBeanName);
            PluginGenerate generate = new PluginGenerate(bean);
            generate.generate().forEach(factory::addPlugin);
        }

        // 注册环境变量中配置的插件
        Class<? extends ProxyPlugin>[] pluginClasses = factoryConfig.getPlugins();
        if (ContainerUtils.isNotEmptyArray(pluginClasses)) {
            for (Class<? extends ProxyPlugin> pluginClass : pluginClasses) {
                factory.addPlugin(pluginClass);
            }
        }
    }

    /**
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void httpParamSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {

        //设置自动URL推导相关的配置
        factory.setEnableAutoUrlDerivation(factoryConfig.getEnableAutoUrlDerivation());
        factory.setAutoDerivationDefMethod(factoryConfig.getAutoDerivationDefMethod());

        // 请求参数设置
        factory.setHeaders(factoryConfig.getHeaderParams());
        factory.setPathParameters(factoryConfig.getPathParams());
        factory.setQueryParameters(factoryConfig.getQueryParams());
        parameterConvertSetting(factoryConfig);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T> T createObject(String configDesc, SimpleGenerateEntry<T> generateEntry) {
        if (generateEntry.hasBeanName()) {
            return (T) applicationContext.getBean(generateEntry.getBeanName());
        } else if (generateEntry.hasType()) {
            return applicationContext.getBeanProvider(generateEntry.getType()).stream().findFirst().orElse(ClassUtils.newObject(generateEntry.getType()));
        }
        throw new LuckyRuntimeException("{} Failed to create an object instance. No configuration item [bean-name, type] was provided.", FontUtil.getRedStr("[configKey: '") + FontUtil.getRedUnderline(configDesc) + FontUtil.getRedStr("']"));
    }


    private void configApiSourceSetting() {
        String[] beanNames = applicationContext.getBeanNamesForType(ConfigurationSource.class);
        for (String beanName : beanNames) {
            ConfigurationApiFunctionalSupport.addConfigSource(beanName, applicationContext.getBean(beanName, ConfigurationSource.class));
        }
    }


    //--------------------------------------------------------------------------------------------
    //                              Conditional Beans
    //--------------------------------------------------------------------------------------------

    /******************************** PackTypeParser *************************************/

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"reactor.core.publisher.Flux"})
    static class ReactorPackTypeParserConfig {

        @Bean
        @Role(ROLE_INFRASTRUCTURE)
        public PackTypeParser reactorFluxMethodPackTypeParser() {
            return new FluxMethodPackTypeParser();
        }

        @Bean
        @Role(ROLE_INFRASTRUCTURE)
        public PackTypeParser reactorMonoMethodPackTypeParser() {
            return new MonoMethodPackTypeParser();
        }

    }

    /********************** ContentEncodingConvertor *************************************/


    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"org.brotli.dec.BrotliInputStream"})
    static class BrotliContentEncodingConvertorConfig {

        @Bean
        @Role(ROLE_INFRASTRUCTURE)
        public ContentEncodingConvertor brotliContentEncodingConvertor() {
            return new BrotliContentEncodingConvertor();
        }
    }

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"com.github.luben.zstd.Zstd"})
    static class ZstdContentEncodingConvertorConfig {

        @Bean
        @Role(ROLE_INFRASTRUCTURE)
        public ContentEncodingConvertor zstdContentEncodingConvertor() {
            return new ZstdContentEncodingConvertor();
        }
    }

    /********************** Response.AutoConvert *************************************/

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"com.google.protobuf.Parser"})
    static class ProtobufAutoConvertConfig {

        @Bean
        @Role(ROLE_INFRASTRUCTURE)
        public Response.AutoConvert protobufAutoConvert() {
            return new ProtobufAutoConvert();
        }
    }

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"org.springframework.web.multipart.MultipartFile"})
    static class SpringMultipartFileAutoConvertConfig {
        @Bean
        @Role(ROLE_INFRASTRUCTURE)
        public Response.AutoConvert springMultipartFileAutoConvert() {
            HttpClientProxyObjectFactory.addNotAutoCloseResourceTypes(ClassUtils.getClass("org.springframework.web.multipart.MultipartFile"));
            return new SpringMultipartFileAutoConvert();
        }

        @Bean
        @Role(ROLE_INFRASTRUCTURE)
        public ParameterConvert springMultipartFileParameterConvert() {
            return new SpringMultipartFileParameterConvert();
        }
    }


    /********************** HttpExecutor *************************************/

    @Role(ROLE_INFRASTRUCTURE)
    static class JdkHttpExecutorConfig {

        @Order(4)
        @Bean(DEFAULT_JDK_EXECUTOR_BEAN_NAME)
        @Role(ROLE_INFRASTRUCTURE)
        public HttpExecutor luckyJdkHttpExecutor(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
            return HttpExecutorConfiguration.createJdkHttpExecutor(factoryConfig.getHttpExecutor().getGlobal());
        }

    }

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"okhttp3.OkHttpClient"})
    static class OkHttpExecutorConfig {

        @Order(3)
        @Bean(DEFAULT_OKHTTP_EXECUTOR_BEAN_NAME)
        @Role(ROLE_INFRASTRUCTURE)
        public HttpExecutor luckyOkHttp3Executor(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
            return HttpExecutorConfiguration.createOkHttpExecutor(factoryConfig.getHttpExecutor().getGlobal());
        }

    }

    @Order(2)
    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"org.apache.hc.client5.http.classic.HttpClient"})
    static class ApacheHttpV5ExecutorConfig {

        @Bean(DEFAULT_HTTP_CLIENT_V5_EXECUTOR_BEAN_NAME)
        @Role(ROLE_INFRASTRUCTURE)
        public HttpExecutor luckyApacheHttpExecutor(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
            return HttpExecutorConfiguration.createHttpClient5Executor(factoryConfig.getHttpExecutor().getGlobal());
        }

    }

    @Order(1)
    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"org.apache.http.client.HttpClient"})
    static class ApacheHttpExecutorConfig {

        @Bean(DEFAULT_HTTP_CLIENT_EXECUTOR_BEAN_NAME)
        @Role(ROLE_INFRASTRUCTURE)
        public HttpExecutor luckyApacheHttpExecutor(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
            return HttpExecutorConfiguration.createHttpClientExecutor(factoryConfig.getHttpExecutor().getGlobal());
        }
    }

    /********************** Validation *************************************/

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"javax.validation.Validator"})
    static class ValidationAutoConfig {

        @Role(ROLE_INFRASTRUCTURE)
        @Bean(DEFAULT_VALIDATION_PLUGIN_BEAN_NAME)
        public ValidationPlugin validationPlugin(@Autowired(required = false) Validator validator) {
            if (validator == null) {
                return new ValidationPluginProvider(new ValidationPlugin());
            }
            return new ValidationPluginProvider(new ValidationPlugin(validator));
        }

    }
}
