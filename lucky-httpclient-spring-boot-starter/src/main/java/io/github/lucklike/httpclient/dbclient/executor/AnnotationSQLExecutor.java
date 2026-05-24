package io.github.lucklike.httpclient.dbclient.executor;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.ParameterContext;
import io.github.lucklike.httpclient.dbclient.FlatBeanSqlParameterSource;
import io.github.lucklike.httpclient.dbclient.SQLType;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于名称的简单 SQL 执行器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 00:10
 */
public class AnnotationSQLExecutor extends AbstractMCNamedJdbcTemplateSQLExecutor {

    private final String sqlTemp;
    private final SqlParameterSource sqlParamSource;
    private final SqlParameterSource[] batchSqlParamSource;

    public AnnotationSQLExecutor(MethodContext mc, SQLType type, String sqlTemp) throws SQLException {
        super(mc, type);
        this.sqlTemp = sqlTemp;
        if (type == SQLType.BATCH) {
            this.batchSqlParamSource = createBatchSqlParameterSource(mc);
            this.sqlParamSource = null;
        } else {
            this.sqlParamSource = createSqlParameterSource(mc);
            this.batchSqlParamSource = null;
        }

    }

    /**
     * 构建普通 SQL 参数源
     *
     * @param mc 方法上下文
     * @return 普通 SQL 参数源
     */
    private SqlParameterSource createSqlParameterSource(MethodContext mc) {
        FlatBeanSqlParameterSource sqlParamSource = new FlatBeanSqlParameterSource();
        for (ParameterContext pc : mc.getParameterContexts()) {
            sqlParamSource.addValue(pc.getName(), pc.getValue());
        }
        return sqlParamSource;
    }

    /**
     * 构建批量操作 SQL 参数源
     *
     * @param mc 方法上下文
     * @return 批量操作 SQL 参数源
     * @throws SQLException 构建过程可能出现的异常
     */
    @SuppressWarnings("unchecked")
    private SqlParameterSource[] createBatchSqlParameterSource(MethodContext mc) throws SQLException {
        Iterable<Object> iterable = null;
        for (ParameterContext pc : mc.getParameterContexts()) {
            Object value = pc.getValue();
            if (ContainerUtils.isIterable(value)) {
                iterable = ContainerUtils.getIterable(value);
                break;
            }
        }

        if (iterable == null) {
            throw new SQLException("批量操作参数异常");
        }

        List<SqlParameterSource> sqlParameterSources = new ArrayList<>();
        for (Object obj : iterable) {
            if (obj instanceof Map) {
                sqlParameterSources.add(new MapSqlParameterSource((Map<String, ?>) obj));
            } else {
                sqlParameterSources.add(new BeanPropertySqlParameterSource(obj));
            }
        }
        return sqlParameterSources.toArray(new SqlParameterSource[0]);
    }

    @Override
    public Object execute() {
        switch (getSqlType()) {
            case SELECT:
                return query(sqlTemp, sqlParamSource);
            case UPDATE:
                return update(sqlTemp, sqlParamSource);
            default:
                return batchUpdate(sqlTemp, batchSqlParamSource);
        }
    }
}
