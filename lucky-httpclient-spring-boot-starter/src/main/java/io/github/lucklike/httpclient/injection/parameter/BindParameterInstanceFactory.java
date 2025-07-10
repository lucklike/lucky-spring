package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.httpclient.proxy.spel.ParameterInfo;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.BeanFunction;
import io.github.lucklike.httpclient.injection.Bind;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;

/**
 * 支持{@link Bind @Bind}注解功能的参数实例工厂
 */
public class BindParameterInstanceFactory extends AnnotationParameterInstanceFactory<Bind> {

    @Override
    protected Object doCreateInstance(ParameterInfo parameterInfo, ResolvableType realType, Bind bindAnn) {
        return BeanFunction.env(bindAnn.value(), realType);
    }

}
