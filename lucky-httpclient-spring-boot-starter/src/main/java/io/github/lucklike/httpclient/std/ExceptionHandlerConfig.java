package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 异常处理配置类
 */
public class ExceptionHandlerConfig {

    /**
     * 条件表达式
     */
    private String condition;

    /**
     * 异常类型
     */
    private Set<Class<? extends Throwable>> exceptionClasses;

    /**
     * 异常比较算法
     */
    private Compare exceptionCompare = Compare.EQUALS;

    /**
     * 需要执行的命令
     */
    private List<String> running;

    /**
     * 异常返回
     */
    private String result;


    /**
     * 条件表达式
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
     * 可以处理的异常类型
     *
     * @return 异常类型
     */
    public Set<Class<? extends Throwable>> getExceptionClasses() {
        return exceptionClasses;
    }

    /**
     * 设置异常类型
     *
     * @param exceptionClasses 异常类型
     */
    public void setExceptionClasses(Set<Class<? extends Throwable>> exceptionClasses) {
        this.exceptionClasses = exceptionClasses;
    }

    /**
     * 异常比较算法
     *
     * @return 异常比较算法
     */
    public Compare getExceptionCompare() {
        return exceptionCompare;
    }

    /**
     * 设置异常比较算法
     *
     * @param exceptionCompare 异常比较算法
     */
    public void setExceptionCompare(Compare exceptionCompare) {
        this.exceptionCompare = exceptionCompare;
    }

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
     * 是否是有效的配置
     *
     * @return 是否是有效配置
     */
    public boolean effective() {
        return ContainerUtils.isNotEmptyCollection(running) || StringUtils.hasText(result);
    }

    /**
     * 类型限制比较算法
     */
    public enum Compare {
        /**
         * 通过{@link Class#equals(Object)}方法进行比较
         */
        EQUALS,

        /**
         * 通过{@link Class#isAssignableFrom(Class)}方法进行比较
         */
        EXTEND,
    }

}
