package io.github.lucklike.httpclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.ScanUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.Version;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import io.github.lucklike.httpclient.annotation.LuckyHttpClientScan;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.Map;

/**
 * lucky-http-clientBean定义信息注册器，用于收集项目中需要代理的http接口，并为其生成BeanDefinition信息
 * 之后注入到Spring容器中
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:59
 */
public class LuckyHttpClientImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger log = LoggerFactory.getLogger(LuckyHttpClientImportBeanDefinitionRegistrar.class);

    /**
     * HTTP组件注解全类名
     */
    private final String HTTP_CLIENT_COMPONENT = HttpClientComponent.class.getName();

    /**
     * Spring组件注解全类名
     */
    private final String SPRING_COMPONENT = Component.class.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {

        // 构造BeanDefinition生成器
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(LuckyHttpClientScan.class.getName());
        ProxyModel globalProxyModel = (ProxyModel) attributes.get("proxyModel");
        String proxyFactoryName = (String) attributes.get("proxyFactoryName");
        log.info("HttpClientProxyObjectFactory bean object '{}' is registered, and the proxy object will be created using the '{}' method", proxyFactoryName, globalProxyModel);
        LuckyHttpClientBeanDefinitionGenerator beanDefinitionGenerator =
                new LuckyHttpClientBeanDefinitionGenerator(proxyFactoryName, globalProxyModel);


        // 获取需要扫描的包
        String[] scannedPackages = (String[]) attributes.get("basePackages");
        Class<?>[] scannedClasses = (Class<?>[]) attributes.get("basePackageClasses");
        if (ContainerUtils.isEmptyArray(scannedPackages) && ContainerUtils.isEmptyArray(scannedClasses)) {
            scannedClasses = new Class[]{ClassUtils.getClass(importingClassMetadata.getClassName())};
        }
        String[] finalScannedPackages = ScanUtils.getPackages(scannedClasses, scannedPackages);
        log.info("Lucky-HttpClient Start scanning the package {}", Arrays.toString(finalScannedPackages));

        // 包扫描以及BeanDefinition注册
        ScanUtils.resourceHandle(finalScannedPackages, r -> {
            AnnotationMetadata annotationMetadata = ScanUtils.resourceToAnnotationMetadata(r);
            if (isLuckyHttpComponent(annotationMetadata)) {

                // 获取Class名称
                String beanClassName = annotationMetadata.getClassName();

                // 确认代理模式
                ProxyModel proxyModel = getProxyModel(annotationMetadata);

                // 生成BeanDefinition
                BeanDefinition definition = beanDefinitionGenerator.createHttpClientBeanDefinition(ClassUtils.getClass(beanClassName), proxyModel);

                // 获取Bean名称并注册
                String beanName = generateBeanName(annotationMetadata);
                if (!registry.containsBeanDefinition(beanName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("@HttpClientComponent '{}' is registered", beanClassName);
                    }
                    registry.registerBeanDefinition(beanName, definition);
                } else {
                    throw new BeanCreationException("There are multiple @HttpClientComponent named '" + beanName + "' : [" + registry.getBeanDefinition(beanName).getBeanClassName() + ", " + beanClassName + "]");
                }
            }
        });

        Version.printVersion();
    }

    /**
     * 确定某个注解元素使用的代理模型
     *
     * @param annotationMetadata 注解元素
     * @return 代理模型
     */
    private ProxyModel getProxyModel(AnnotationMetadata annotationMetadata) {
        return (ProxyModel) annotationMetadata.getAnnotationAttributes(HTTP_CLIENT_COMPONENT).get("proxyModel");
    }

    /**
     * 为注解元素生成Bean名称
     *
     * @param annotationMetadata 注解元素
     * @return Bean名称
     */
    private String generateBeanName(AnnotationMetadata annotationMetadata) {
        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(SPRING_COMPONENT);
        String value = (String) attributes.get("value");
        if (StringUtils.hasText(value)) {
            return value;
        }
        String beanClassName = annotationMetadata.getClassName();
        String shortClassName = org.springframework.util.ClassUtils.getShortName(beanClassName);
        return Introspector.decapitalize(shortClassName);
    }

    /**
     * 判断某个注解元数据是否为Lucky的Http组件
     * <pre>
     *     1.不能是注解类型
     *     2.不能是具体类型（必须是接口或者抽象类）
     *     3.必须是独立的
     *     4.必须被{@link HttpClientComponent}注解标注
     * </pre>
     *
     * @param annotationMetadata 注解元数据
     * @return 该注解元数据是否为Lucky的Http组件
     */
    private boolean isLuckyHttpComponent(AnnotationMetadata annotationMetadata) {
        return !annotationMetadata.isAnnotation() &&
                !annotationMetadata.isConcrete() &&
                annotationMetadata.isIndependent() &&
                annotationMetadata.isAnnotated(HTTP_CLIENT_COMPONENT);
    }
}
