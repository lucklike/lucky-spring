package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.common.FlatBean;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 03:25
 */
public class FlatBeanSqlParameterSource extends AbstractSqlParameterSource {

    private final FlatBean<Map<String, Object>> flatBean = FlatBean.of(new LinkedHashMap<>());

    @Override
    public boolean hasValue(@NonNull String paramName) {
        return flatBean.tryGet(paramName).isExist();
    }

    @Nullable
    @Override
    public Object getValue(@NonNull String paramName) throws IllegalArgumentException {
        return flatBean.tryGet(paramName).getValue();
    }

    /**
     * Add a parameter to this parameter source.
     *
     * @param paramName the name of the parameter
     * @param value     the value of the parameter
     * @return a reference to this parameter source,
     * so it's possible to chain several calls together
     */
    public FlatBeanSqlParameterSource addValue(String paramName, @Nullable Object value) {
        Assert.notNull(paramName, "Parameter name must not be null");
        this.flatBean.set(paramName, value);
        if (value instanceof SqlParameterValue) {
            registerSqlType(paramName, ((SqlParameterValue) value).getSqlType());
        }
        return this;
    }

    /**
     * Add a parameter to this parameter source.
     *
     * @param paramName the name of the parameter
     * @param value     the value of the parameter
     * @param sqlType   the SQL type of the parameter
     * @return a reference to this parameter source,
     * so it's possible to chain several calls together
     */
    public FlatBeanSqlParameterSource addValue(String paramName, @Nullable Object value, int sqlType) {
        Assert.notNull(paramName, "Parameter name must not be null");
        this.flatBean.set(paramName, value);
        registerSqlType(paramName, sqlType);
        return this;
    }

    /**
     * Add a parameter to this parameter source.
     *
     * @param paramName the name of the parameter
     * @param value     the value of the parameter
     * @param sqlType   the SQL type of the parameter
     * @param typeName  the type name of the parameter
     * @return a reference to this parameter source,
     * so it's possible to chain several calls together
     */
    public FlatBeanSqlParameterSource addValue(String paramName, @Nullable Object value, int sqlType, String typeName) {
        Assert.notNull(paramName, "Parameter name must not be null");
        this.flatBean.set(paramName, value);
        registerSqlType(paramName, sqlType);
        registerTypeName(paramName, typeName);
        return this;
    }

    /**
     * Add a Map of parameters to this parameter source.
     *
     * @param values a Map holding existing parameter values (can be {@code null})
     * @return a reference to this parameter source,
     * so it's possible to chain several calls together
     */
    public FlatBeanSqlParameterSource addValues(@Nullable Map<String, ?> values) {
        if (values != null) {
            values.forEach((key, value) -> {
                this.flatBean.set(key, value);
                if (value instanceof SqlParameterValue) {
                    registerSqlType(key, ((SqlParameterValue) value).getSqlType());
                }
            });
        }
        return this;
    }

    @Override
    public String toString() {
        return flatBean.getBean().toString();
    }
}
