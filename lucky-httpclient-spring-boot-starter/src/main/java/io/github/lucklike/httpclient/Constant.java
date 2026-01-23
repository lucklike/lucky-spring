package io.github.lucklike.httpclient;

/**
 * 常量
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 04:15
 */
public class Constant {


    /**
     * Spring函数空间
     */
    public final static String SPRING_FUNCTION_SPACE = "__Func::Spring__";

    /**
     * 简单HTTP执行器
     */
    public final static String SIMPLE_HTTP_EXECUTOR = "__Func::SimpleHttpExecutor__";


    public static final String PROXY_FACTORY_BEAN_NAME = "__luckyHttpClientProxyFactory__";
    public static final String PROXY_FACTORY_CONFIG_BEAN_NAME = "__luckyHttpClientProxyObjectFactoryConfiguration__";
    public static final String DEFAULT_JDK_EXECUTOR_BEAN_NAME = "__luckyJdkHttpExecutor__";
    public static final String DEFAULT_HTTP_CLIENT_EXECUTOR_BEAN_NAME = "__luckyApacheHttpExecutor__";
    public static final String DEFAULT_HTTP_CLIENT_V5_EXECUTOR_BEAN_NAME = "__luckyApacheHttpV5Executor__";
    public static final String DEFAULT_OKHTTP_EXECUTOR_BEAN_NAME = "__luckyOkHttpExecutor__";
    public static final String DEFAULT_VALIDATION_PLUGIN_BEAN_NAME = "__validationPlugin__";


    public static final String INIT_BIND_PARAMETER_CONVERT = "__initBindParameterConvert__";


    public static final String DESTROY_METHOD = "shutdown";
    public static final String JDK_PROXY_METHOD = "getJdkProxyObject";
    public static final String CGLIB_PROXY_METHOD = "getCglibProxyObject";
    public static final String AUTO_METHOD = "getProxyObject";

    public static final String SPRING_ENV_CONFIG_SOURCE = "__springEnvConfigSource__";

}
