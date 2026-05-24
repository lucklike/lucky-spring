package io.github.lucklike.httpclient.dbclient.executor;

import com.luckyframework.common.StringUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.dbclient.CachedAnnotationRowMapper;
import io.github.lucklike.httpclient.dbclient.SQLType;
import io.github.lucklike.httpclient.dbclient.annotation.DBClient;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基于{@link NamedParameterJdbcTemplate} + {@link MethodContext}实现的 SQL 执行器抽象类
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 00:17
 */
public abstract class AbstractMCNamedJdbcTemplateSQLExecutor implements SQLExecutor {

    /**
     * SQL类型
     */
    private final SQLType sqlType;

    /**
     * {@link MethodContext}实例
     */
    private final MethodContext methodContext;

    /**
     * {@link NamedParameterJdbcTemplate}实例
     */
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AbstractMCNamedJdbcTemplateSQLExecutor(MethodContext mc, SQLType sqlType) {
        this.sqlType = sqlType;
        this.methodContext = mc;
        this.namedParameterJdbcTemplate = findNamedParameterJdbcTemplate(mc);
    }

    /**
     * 获取{@link JdbcTemplate}实例对象
     *
     * @return {@link JdbcTemplate}实例对象
     */
    public JdbcTemplate getJdbcTemplate() {
        return namedParameterJdbcTemplate.getJdbcTemplate();
    }

    /**
     * 获取{@link NamedParameterJdbcTemplate}实例对象
     *
     * @return {@link NamedParameterJdbcTemplate}实例对象
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    /**
     * 获取{@link MethodContext}实例对象
     *
     * @return {@link MethodContext}实例对象
     */
    public MethodContext getMethodContext() {
        return methodContext;
    }

    /**
     * 获取 SQL 类型
     *
     * @return SQL 类型
     */
    public SQLType getSqlType() {
        return sqlType;
    }

    protected Object query(String sqlTemp, Object[] sqlArgs) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        ResolvableType resultType = methodContext.getResultResolvableType();
        RowMapper<?> rowMapper = createRowMapper();

        // 集合类型
        if (Collection.class.isAssignableFrom(resultType.toClass())) {
            List<?> queryResult = jdbcTemplate.query(sqlTemp, rowMapper, sqlArgs);
            return ConversionUtils.conversion(queryResult, resultType);
        }

        // Map类型
        if (resultType.toClass() == Map.class) {
            return jdbcTemplate.query(sqlTemp, rowMapper, sqlArgs).stream().findFirst().orElse(null);
        }

        // Bean 类型
        return jdbcTemplate.query(sqlTemp, rowMapper, sqlArgs).stream().findFirst().orElse(null);
    }

    /**
     * 执行查询类 SQL
     *
     * @param sqlTemp        SQL模板
     * @param sqlParamSource SQL 执行参数
     * @return SQL 执行结果
     */
    protected Object query(String sqlTemp, SqlParameterSource sqlParamSource) {
        ResolvableType resultType = methodContext.getResultResolvableType();
        RowMapper<?> rowMapper = createRowMapper();

        // 集合类型
        if (Collection.class.isAssignableFrom(resultType.toClass())) {
            List<?> queryResult = namedParameterJdbcTemplate.query(sqlTemp, sqlParamSource, rowMapper);
            return ConversionUtils.conversion(queryResult, resultType);
        }

        // Map类型
        if (resultType.toClass() == Map.class) {
            return namedParameterJdbcTemplate.query(sqlTemp, sqlParamSource, rowMapper).stream().findFirst().orElse(null);
        }

        // Bean 类型
        return namedParameterJdbcTemplate.query(sqlTemp, sqlParamSource, rowMapper).stream().findFirst().orElse(null);
    }

    /**
     * 执行编辑类的 SQL
     *
     * @param sqlTemp        SQL模板
     * @param sqlParamSource SQL 执行参数
     * @return SQL 执行结果
     */
    protected Object update(String sqlTemp, Object[] sqlParamSource) {
        KeyHolder keyHolder = methodContext.getArgument(KeyHolder.class);
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        int update;
        if (keyHolder == null) {
            update = jdbcTemplate.update(sqlTemp, sqlParamSource);
        } else {
            update = jdbcTemplate.update(sqlTemp, sqlParamSource, keyHolder);
        }
        // Void方法
        if (methodContext.isVoidMethod()) {
            return null;
        }
        // 方法返回值为 boolean 或者 Boolean
        Class<?> resultType = methodContext.getResultResolvableType().toClass();
        if (resultType == boolean.class || resultType == Boolean.class) {
            return update > 0;
        }

        // 其他类型直接进行转换
        return ConversionUtils.conversion(update, resultType);
    }

    /**
     * 执行编辑类的 SQL
     *
     * @param sqlTemp SQL模板
     * @param sqlArgs SQL 执行参数
     * @return SQL 执行结果
     */
    protected Object update(String sqlTemp, SqlParameterSource sqlArgs) {
        KeyHolder keyHolder = methodContext.getArgument(KeyHolder.class);
        int update;
        if (keyHolder == null) {
            update = namedParameterJdbcTemplate.update(sqlTemp, sqlArgs);
        } else {
            update = namedParameterJdbcTemplate.update(sqlTemp, sqlArgs, keyHolder);
        }
        // Void方法
        if (methodContext.isVoidMethod()) {
            return null;
        }
        // 方法返回值为 boolean 或者 Boolean
        Class<?> resultType = methodContext.getResultResolvableType().toClass();
        if (resultType == boolean.class || resultType == Boolean.class) {
            return update > 0;
        }

        // 其他类型直接进行转换
        return ConversionUtils.conversion(update, resultType);
    }

    /**
     * 执行批量 UPDATE 操作
     *
     * @param sqlTemp              SQL模板
     * @param batchSqlParamSources 批量执行参数
     * @return 执行结果
     */
    protected Object batchUpdate(String sqlTemp, List<Object[]> batchSqlParamSources) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        int[] updates = jdbcTemplate.batchUpdate(sqlTemp, batchSqlParamSources);

        // Void方法
        if (methodContext.isVoidMethod()) {
            return null;
        }

        Class<?> resultClass = methodContext.getResultResolvableType().toClass();
        if (resultClass == boolean.class || resultClass == Boolean.class) {
            for (int update : updates) {
                if (update > 0) {
                    return true;
                }
            }
            return false;
        }
        return ConversionUtils.conversion(updates, methodContext.getResultResolvableType());
    }

    /**
     * 执行批量 UPDATE 操作
     *
     * @param sqlTemp              SQL模板
     * @param batchSqlParamSources 批量执行参数
     * @return 执行结果
     */
    protected Object batchUpdate(String sqlTemp, SqlParameterSource[] batchSqlParamSources) {
        int[] updates = namedParameterJdbcTemplate.batchUpdate(sqlTemp, batchSqlParamSources);

        // Void方法
        if (methodContext.isVoidMethod()) {
            return null;
        }

        Class<?> resultClass = methodContext.getResultResolvableType().toClass();
        if (resultClass == boolean.class || resultClass == Boolean.class) {
            for (int update : updates) {
                if (update > 0) {
                    return true;
                }
            }
            return false;
        }
        return ConversionUtils.conversion(updates, methodContext.getResultResolvableType());
    }

    protected RowMapper<?> createRowMapper() {
        ResolvableType resultType = methodContext.getResultResolvableType();
        // 集合类型
        if (Collection.class.isAssignableFrom(resultType.toClass())) {
            Class<?> elementType = resultType.getGeneric(0).toClass();
            if (elementType == Map.class) {
                return new ColumnMapRowMapper();
            } else {
                return new CachedAnnotationRowMapper<>(elementType);
            }
        }

        // Map类型
        if (resultType.toClass() == Map.class) {
            return new ColumnMapRowMapper();
        }

        // Bean 类型
        return new CachedAnnotationRowMapper<>(resultType.toClass());
    }

    /**
     * 获取{@link NamedParameterJdbcTemplate}实现类
     *
     * @param mc 方法上下文
     * @return {@link NamedParameterJdbcTemplate}实例对象
     */
    private NamedParameterJdbcTemplate findNamedParameterJdbcTemplate(MethodContext mc) {
        DBClient dbClientAnn = mc.getMergedAnnotationCheckParent(DBClient.class);
        String tempBeanName = dbClientAnn.value();
        return StringUtils.hasText(tempBeanName)
                ? ApplicationContextUtils.getBean(tempBeanName, NamedParameterJdbcTemplate.class)
                : ApplicationContextUtils.getBean(NamedParameterJdbcTemplate.class);
    }
}
