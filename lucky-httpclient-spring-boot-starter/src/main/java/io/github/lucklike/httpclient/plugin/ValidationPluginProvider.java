package io.github.lucklike.httpclient.plugin;

import com.luckyframework.httpclient.generalapi.plugin.ValidationPlugin;
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
}
