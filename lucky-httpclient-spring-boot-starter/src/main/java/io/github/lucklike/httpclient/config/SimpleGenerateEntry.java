package io.github.lucklike.httpclient.config;

/**
 * 简单生成器实体
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/3/9 16:37
 */
public class SimpleGenerateEntry<T> {

    /**
     * 生成器需要生成对象的Class
     */
    private Class<T> type;

    /**
     * 对象在SpringIOC容器中的名称
     */
    private String beanName = "";

    /**
     * 生成对象的Class
     *
     * @return 生成对象的Class
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * 设置生成对象的Class
     *
     * @param type 生成对象的Class
     */
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * 获取对象在SpringIOC容器中的名称
     *
     * @return 对象在SpringIOC容器中的名称
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * 对象在SpringIOC容器中的名称
     *
     * @param beanName 对象在SpringIOC容器中的名称
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
