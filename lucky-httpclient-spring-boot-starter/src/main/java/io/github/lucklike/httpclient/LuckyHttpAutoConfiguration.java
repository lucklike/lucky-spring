package io.github.lucklike.httpclient;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.exception.LuckyRuntimeException;
import com.luckyframework.httpclient.core.CookieStore;
import com.luckyframework.httpclient.core.executor.HttpClientExecutor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.ssl.SSLUtils;
import com.luckyframework.httpclient.core.ssl.TrustAllHostnameVerifier;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.creator.ObjectCreator;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.handle.HttpExceptionHandle;
import com.luckyframework.httpclient.proxy.interceptor.CookieManagerInterceptor;
import com.luckyframework.httpclient.proxy.interceptor.Interceptor;
import com.luckyframework.httpclient.proxy.interceptor.RedirectInterceptor;
import com.luckyframework.httpclient.proxy.spel.SpELConvert;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.spel.SpELRuntime;
import com.luckyframework.threadpool.ThreadPoolFactory;
import com.luckyframework.threadpool.ThreadPoolParam;
import io.github.lucklike.httpclient.config.GenerateEntry;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.config.HttpExecutorFactory;
import io.github.lucklike.httpclient.config.InterceptorGenerateEntry;
import io.github.lucklike.httpclient.config.ObjectCreatorFactory;
import io.github.lucklike.httpclient.config.PoolParamHttpExecutorFactory;
import io.github.lucklike.httpclient.config.SimpleGenerateEntry;
import io.github.lucklike.httpclient.config.SpELRuntimeFactory;
import io.github.lucklike.httpclient.config.impl.BeanSpELRuntimeFactoryFactory;
import io.github.lucklike.httpclient.config.impl.OkHttp3ExecutorFactory;
import io.github.lucklike.httpclient.config.impl.SpecifiedInterfacePrintLogInterceptor;
import io.github.lucklike.httpclient.convert.HttpExecutorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.ObjectCreatorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.SpELRuntimeFactoryInstanceConverter;
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
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.github.lucklike.httpclient.Constant.DEFAULT_HTTP_CLIENT_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_JDK_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DEFAULT_OKHTTP3_EXECUTOR_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.DESTROY_METHOD;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_CONFIG_BEAN_NAME;

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
        // 使用工厂构建一个SpELRuntime对象
        SpELRuntimeFactory spELRuntimeFactory = factoryConfig.getSpringElRuntimeFactory();
        spELRuntimeFactory = spELRuntimeFactory == null ? new BeanSpELRuntimeFactoryFactory() : spELRuntimeFactory;
        SpELRuntime spELRuntime = spELRuntimeFactory.getSpELRuntime();

        // 使用SpELRuntime对象构建一个SpELConvert对象，并导入公共包
        List<String> springElPackageImports = factoryConfig.getSpringElPackageImports();
        SpELConvert spELConvert = new SpringSpELConvert(spELRuntime, applicationContext.getEnvironment());
        if (!ContainerUtils.isEmptyCollection(springElPackageImports)) {
            springElPackageImports.forEach(spELConvert::importPackage);
        }
        factory.setSpELConverter(spELConvert);
    }

    /**
     * 设置工厂SpEL表达式配置参数
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void factoryExpressionParamSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        ConfigurationMap expressionParams = factoryConfig.getExpressionParams();
        if (expressionParams != null) {
            expressionParams.forEach(factory::addExpressionParam);
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
                    Integer maxIdleConnections = factoryConfig.getMaxIdleConnections();
                    Long keepAliveDuration = factoryConfig.getKeepAliveDuration();
                    TimeUnit keepAliveTimeUnit = factoryConfig.getKeepAliveTimeUnit();

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
        ThreadPoolParam poolParam = factoryConfig.getThreadPoolParam();
        if (poolParam != null) {
            factory.setAsyncExecutorSupplier(() -> ThreadPoolFactory.createThreadPool(poolParam));
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

        // 检查是否需要注册支持自动重定向功能的拦截器
        if (factoryConfig.isAutoRedirect()) {
            factory.addInterceptor(RedirectInterceptor.class, Scope.METHOD, interceptor -> {
                if (ContainerUtils.isNotEmptyArray(factoryConfig.getRedirectStatus())) {
                    interceptor.setRedirectStatus(factoryConfig.getRedirectStatus());
                }
                if (StringUtils.hasText(factoryConfig.getRedirectCondition())) {
                    interceptor.setRedirectCondition(factoryConfig.getRedirectCondition());
                }
                if (StringUtils.hasText(factoryConfig.getRedirectLocation())) {
                    interceptor.setRedirectLocationExp(factoryConfig.getRedirectLocation());
                }
            }, 100);
        }

        // 检查是否开启了Cookie管理功能，开启则注入相关的拦截器
        if (factoryConfig.isEnableCookieManage()) {
            SimpleGenerateEntry<CookieStore> cookieStoreGenerate = factoryConfig.getCookieStoreGenerate();
            if (cookieStoreGenerate != null) {
                CookieStore cookieStore;
                if (StringUtils.hasText(cookieStoreGenerate.getBeanName())) {
                    cookieStore = applicationContext.getBean(cookieStoreGenerate.getBeanName(), CookieStore.class);
                } else {
                    cookieStore = ClassUtils.newObject(cookieStoreGenerate.getType());
                }

                factory.addInterceptor(CookieManagerInterceptor.class, Scope.SINGLETON, cmi -> cmi.setCookieStore(cookieStore), 100);
            } else {
                factory.addInterceptor(CookieManagerInterceptor.class, Scope.SINGLETON, 1000);
            }
        }

        // 检查是否需要注册日志打印的拦截器
        Set<String> printLogPackages = factoryConfig.getPrintLogPackages();
        if (!ContainerUtils.isEmptyCollection(printLogPackages)) {
            // 注册负责日志打印的拦截器
            factory.addInterceptor(SpecifiedInterfacePrintLogInterceptor.class, Scope.METHOD_CONTEXT, interceptor -> {
                interceptor.setPrintLogPackageSet(printLogPackages);
                interceptor.setPrintRequestLog(factoryConfig.isEnableRequestLog());
                interceptor.setPrintResponseLog(factoryConfig.isEnableResponseLog());
                interceptor.setReqCondition(factoryConfig.getPrintReqLogCondition());
                interceptor.setRespCondition(factoryConfig.getPrintRespLogCondition());
                interceptor.setPrintAnnotationInfo(factoryConfig.isEnablePrintAnnotationInfo());
                interceptor.setPrintArgsInfo(factoryConfig.isEnablePrintArgsInfo());
                Set<String> allowPrintLogBodyMimeTypes = factoryConfig.getAllowPrintLogBodyMimeTypes();
                if (!ContainerUtils.isEmptyCollection(allowPrintLogBodyMimeTypes)) {
                    interceptor.setAllowPrintLogBodyMimeTypes(allowPrintLogBodyMimeTypes);
                }
                interceptor.setAllowPrintLogBodyMaxLength(factoryConfig.getAllowPrintLogBodyMaxLength());
            });

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
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void sslSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        // 检查是否需要注册忽略SSL证书认证的拦截器
        if (factoryConfig.isIgnoreSSLVerify()) {
            try {
                factory.setHostnameVerifier(TrustAllHostnameVerifier.DEFAULT_INSTANCE);
                factory.setSslSocketFactory(SSLUtils.createIgnoreVerifySSL(factoryConfig.getSslProtocol()).getSocketFactory());
            }catch (Exception e) {
                throw new LuckyRuntimeException(e);
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
        factory.setFormParameters(factoryConfig.getFormParams());
        factory.setMultipartFormParams(factoryConfig.getMultipartFormSimpleParams());
        ConfigurationMap multipartFormResourceParams = factoryConfig.getMultipartFormResourceParams();
        if (multipartFormResourceParams != null) {
            multipartFormResourceParams.forEach((k, v) -> factory.addResources(k, ConversionUtils.conversion(v, Resource[].class)));
        }
        if (factoryConfig.isEnableContentCompress()) {
            factory.addHeader("Accept-Encoding", "gzip, deflate, br, zstd");
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
