package io.github.lucklike.httpclient.factory;

/**
 * Lucky组件代理对象工厂类
 */
public interface LuckyComponentProxyObjectFactory {

    /**
     * 获取代理对象
     *
     * @param clazz 代理对象Class
     * @param <T>   代理对象类型泛型
     * @return 代理对象
     */
    <T> T getProxyObject(Class<T> clazz);

    /**
     * 获取 Cglib 代理对象
     *
     * @param clazz 代理对象Class
     * @param <T>   代理对象类型泛型
     * @return 代理对象
     */
    <T> T getCglibProxyObject(Class<T> clazz);

    /**
     * 获取 Jdk 代理对象
     *
     * @param clazz 代理对象Class
     * @param <T>   代理对象类型泛型
     * @return 代理对象
     */
    <T> T getJdkProxyObject(Class<T> clazz);

    /**
     * Shutdown
     */
    void shutdown();


}
