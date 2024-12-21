package io.github.lucklike.httpclient;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.reflect.AnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Parameter;

import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$PARAMETER_INSTANCE_FUNCTION$__;

/**
 * 提供Bean相关操作的函数
 */
public class BeanFunction {

    /**
     * 获取参数对应的实例对象
     *
     * @param parameter 参数实例
     * @return Bean的实例
     */
    @FunctionAlias(__$PARAMETER_INSTANCE_FUNCTION$__)
    public static Object getParameterInstance(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        Qualifier qualifierAnn = AnnotationUtils.findMergedAnnotation(parameter, Qualifier.class);
        if (qualifierAnn != null && StringUtils.hasText(qualifierAnn.value())) {
            return ApplicationContextUtils.getBean(qualifierAnn.value(), parameterType);
        }
        return ApplicationContextUtils.getBean(parameterType);
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
}