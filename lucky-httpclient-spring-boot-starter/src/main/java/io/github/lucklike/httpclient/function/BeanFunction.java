package io.github.lucklike.httpclient.function;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.FontUtil;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.FunctionFilter;
import com.luckyframework.httpclient.proxy.spel.Namespace;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.annotation.AllowNull;
import io.github.lucklike.httpclient.injection.BindException;
import io.github.lucklike.httpclient.injection.WrapType;
import io.github.lucklike.httpclient.injection.parameter.ParameterInstanceFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Supplier;

import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$PARAMETER_INSTANCE_FUNCTION$__;
import static io.github.lucklike.httpclient.Constant.SPRING_FUNCTION_SPACE;

/**
 * 提供Bean相关操作的函数
 */
@Namespace(SPRING_FUNCTION_SPACE)
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
        ResolvableType paramType = parameterInfo.getResolvableType();
        WrapType wrapType = WrapType.of(paramType);
        ObjectProvider<Object> beanProvider = ApplicationContextUtils.getBeanProvider(wrapType.getTargetType(paramType));

        // ObjectProvider类型不用包装，直接返回
        if (wrapType == WrapType.OBJECT_PROVIDER) {
            return beanProvider;
        }

        // 将参数实例获取逻辑封装为Supplier
        Supplier<?> objectSupplier = () -> {
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
        };

        return wrapType.wrap(objectSupplier);
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
        if (beanInfo instanceof ResolvableType) {
            return ApplicationContextUtils.getBeanProvider(((ResolvableType) beanInfo)).getObject();
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
     * @param type   指定的映射类型(支持的类型有：Class、ResolvableType)
     * @param <T>    映射类型的泛型
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    public static <T> T env(String prefix, Object... type) {
        Environment env = ApplicationContextUtils.getEnvironment();
        if (env.containsProperty(prefix)) {
            return (T) env.getRequiredProperty(prefix, getConvertClass(type));
        }

        return (T) Binder.get(env)
                .bind(ConfigurationPropertyName.adapt(prefix, '.'), Bindable.of(getConvertType(type)))
                .orElseThrow(() -> {
                    String tag = FontUtil.getBackRedStr(" CONFIG BIND EXCEPTION ");
                    return new BindException(
                            "     \n\t{2}\n\t❌ An exception occurred when binding the configuration ['{0}'] to an object of type {1}. \n\t👉 1. Please check whether the configuration ['{0}'] exists? \n\t👉 2. Please check whether the binding type [{1}] is reasonable?\n\t{2}",
                            FontUtil.getWhiteStr(prefix),
                            FontUtil.getWhiteUnderline(getConvertType(type).toString()),
                            tag
                    );
                });
    }

    /**
     * 将环境变量中的某一段配置绑定到否个实体类对象上
     *
     * @param targetObject 用于绑定配置的实体类对象
     * @param prefix       配置
     */
    public static void bind(Object targetObject, String prefix) {
        Binder.get(ApplicationContextUtils.getEnvironment())
                .bind(ConfigurationPropertyName.adapt(prefix, '.'), Bindable.ofInstance(targetObject))
                .orElseGet(() -> targetObject);
    }

    /**
     * 初始化绑定,如果target对象中的某个属性不为初始值时（引用类型的初始值为null， 基本类型的初始值参考JDK规范），拷贝时则忽略该属性
     *
     * @param targetObject 用于绑定配置的实体类对象
     * @param prefix       配置
     */
    public static void initBind(Object targetObject, String prefix) {
        Object configObj = env(prefix, targetObject.getClass());
        CommonFunctions.initCopy(configObj, targetObject);
    }

    @FunctionFilter
    private static ResolvableType getConvertType(Object[] type) {
        if (ContainerUtils.isEmptyArray(type)) {
            return ResolvableType.forClass(LinkedHashMap.class);
        }
        return CommonFunctions.toResolvableType(type[0]);
    }

    @NonNull
    @FunctionFilter
    private static Class<?> getConvertClass(Object[] type) {
        return Objects.requireNonNull(getConvertType(type).resolve());
    }
}
