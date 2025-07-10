package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;


/**
 * 支持{@link Value @Value}注解功能的参数实例工厂
 */
public class ValueParameterInstanceFactory extends AnnotationParameterInstanceFactory<Value> {

    @Override
    protected Object doCreateInstance(ParameterInfo parameterInfo, ResolvableType realType, Value valueAnn) {
        Environment env = ApplicationContextUtils.getEnvironment();
        return ConversionUtils.conversion(env.resolveRequiredPlaceholders(valueAnn.value()), realType);
    }

}
