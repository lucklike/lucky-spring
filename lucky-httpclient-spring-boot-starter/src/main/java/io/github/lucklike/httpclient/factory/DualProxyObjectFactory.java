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
public class DualProxyObjectFactory implements LuckyComponentProxyObjectFactory {

    public final Supplier<HttpClientProxyObjectFactory> httpClientProxyObjectFactorySupplier;
    public HttpClientProxyObjectFactory httpClientProxyObjectFactory;

    public DualProxyObjectFactory(Supplier<HttpClientProxyObjectFactory> httpClientProxyObjectFactorySupplier) {
        this.httpClientProxyObjectFactorySupplier = httpClientProxyObjectFactorySupplier;
    }

    public synchronized HttpClientProxyObjectFactory getHttpClientProxyObjectFactory() {
        if (httpClientProxyObjectFactory == null) {
            httpClientProxyObjectFactory = httpClientProxyObjectFactorySupplier.get();
        }
        return httpClientProxyObjectFactory;
    }

    public synchronized void clearHttpClientProxyObjectFactoryInstance(boolean needShutdown) {
        if  (needShutdown && httpClientProxyObjectFactory != null) {
            httpClientProxyObjectFactory.shutdown();
        }
        httpClientProxyObjectFactory = null;
    }


    public <T> T getProxyObject(Class<T> clazz) {
        getHttpClientProxyObjectFactory().getProxyObject(clazz);
        return (T) ProxyFactory.getCglibProxyObject(clazz, Enhancer::create, new HttpClientProxyMethodInterceptor(() -> getHttpClientProxyObjectFactory().getProxyObject(clazz)));
    }

    @Override
    public <T> T getCglibProxyObject(Class<T> clazz) {
        getHttpClientProxyObjectFactory().getCglibProxyObject(clazz);
        return (T) ProxyFactory.getCglibProxyObject(clazz, Enhancer::create, new HttpClientProxyMethodInterceptor(() -> getHttpClientProxyObjectFactory().getCglibProxyObject(clazz)));
    }

    @Override
    public <T> T getJdkProxyObject(Class<T> clazz) {
        getHttpClientProxyObjectFactory().getJdkProxyObject(clazz);
        return (T) ProxyFactory.getCglibProxyObject(clazz, Enhancer::create, new HttpClientProxyMethodInterceptor(() -> getHttpClientProxyObjectFactory().getJdkProxyObject(clazz)));
    }

    public void shutdown() {
        getHttpClientProxyObjectFactory().shutdown();
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
