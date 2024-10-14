package io.github.lucklike.httpclient.annotation;

import static io.github.lucklike.httpclient.Constant.AUTO_METHOD;
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
     * 自动选择代理模式
     * <pre>
     *     1.代理类为接口时使用JDK代理
     *     2.代理类为非接口时使用Cglib代理
     * </pre>
     */
    AUTO(AUTO_METHOD),

    /**
     * 跟随全局
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
