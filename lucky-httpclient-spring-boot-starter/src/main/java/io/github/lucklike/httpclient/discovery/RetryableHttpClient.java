package io.github.lucklike.httpclient.discovery;

import com.luckyframework.httpclient.proxy.TAG;
import com.luckyframework.httpclient.proxy.annotations.RetryMeta;
import com.luckyframework.httpclient.proxy.annotations.Retryable;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可重试的HttpClient注解，用于将某个接口声明为httpclient代理接口
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/9/17 00:46
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@HttpClient
@Retryable
public @interface RetryableHttpClient {

    //---------------------------------------------------------------------------
    //                             HttpClient
    //---------------------------------------------------------------------------

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClient.class, attribute = "beanId")
    String beanId() default "";

    /**
     * 同url
     */
    @AliasFor(annotation = HttpClient.class, attribute = "value")
    String value() default "";

    /**
     * 支持SpEL表达式
     * 用于获取URL的表达式
     */
    @AliasFor(annotation = HttpClient.class, attribute = "url")
    String url() default "";

    /**
     * 支持SpEL表达式
     * 服务名称，用于注册中心发现服务
     */
    @AliasFor(annotation = HttpClient.class, attribute = "service")
    String service() default "";

    /**
     * 支持SpEL表达式
     * path，全局路径前缀，请求时会自动加上
     */
    @AliasFor(annotation = HttpClient.class, attribute = "path")
    String path() default "";

    /**
     * 代理模式
     */
    @AliasFor(annotation = HttpClient.class, attribute = "proxyModel")
    ProxyModel proxyModel() default ProxyModel.DEFAULT;

    //---------------------------------------------------------------------------
    //                             Retryable
    //---------------------------------------------------------------------------

    /**
     * 是否开启重试功能
     */
    @AliasFor(annotation = RetryMeta.class, attribute = "enable")
    String enable() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().isEnable()}";

    /**
     * 任务名称
     */
    @AliasFor(annotation = Retryable.class, attribute = "name")
    String nameFormat() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().getNameFormat()}";

    /**
     * 最大重试次数
     */
    @AliasFor(annotation = Retryable.class, attribute = "retryCount")
    String retryCount() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().}";

    /**
     * 重试等待时长
     */
    @AliasFor(annotation = Retryable.class, attribute = "waitMillis")
    String waitMillis() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().getWaitMillis()}";

    /**
     * 最大的重试等待时间
     */
    @AliasFor(annotation = Retryable.class, attribute = "maxWaitMillis")
    String maxWaitMillis() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().getMaxWaitMillis()}";

    /**
     * 最小的重试等待时间
     */
    @AliasFor(annotation = Retryable.class, attribute = "minWaitMillis")
    String minWaitMillis() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().getMinWaitMillis()}";

    /**
     * 延时倍数，下一次等待时间与上一次等待时间的比值
     */
    @AliasFor(annotation = Retryable.class, attribute = "multiplier")
    String multiplier() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().getMultiplier()}";

    /**
     * 需要重试的异常列表
     */
    @AliasFor(annotation = Retryable.class, attribute = "include")
    Class<? extends Throwable>[] include() default Exception.class;

    /**
     * 不需要处理的异常列表
     */
    @AliasFor(annotation = Retryable.class, attribute = "exclude")
    Class<? extends Throwable>[] exclude() default {};

    /**
     * 正常情况下的HTTP响应状态码, 这些状态码以外的状态码均需要进行重试
     */
    @AliasFor(annotation = Retryable.class, attribute = "normalStatus")
    int[] normalStatus() default {};

    /**
     * 异常情况的状态码，出现这些状态码时需要进行重试
     */
    @AliasFor(annotation = Retryable.class, attribute = "exceptionStatus")
    int[] exceptionStatus() default {};

    /**
     * 重试表达式，当该表达式返回true时才有可能进行重试，<b>SpEL表达式部分需要写在#{}中，且表达式的结果必须为boolean类型</b>
     *
     * <pre>
     * SpEL表达式内置参数有：
     *  1.通用内置参数有：
     * root: {
     *      <b>SpEL Env : </b>
     *      {@value TAG#SPRING_ROOT_VAL}
     *      {@value TAG#SPRING_VAL}
     *
     *      <b>Context : </b>
     *      {@value TAG#METHOD_CONTEXT}
     *      {@value TAG#CLASS_CONTEXT}
     *      {@value TAG#CLASS}
     *      {@value TAG#METHOD}
     *      {@value TAG#THIS}
     *      {@value TAG#PARAM_TYPE}
     *      {@value TAG#PN}
     *      {@value TAG#PN_TYPE}
     *      {@value TAG#PARAM_NAME}
     *
     *      <b>Request : </b>
     *      {@value TAG#REQUEST}
     *      {@value TAG#REQUEST_URL}
     *      {@value TAG#REQUEST_METHOD}
     *      {@value TAG#REQUEST_QUERY}
     *      {@value TAG#REQUEST_PATH}
     *      {@value TAG#REQUEST_FORM}
     *      {@value TAG#REQUEST_HEADER}
     *      {@value TAG#REQUEST_COOKIE}
     *
     *      <b>Response : </b>
     *      {@value TAG#RESPONSE}
     *      {@value TAG#RESPONSE_STATUS}
     *      {@value TAG#CONTENT_LENGTH}
     *      {@value TAG#CONTENT_TYPE}
     *      {@value TAG#RESPONSE_HEADER}
     *      {@value TAG#RESPONSE_COOKIE}
     *      {@value TAG#RESPONSE_BODY}
     * }
     * </pre>
     */
    @AliasFor(annotation = Retryable.class, attribute = "retryCount")
    String condition() default "#{@__luckyHttpClientProxyObjectFactoryConfiguration__.getRetry().getCondition()}";

}
