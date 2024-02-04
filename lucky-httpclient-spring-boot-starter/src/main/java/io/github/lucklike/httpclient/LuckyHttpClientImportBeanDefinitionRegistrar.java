package io.github.lucklike.httpclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.ScanUtils;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import io.github.lucklike.httpclient.annotation.LuckyHttpClientScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

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

    private final String HTTP_CLIENT_COMPONENT = HttpClientComponent.class.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(LuckyHttpClientScan.class.getName());
        boolean useCglibProxy = (boolean) attributes.get("useCglibProxy");
        String proxyFactoryName = (String) attributes.get("proxyFactoryName");
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
        ScanUtils.resourceHandle(finalScannedPackages, r -> {
            AnnotationMetadata annotationMetadata = ScanUtils.resourceToAnnotationMetadata(r);
            if (annotationMetadata.isAnnotated(HTTP_CLIENT_COMPONENT)) {
                beanDefinitionGenerator.addHttpClientClasses(ClassUtils.getClass(annotationMetadata.getClassName()));
            }
        });
    }
}
