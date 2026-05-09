package io.github.lucklike.httpclient.std;

import org.springframework.core.io.Resource;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/10 01:21
 */
public class ConditionBody {
    /**
     * 条件表达式
     */
    private String condition;

    /**
     * 请求体表达式，只支持{@link Resource}、{@link String}类型结果
     */
    private String body;

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
     * 获取请求体表达式，只支持{@link Resource}、{@link String}类型结果
     *
     * @return 请求体表达式，只支持{@link Resource}、{@link String}类型结果
     */
    public String getBody() {
        return body;
    }

    /**
     * 设置请求体表达式，只支持{@link Resource}、{@link String}类型结果
     *
     * @param body 请求体表达式，只支持{@link Resource}、{@link String}类型结果
     */
    public void setBody(String body) {
        this.body = body;
    }
}
