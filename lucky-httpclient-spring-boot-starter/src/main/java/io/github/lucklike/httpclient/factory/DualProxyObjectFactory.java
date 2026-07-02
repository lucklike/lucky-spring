package io.github.lucklike.httpclient.factory;

import com.luckyframework.exception.LuckyInvocationTargetException;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.proxy.ProxyFactory;
import com.luckyframework.reflect.MethodUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 双重代理的对象工厂
 */
public class DualProxyObjectFactory implements LuckyComponentProxyObjectFactory{

    public final HttpClientProxyObjectFactory httpClientProxyObjectFactory;

    public DualProxyObjectFactory(HttpClientProxyObjectFactory httpClientProxyObjectFactory) {
        this.httpClientProxyObjectFactory = httpClientProxyObjectFactory;
    }

    public HttpClientProxyObjectFactory getHttpClientProxyObjectFactory() {
        return httpClientProxyObjectFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxyObject(Class<T> clazz) {
        httpClientProxyObjectFactory.getProxyObject(clazz);
        return (T) ProxyFactory.getCglibProxyObject(clazz, Enhancer::create, new HttpClientProxyMethodInterceptor(clazz));

    }

    public void shutdown() {
        httpClientProxyObjectFactory.shutdown();
    }


    class HttpClientProxyMethodInterceptor implements MethodInterceptor {

        public final Class<?> targetClass;

        HttpClientProxyMethodInterceptor(Class<?> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            try {
                Object httppProxyObject = httpClientProxyObjectFactory.getProxyObject(targetClass);
                return MethodUtils.invoke(httppProxyObject, method, objects);
            } catch (LuckyInvocationTargetException e) {
                throw e.getCause();
            }
        }

    }
}
