package io.github.lucklike.httpclient.factory;

import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;

public class DefaultProxyObjectFactory implements LuckyComponentProxyObjectFactory {

    public final HttpClientProxyObjectFactory httpClientProxyObjectFactory;

    public DefaultProxyObjectFactory(HttpClientProxyObjectFactory httpClientProxyObjectFactory) {
        this.httpClientProxyObjectFactory = httpClientProxyObjectFactory;
    }

    @Override
    public <T> T getProxyObject(Class<T> clazz) {
        return httpClientProxyObjectFactory.getProxyObject(clazz);
    }

    @Override
    public void shutdown() {
        httpClientProxyObjectFactory.shutdown();
    }
}
