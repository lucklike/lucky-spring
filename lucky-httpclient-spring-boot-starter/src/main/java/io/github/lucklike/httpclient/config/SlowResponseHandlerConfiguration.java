package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.slow.AbstractSlowResponseHandler;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 慢响应处理相关配置
 */
public class SlowResponseHandlerConfiguration {

    /**
     * 定义慢响应最大时间
     */
    private long slowTime;

    /**
     * 慢响应处理器
     */
    @NestedConfigurationProperty
    private SimpleGenerateEntry<? extends AbstractSlowResponseHandler> handler;

    /**
     * 获取定义的慢响应最大时间
     *
     * @return 定义的慢响应最大时间
     */
    public long getSlowTime() {
        return slowTime;
    }

    /**
     * 设置定义的慢响应最大时间
     *
     * @param slowTime 定义的慢响应最大时间
     */
    public void setSlowTime(long slowTime) {
        this.slowTime = slowTime;
    }

    /**
     * 获取慢响应处理器
     *
     * @return 慢响应处理器
     */
    public SimpleGenerateEntry<? extends AbstractSlowResponseHandler> getHandler() {
        return handler;
    }

    /**
     * 设置慢响应处理器
     *
     * @param handler 慢响应处理器
     */
    public void setHandler(SimpleGenerateEntry<? extends AbstractSlowResponseHandler> handler) {
        this.handler = handler;
    }
}
