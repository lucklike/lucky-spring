package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;

/**
 * 参数实例工厂
 */
public interface ParameterInstanceFactory {

    /**
     * 是否可以处理该参数信息
     *
     * @param parameterInfo 参数信息
     * @return 是否可以处理当前参数信息
     */
    boolean canCreateInstance(ParameterInfo parameterInfo);

    /**
     * 使用当前参数信息创建参数实例
     *
     * @param parameterInfo 参数信息
     * @return 参数实例对象
     */
    Object createInstance(ParameterInfo parameterInfo);
}
