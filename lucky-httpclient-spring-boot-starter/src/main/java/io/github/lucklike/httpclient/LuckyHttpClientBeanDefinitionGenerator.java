package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * lucky-http-client接口代理对象定义信息生成器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 04:29
 */
public class LuckyHttpClientBeanDefinitionGenerator {

    /**
     * Spring容器中{@link HttpClientProxyObjectFactory}对象的Bean的名称
     */
    private final String httpClientProxyObjectFactoryBeanName;

    /**
     * 全局代理模式
     */
    private final ProxyModel globalProxyModel;

    /**
     * HTTP客户端BeanDefinition生成器构造器
     *
     * @param httpClientProxyObjectFactoryBeanName 指定Spring容器中{@link HttpClientProxyObjectFactory}对象的Bean的名称
     * @param globalProxyModel                     全局代理模式
     */
    public LuckyHttpClientBeanDefinitionGenerator(String httpClientProxyObjectFactoryBeanName, ProxyModel globalProxyModel) {
        this.httpClientProxyObjectFactoryBeanName = httpClientProxyObjectFactoryBeanName;
        this.globalProxyModel = globalProxyModel == ProxyModel.DEFAULT ? ProxyModel.JDK : globalProxyModel;
    }

    /**
     * HTTP客户端BeanDefinition生成器构造器，全局使用JDK动态代理
     *
     * @param httpClientProxyObjectFactoryBeanName 指定Spring容器中{@link HttpClientProxyObjectFactory}对象的Bean的名称
     */
    public LuckyHttpClientBeanDefinitionGenerator(String httpClientProxyObjectFactoryBeanName) {
        this(httpClientProxyObjectFactoryBeanName, ProxyModel.JDK);
    }

    /**
     * 创建一个HTTP客户端代理对象的BeanDefinition
     *
     * @param httpClientClass 代理对象的Class
     * @param proxyModel      代理模式
     * @return 代理对象的BeanDefinition
     */
    public BeanDefinition createHttpClientBeanDefinition(Class<?> httpClientClass, ProxyModel proxyModel) {
        proxyModel = proxyModel == ProxyModel.DEFAULT ? this.globalProxyModel : proxyModel;
        RootBeanDefinition beanDefinition = new RootBeanDefinition(httpClientClass);
        beanDefinition.setFactoryBeanName(httpClientProxyObjectFactoryBeanName);
        beanDefinition.setFactoryMethodName(proxyModel.getProxyMethod());
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(httpClientClass);
        return beanDefinition;
    }
}
