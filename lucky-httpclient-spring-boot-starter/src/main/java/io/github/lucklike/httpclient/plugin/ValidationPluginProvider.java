package io.github.lucklike.httpclient.plugin;

import com.luckyframework.httpclient.generalapi.plugin.Validated;
import com.luckyframework.httpclient.generalapi.plugin.ValidationPlugin;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.plugin.ExecuteMeta;
import com.luckyframework.httpclient.proxy.plugin.ProxyDecorator;

import javax.validation.Validator;


/**
 * 参数校验对象持有者
 */
public class ValidationPluginProvider extends ValidationPlugin {

    private final ValidationPlugin validationPlugin;

    public ValidationPluginProvider(ValidationPlugin validationPlugin) {
        super((Validator) null);
        this.validationPlugin = validationPlugin;
    }

    @Override
    public Object decorate(ProxyDecorator decorator) throws Throwable {
        return validationPlugin.decorate(decorator);
    }

    @Override
    protected Class<?>[] determineValidationGroups(ExecuteMeta executeMeta) {
        return executeMeta.getMetaContext().getMergedAnnotationCheckParent(Validated.class).value();
    }

    @Override
    public boolean match(ExecuteMeta meta) {
        MethodMetaContext context = meta.getMetaContext();
        return context.isAnnotatedCheckParent(Validated.class);
    }
}
