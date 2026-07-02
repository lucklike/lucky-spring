package io.github.lucklike.httpclient.factory;

/**
 * Lucky组件代理对象工厂类
 */
public interface LuckyComponentProxyObjectFactory {

    /**
     * 获取代理对象
     * @param clazz 代理对象Class
     * @return 代理对象
     * @param <T> 代理对象类型泛型
     */
    <T> T getProxyObject(Class<T> clazz);

    /**
     * Shutdown
     */
    void shutdown();


}
