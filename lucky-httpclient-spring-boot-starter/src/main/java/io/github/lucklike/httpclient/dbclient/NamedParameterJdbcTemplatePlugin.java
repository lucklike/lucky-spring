package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.ParameterContext;
import com.luckyframework.httpclient.proxy.plugin.ExecuteMeta;
import com.luckyframework.httpclient.proxy.plugin.ProxyDecorator;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基于{@link NamedParameterJdbcTemplate}实现的数据库通讯客户端插件
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 03:07
 */
public class NamedParameterJdbcTemplatePlugin implements ProxyPlugin {

    @Override
    public Object decorate(ProxyDecorator decorator) throws Throwable {
        // 构建方法上下文
        ExecuteMeta meta = decorator.getMeta();
        MethodContext mc = new MethodContext(meta.getMetaContext(), meta.getArgs());

        // 没有被 SQL 注解标注时直接返回 null
        SQL sqlAnn = mc.getMergedAnnotation(SQL.class);
        if (sqlAnn == null) {
            return null;
        }

        // 执行 SQL
        return executeSQL(mc, sqlAnn);
    }

    private Object executeSQL(MethodContext mc, SQL sqlAnn) throws SQLException {
        // 计算 SQL 模板
        String sqlTemp = mc.parseExpression(sqlAnn.sql(), String.class);
        SQLType type = sqlAnn.type();

        // 构建 SQL 执行器
        SQLExecute sqlExecute;
        if (type == SQLType.BATCH) {
            sqlExecute = SQLExecute.batchOf(mc, sqlTemp, createBatchSqlParameterSource(mc));
        } else {
            sqlExecute = SQLExecute.of(mc, sqlTemp, createSqlParameterSource(mc));
        }

        // 执行结果
        switch (type) {
            case SELECT:
                return sqlExecute.query();
            case UPDATE:
                return sqlExecute.update();
            default:
                return sqlExecute.batchUpdate();
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

    /**
     * SQL执行器
     */
    static class SQLExecute {
        private final MethodContext mc;
        private final String sqlTemp;
        private final SqlParameterSource sqlParamSource;
        private final SqlParameterSource[] batchSqlParamSource;
        private final NamedParameterJdbcTemplate jdbcTemplate;

        private SQLExecute(MethodContext mc, String sqlTemp, SqlParameterSource sqlParamSource) {
            this.mc = mc;
            this.sqlTemp = sqlTemp;
            this.sqlParamSource = sqlParamSource;
            this.batchSqlParamSource = null;
            this.jdbcTemplate = findNamedParameterJdbcTemplate(mc);

        }

        private SQLExecute(MethodContext mc, String sqlTemp, SqlParameterSource[] batchSqlParamSource) {
            this.mc = mc;
            this.sqlTemp = sqlTemp;
            this.sqlParamSource = null;
            this.batchSqlParamSource = batchSqlParamSource;
            this.jdbcTemplate = findNamedParameterJdbcTemplate(mc);
        }

        public static SQLExecute of(MethodContext mc, String sqlTemp, SqlParameterSource sqlParamSource) {
            return new SQLExecute(mc, sqlTemp, sqlParamSource);
        }

        public static SQLExecute batchOf(MethodContext mc, String sqlTemp, SqlParameterSource[] batchSqlParamSource) {
            return new SQLExecute(mc, sqlTemp, batchSqlParamSource);
        }

        /**
         * 获取{@link NamedParameterJdbcTemplate}实现类
         *
         * @param mc 方法上下文
         * @return {@link NamedParameterJdbcTemplate}实例对象
         */
        private NamedParameterJdbcTemplate findNamedParameterJdbcTemplate(MethodContext mc) {
            DBClient dbClientAnn = mc.getMergedAnnotation(DBClient.class);
            String tempBeanName = dbClientAnn.value();
            return StringUtils.hasText(tempBeanName)
                    ? ApplicationContextUtils.getBean(tempBeanName, NamedParameterJdbcTemplate.class)
                    : ApplicationContextUtils.getBean(NamedParameterJdbcTemplate.class);
        }

        /**
         * 执行查询类 SQL
         *
         * @return SQL 执行结果
         */
        public Object query() {
            ResolvableType resultType = mc.getResultResolvableType();

            // 集合类型
            if (Collection.class.isAssignableFrom(resultType.toClass())) {
                Class<?> elementType = resultType.getGeneric(0).toClass();
                RowMapper<?> rowMapper;
                if (elementType == Map.class) {
                    rowMapper = new ColumnMapRowMapper();
                } else {
                    rowMapper = new BeanPropertyRowMapper<>(elementType);
                }
                List<?> queryResult = jdbcTemplate.query(sqlTemp, sqlParamSource, rowMapper);
                return ConversionUtils.conversion(queryResult, resultType);
            }

            // Map类型
            if (resultType.toClass() == Map.class) {
                return jdbcTemplate.query(sqlTemp, sqlParamSource, new ColumnMapRowMapper()).stream().findFirst().orElse(null);
            }

            // Bean 类型
            return jdbcTemplate.query(sqlTemp, sqlParamSource, new BeanPropertyRowMapper<>(resultType.toClass())).stream().findFirst().orElse(null);
        }

        /**
         * 执行编辑类的 SQL
         *
         * @return SQL 执行结果
         */
        public Object update() {
            KeyHolder keyHolder = mc.getArgument(KeyHolder.class);
            int update;
            if (keyHolder == null) {
                update = jdbcTemplate.update(sqlTemp, sqlParamSource);
            } else {
                update = jdbcTemplate.update(sqlTemp, sqlParamSource, keyHolder);
            }
            // Void方法
            if (mc.isVoidMethod()) {
                return null;
            }
            // 方法返回值为 boolean 或者 Boolean
            Class<?> resultType = mc.getResultResolvableType().toClass();
            if (resultType == boolean.class || resultType == Boolean.class) {
                return update > 0;
            }

            // 其他类型直接进行转换
            return ConversionUtils.conversion(update, resultType);
        }


        public Object batchUpdate() {
            int[] updates = jdbcTemplate.batchUpdate(sqlTemp, batchSqlParamSource);

            // Void方法
            if (mc.isVoidMethod()) {
                return null;
            }

            Class<?> resultClass = mc.getResultResolvableType().toClass();
            if (resultClass == boolean.class || resultClass == Boolean.class) {
                for (int update : updates) {
                    if (update > 0) {
                        return true;
                    }
                }
                return false;
            }
            return ConversionUtils.conversion(updates, mc.getResultResolvableType());
        }
    }
}
