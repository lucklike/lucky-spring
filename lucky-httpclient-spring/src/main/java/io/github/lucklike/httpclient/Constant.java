package io.github.lucklike.httpclient;

/**
 * 常量
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 04:15
 */
public class Constant {

    public static final String PROXY_FACTORY_BEAN_NAME = "luckyHttpClientProxyFactory";
    public static final String DESTROY_METHOD = "shutdown";

    public static final String JDK_PROXY_METHOD = "getJdkProxyObject";
    public static final String CGLIB_PROXY_METHOD = "getCglibProxyObject";

}
