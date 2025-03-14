package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.meta.Response;

import java.lang.reflect.Type;


/**
 * 具备定位器功能的自动转换器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2025/3/15 02:19
 */
public abstract class LocatorAutoConvert implements Response.AutoConvert, Locator<Response.AutoConvert> {


    public static LocatorAutoConvert of(Response.AutoConvert autoConvert, RType rType, Integer index, Class<? extends Response.AutoConvert> indexClass) {
        return new LocatorAutoConvert() {
            @Override
            public boolean can(Response resp, Type type) {
                return autoConvert.can(resp, type);
            }

            @Override
            public <T> T convert(Response resp, Type type) {
                return autoConvert.convert(resp, type);
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
            public Class<? extends Response.AutoConvert> indexClass() {
                return indexClass;
            }
        };
    }

}
