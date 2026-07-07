package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;

import java.util.List;

/**
 * 异常处理配置类
 */
public class ExceptionHandlerConfig {

    /**
     * 需要执行的命令
     */
    protected List<String> running;

    /**
     * 异常返回
     */
    protected String result;

    /**
     * 转成自定义异常
     */
    protected String exception;

    /**
     * 需要执行的命令，SpEL表达式
     *
     * @return 需要执行的命令
     */
    public List<String> getRunning() {
        return running;
    }

    /**
     * 设置需要执行的命令
     *
     * @param running 需要执行的命令
     */
    public void setRunning(List<String> running) {
        this.running = running;
    }

    /**
     * 异常返回结果
     *
     * @return 异常返回结果
     */
    public String getResult() {
        return result;
    }

    /**
     * 设置异常返回结果
     *
     * @param result 异常返回结果
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * 自定义异常
     *
     * @return 自定义异常
     */
    public String getException() {
        return exception;
    }

    /**
     * 自定义异常
     *
     * @param exception 自定义异常
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * 是否是有效的配置
     *
     * @return 是否是有效配置
     */
    public boolean effective() {
        return ContainerUtils.isNotEmptyCollection(running) || StringUtils.hasText(result) || StringUtils.hasText(exception);
    }
}
