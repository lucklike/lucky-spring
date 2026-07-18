package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.proxy.annotations.ExceptionHandle;
import com.luckyframework.httpclient.proxy.annotations.HttpRequest;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.RespConvert;
import com.luckyframework.httpclient.proxy.configapi.ApiConfig;
import com.luckyframework.httpclient.proxy.generator.GeneratedResponseJavaBeanFunction;
import com.luckyframework.httpclient.proxy.mock.Mock;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import io.github.lucklike.httpclient.discovery.HttpClient;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标准的HTTP客户端
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiConfig
@HttpRequest
@HttpClient(urlFunc = "__std_http_server_url__", serviceFunc = "__std_http_service_name__")
@Mock(enableFunc = "__std_mock_enable__", mockFunc = "__std_mock_result__")
@RespConvert(metaTypeFunc = "__std_response_meta_type__", resultFunc = "__std_result_convert__", respContentTypeFunc = "__std_response_content_type__")
@ExceptionHandle(conditionFunc = "__std_enable_exception_handler__", handleFunc = "__std_exception_handler__", exceptions = Throwable.class)
@SpELImport({GeneratedResponseJavaBeanFunction.class, StdHttpClientFunction.class})
public @interface StdHttpClient {

    /**
     * 配置ID，配置ID相同的接口共享同一套配置
     */
    @AliasFor(annotation = ApiConfig.class, attribute = "value")
    String configId() default "";

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClient.class, attribute = "beanId")
    String beanId() default "";

    /**
     * 支持SpEL表达式
     * path，全局路径前缀，请求时会自动加上
     */
    @AliasFor(annotation = HttpClient.class, attribute = "path")
    String path() default "";

    /**
     * 生命周期管理器对象的 Class
     */
    Class<? extends LifeCycleManager> lifecycle() default StandardLifeCycleManager.class;

    /**
     * 生命周期管理器对象的 Bean 名称
     */
    String lifecycleBean() default "";

    /**
     * 生命周期管理器对象生成器对象
     */
    ObjectGenerate lifecycleGenerate() default @ObjectGenerate(LifeCycleManager.class);
}
