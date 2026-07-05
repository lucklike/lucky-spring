package io.github.lucklike.httpclient.factory;

import com.luckyframework.exception.LuckyInvocationTargetException;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.proxy.ProxyFactory;
import com.luckyframework.reflect.MethodUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * 双重代理的对象工厂
 */
@SuppressWarnings("unchecked")
public class DualProxyObjectFactory implements LuckyComponentProxyObjectFactory{

    public final HttpClientProxyObjectFactory httpClientProxyObjectFactory;

    public DualProxyObjectFactory(HttpClientProxyObjectFactory httpClientProxyObjectFactory) {
        this.httpClientProxyObjectFactory = httpClientProxyObjectFactory;
    }

    public HttpClientProxyObjectFactory getHttpClientProxyObjectFactory() {
        return httpClientProxyObjectFactory;
    }


    public <T> T getProxyObject(Class<T> clazz) {
        httpClientProxyObjectFactory.getProxyObject(clazz);
        return (T) ProxyFactory.getCglibProxyObject(clazz, Enhancer::create, new HttpClientProxyMethodInterceptor(() -> httpClientProxyObjectFactory.getProxyObject(clazz)));
    }

    @Override
    public <T> T getCglibProxyObject(Class<T> clazz) {
        httpClientProxyObjectFactory.getCglibProxyObject(clazz);
        return (T) ProxyFactory.getCglibProxyObject(clazz, Enhancer::create, new HttpClientProxyMethodInterceptor(() -> httpClientProxyObjectFactory.getCglibProxyObject(clazz)));
    }

    @Override
    public <T> T getJdkProxyObject(Class<T> clazz) {
        httpClientProxyObjectFactory.getJdkProxyObject(clazz);
        return (T) ProxyFactory.getCglibProxyObject(clazz, Enhancer::create, new HttpClientProxyMethodInterceptor(() -> httpClientProxyObjectFactory.getJdkProxyObject(clazz)));
    }

    public void shutdown() {
        httpClientProxyObjectFactory.shutdown();
    }


    static class HttpClientProxyMethodInterceptor implements MethodInterceptor {
        public final Supplier<Object> proxyObjectSupplier;

        HttpClientProxyMethodInterceptor(Supplier<Object> proxyObjectSupplier) {
            this.proxyObjectSupplier = proxyObjectSupplier;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            try {
                return MethodUtils.invoke(proxyObjectSupplier.get(), method, objects);
            } catch (LuckyInvocationTargetException e) {
                throw e.getCause();
            }
        }

    }
}
