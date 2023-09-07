package io.github.lucklike.httpclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.exception.LuckyRuntimeException;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * lucky-http-clientBean定义信息注册器，用于收集项目中需要代理的http接口，并为其生成BeanDefinition信息
 * 之后注入到Spring容器中
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:59
 */
public class LuckyHttpClientImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private final static String PATH_PREFIX = "classpath*:";
    private final static String PATH_SUFFIX = "/**/*.class";
    private final static String[] DEFAULT_PACKAGE = {""};

    private static boolean loader;

    private final PathMatchingResourcePatternResolver PM = new PathMatchingResourcePatternResolver();
    private final CachingMetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();
    private final String LUCKY_HTTP_CLIENT_NAME = HttpClient.class.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        if (!loader) {
            Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableLuckyHttpClient.class.getName());
            boolean useCglibProxy = (boolean) attributes.get("useCglibProxy");
            String proxyFactoryName = (String) attributes.get("proxyFactoryName");
            LuckyHttpClientBeanDefinitionGenerator beanDefinitionGenerator =
                    new LuckyHttpClientBeanDefinitionGenerator(proxyFactoryName, useCglibProxy);
            addHttpClientClasses(beanDefinitionGenerator, importingClassMetadata);
            beanDefinitionGenerator.getLuckyHttpClientBeanDefinitions().forEach(definition -> {
                registry.registerBeanDefinition(getBeanName(((RootBeanDefinition) definition).getBeanClass()), definition);
            });
            loader = true;
        }
    }

    private void addHttpClientClasses(LuckyHttpClientBeanDefinitionGenerator beanDefinitionGenerator, AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableLuckyHttpClient.class.getName());
        String[] basePackages = (String[]) attributes.get("basePackages");
        Class<?>[] basePackageClasses = (Class<?>[]) attributes.get("basePackageClasses");
        if (ContainerUtils.isEmptyArray(basePackages) && ContainerUtils.isEmptyArray(basePackageClasses)) {
            Class<?>[] rootClasses = new Class[] {ClassUtils.getClass(importingClassMetadata.getClassName())};
            scanPackagesAdd(beanDefinitionGenerator, getPackages(rootClasses));
        } else if (ContainerUtils.isEmptyArray(basePackages)){
            scanPackagesAdd(beanDefinitionGenerator, getPackages(basePackageClasses));
        } else {
            scanPackagesAdd(beanDefinitionGenerator, basePackages);
        }
    }

    private void scanPackagesAdd(LuckyHttpClientBeanDefinitionGenerator beanDefinitionGenerator, String[] basePackages) {
        Set<Resource> classResources = new HashSet<>();
        // 搜集所有class资源
        basePackages = ContainerUtils.isEmptyArray(basePackages) ? DEFAULT_PACKAGE : basePackages;
        for (String basePackage : basePackages) {
            String packagePath = null;
            try {
                packagePath = basePackage.replaceAll("\\.", "/");
                packagePath = PATH_PREFIX + packagePath + PATH_SUFFIX;
                Resource[] resources = PM.getResources(packagePath);
                classResources.addAll(Arrays.asList(resources));
            } catch (IOException e) {
                throw new LuckyRuntimeException(e, "An exception occurred while scanning lucky http client configuration information: {}", packagePath);
            }
        }
        scanResourcesAdd(beanDefinitionGenerator, classResources);
    }

    private void scanResourcesAdd(LuckyHttpClientBeanDefinitionGenerator beanDefinitionGenerator, Set<Resource> classResources) {
        for (Resource resource : classResources) {
            try {
                AnnotationMetadata annotationMetadata = METADATA_READER_FACTORY.getMetadataReader(resource).getAnnotationMetadata();
                if (annotationMetadata.isAnnotated(LUCKY_HTTP_CLIENT_NAME)) {
                    beanDefinitionGenerator.addHttpClientClasses(ClassUtils.getClass(annotationMetadata.getClassName()));
                }
            } catch (IOException e) {
                throw new LuckyRuntimeException(e, "An exception occurred while scanning lucky http client configuration information: {}", resource);
            }
        }
    }

    private String[] getPackages(Class<?>[] rootClass) {
        if (rootClass == null || rootClass.length == 0) {
            return new String[]{""};
        }
        String[] basePackages = new String[rootClass.length];
        for (int i = 0; i < rootClass.length; i++) {
            Package aPackage = rootClass[i].getPackage();
            basePackages[i] = aPackage == null ? "" : aPackage.getName();
        }
        return basePackages;
    }

    /**
     * 获取扫描元素的配置名称
     *
     * @param aClass 扫描元素
     */
    public String getBeanName(Class<?> aClass) {
        Component component = AnnotationUtils.findMergedAnnotation(aClass, Component.class);
        if (component == null) {
            return aClass.getName();
        }

        String beanName = component.value();
        if (StringUtils.hasText(beanName)) {
            return beanName;
        }
        String shortName = org.springframework.util.ClassUtils.getShortName(aClass.getName());
        return Introspector.decapitalize(shortName);
    }
}
