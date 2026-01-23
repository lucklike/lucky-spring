package io.github.lucklike.httpclient.convert;

import com.luckyframework.httpclient.proxy.SpELVariableNote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 初始化绑定注解，将环境变量中的某段配置作为初始化值绑定到某个对象上
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InitBind {

    /**
     * 需要绑定的配置前缀集合，支持配置多个前缀，如果配置多个则会按照配置的顺序进行绑定
     * 支持 SpEL 表达式
     *
     * @see SpELVariableNote
     */
    String[] value();

    /**
     * 指定生效的类型<br/>
     * <b>该注解标注在类、方法上时必须配置，被标注的类型必须是一个POJO类型</b>
     */
    Class<?>[] types() default {};

    /**
     * 是否允许配置不存在
     */
    boolean allowConfigNotExist() default true;

}
