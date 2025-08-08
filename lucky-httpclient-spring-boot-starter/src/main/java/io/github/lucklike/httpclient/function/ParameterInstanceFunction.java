package io.github.lucklike.httpclient.function;

import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.InternalVarName;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.annotation.AllowNull;
import io.github.lucklike.httpclient.injection.WrapType;
import io.github.lucklike.httpclient.injection.parameter.ParameterInstanceFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;

import java.util.Iterator;
import java.util.function.Supplier;

import static com.luckyframework.httpclient.proxy.spel.InternalVarName.__$PARAMETER_INSTANCE_FUNCTION$__;

/**
 * 提供{@link InternalVarName#__$PARAMETER_INSTANCE_FUNCTION$__}函数实现
 */
public class ParameterInstanceFunction {


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
}
