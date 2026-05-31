package io.github.lucklike.httpclient.dbclient.plugin;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.lang.Nullable;

/**
 * 上下文 SpEL SQl 参数源
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 13:06
 */
public class ContentSpELSqlParameterSource extends AbstractSqlParameterSource {

    private final MethodContext mc;

    public ContentSpELSqlParameterSource(@Nullable MethodContext mc) {
        this.mc = mc;
    }

    @Override
    public boolean hasValue(String paramName) {
        return getValue(paramName) != null;
    }

    @Nullable
    @Override
    public Object getValue(String paramName) throws IllegalArgumentException {
        return mc.getRootVar(paramName);
    }
}
