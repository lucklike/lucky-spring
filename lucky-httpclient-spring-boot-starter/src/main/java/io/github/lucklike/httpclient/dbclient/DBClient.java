package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.httpclient.proxy.annotations.CustomizedProtocol;
import com.luckyframework.httpclient.proxy.plugin.Plugin;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import io.github.lucklike.httpclient.annotation.LuckyComponent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库客户端，基于{@link NamedParameterJdbcTemplate}实现的数据库客户端
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 03:03
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@LuckyComponent
@CustomizedProtocol
@SpELImport(SQLFunctions.class)
@Plugin(pluginClass = NamedParameterJdbcTemplatePlugin.class)
public @interface DBClient {

    /**
     * Spring容器中{@link NamedParameterJdbcTemplate}Bean 的名称
     */
    String value() default "";
}
