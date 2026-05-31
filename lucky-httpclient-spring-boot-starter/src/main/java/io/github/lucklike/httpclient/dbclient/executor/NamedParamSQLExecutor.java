package io.github.lucklike.httpclient.dbclient.executor;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.FontUtil;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.ParameterContext;
import io.github.lucklike.httpclient.dbclient.plugin.ContentSpELSqlParameterSource;
import io.github.lucklike.httpclient.dbclient.sql.SQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 基于名称的简单 SQL 执行器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 00:10
 */
public class NamedParamSQLExecutor extends AbstractMCNamedJdbcTemplateSQLExecutor {

    private static final Logger logger = LoggerFactory.getLogger(NamedParamSQLExecutor.class);

    private final String sqlTemp;
    private final SqlParameterSource sqlParamSource;
    private final SqlParameterSource[] batchSqlParamSource;

    public NamedParamSQLExecutor(MethodContext mc, SQLType type, String sqlTemp) throws SQLException {
        super(mc, type);
        this.sqlTemp = sqlTemp;
        if (type == SQLType.BATCH) {
            this.batchSqlParamSource = createBatchSqlParameterSource(mc);
            this.sqlParamSource = null;
        } else {
            this.sqlParamSource = new ContentSpELSqlParameterSource(mc);
            this.batchSqlParamSource = null;
        }

    }

    /**
     * Builds a batch operation SQL parameter source array.
     * <p>
     * This method iterates through all method parameters to find the first Iterable type parameter,
     * then converts each element in the Iterable into a SqlParameterSource that can be used for
     * batch SQL operations. Each element can be either a Map (converted to MapSqlParameterSource)
     * or a JavaBean (converted to BeanPropertySqlParameterSource).
     * </p>
     *
     * <p><b>Usage Example:</b>
     * <pre>
     * // For batch insert with List&lt;Map&lt;String, Object&gt;&gt;
     * List&lt;Map&lt;String, Object&gt;&gt; batchData = ...;
     * methodContext.setParameters(batchData, otherParams...);
     * SqlParameterSource[] sources = createBatchSqlParameterSource(methodContext);
     *
     * // For batch update with List&lt;User&gt; (JavaBean)
     * List&lt;User&gt; users = ...;
     * SqlParameterSource[] sources = createBatchSqlParameterSource(methodContext);
     * </pre>
     * </p>
     *
     * @param mc The method context containing all parameters of the target method.
     *          Must contain at least one parameter of type Iterable (e.g., List, Set, Collection).
     * @return An array of SqlParameterSource objects, where each element corresponds to one item
     *         in the source Iterable. Never returns null.
     * @throws SQLException If no Iterable parameter is found in the method context, or if the
     *                      Iterable is null. The exception message will indicate the specific
     *                      batch operation parameter error.
     *
     * @see SqlParameterSource
     * @see MapSqlParameterSource
     * @see BeanPropertySqlParameterSource
     * @see ContainerUtils#isIterable(Object)
     * @see ContainerUtils#getIterable(Object)
     */
    @SuppressWarnings("unchecked")
    private SqlParameterSource[] createBatchSqlParameterSource(MethodContext mc) throws SQLException {
        // Step 1: Locate the first iterable parameter in the method context
        Iterable<Object> iterable = null;
        for (ParameterContext pc : mc.getParameterContexts()) {
            Object value = pc.getValue();
            if (ContainerUtils.isIterable(value)) {
                iterable = ContainerUtils.getIterable(value);
                break; // Use the first iterable found
            }
        }

        // Step 2: Validate that an iterable parameter exists
        if (iterable == null) {
            throw new SQLException("Batch operation parameter error: no Iterable parameter found in method context");
        }

        // Step 3: Convert each element in the iterable to a SqlParameterSource
        List<SqlParameterSource> sqlParameterSources = new ArrayList<>();
        for (Object obj : iterable) {
            if (obj == null) {
                // Optionally handle null elements - you may want to skip or throw exception
                // For now, we'll add an empty parameter source
                sqlParameterSources.add(new MapSqlParameterSource());
                continue;
            }

            if (obj instanceof Map) {
                // Convert Map to MapSqlParameterSource for named parameter binding
                sqlParameterSources.add(new MapSqlParameterSource((Map<String, ?>) obj));
            } else {
                // Convert JavaBean to BeanPropertySqlParameterSource for property-based binding
                sqlParameterSources.add(new BeanPropertySqlParameterSource(obj));
            }
        }

        // Step 4: Return as array
        return sqlParameterSources.toArray(new SqlParameterSource[0]);
    }

    @Override
    public Object execute() {
        switch (getSqlType()) {
            case SELECT:
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n\tSQL   : {}\n\tPARAM : {}\n>>", sqlTemp, sqlParamSource)));
                return queryAutoSelectModel(sqlTemp, sqlParamSource);
            case UPDATE:
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n\tSQL   : {}\n\tPARAM : {}\n>>", sqlTemp, sqlParamSource)));
                return update(sqlTemp, sqlParamSource);
            default:
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n\tSQL   : {}\n\tPARAM : {}\n>>", sqlTemp, Arrays.toString(batchSqlParamSource))));
                return batchUpdate(sqlTemp, batchSqlParamSource);
        }
    }
}
