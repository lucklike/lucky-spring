package io.github.lucklike.httpclient.config;

import com.luckyframework.common.StringUtils;

/**
 * 类型别名
 */
public class TypeAlias {

    /**
     * 别名
     */
    private String alias;

    /**
     * 类型
     */
    private Class<?> type;

    /**
     * 是否自动注册该类型的数组类型
     */
    private boolean autoAddArrayType = false;


    /**
     * 获取类型别名
     *
     * @return 类型别名
     */
    public String getAlias() {
        return StringUtils.hasText(alias) ? alias : type.getSimpleName();
    }

    /**
     * 设置类型别名
     *
     * @param alias 类型别名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * 是否注册当前类型的数组类型
     *
     * @return 是否注册当前类型的数组类型
     */
    public boolean isAutoAddArrayType() {
        return autoAddArrayType;
    }

    /**
     * 设置是否注册当前类型的数组类型
     *
     * @param autoAddArrayType 是否注册当前类型的数组类型
     */
    public void setAutoAddArrayType(boolean autoAddArrayType) {
        this.autoAddArrayType = autoAddArrayType;
    }
}
