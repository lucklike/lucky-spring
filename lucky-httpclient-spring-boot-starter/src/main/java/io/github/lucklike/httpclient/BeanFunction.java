package io.github.lucklike.httpclient;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.annotation.AllowNull;
import io.github.lucklike.httpclient.parameter.ParameterInstanceFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.env.Environment;

import java.util.Iterator;

import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$PARAMETER_INSTANCE_FUNCTION$__;

/**
 * 提供Bean相关操作的函数
 */
public class BeanFunction {

    /**
     * 获取参数对应的实例对象
     *
     * @param parameterInfo 参数实例
     * @return Bean的实例
     */
    @FunctionAlias(__$PARAMETER_INSTANCE_FUNCTION$__)
    public static Object getParameterInstance(ParameterInfo parameterInfo) {

        // 使用Spring容器中的ParameterInstanceFactory来创建参数实例
        ObjectProvider<ParameterInstanceFactory> factoryBeanProvider = ApplicationContextUtils.getBeanProvider(ParameterInstanceFactory.class);
        Iterator<ParameterInstanceFactory> iterator = factoryBeanProvider.orderedStream().iterator();
        while (iterator.hasNext()) {
            ParameterInstanceFactory factory = iterator.next();
            if (factory.canCreateInstance(parameterInfo)) {
                return factory.createInstance(parameterInfo);
            }
        }

        // 使用类型查找
        ObjectProvider<Object> beanProvider = ApplicationContextUtils.getBeanProvider(parameterInfo.getResolvableType());
        try {
            return beanProvider.getObject();
        } catch (NoSuchBeanDefinitionException e) {
            // 找到多个Bean时抛异常
            if (beanProvider.stream().count() > 1) {
                throw e;
            }

            // 找不到Bean时判断有无@AllowNull注解，有则注入null值，否则抛异常
            AllowNull allowNullAnn = AnnotationUtils.sameAnnotationCombined(parameterInfo.getParameter(), AllowNull.class);
            if (allowNullAnn != null && allowNullAnn.value()) {
                return null;
            }
            throw e;
        }

    }

    /**
     * 使用Bean信息获取Bean实例
     *
     * @param beanInfo Bean信息
     * @return Bean实例
     */
    public static Object bean(Object beanInfo) {
        if (beanInfo instanceof String) {
            return ApplicationContextUtils.getBean((String) beanInfo);
        }
        if (beanInfo instanceof Class<?>) {
            return ApplicationContextUtils.getBean((Class<?>) beanInfo);
        }
        throw new IllegalArgumentException("beanInfo must be of type String or Class.");
    }

    /**
     * 是否存在相应的Bean实例
     *
     * @param beanName Bean名称
     * @return 是否存在相应的Bean实例
     */
    public static boolean hasBean(String beanName) {
        return ApplicationContextUtils.containsBean(beanName);
    }

    /**
     * 是否存在相应的Bean定义信息
     *
     * @param beanName Bean名称
     * @return 是否存在相应的Bean定义信息
     */
    public static boolean hasBeanDefinition(String beanName) {
        return ApplicationContextUtils.containsBeanDefinition(beanName);
    }

    /**
     * 发布事件
     *
     * @param event 事件
     */
    public static void publishEvent(Object event) {
        ApplicationContextUtils.publishEvent(event);
    }

    /**
     * 获取 application 唯一ID
     */
    public static String applicationId() {
        return ApplicationContextUtils.getId();
    }

    /**
     * 获取 application 名称
     *
     * @return application 名称
     */
    public static String applicationName() {
        return ApplicationContextUtils.getApplicationName();
    }

    /**
     * 获取 display 名称
     *
     * @return display 名称
     */
    public static String displayName() {
        return ApplicationContextUtils.getDisplayName();
    }

    /**
     * 第一次加载此上下文时的时间戳（毫秒）
     *
     * @return 第一次加载此上下文时的时间戳（毫秒）
     */
    public static long startupDate() {
        return ApplicationContextUtils.getStartupDate();
    }

    /**
     * 获取环境变量中的某段配置，并映射成指定的类型的对象
     *
     * @param prefix 配置
     * @param clazz  指定的映射类型
     * @param <T>    映射类型的泛型
     * @return 配置值
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T env(String prefix, Class<T>... clazz) {
        Environment env = ApplicationContextUtils.getEnvironment();

        if (env.containsProperty(prefix)) {
            Class<?> type = ContainerUtils.isNotEmptyArray(clazz) ? clazz[0] : String.class;
            return (T) env.getRequiredProperty(prefix, type);
        }

        Class<?> type = ContainerUtils.isNotEmptyArray(clazz) ? clazz[0] : ConfigurationMap.class;
        return (T) Binder.get(env)
                .bind(ConfigurationPropertyName.adapt(prefix, '.'), Bindable.of(type))
                .orElseThrow(() -> new IllegalStateException("Required key '" + prefix + "' not found"));
    }
}
