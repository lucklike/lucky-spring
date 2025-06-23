package io.github.lucklike.httpclient.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.env.Environment;

/**
 * 支持{@link Bind @HttpReference}注解功能的参数实例工厂
 */
public class BindParameterInstanceFactory implements ParameterInstanceFactory {
    @Override
    public boolean canCreateInstance(ParameterInfo parameterInfo) {
        return AnnotationUtils.isAnnotated(parameterInfo.getParameter(), Bind.class);
    }

    @Override
    public Object createInstance(ParameterInfo parameterInfo) {

        Environment env = ApplicationContextUtils.getEnvironment();
        Bind bindAnn = AnnotationUtils.getMergedAnnotation(parameterInfo.getParameter(), Bind.class);
        String prefix = bindAnn.value();

        if (env.containsProperty(prefix)) {
            return env.getRequiredProperty(prefix, parameterInfo.getParameter().getType());
        }

        return Binder.get(env)
                .bind(ConfigurationPropertyName.adapt(prefix, '.'), Bindable.of(parameterInfo.getResolvableType()))
                .orElseThrow(() -> new IllegalStateException("Required key '" + prefix + "' not found"));
    }
}
