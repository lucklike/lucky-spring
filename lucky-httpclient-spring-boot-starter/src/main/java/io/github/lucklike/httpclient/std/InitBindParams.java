package io.github.lucklike.httpclient.std;

import java.util.List;
import java.util.Map;

/**
 * 用于初始化绑定的参数
 */
public class InitBindParams {
    /**
     * 指定绑定的类型
     */
    private List<Class<?>> bindClasses;

    /**
     * 具体的绑定参数配置
     */
    private Map<String, Object> bindParams;

    /**
     * 获取指定绑定的类型
     *
     * @return 指定绑定的类型
     */
    public List<Class<?>> getBindClasses() {
        return bindClasses;
    }

    /**
     * 设置指定绑定的类型
     *
     * @param bindClasses 指定绑定的类型
     */
    public void setBindClasses(List<Class<?>> bindClasses) {
        this.bindClasses = bindClasses;
    }

    /**
     * 获取具体的绑定参数配置
     *
     * @return 具体的绑定参数配置
     */
    public Map<String, Object> getBindParams() {
        return bindParams;
    }

    /**
     * 设置具体的绑定参数配置
     *
     * @param bindParams 具体的绑定参数配置
     */
    public void setBindParams(Map<String, Object> bindParams) {
        this.bindParams = bindParams;
    }
}
