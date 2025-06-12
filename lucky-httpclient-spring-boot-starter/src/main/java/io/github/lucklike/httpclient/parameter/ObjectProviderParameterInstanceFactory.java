package io.github.lucklike.httpclient.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;

/**
 * 支持{@link ObjectProvider}类型参数实例工厂
 */
public class ObjectProviderParameterInstanceFactory implements ParameterInstanceFactory {

    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        ResolvableType paramType = parameterInfo.getResolvableType();
        return ObjectProvider.class.isAssignableFrom(paramType.resolve()) && paramType.hasGenerics();
    }

    @Override
    public Object createInstance(ParameterInfo parameterInfo) {
        ResolvableType realType = parameterInfo.getResolvableType().getGeneric(0);
        return ApplicationContextUtils.getBeanProvider(realType.resolve());
    }

}
