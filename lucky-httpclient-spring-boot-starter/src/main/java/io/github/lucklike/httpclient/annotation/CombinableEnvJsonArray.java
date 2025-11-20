package io.github.lucklike.httpclient.annotation;

import com.luckyframework.httpclient.proxy.annotations.CombineJson;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.StaticParam;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.setter.JsonArrayBodyFactoryParameterSetter;
import com.luckyframework.reflect.Combination;
import io.github.lucklike.httpclient.statics.EnvironmentJsonArrayResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从环境变量中提取JSON对象请求体的解析器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/11/19 18:30
 * @see CombineJson
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@StaticParam(
        setter = @ObjectGenerate(clazz = JsonArrayBodyFactoryParameterSetter.class, scope = Scope.METHOD),
        resolver = @ObjectGenerate(EnvironmentJsonArrayResolver.class)
)
@Combination(StaticParam.class)
public @interface CombinableEnvJsonArray {


    /**
     * 指定环境变量key
     */
    String value();

    /**
     * 元素类型
     */
    Class<?> elementClass();

    /**
     * 是否允许配置不存在
     */
    boolean allowConfigNotExist() default true;

    /**
     * 数组前缀
     */
    String prefix() default "\\$";
}
