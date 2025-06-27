package io.github.lucklike.httpclient.injection;

public interface FieldInjection {

    boolean canInject(Object bean, String beanName, FieldInfo fieldInfo);

    Object getInjectObject(Object bean, String beanName, FieldInfo fieldInfo);

}
