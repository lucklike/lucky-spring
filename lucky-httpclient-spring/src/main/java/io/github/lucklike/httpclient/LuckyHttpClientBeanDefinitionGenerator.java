package io.github.lucklike.httpclient;

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

    private final String proxyMethodName;

    private final Set<Class<?>> httpClientClasses = new LinkedHashSet<>(16);

    public LuckyHttpClientBeanDefinitionGenerator(String httpClientProxyObjectFactoryBeanName, boolean useCglibProxy) {
        this.httpClientProxyObjectFactoryBeanName = httpClientProxyObjectFactoryBeanName;
        this.proxyMethodName = useCglibProxy ? CGLIB_PROXY_METHOD : JDK_PROXY_METHOD;
    }

    public LuckyHttpClientBeanDefinitionGenerator(String httpClientProxyObjectFactoryBeanName) {
        this(httpClientProxyObjectFactoryBeanName, false);
    }

    public void addHttpClientClasses(Class<?>... httpClientClasses) {
        this.httpClientClasses.addAll(Arrays.asList(httpClientClasses));
    }

    public List<BeanDefinition> getLuckyHttpClientBeanDefinitions() {
        return this.httpClientClasses
                .stream()
                .map(this::createHttpClientBeanDefinition)
                .collect(Collectors.toList());
    }

    private BeanDefinition createHttpClientBeanDefinition(Class<?> httpClientClass) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(httpClientClass);
        beanDefinition.setFactoryBeanName(httpClientProxyObjectFactoryBeanName);
        beanDefinition.setFactoryMethodName(proxyMethodName);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(httpClientClass);
        return beanDefinition;
    }


}
