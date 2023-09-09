package io.github.lucklike.httpclient;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.core.executor.HttpClientExecutor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.executor.OkHttpExecutor;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.ObjectCreator;
import com.luckyframework.httpclient.proxy.SpELConvert;
import com.luckyframework.spel.SpELRuntime;
import com.luckyframework.threadpool.ThreadPoolFactory;
import com.luckyframework.threadpool.ThreadPoolParam;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import io.github.lucklike.httpclient.config.HttpExceptionHandleFactory;
import io.github.lucklike.httpclient.config.HttpExecutorFactory;
import io.github.lucklike.httpclient.config.ObjectCreatorFactory;
import io.github.lucklike.httpclient.config.RequestAfterProcessorsFactory;
import io.github.lucklike.httpclient.config.ResponseAfterProcessorsFactory;
import io.github.lucklike.httpclient.convert.HttpExceptionHandleFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.HttpExecutorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.ObjectCreatorFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.RequestAfterProcessorsFactoryInstanceConverter;
import io.github.lucklike.httpclient.convert.ResponseAfterProcessorsFactoryInstanceConverter;
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

import static io.github.lucklike.httpclient.Constant.DESTROY_METHOD;
import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_BEAN_NAME;

/**
 * <pre>
 * lucky-httpclient自动配置类,此类会自动向Spring容器中注入以下Bean对象：
 *     1.{@link HttpClientProxyObjectFactory}对象
 *     2.{@link SpELRuntime}对象
 *     3.{@link HttpExecutor}对象
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


    @Bean("conversionService")
    public ConversionServiceFactoryBean conversionServiceFactoryBean() {
        ConversionServiceFactoryBean factoryBean = new ConversionServiceFactoryBean();
        factoryBean.setConverters(new HashSet<>(Arrays.asList(
                new SpELRuntimeFactoryInstanceConverter(),
                new ObjectCreatorFactoryInstanceConverter(),
                new HttpExecutorFactoryInstanceConverter(),
                new HttpExceptionHandleFactoryInstanceConverter(),
                new RequestAfterProcessorsFactoryInstanceConverter(),
                new ResponseAfterProcessorsFactoryInstanceConverter()
        )));
        return factoryBean;
    }

    @Bean("luckyHttpClientProxyObjectFactoryConfiguration")
    @ConfigurationProperties("spring.lucky.http-client")
    public HttpClientProxyObjectFactoryConfiguration httpClientProxyObjectFactoryConfiguration() {
        return new HttpClientProxyObjectFactoryConfiguration();
    }

    @Bean(name = PROXY_FACTORY_BEAN_NAME, destroyMethod = DESTROY_METHOD)
    public HttpClientProxyObjectFactory luckyHttpClientProxyFactory(@Qualifier("luckyHttpClientProxyObjectFactoryConfiguration") HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        factorySpELConvertSetting(factoryConfig);
        factoryExpressionParamSetting(factoryConfig);

        HttpClientProxyObjectFactory factory = new HttpClientProxyObjectFactory();
        asyncExecuteSetting(factory, factoryConfig);
        objectCreateSetting(factory, factoryConfig);
        httpExecuteSetting(factory, factoryConfig);
        exceptionHandlerSetting(factory, factoryConfig);
        timeoutSetting(factory, factoryConfig);
        afterProcessorSetting(factory, factoryConfig);
        parameterSetting(factory, factoryConfig);

        return factory;
    }

    /**
     * 设置{@link SpELConvert SPEL表达式转换器}，首先尝试从配置中读取用户配置的{@link io.github.lucklike.httpclient.config.SpELRuntimeFactory},
     * 如果存在则采用该工厂创建，否则使用默认实例
     *
     * @param factoryConfig 工厂配置
     */
    private void factorySpELConvertSetting(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        SpELRuntime spELRuntime;
        io.github.lucklike.httpclient.config.SpELRuntimeFactory spELRuntimeFactory = factoryConfig.getSpelruntimeFactory();
        if (spELRuntimeFactory == null) {
            spELRuntime = new SpELRuntimeFactory(applicationContext).createSpELRuntime();
        } else {
            spELRuntime = spELRuntimeFactory.getSpELRuntime();
        }
        HttpClientProxyObjectFactory.setSpELConverter(new SpELConvert(spELRuntime));
    }

    /**
     * 设置工厂SpEL表达式配置参数
     *
     * @param factoryConfig 工厂配置
     */
    private void factoryExpressionParamSetting(HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        ConfigurationMap expressionParams = factoryConfig.getExpressionParams();
        if (expressionParams != null) {
            expressionParams.forEach(HttpClientProxyObjectFactory::addExpressionParam);
        }
    }

    /**
     * 设置{@link HttpExecutor HTTP请求执行器}，首先尝试从配置中读取用户配置的{@link HttpExecutorFactory},
     * 如果存在则采用该工厂创建，否则直接从Spring容器中获取
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void httpExecuteSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        HttpExecutorFactory httpExecutorFactory = factoryConfig.getHttpExecutorFactory();
        if (httpExecutorFactory == null) {
            factory.setHttpExecutor(applicationContext.getBean(HttpExecutor.class));
        } else {
            factory.setHttpExecutor(httpExecutorFactory.getHttpExecutor());
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
            factory.setExecutorSupplier(() -> ThreadPoolFactory.createThreadPool(poolParam));
        }
    }

    /**
     * 设置公用的{@link com.luckyframework.httpclient.proxy.HttpExceptionHandle 异常处理器}，首先尝试从配置中读取用户配置的{@link HttpExceptionHandleFactory},
     * 如果存在则采用该工厂创建，否则使用默认对象
     *
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void exceptionHandlerSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        HttpExceptionHandleFactory httpExceptionHandleFactory = factoryConfig.getHttpExceptionHandleFactory();
        if (httpExceptionHandleFactory != null) {
            factory.setExceptionHandle(httpExceptionHandleFactory.getHttpExceptionHandle());
        }
    }

    /**
     * 请求、响应处理器设置
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    private void afterProcessorSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        RequestAfterProcessorsFactory requestAfterProcessorsFactory = factoryConfig.getRequestAfterProcessorsFactory();
        if (requestAfterProcessorsFactory != null) {
            factory.addRequestAfterProcessors(requestAfterProcessorsFactory.getRequestAfterProcessors());
        }

        ResponseAfterProcessorsFactory responseAfterProcessorsFactory = factoryConfig.getResponseAfterProcessorsFactory();
        if (responseAfterProcessorsFactory != null) {
            factory.addResponseAfterProcessors(responseAfterProcessorsFactory.getResponseAfterProcessors());
        }
    }

    /**
     * 公共参数设置
     * @param factory       工厂实例
     * @param factoryConfig 工厂配置
     */
    public void parameterSetting(HttpClientProxyObjectFactory factory, HttpClientProxyObjectFactoryConfiguration factoryConfig) {
        factory.setHeaders(factoryConfig.getHeaderParams());
        factory.setPathParameters(factoryConfig.getPathParams());
        factory.setQueryParameters(factoryConfig.getQueryParams());
        factory.setFormParameters(factoryConfig.getFormParams());
        ConfigurationMap resourceParams = factoryConfig.getResourceParams();
        if (resourceParams != null) {
            resourceParams.forEach((k, v) -> factory.addResources(k, ConversionUtils.conversion(v, Resource[].class)));
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

        @Bean
        public HttpExecutor luckyJdkHttpExecutor() {
            return new JdkHttpExecutor();
        }

    }

    @ConditionalOnClass(name = {"okhttp3.OkHttpClient"})
    static class OkHttpExecutorConfig {

        @Bean
        public HttpExecutor luckyOkHttpExecutor() {
            return new OkHttpExecutor();
        }

    }

    @ConditionalOnClass(name = {"org.apache.http.client.HttpClient"})
    static class ApacheHttpExecutorConfig {

        @Bean
        public HttpExecutor luckyApacheHttpExecutor() {
            return new HttpClientExecutor();
        }

    }

}
