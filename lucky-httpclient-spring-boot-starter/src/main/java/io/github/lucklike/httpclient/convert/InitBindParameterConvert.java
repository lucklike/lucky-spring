package io.github.lucklike.httpclient.convert;

import com.luckyframework.exception.LuckyReflectionException;
import com.luckyframework.httpclient.core.util.BeanUtils;
import com.luckyframework.httpclient.core.util.PropertyConvert;
import com.luckyframework.httpclient.core.util.PropertyFilter;
import com.luckyframework.httpclient.core.util.PropertyInfo;
import com.luckyframework.httpclient.proxy.context.Context;
import com.luckyframework.httpclient.proxy.context.ValueContext;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.httpclient.proxy.unpack.ParameterConvert;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.function.BeanFunction;
import io.github.lucklike.httpclient.injection.BindException;

import static com.luckyframework.httpclient.core.util.BeanUtils.copyProperties;
import static io.github.lucklike.httpclient.function.BeanFunction.env;

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
        for (String conf : initBind.value()) {
            try {
                String propertyName = context.parseExpression(conf);
                initBind(context, value, propertyName);
            } catch (BindException e) {
                if (!initBind.allowConfigNotExist()) {
                    throw e;
                }
            }
        }
        return value;
    }

    /**
     * 初始化绑定
     *
     * @param context      上下文对象
     * @param targetObject 真实对象
     * @param prefix       配置前缀
     */
    public static void initBind(Context context, Object targetObject, String prefix) {
        Object configObj = env(prefix, targetObject.getClass());
        BeanUtils.TargetPropertyIsDefValueExecuteCopy filter = new BeanUtils.TargetPropertyIsDefValueExecuteCopy();
        copyProperties(configObj, targetObject, filter, new SpELPropertyConvert(context, filter));
    }

    /**
     * 默认的属性转换器
     */
    static class SpELPropertyConvert implements PropertyConvert {

        private final PropertyFilter filter;
        private final Context context;

        SpELPropertyConvert(Context context, PropertyFilter filter) {
            this.filter = filter;
            this.context = context;
        }


        @Override
        public void convert(PropertyInfo sourceProperty, PropertyInfo targetProperty) {
            // 进行SpEL计算
            Object propertyValue = sourceProperty.getValue();
            if (propertyValue instanceof String) {
                propertyValue = context.parseExpression(propertyValue.toString(), String.class);
            }

            if (sourceProperty.isJdkType()) {
                targetProperty.setValue(propertyValue);
            } else {

                //目标对象的属性不为null时，直接进行属性的拷贝
                if (propertyValue != null) {
                    copyProperties(sourceProperty.getValue(), propertyValue, filter, this);
                }
                // 目标对象的属性为null时，尝试使用反射调用其无参构造器进行构造之后再进行属性的拷贝
                else {
                    try {
                        Object newTargetPropertyValue = ClassUtils.newObject(targetProperty.getDescriptor().getPropertyType());
                        copyProperties(sourceProperty.getValue(), newTargetPropertyValue, filter, this);
                        targetProperty.setValue(newTargetPropertyValue);
                    } catch (LuckyReflectionException e) {
                        // ignore
                    }
                }


            }
        }
    }
}
