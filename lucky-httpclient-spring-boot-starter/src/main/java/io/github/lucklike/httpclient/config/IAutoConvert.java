package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.meta.Response;

/**
 * 用于指定注册类型的接口
 */
public interface IAutoConvert extends Response.AutoConvert {

    /**
     * 指定注册类型
     *
     * @return 注册类型
     */
    default RType rType() {
        return RType.ADD;
    }

    /**
     * 指定注册位置
     *
     * @return 注册位置
     */
    default Integer index() {
        return null;
    }

    /**
     * 用于定位注册位置的转换类Class
     *
     * @return 用于定位注册位置的转换类Class
     */
    default Class<? extends Response.AutoConvert> indexClass() {
        return null;
    }
}
