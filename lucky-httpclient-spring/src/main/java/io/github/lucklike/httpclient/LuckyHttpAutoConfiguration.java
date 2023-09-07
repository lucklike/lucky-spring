package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.core.executor.HttpClientExecutor;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.executor.OkHttpExecutor;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.SpELConvert;
import com.luckyframework.spel.SpELRuntime;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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
public class LuckyHttpAutoConfiguration implements BeanFactoryAware, EnvironmentAware {

    private BeanFactory beanFactory;
    private Environment environment;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = PROXY_FACTORY_BEAN_NAME, destroyMethod = DESTROY_METHOD)
    public HttpClientProxyObjectFactory luckyHttpClientProxyFactory(SpELRuntime spELRuntime, HttpExecutor httpExecutor) {
        HttpClientProxyObjectFactory.setSpELConverter(new SpELConvert(spELRuntime));
        HttpClientProxyObjectFactory factory = new HttpClientProxyObjectFactory();
        factory.setObjectCreator(new BeanObjectCreator(beanFactory));
        factory.setHttpExecutor(httpExecutor);
        return factory;
    }

    @Bean
    public SpELRuntime luckySpELRuntime() {
        return new SpELRuntimeFactory(beanFactory, environment).createSpELRuntime();
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
