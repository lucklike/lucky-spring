package io.github.lucklike.httpclient.convert;

import com.luckyframework.httpclient.proxy.context.ValueContext;
import com.luckyframework.httpclient.proxy.unpack.ParameterConvert;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.injection.BindException;

/**
 * 用于将环境变量中的某段配置绑定到某个参数上的转换类
 */
public class InitBindParameterConvert implements ParameterConvert {

    @Override
    public boolean canConvert(ValueContext context, Object value) {

        //value为null时不进行转换
        if (value == null) {
            return false;
        }

        // 参数、方法、类上均没有被@InitBind标注时不进行绑定
        if (!context.isAnnotatedCheckParent(InitBind.class)) {
            return false;
        }

        // 标注了禁止绑定注解时不进行绑定
        if (context.isAnnotatedCheckParent(InitBindProhibition.class)) {
            return false;
        }

        // 标注在参数或者属性上时，可以应用
        if (context.isAnnotated(InitBind.class)) {
            return true;
        }

        // 标注在类上或者方法上时，需要检查type是否与当前参数类型相匹配
        InitBind initBind = context.getMergedAnnotationCheckParent(InitBind.class);
        Class<?> valueType = context.getType().resolve();
        for (Class<?> type : initBind.types()) {
            // 不对JDK中的类型提供支持
            if (ClassUtils.isJdkType(type)) {
                continue;
            }
            if (type.isAssignableFrom(valueType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object convert(ValueContext context, Object value) {
        InitBind initBind = context.getMergedAnnotationCheckParent(InitBind.class);
        try {
            String propertyName = context.parseExpression(initBind.value());
            BeanFunction.initBind(value, propertyName);
            return value;
        } catch (BindException e) {
            if (initBind.allowConfigNotExist()) {
                return value;
            }
            throw e;
        }
    }
}
