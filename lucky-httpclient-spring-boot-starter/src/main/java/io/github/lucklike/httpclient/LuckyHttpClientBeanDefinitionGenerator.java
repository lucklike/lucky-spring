package io.github.lucklike.httpclient;

import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lucklike.httpclient.Constant.CGLIB_PROXY_METHOD;
import static io.github.lucklike.httpclient.Constant.JDK_PROXY_METHOD;

/**
 * lucky-http-client接口代理对象定义信息生成器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 04:29
 */
public class LuckyHttpClientBeanDefinitionGenerator {

    private final String httpClientProxyObjectFactoryBeanName;

    private final Set<ProxyInfo> proxyInfoSet = new LinkedHashSet<>(16);

    private final ProxyModel proxyModel;

    public LuckyHttpClientBeanDefinitionGenerator(String httpClientProxyObjectFactoryBeanName, ProxyModel proxyModel) {
        this.httpClientProxyObjectFactoryBeanName = httpClientProxyObjectFactoryBeanName;
        this.proxyModel = proxyModel == ProxyModel.DEFAULT ? ProxyModel.JDK : proxyModel;
    }

    public LuckyHttpClientBeanDefinitionGenerator(String httpClientProxyObjectFactoryBeanName) {
        this(httpClientProxyObjectFactoryBeanName, ProxyModel.JDK);
    }

    public void addHttpClientClass(Class<?> httpClientClasses, ProxyModel proxyModel) {
        proxyModel = proxyModel == ProxyModel.DEFAULT ? this.proxyModel : proxyModel;
        this.proxyInfoSet.add(new ProxyInfo(httpClientClasses, proxyModel));
    }

    public List<BeanDefinition> getLuckyHttpClientBeanDefinitions() {
        return this.proxyInfoSet
                .stream()
                .map(this::createHttpClientBeanDefinition)
                .collect(Collectors.toList());
    }

    private BeanDefinition createHttpClientBeanDefinition(ProxyInfo proxyInfo) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(proxyInfo.getProxyClass());
        beanDefinition.setFactoryBeanName(httpClientProxyObjectFactoryBeanName);
        beanDefinition.setFactoryMethodName(proxyInfo.getProxyModel().getProxyMethod());
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(proxyInfo.getProxyClass());
        return beanDefinition;
    }

    /**
     * 代理信息类
     */
    class ProxyInfo {
        private final Class<?> proxyClass;
        private final ProxyModel proxyModel;

        ProxyInfo(Class<?> proxyClass, ProxyModel proxyModel) {
            this.proxyClass = proxyClass;
            this.proxyModel = proxyModel;
        }

        public Class<?> getProxyClass() {
            return proxyClass;
        }

        public ProxyModel getProxyModel() {
            return proxyModel;
        }
    }

}
