package io.github.lucklike.httpclient.config.httpexecutor;

import io.github.lucklike.httpclient.config.HttpExecutorFactory;
import io.github.lucklike.httpclient.config.impl.HttpExecutorEnum;

import java.net.HttpURLConnection;

/**
 * 全局通用的HTTP执行器配置
 */
public class GlobalConfiguration extends CommonConfiguration {

    /**
     * 指定使用的HTTP执行器Bean的名称
     */
    private String executorBean;

    /**
     * Http请求执行器工厂
     */
    private HttpExecutorFactory executorFactory;

    /**
     * 使用执行器枚举来指定执行器
     */
    private HttpExecutorEnum executor;


    //------------------------------------------------------------------------------------------------
    //                                Setter methods
    //------------------------------------------------------------------------------------------------


    /**
     * 设置{@link HttpExecutorFactory HTTP执行器工厂}
     *
     * @param executorFactory HTTP执行器工厂
     */
    public void setExecutorFactory(HttpExecutorFactory executorFactory) {
        this.executorFactory = executorFactory;
    }

    /**
     * 使用执行器枚举来指定执行器<br/>
     * {@link HttpExecutorEnum#JDK JDK}: 使用JDK的{@link HttpURLConnection}实现的执行器。<br/>
     * {@link HttpExecutorEnum#OKHTTP OK_HTTP}: 使用OkHttp3实现的执行器。<br/>
     * {@link HttpExecutorEnum#HTTP_CLIENT HTTP_CLIENT}: 使用Apache HttpClient实现的执行器。<br/>
     *
     * @param executor 执行器枚举
     */
    public void setExecutor(HttpExecutorEnum executor) {
        this.executor = executor;
    }

    /**
     * 设置使用HTTP执行器的SpringBean的名称
     *
     * @param executorBean HTTP执行器的SpringBean的名称
     */
    public void setExecutorBean(String executorBean) {
        this.executorBean = executorBean;
    }


    //------------------------------------------------------------------------------------------------
    //                                Getter methods
    //------------------------------------------------------------------------------------------------


    /**
     * 获取{@link HttpExecutorFactory HTTP请求执行器工厂}
     *
     * @return HTTP请求执行器工厂
     */
    public HttpExecutorFactory getExecutorFactory() {
        return executorFactory;
    }

    /**
     * 获取执行器对应的执行器枚举
     *
     * @return 执行器枚举
     */
    public HttpExecutorEnum getExecutor() {
        return executor;
    }

    /**
     * HTTP执行器的SpringBean的名称
     *
     * @return HTTP执行器的SpringBean的名称
     */
    public String getExecutorBean() {
        return executorBean;
    }


}