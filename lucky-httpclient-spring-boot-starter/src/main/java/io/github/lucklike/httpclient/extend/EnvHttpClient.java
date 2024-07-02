package io.github.lucklike.httpclient.extend;

import com.luckyframework.httpclient.proxy.annotations.HttpRequest;
import com.luckyframework.httpclient.proxy.annotations.InterceptorRegister;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.ResultConvert;
import com.luckyframework.httpclient.proxy.annotations.StaticParam;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.reflect.Combination;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明式Http客户端的注解，支持在读取Spring环境变量中配置的请求与响应转换配置
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:06
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@HttpRequest
@InterceptorRegister(
        intercept = @ObjectGenerate(clazz = EnvironmentApiCallable.class, scope = Scope.CLASS),
        priority = 9000
)
@ResultConvert(
        convert = @ObjectGenerate(clazz = EnvironmentApiCallable.class, scope = Scope.CLASS))
@StaticParam(
        resolver = @ObjectGenerate(clazz = EnvironmentApiCallable.class, scope = Scope.CLASS),
        setter = @ObjectGenerate(EnvironmentApiParameterSetter.class)
)
@HttpClientComponent
@Combination({StaticParam.class, InterceptorRegister.class})
public @interface EnvHttpClient {


    String prefix() default "";

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClientComponent.class, attribute = "name")
    String name() default "";
}
