package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;

import java.util.Set;

/**
 * 支持条件的异常处理器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/7/6 11:45
 */
public class ConditionExceptionHandlerConfig extends ExceptionHandlerConfig {

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
     * 是否是有效的配置
     *
     * @return 是否是有效配置
     */
    public boolean effective() {
        return (StringUtils.hasText(condition) || ContainerUtils.isNotEmptyCollection(exceptionClasses)) && super.effective();
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
