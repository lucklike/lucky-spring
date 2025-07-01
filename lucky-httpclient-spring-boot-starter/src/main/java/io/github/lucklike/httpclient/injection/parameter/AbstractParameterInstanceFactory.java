package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.spel.LazyValue;
import io.github.lucklike.httpclient.injection.WrapType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;

import java.util.Optional;
import java.util.function.Supplier;


/**
 * 1. 参数实例工厂的基本实现，该抽象类将属性注入拆分为如下两种具体的情况
 * <p>
 * 并提供懒加载功能，支持注入懒加载类型
 * <pre>
 *     {@link LazyValue}
 *     {@link Supplier}
 *     {@link ObjectProvider}
 *     {@link Optional}
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/06/27 13:18
 */
public abstract class AbstractParameterInstanceFactory implements ParameterInstanceFactory {

    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        ResolvableType paramType = parameterInfo.getResolvableType();
        return doCanCreateInstance(parameterInfo, WrapType.of(paramType).getTargetType(paramType));
    }

    @Override
    public Object createInstance(ParameterInfo parameterInfo) {
        ResolvableType paramType = parameterInfo.getResolvableType();
        WrapType wrapType = WrapType.of(paramType);
        return wrapType.wrap(() -> doCreateInstance(parameterInfo, wrapType.getTargetType(paramType)));
    }

    /**
     * 是否可以处理该参数信息
     *
     * @param parameterInfo 参数信息
     * @param realType      待转换的真实类型
     * @return 是否可以处理当前参数信息
     */
    public abstract boolean doCanCreateInstance(ParameterInfo parameterInfo, ResolvableType realType);

    /**
     * 使用当前参数信息创建参数实例
     *
     * @param parameterInfo 参数信息
     * @param realType      待转换的真实类型
     * @return 参数实例对象
     */
    public abstract Object doCreateInstance(ParameterInfo parameterInfo, ResolvableType realType);

}
