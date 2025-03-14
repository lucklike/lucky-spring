package io.github.lucklike.httpclient.config;


import com.luckyframework.httpclient.proxy.unpack.ParameterConvert;

/**
 * AutoConvert配置对象
 */
public class ParameterConvertConfig {

    /**
     * 注册类型
     */
    private RType type = RType.ADD;

    /**
     * 指定注册位置
     */
    private Integer index;

    /**
     * 用于定位的转换器Class
     */
    private Class<? extends ParameterConvert> indexClass;

    /**
     * 待注册的转换器Class
     */
    private Class<? extends ParameterConvert> clazz;


    /**
     * 获取注册类型
     *
     * @return 注册类型
     */
    public RType getType() {
        return type;
    }

    /**
     * 设置注册类型
     *
     * @param type 注册类型
     */
    public void setType(RType type) {
        this.type = type;
    }

    /**
     * 获取注册位置
     *
     * @return 注册位置
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * 设置注册位置
     *
     * @param index 注册位置
     */
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * 获取用于定位的转换器类型
     *
     * @return 用于定位的转换器类型
     */
    public Class<? extends ParameterConvert> getIndexClass() {
        return indexClass;
    }

    /**
     * 设置用于定位的转换器类型
     *
     * @param indexClass 用于定位的转换器类型
     */
    public void setIndexClass(Class<? extends ParameterConvert> indexClass) {
        this.indexClass = indexClass;
    }

    /**
     * 获取待注册的转换器Class
     *
     * @return 待注册的转换器Class
     */
    public Class<? extends ParameterConvert> getClazz() {
        return clazz;
    }

    /**
     * 设置待注册的转换器Class
     * @param clazz 待注册的转换器Class
     */
    public void setClazz(Class<? extends ParameterConvert> clazz) {
        this.clazz = clazz;
    }
}
