package io.github.lucklike.httpclient.injection;

/**
 * 属性注入器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/06/27 12:06
 * @see LuckyAutoInjectionBeanPostProcessor
 */
public interface PropertyInjection {

    /**
     * 是否可以为当前Bean对象的某个属性注入值
     *
     * @param bean         当前Bean对象
     * @param beanName     当前Bean对象的名称
     * @param propertyInfo 带注入的属性信息
     * @return 是否可以注入
     */
    boolean canInject(Object bean, String beanName, PropertyInfo propertyInfo);


    /**
     * 返回当前Bean对象对应属性的注入值
     *
     * @param bean         当前Bean对象
     * @param beanName     当前Bean对象的名称
     * @param propertyInfo 带注入的属性信息
     * @return 当前Bean对象对应属性的注入值
     */
    Object getInjectObject(Object bean, String beanName, PropertyInfo propertyInfo);

}
