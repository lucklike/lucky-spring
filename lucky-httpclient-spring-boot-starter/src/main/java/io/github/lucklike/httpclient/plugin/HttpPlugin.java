package io.github.lucklike.httpclient.plugin;

import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
@Role(ROLE_INFRASTRUCTURE)
public @interface HttpPlugin {
}
