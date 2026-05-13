package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.configapi.MultipartFormData;

/**
 * 支持条件的multipart/form-data类型参数
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/10 00:57
 */
public class ConditionMultipartFormData extends MultipartFormData {
    /**
     * 条件表达式
     */
    private String condition;

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
     * @param condition 设置条件表达式
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * 是否是有效的配置
     *
     * @return 是否是有效配置
     */
    public boolean effective() {
        return StringUtils.hasText(condition) &&
                (ContainerUtils.isNotEmptyMap(getTxt()) || ContainerUtils.isNotEmptyMap(getFile()));
    }
}
