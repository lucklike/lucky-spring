package io.github.lucklike.httpclient.config;

/**
 * 定位器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/15 02:13
 */
public interface Locator<T> {

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
     * 用于定位位置的Class
     *
     * @return 用于定位位置的Class
     */
    default Class<? extends T> indexClass() {
        return null;
    }
}
