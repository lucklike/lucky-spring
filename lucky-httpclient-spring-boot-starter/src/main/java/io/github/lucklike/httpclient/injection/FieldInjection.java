package io.github.lucklike.httpclient.injection;

public abstract interface FieldInjection {

    boolean canInject(Object bean, String beanName, FieldInfo fieldInfo);

    Object getInjectObject(Object bean, String beanName, FieldInfo fieldInfo);

}
