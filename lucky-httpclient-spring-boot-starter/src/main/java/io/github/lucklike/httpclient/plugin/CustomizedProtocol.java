package io.github.lucklike.httpclient.plugin;

import com.luckyframework.httpclient.proxy.annotations.HttpRequest;
import com.luckyframework.httpclient.proxy.plugin.Plugin;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import io.github.lucklike.httpclient.discovery.HttpClient;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 自定义协议
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@HttpRequest
@HttpClient
@Plugin
public @interface CustomizedProtocol {

    /**
     * 插件Class
     */
    @AliasFor(annotation =  Plugin.class, attribute = "pluginClass")
    Class<? extends ProxyPlugin> protocolPlugin();
}
