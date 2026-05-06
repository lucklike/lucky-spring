package io.github.lucklike.httpclient.annotation;

import com.luckyframework.httpclient.proxy.annotations.JavaParam;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.StaticParam;
import com.luckyframework.httpclient.proxy.setter.JavaFlatBeanParameterSetter;
import com.luckyframework.reflect.Combination;
import io.github.lucklike.httpclient.statics.EnvironmentJavaObjectResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 从环境变量中提取JAVA对象请求体的解析器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/11/19 18:30
 * @see JavaParam
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@StaticParam(
        setter = @ObjectGenerate(JavaFlatBeanParameterSetter.class),
        resolver = @ObjectGenerate(EnvironmentJavaObjectResolver.class)
)
@Combination(StaticParam.class)
public @interface EnvironmentJava {

    /**
     * 数组类型
     */
    String ARRAY = "#{type_of(T(java.util.List), T(java.util.LinkedHashMap))}";

    /**
     * Map 类型
     */
    String MAP = "#{type_of(T(java.util.LinkedHashMap), T(String), T(Object))}";

    /**
     * 指定环境变量key
     */
    String value();

    /**
     * 绑定到的具体类型，默认为{@link java.util.LinkedHashMap}
     */
    String type() default MAP;

    /**
     * 是否允许配置不存在
     */
    boolean allowConfigNotExist() default true;

}
