package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.meta.CookieStore;
import com.luckyframework.httpclient.proxy.interceptor.PriorityConstant;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Cookie管理器相关配置
 */
public class CookieManageConfiguration {

    /**
     * 是否开启Cookie管理功能
     */
    private boolean enable;

    /**
     * CookieStore生成器
     */
    @NestedConfigurationProperty
    private SimpleGenerateEntry<CookieStore> cookieStore;

    /**
     * Cookie管理器拦截器的优先级，默认{@value  PriorityConstant#COOKIE_MANAGE_PRIORITY}
     */
    private Integer priority = PriorityConstant.COOKIE_MANAGE_PRIORITY;


    /**
     * 设置是否开启Cookie管理功能
     *
     * @param enable 是否开启Cookie管理功能
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 设置Cookie管理器拦截器的优先级
     *
     * @param priority Cookie管理器拦截器的优先级
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * 设置CookieStore对象生成器
     *
     * @param cookieStore CookieStore对象生成器
     */
    public void setCookieStore(SimpleGenerateEntry<CookieStore> cookieStore) {
        this.cookieStore = cookieStore;
    }

    /**
     * 是否开启Cookie管理功能
     *
     * @return 是否开启Cookie管理功能
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * 获取Cookie管理器拦截器的优先级
     *
     * @return Cookie管理器拦截器的优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 获取CookieStore对象生成器
     *
     * @return CookieStore对象生成器
     */
    public SimpleGenerateEntry<CookieStore> getCookieStore() {
        return cookieStore;
    }

}
