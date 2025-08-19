package io.github.lucklike.httpclient.config;

import com.luckyframework.common.StringUtils;

/**
 * 类型简写
 */
public class TypeAbbreviation {

    /**
     * 缩写
     */
    private String abbreviation;

    /**
     * 类型
     */
    private Class<?> clazz;

    /**
     * 是否自动注册该类型的数组类型
     */
    private boolean autoAddArrayType = false;


    /**
     * 获取类型别名
     *
     * @return 类型别名
     */
    public String getAbbreviation() {
        return StringUtils.hasText(abbreviation) ? abbreviation : clazz.getSimpleName();
    }

    /**
     * 设置类型别名
     *
     * @param abbreviation 类型别名
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * 设置类型
     *
     * @param clazz 类型
     */
    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
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
