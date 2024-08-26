package io.github.lucklike.httpclient.annotation;

import javax.xml.bind.annotation.XmlType;

import static io.github.lucklike.httpclient.Constant.CGLIB_PROXY_METHOD;
import static io.github.lucklike.httpclient.Constant.JDK_PROXY_METHOD;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2024/8/27 00:48
 */
public enum ProxyModel {

    /**
     * JDK动态代理
     */
    JDK(JDK_PROXY_METHOD),

    /**
     * Cglib动态代理
     */
    CGLIB(CGLIB_PROXY_METHOD),

    /**
     * 默认代理
     */
    DEFAULT("");


    private final String proxyMethod;

    ProxyModel(String proxyMethod) {
        this.proxyMethod = proxyMethod;
    }

    public String getProxyMethod() {
        return proxyMethod;
    }
}
