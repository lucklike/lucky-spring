package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.async.Model;
import io.github.lucklike.httpclient.config.impl.LazyThreadPoolParam;

import java.util.Map;

/**
 * HTTP异步线程池配置
 */
public class HttpAsyncThreadPoolConfiguration extends LazyThreadPoolParam {

    /**
     * HTTP请求的异步模型
     */
    private Model asyncModel;

    /**
     * 默认异步执行器的最大并发数，小于0时表示不限制并发数
     */
    private int defaultExecutorConcurrency = -1;

    /**
     * 备用线程池
     */
    private Map<String, LazyThreadPoolParam> alternative;


    //------------------------------------------------------------------------------------------------
    //                                Setter methods
    //------------------------------------------------------------------------------------------------

    /**
     * 设置HTTP异步模型
     *
     * @param asyncModel 异步模型
     */
    public void setAsyncModel(Model asyncModel) {
        this.asyncModel = asyncModel;
    }

    /**
     * 设置默认异步执行器的最大并发数，小于0表示不限制并发数
     *
     * @param defaultExecutorConcurrency 默认异步执行器的最大并发数
     */
    public void setDefaultExecutorConcurrency(int defaultExecutorConcurrency) {
        this.defaultExecutorConcurrency = defaultExecutorConcurrency;
    }


    /**
     * 设置备选线程池参数
     *
     * @param alternative 备选线程池参数
     */
    public void setAlternative(Map<String, LazyThreadPoolParam> alternative) {
        this.alternative = alternative;
    }

    //------------------------------------------------------------------------------------------------
    //                                Getter methods
    //------------------------------------------------------------------------------------------------

    /**
     * 获取HTTP异步模型
     *
     * @return 异步模型
     */
    public Model getAsyncModel() {
        return asyncModel;
    }

    /**
     * 获取默认异步执行器的最大并发数
     *
     * @return 默认异步执行器的最大并发数
     */
    public int getDefaultExecutorConcurrency() {
        return defaultExecutorConcurrency;
    }

    /**
     * 获取备选线程池参数
     *
     * @return 备选线程池参数
     */
    public Map<String, LazyThreadPoolParam> getAlternative() {
        return alternative;
    }

}
