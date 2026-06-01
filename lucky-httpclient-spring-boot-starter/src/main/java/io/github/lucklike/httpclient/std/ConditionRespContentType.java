package io.github.lucklike.httpclient.std;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import org.springframework.core.ResolvableType;

public class ConditionRespContentType {
    /**
     * 条件表达式
     */
    private String condition;

    /**
     * 强制指定响应体Content-Type
     *
     */
    private String responseContentType;

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
     * 强制指定响应体Content-Type
     *
     * @return 响应体Content-Type
     */
    public String getResponseContentType() {
        return responseContentType;
    }

    /**
     * 设置强制指定响应体Content-Type
     *
     * @param responseContentType 响应体Content-Type
     */
    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    /**
     * 是否是有效的配置
     *
     * @return 是否是有效配置
     */
    public boolean effective() {
        return StringUtils.hasText(condition) && StringUtils.hasText(responseContentType);
    }
}
