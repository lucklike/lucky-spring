package io.github.lucklike.httpclient;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.ScanUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.convert.ProtobufAutoConvert;
import com.luckyframework.httpclient.core.convert.SpringMultipartFileAutoConvert;
import com.luckyframework.httpclient.core.encoder.BrotliContentEncodingConvertor;
import com.luckyframework.httpclient.core.encoder.ContentEncodingConvertor;
import com.luckyframework.httpclient.core.encoder.ZstdContentEncodingConvertor;
import com.luckyframework.httpclient.core.executor.HttpClientExecutor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.meta.CookieStore;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.core.processor.AbstractSaveResultResponseProcessor;
import com.luckyframework.httpclient.core.ssl.HostnameVerifierFactory;
import com.luckyframework.httpclient.core.ssl.KeyStoreInfo;
import com.luckyframework.httpclient.core.ssl.SSLException;
import com.luckyframework.httpclient.core.ssl.SSLSocketFactoryFactory;
import com.luckyframework.httpclient.core.ssl.SSLUtils;
import com.luckyframework.httpclient.core.ssl.TrustAllHostnameVerifier;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationApiFunctionalSupport;
import com.luckyframework.httpclient.proxy.configapi.ConfigurationSource;
import com.luckyframework.httpclient.proxy.creator.ObjectCreator;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.handle.HttpExceptionHandle;
import com.luckyframework.httpclient.proxy.interceptor.CookieManagerInterceptor;
import com.luckyframework.httpclient.proxy.interceptor.Interceptor;
import com.luckyframework.httpclient.proxy.interceptor.RedirectInterceptor;
import com.luckyframework.httpclient.proxy.spel.SpELConvert;
import com.luckyframework.httpclient.proxy.spel.StaticClassEntry;
import com.luckyframework.httpclient.proxy.spel.StaticMethodEntry;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.spel.ParamWrapper;
import com.luckyframework.spel.SpELRuntime;
import com.luckyframework.threadpool.ThreadPoolFactory;
import com.luckyframework.threadpool.ThreadPoolParam;
import io.github.lucklike.httpclient.config.CookieManageConfiguration;
import io.github.lucklike.httpclient.config.GenerateEntry;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.config.HttpConnectionPoolConfiguration;
import io.github.lucklike.httpclient.config.HttpExecutorFactory;
import io.github.lucklike.httpclient.config.InterceptorGenerateEntry;
import io.github.lucklike.httpclient.config.KeyStoreConfiguration;
import io.github.lucklike.httpclient.config.LoggerConfiguration;
import io.github.lucklike.httpclient.config.ObjectCreatorFactory;
import io.github.lucklike.httpclient.config.PoolParamHttpExecutorFactory;
import io.github.lucklike.httpclient.config.RedirectConfiguration;
import io.github.lucklike.httpclient.config.ResponseConvertConfiguration;
import io.github.lucklike.httpclient.config.SSLConfiguration;
import io.github.lucklike.httpclient.config.SimpleGenerateEntry;
import io.github.lucklike.httpclient.config.SpELConfiguration;
import io.github.lucklike.httpclient.config.SpELRuntimeFactory;
import io.github.lucklike.httpclient.config.impl.BeanSpELRuntimeFactoryFactory;
import io.github.lucklike.httpclient.config.impl.MultipartThreadPoolParam;
import io.github.lucklike.httpclient.config.impl.OkHttp3ExecutorFactory;
import io.github.lucklike.httpclient.config.impl.SpecifiedInterfacePrintLogInterceptor;
import io.github.lucklike.httpclient.configapi.SpringEnvironmentConfigurationSource;
import io.github.lucklike.httpclient.convert.HttpExecutorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.ObjectCreatorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.SpELRuntimeFactoryInstanceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.type.AnnotationMetadata;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.github.lucklike.httpclient.Constant.*;

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
public class LuckyHttpAutoConfiguration implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(LuckyHttpAutoConfiguration.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        ApplicationContextUtils.setApplicationContext(applicationContext);
    }

    /**
     * 转换器相关的配置
     */
    @Bean("conversionService")
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
    public ConfigurationSource springEnvConfigSource() {
        return new SpringEnvironmentConfigurationSource();
    }

    /**
     * 从环境变量获取必要的配置
     */
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
    @Bean(name = PROXY_FACTORY_BEAN_NAME, destroyMethod = DESTROY_METHOD)
    public HttpClientProxyObjectFactory luckyHttpClientProxyFactory(@Qualifier(PROXY_FACTORY_CONFIG_BEAN_NAME) HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        HttpClientProxyObjectFactory factory = new HttpClientProxyObjectFactory();

        objectCreateSetting(factory, factoryConfig);
        factorySpELConvertSetting(factory, factoryConfig);
        factoryExpressionParamSetting(factory, factoryConfig);
        asyncExecuteSetting(factory, factoryConfig);
        httpExecuteSetting(factory, factoryConfig);
        exceptionHandlerSetting(factory, factoryConfig);
        timeoutSetting(factory, factoryConfig);
        interceptorSetting(factory, factoryConfig);
        sslSetting(factory, factoryConfig);
        parameterSetting(factory, factoryConfig);
        responseConvertSetting(factory, factoryConfig);
        configApiSourceSetting();

        return factory;
    }

    /**
     * 设置{@link SpELConvert SPEL表达式转换器}，首先尝试从配置中读取用户配置的{@link io.github.lucklike.httpclient.config.SpELRuntimeFactory},
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

        // 导入SpEL依赖包
        List<String> springElPackageImports = springElConfig.getImportPackages();
        if (ContainerUtils.isNotEmptyCollection(springElPackageImports)) {
            springElPackageImports.forEach(factory::importPackage);
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
        StaticClassEntry[] springElFunctionClasses = springElConfig.getFunctionClasses();
        if (ContainerUtils.isNotEmptyArray(springElFunctionClasses)) {
            for (StaticClassEntry springElFunctionClass : springElFunctionClasses) {
                factory.addSpringElFunctionClass(springElFunctionClass);
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
     * 1.如果配置了 httpExecutorBean，则直接使用该Bean名称对应的执行器
     * 2.如果配置了httpExecutorFactory，则使用httpExecutorFactory来创建执行器
     * 3.如果配置了HttpExecutorEnum，则使用枚举中指定的httpExecutorFactory来创建执行器
     * 如果httpExecutorFactory为{@link PoolParamHttpExecutorFactory},则还需要设置连接池参数
     * 4.在Spring容器中按类型查找，将找到的Bean设置为执行器
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void httpExecuteSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        String httpExecutorBean = factoryConfig.getHttpExecutorBean();
        if (StringUtils.hasText(httpExecutorBean)) {
            factory.setHttpExecutor(applicationContext.getBean(httpExecutorBean, HttpExecutor.class));
        } else {
            HttpExecutorFactory httpExecutorFactory = factoryConfig.getHttpExecutorFactory();
            if (httpExecutorFactory == null && factoryConfig.getHttpExecutor() != null) {
                httpExecutorFactory = factoryConfig.getHttpExecutor().HttpExecutorFactory();
            }
            if (httpExecutorFactory != null) {
                if (httpExecutorFactory instanceof PoolParamHttpExecutorFactory) {
                    PoolParamHttpExecutorFactory poolParamHttpExecutorFactory = (PoolParamHttpExecutorFactory) httpExecutorFactory;
                    HttpConnectionPoolConfiguration httpConnPoolConfig = factoryConfig.getHttpConnectionPool();
                    Integer maxIdleConnections = httpConnPoolConfig.getMaxIdleConnections();
                    Long keepAliveDuration = httpConnPoolConfig.getKeepAliveDuration();
                    TimeUnit keepAliveTimeUnit = httpConnPoolConfig.getKeepAliveTimeUnit();

                    if (maxIdleConnections != null && maxIdleConnections > 0) {
                        poolParamHttpExecutorFactory.setMaxIdleConnections(maxIdleConnections);
                    }

                    if (keepAliveDuration != null && keepAliveDuration > 0) {
                        poolParamHttpExecutorFactory.setKeepAliveDuration(keepAliveDuration);
                    }

                    if (keepAliveTimeUnit != null) {
                        poolParamHttpExecutorFactory.setTimeUnit(keepAliveTimeUnit);
                    }

                }
                factory.setHttpExecutor(httpExecutorFactory.getHttpExecutor());
            } else {
                factory.setHttpExecutor(applicationContext.getBean(HttpExecutor.class));
            }
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

        // 导入Spring容器中配置的Executor
        String[] executorBeanNames = applicationContext.getBeanNamesForType(Executor.class);
        if (ContainerUtils.isNotEmptyArray(executorBeanNames)) {
            for (String executorBeanName : executorBeanNames) {
                factory.addAlternativeAsyncExecutor(executorBeanName, () -> applicationContext.getBean(Executor.class));
            }
        }

        // 导入用户配置的的Executor
        MultipartThreadPoolParam multiPoolParam = factoryConfig.getAsyncThreadPool();
        if (multiPoolParam != null) {
            factory.setAsyncExecutor(() -> ThreadPoolFactory.createThreadPool(multiPoolParam));

            Map<String, ThreadPoolParam> alternativePoolParamMap = multiPoolParam.getAlternative();
            if (ContainerUtils.isNotEmptyMap(alternativePoolParamMap)) {
                alternativePoolParamMap.forEach((name, poolParam) -> {
                    factory.addAlternativeAsyncExecutor(name, () -> ThreadPoolFactory.createThreadPool(poolParam));
                });
            }
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
                factory.addInterceptor(CookieManagerInterceptor.class, Scope.SINGLETON, cmi -> cmi.setCookieStore(createObject(cookieStoreGenerate)), priority);
            } else {
                factory.addInterceptor(CookieManagerInterceptor.class, Scope.SINGLETON, priority);
            }
        }

        // 检查是否需要注册日志打印的拦截器
        LoggerConfiguration loggerConfig = factoryConfig.getLogger();
        Set<String> loggerPackages = loggerConfig.getPackages();
        if (ContainerUtils.isNotEmptyCollection(loggerPackages)) {
            // 注册负责日志打印的拦截器
            factory.addInterceptor(SpecifiedInterfacePrintLogInterceptor.class, Scope.METHOD_CONTEXT, interceptor -> {
                interceptor.setPrintLogPackageSet(loggerPackages);
                interceptor.setPrintRequestLog(loggerConfig.isEnableReqLog());
                interceptor.setPrintResponseLog(loggerConfig.isEnableRespLog());
                interceptor.setReqCondition(loggerConfig.getReqLogCondition());
                interceptor.setRespCondition(loggerConfig.getRespLogCondition());
                interceptor.setPrintAnnotationInfo(loggerConfig.isEnableAnnotationLog());
                interceptor.setPrintArgsInfo(loggerConfig.isEnableArgsLog());
                interceptor.setForcePrintBody(loggerConfig.isForcePrintBody());
                interceptor.setPrintRespHeader(loggerConfig.isEnableRespHeaderLog());
                Set<String> allowPrintLogBodyMimeTypes = loggerConfig.getSetAllowMimeTypes();
                if (ContainerUtils.isNotEmptyCollection(allowPrintLogBodyMimeTypes)) {
                    interceptor.setAllowPrintLogBodyMimeTypes(allowPrintLogBodyMimeTypes);
                }
                Set<String> addAllowPrintLogBodyMimeTypes = loggerConfig.getAddAllowMimeTypes();
                if (ContainerUtils.isNotEmptyCollection(addAllowPrintLogBodyMimeTypes)) {
                    interceptor.addAllowPrintLogBodyMimeTypes(addAllowPrintLogBodyMimeTypes);
                }
                interceptor.setAllowPrintLogBodyMaxLength(loggerConfig.getBodyMaxLength());
            }, loggerConfig.getPriority());

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
        KeyStoreConfiguration[] sslContexts = sslConfig.getKeyStores();
        if (ContainerUtils.isNotEmptyArray(sslContexts)) {
            for (KeyStoreConfiguration sslContext : sslContexts) {
                factory.addKeyStoreInfo(sslContext.getId(), sslContext);
            }
        }

        // 开启全局SSL配置
        if (Objects.equals(Boolean.TRUE, sslConfig.getGlobalEnable())) {

            // HostnameVerifier
            HostnameVerifier hostnameVerifier = TrustAllHostnameVerifier.DEFAULT_INSTANCE;
            SimpleGenerateEntry<HostnameVerifierFactory> hvbFactory = sslConfig.getHostnameVerifier();
            if (hvbFactory != null) {
                if (StringUtils.hasText(hvbFactory.getBeanName())) {
                    hostnameVerifier = applicationContext.getBean(hvbFactory.getBeanName(), HostnameVerifierFactory.class).getHostnameVerifier();
                } else if (hvbFactory.getType() != null) {
                    hostnameVerifier = ClassUtils.newObject(hvbFactory.getType()).getHostnameVerifier();
                }
            } else if (StringUtils.hasText(sslConfig.getHostnameVerifierExpression())) {
                hostnameVerifier = factory.getSpELConverter().parseExpression(new ParamWrapper(sslConfig.getHostnameVerifierExpression()).setExpectedResultType(HostnameVerifier.class));
            }
            factory.setHostnameVerifier(hostnameVerifier);

            // SSLSocketFactory
            SimpleGenerateEntry<SSLSocketFactoryFactory> sslFactoryConfig = sslConfig.getSslSocketFactory();
            if (sslFactoryConfig != null && (StringUtils.hasText(sslFactoryConfig.getBeanName()) || sslFactoryConfig.getType() != null)) {
                if (StringUtils.hasText(sslFactoryConfig.getBeanName())) {
                    factory.setSslSocketFactory(applicationContext.getBean(sslFactoryConfig.getBeanName(), SSLSocketFactoryFactory.class).getSSLSocketFactory());
                } else {
                    factory.setSslSocketFactory(ClassUtils.newObject(sslFactoryConfig.getType()).getSSLSocketFactory());
                }
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

                factory.setSslSocketFactory(SSLUtils.createSSLContext(sslConfig.getGlobalProtocol(), keyStoreInfo, trustStoreInfo).getSocketFactory());
            }
        }
    }

    /**
     * 公共参数设置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    public void parameterSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        factory.setHeaders(factoryConfig.getHeaderParams());
        factory.setPathParameters(factoryConfig.getPathParams());
        factory.setQueryParameters(factoryConfig.getQueryParams());
    }

    /**
     * 设置响应结果自动转换器
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void responseConvertSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {

        // 注册Spring容器中的Response.AutoConvert
        for (String autoConvertBeanName : applicationContext.getBeanNamesForType(Response.AutoConvert.class)) {
            Response.addAutoConvert(applicationContext.getBean(autoConvertBeanName, Response.AutoConvert.class));
        }

        ResponseConvertConfiguration responseConvertConfig = factoryConfig.getResponseConvert();

        // 注册配置文件中配置的Response.AutoConvert
        Class<? extends Response.AutoConvert>[] responseAutoConverts = responseConvertConfig.getResponseAutoConverts();
        if (ContainerUtils.isNotEmptyArray(responseAutoConverts)) {
            Stream.of(responseAutoConverts).forEach(racClass -> Response.addAutoConvert(ClassUtils.newObject(racClass)));
        }

        // 注册Spring容器中的ContentEncodingConvertor
        for (String autoConvertBeanName : applicationContext.getBeanNamesForType(ContentEncodingConvertor.class)) {
            AbstractSaveResultResponseProcessor.addContentEncodingConvertor(applicationContext.getBean(autoConvertBeanName, ContentEncodingConvertor.class));
        }

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
     * 超时时间设置
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void timeoutSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        Integer connectionTimeout = factoryConfig.getConnectionTimeout();
        Integer readTimeout = factoryConfig.getReadTimeout();
        Integer writeTimeout = factoryConfig.getWriteTimeout();

        if (connectionTimeout != null) {
            factory.setConnectionTimeout(connectionTimeout);
        }
        if (readTimeout != null) {
            factory.setReadTimeout(readTimeout);
        }
        if (writeTimeout != null) {
            factory.setWriteTimeout(writeTimeout);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createObject(SimpleGenerateEntry<T> generateEntry) {
        if (StringUtils.hasText(generateEntry.getBeanName())) {
            return (T) applicationContext.getBean(generateEntry.getBeanName());
        } else {
            return ClassUtils.newObject(generateEntry.getType());
        }
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

    /********************** ContentEncodingConvertor *************************************/

    @ConditionalOnClass(name = {"org.brotli.dec.BrotliInputStream"})
    static class BrotliContentEncodingConvertorConfig {

        @Bean
        public ContentEncodingConvertor brotliContentEncodingConvertor() {
            return new BrotliContentEncodingConvertor();
        }
    }

    @ConditionalOnClass(name = {"com.github.luben.zstd.Zstd"})
    static class ZstdContentEncodingConvertorConfig {

        @Bean
        public ContentEncodingConvertor zstdContentEncodingConvertor() {
            return new ZstdContentEncodingConvertor();
        }
    }

    /********************** Response.AutoConvert *************************************/

    @ConditionalOnClass(name = {"com.google.protobuf.Parser"})
    static class ProtobufAutoConvertConfig {

        @Bean
        public Response.AutoConvert protobufAutoConvert() {
            return new ProtobufAutoConvert();
        }
    }

    @ConditionalOnClass(name = {"org.springframework.web.multipart.MultipartFile"})
    static class SpringMultipartFileAutoConvertConfig {
        @Bean
        public Response.AutoConvert springMultipartFileAutoConvert() {
            HttpClientProxyObjectFactory.addNotAutoCloseResourceTypes(ClassUtils.getClass("org.springframework.web.multipart.MultipartFile"));
            return new SpringMultipartFileAutoConvert();
        }
    }


    /********************** HttpExecutor *************************************/

    @ConditionalOnMissingClass({"okhttp3.OkHttpClient", "org.apache.http.client.HttpClient"})
    static class JdkHttpExecutorConfig {

        @Bean(DEFAULT_JDK_EXECUTOR_BEAN_NAME)
        public HttpExecutor luckyJdkHttpExecutor() {
            return new JdkHttpExecutor();
        }

    }

    @ConditionalOnClass(name = {"okhttp3.OkHttpClient"})
    static class OkHttpExecutorConfig {

        @Bean(DEFAULT_OKHTTP3_EXECUTOR_BEAN_NAME)
        public HttpExecutor luckyOkHttp3Executor() {
            return new OkHttp3ExecutorFactory().getHttpExecutor();
        }

    }

    @ConditionalOnClass(name = {"org.apache.http.client.HttpClient"})
    static class ApacheHttpExecutorConfig {

        @Bean(DEFAULT_HTTP_CLIENT_EXECUTOR_BEAN_NAME)
        public HttpExecutor luckyApacheHttpExecutor() {
            return new HttpClientExecutor();
        }

    }

}
