package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.interceptor.PriorityConstant;

/**
 * 重定向相关的配置
 */
public class RedirectConfiguration {

    /**
     * 是否开启自动重定向
     */
    private boolean enable;

    /**
     * 需要重定向的状态码，默认重定向状态码：301, 302, 303, 304, 307, 308
     */
    private Integer[] status;

    /**
     * 需要重定向的条件，此处支持SpEL表达式
     */
    private String condition;

    /**
     * 重定向地址表达式，此处支持SpEL表达式，默认值为：#{$respHeader$.Location}
     */
    private String location;

    /**
     * 最大重定向次数，默认值为：5
     */
    private Integer maxCount = 5;

    /**
     * 重定向拦截器的优先级，默认{@value PriorityConstant#REDIRECT_PRIORITY}
     */
    private Integer priority = PriorityConstant.REDIRECT_PRIORITY;


    /**
     * 设置是否开启自动重定向功能
     *
     * @param enable 是否开启自动重定向功能
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 设置需要重定向的状态码
     *
     * @param status 需要重定向的状态码
     */
    public void setStatus(Integer[] status) {
        this.status = status;
    }

    /**
     * 设置需要重定向的条件，此处支持SpEL表达式
     *
     * @param condition 需要重定向的条件，此处支持SpEL表达式
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * 设置重定向地址获取表达式，默认值：#{$respHeader$.Location}
     *
     * @param location 重定向地址获取表达式
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 设置重定向拦截器的优先级
     *
     * @param priority 重定向拦截器的优先级
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * 设置允许的最大重定向次数
     *
     * @param maxCount 允许的最大重定向次数
     */
    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    /**
     * 是否开启了自动重定向功能
     *
     * @return 是否开启了自动重定向功能
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * 获取需要重定向的状态码
     *
     * @return 需要重定向的状态码
     */
    public Integer[] getStatus() {
        return status;
    }

    /**
     * 获取需要重定向的条件表达式
     *
     * @return 需要重定向的条件表达式
     */
    public String getCondition() {
        return condition;
    }

    /**
     * 获取需要重定向的地址表达式，默认值：#{$respHeader$.Location}
     *
     * @return 需要重定向的地址表达式
     */
    public String getLocation() {
        return location;
    }

    /**
     * 获取重定向拦截器的优先级
     *
     * @return 重定向拦截器的优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 获取允许的最大重定向次数
     *
     * @return 允许的最大重定向次数
     */
    public Integer getMaxCount() {
        return maxCount;
    }
}
