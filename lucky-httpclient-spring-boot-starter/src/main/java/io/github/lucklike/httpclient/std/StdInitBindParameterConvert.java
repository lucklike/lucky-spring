package io.github.lucklike.httpclient.std;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.context.ParameterContext;
import com.luckyframework.httpclient.proxy.context.ValueContext;
import com.luckyframework.httpclient.proxy.unpack.ParameterConvert;
import org.springframework.lang.Nullable;

import java.util.List;

import static com.luckyframework.httpclient.proxy.function.CommonFunctions.spelInitCopy;
import static io.github.lucklike.httpclient.std.Constant.STANDARD_API_CONFIG_NAME;

/**
 * 标准化初始化参数绑定
 */
public class StdInitBindParameterConvert implements ParameterConvert {

    @Override
    public boolean canConvert(ValueContext context, @Nullable Object value) {
        // 空值不转换
        if (value == null) {
            return false;
        }
        // 非参数上下文不转换
        if (!(context instanceof ParameterContext)) {
            return false;
        }
        // 没有初始化配置时不转换
        StandardApiConfiguration config = context.getRootVar(STANDARD_API_CONFIG_NAME, StandardApiConfiguration.class);
        if (config ==null || ContainerUtils.isEmptyMap(config.getInitBindParams().getBindParams())) {
            return false;
        }

        // 存在@Init注解时进行转换
        if (context.isAnnotatedCheckParent(Init.class)) {
            return true;
        }

        // 参数类型与指定类型兼容时进行转换
        return isBindParamType(config, context);
    }

    @Override
    public Object convert(ValueContext context, @Nullable Object value) {
        StandardApiConfiguration config = context.getRootVar(STANDARD_API_CONFIG_NAME, StandardApiConfiguration.class);
        spelInitCopy(context, value, config.getInitBindParams().getBindParams());
        return value;
    }


    /**
     * 是否为Bind类型
     *
     * @param config  配置
     * @param context 值上下文
     * @return 是否为Bind类型
     */
    private boolean isBindParamType(StandardApiConfiguration config, ValueContext context) {
        List<Class<?>> classes = config.getInitBindParams().getBindClasses();
        if (ContainerUtils.isEmptyCollection(classes)) {
            return false;
        }

        Class<?> type = context.getType().toClass();
        for (Class<?> clazz : classes) {
            if (clazz.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }
}
