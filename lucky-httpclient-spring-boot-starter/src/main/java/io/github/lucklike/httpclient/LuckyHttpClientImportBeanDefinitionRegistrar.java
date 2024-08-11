package io.github.lucklike.httpclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.ScanUtils;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import io.github.lucklike.httpclient.annotation.LuckyHttpClientScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

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

    private final String HTTP_CLIENT_COMPONENT = HttpClientComponent.class.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(LuckyHttpClientScan.class.getName());
        boolean useCglibProxy = (boolean) attributes.get("useCglibProxy");
        String proxyFactoryName = (String) attributes.get("proxyFactoryName");
        log.info("HttpClientProxyObjectFactory bean object '{}' is registered, and the proxy object will be created using the '{}' method", proxyFactoryName, (useCglibProxy ? "CGLIB" : "JDK"));
        LuckyHttpClientBeanDefinitionGenerator beanDefinitionGenerator =
                new LuckyHttpClientBeanDefinitionGenerator(proxyFactoryName, useCglibProxy);
        addHttpClientClasses(beanDefinitionGenerator, importingClassMetadata);
        beanDefinitionGenerator.getLuckyHttpClientBeanDefinitions().forEach(definition -> {
            String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(definition, registry);
            if (!registry.containsBeanDefinition(beanName)) {
                registry.registerBeanDefinition(beanName, definition);
            }
        });
    }

    private void addHttpClientClasses(LuckyHttpClientBeanDefinitionGenerator beanDefinitionGenerator, AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(LuckyHttpClientScan.class.getName());
        String[] scannedPackages = (String[]) attributes.get("basePackages");
        Class<?>[] scannedClasses = (Class<?>[]) attributes.get("basePackageClasses");

        if (ContainerUtils.isEmptyArray(scannedPackages) && ContainerUtils.isEmptyArray(scannedClasses)) {
            scannedClasses = new Class[]{ClassUtils.getClass(importingClassMetadata.getClassName())};
        }
        String[] finalScannedPackages = ScanUtils.getPackages(scannedClasses, scannedPackages);
        log.info("Lucky-HttpClient Start scanning the package {}", Arrays.toString(finalScannedPackages));
        ScanUtils.resourceHandle(finalScannedPackages, r -> {
            AnnotationMetadata annotationMetadata = ScanUtils.resourceToAnnotationMetadata(r);
            if (annotationMetadata.isAnnotated(HTTP_CLIENT_COMPONENT)) {
                beanDefinitionGenerator.addHttpClientClasses(ClassUtils.getClass(annotationMetadata.getClassName()));
                if (log.isDebugEnabled()) {
                    log.debug("@HttpClientComponent '{}' is registered", annotationMetadata.getClassName());
                }
            }
        });
    }
}
