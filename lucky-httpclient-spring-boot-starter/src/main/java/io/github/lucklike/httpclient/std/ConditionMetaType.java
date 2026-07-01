package io.github.lucklike.httpclient.std;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import org.springframework.core.ResolvableType;

public class ConditionMetaType {
    /**
     * 条件表达式
     */
    private String condition;

    /**
     * 转化元类型表达式，表达式结果必须为{@link ResolvableType}类型
     *
     * @see CommonFunctions#typeOf(Object, Object...)
     */
    private String metaType;

    /**
     * 获取条件表达式
     *
     * @return 条件表达式
     */
    public String getCondition() {
        return condition;
    }

    /**
     * 设置条件表达式
     *
     * @param condition 条件表达式
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * 获取转化元类型表达式，表达式结果必须为{@link ResolvableType}类型
     *
     * @return 转化元类型表达式
     * @see CommonFunctions#typeOf(Object, Object...)
     */
    public String getMetaType() {
        return metaType;
    }

    /**
     * 设置转化元类型表达式，表达式结果必须为{@link ResolvableType}类型
     *
     * @param metaType 转化元类型表达式
     * @see CommonFunctions#typeOf(Object, Object...)
     */
    public void setMetaType(String metaType) {
        this.metaType = metaType;
    }

    /**
     * 是否是有效的配置
     *
     * @return 是否是有效配置
     */
    public boolean effective() {
        return StringUtils.hasText(condition) && StringUtils.hasText(metaType);
    }
}
