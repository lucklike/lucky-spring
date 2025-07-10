package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.context.ValueContext;
import com.luckyframework.httpclient.proxy.unpack.ParameterConvert;

/**
 * 具备定位器功能的参数转换器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/15 02:19
 */
public abstract class LocatorParameterConvert implements ParameterConvert, Locator<ParameterConvert> {


    public static LocatorParameterConvert of(ParameterConvert parameterConvert, RType rType, Integer index, Class<? extends ParameterConvert> indexClass) {
        return new LocatorParameterConvert() {
            @Override
            public boolean canConvert(ValueContext context, Object value) {
                return parameterConvert.canConvert(context, value);
            }

            @Override
            public Object convert(ValueContext context,Object value) {
                return parameterConvert.convert(context, value);
            }

            @Override
            public RType rType() {
                return rType;
            }

            @Override
            public Integer index() {
                return index;
            }

            @Override
            public Class<? extends ParameterConvert> indexClass() {
                return indexClass;
            }
        };
    }
}
