package io.github.lucklike.httpclient.annotation;

import io.github.lucklike.httpclient.LuckyHttpAutoConfiguration;
import io.github.lucklike.httpclient.discovery.ServerDiscoveryAutoConfiguration;
import io.github.lucklike.httpclient.parameter.ParameterInstanceFactoryAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启lucky-http-client的自动配置功能
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 01:45
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        LuckyHttpAutoConfiguration.class,
        ServerDiscoveryAutoConfiguration.class,
        ParameterInstanceFactoryAutoConfiguration.class
})
public @interface EnableLuckyHttpClientAutoConfiguration {


}
