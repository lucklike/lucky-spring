package io.github.lucklike.httpclient.dbclient.executor;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.StringUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.dbclient.annotation.DBClient;
import io.github.lucklike.httpclient.dbclient.plugin.CachedAnnotationRowMapper;
import io.github.lucklike.httpclient.dbclient.sql.SQLType;
import io.github.lucklike.httpclient.dbclient.sql.page.ContextPage;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import io.github.lucklike.httpclient.dbclient.sql.page.strategy.PageSql;
import io.github.lucklike.httpclient.dbclient.sql.page.strategy.PageStrategyFactory;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

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

    private Class<?> getStreamElementClass() {
        return methodContext.getResultResolvableType().getGeneric(0).toClass();
    }

    protected boolean isStreamQuery() {
        return Stream.class == methodContext.getResultResolvableType().toClass();
    }

    protected boolean isPageQuery() {
        return PageResult.class == methodContext.getResultResolvableType().toClass() &&
                methodContext.getArgument(Page.class) != null;
    }

    /**
     * 获取 SQL 类型
     *
     * @return SQL 类型
     */
    public SQLType getSqlType() {
        return sqlType;
    }

    protected Object queryAutoSelectModel(String sqlTemp, Object[] sqlArgs) {
        if (isPageQuery()) {
            return queryPage(sqlTemp, sqlArgs);
        }
        if (isStreamQuery()) {
            return queryForStream(sqlTemp, sqlArgs);
        }
        return query(sqlTemp, sqlArgs);
    }

    protected Object queryAutoSelectModel(String sqlTemp, SqlParameterSource sqlParameterSource) {
        if (isPageQuery()) {
            return queryPage(sqlTemp, sqlParameterSource);
        }
        if (isStreamQuery()) {
            return queryForStream(sqlTemp, sqlParameterSource);
        }
        return query(sqlTemp, sqlParameterSource);
    }

    protected Stream<?> queryForStream(String sqlTemp, Object[] sqlArgs) {
        return getJdbcTemplate().queryForStream(sqlTemp, creatSingleGenericRowMapper(), sqlArgs);
    }

    protected Stream<?> queryForStream(String sqlTemp, SqlParameterSource sqlParamSource) {
        return namedParameterJdbcTemplate.queryForStream(sqlTemp, sqlParamSource, creatSingleGenericRowMapper());
    }

    protected Object queryPage(String sqlTemp, Object[] sqlArgs) {
        Page page = methodContext.getArgument(Page.class);
        PageResult<?> resultPage = new PageResult<>(page);
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        page.setPageStrategyIfNotExist(() -> PageStrategyFactory.getStrategyByDataSource(jdbcTemplate.getDataSource()));
        if (page.isCountTotal()) {
            String countSql = page.buildCountSql(sqlTemp);
            long totalCount = jdbcTemplate.queryForObject(countSql, long.class, sqlArgs);
            resultPage.setTotalCount(totalCount);
        }

        PageSql pageParam = page.buildPageSql(sqlTemp);
        String pageSql = pageParam.getSql();
        Object[] pageArgs = mergeParams(sqlArgs, pageParam.getPageParam());

        Class<?> entityClass = methodContext.getResultResolvableType().getGeneric(0).toClass();
        List queryResult = jdbcTemplate.query(pageSql, new CachedAnnotationRowMapper<>(entityClass), pageArgs);
        resultPage.setRecords(queryResult);
        return resultPage;
    }

    protected Object query(String sqlTemp, Object[] sqlArgs) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        ResolvableType resultType = methodContext.getResultResolvableType();

        // 简单基本类型
        if (ClassUtils.isSimpleBaseType(resultType.toClass())) {
            return jdbcTemplate.queryForObject(sqlTemp, resultType.toClass(), sqlArgs);
        }

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


    protected Object queryPage(String sqlTemp, SqlParameterSource sqlParamSource) {
        ContextPage page = new ContextPage(methodContext, methodContext.getArgument(Page.class));
        PageResult<?> resultPage = new PageResult<>(page);
        page.setPageStrategyIfNotExist(() -> PageStrategyFactory.getStrategyByDataSource(namedParameterJdbcTemplate.getJdbcTemplate().getDataSource()));
        if (page.isCountTotal()) {
            String countSql = page.buildCountSql(sqlTemp);
            Long totalCount = namedParameterJdbcTemplate.queryForObject(countSql, sqlParamSource, long.class);
            resultPage.setTotalCount(totalCount == null ? 0 : totalCount);
        }

        PageSql pageParam = page.buildPageSql(sqlTemp);
        String pageSql = pageParam.getSql();

        List result = namedParameterJdbcTemplate.query(pageSql, sqlParamSource, creatSingleGenericRowMapper());
        resultPage.setRecords(result);
        return resultPage;
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

        // 简单基本类型
        if (ClassUtils.isSimpleBaseType(resultType.toClass())) {
            return namedParameterJdbcTemplate.queryForObject(sqlTemp, sqlParamSource, resultType.toClass());
        }

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

    protected RowMapper<?> creatSingleGenericRowMapper() {
        Class<?> entityClass = methodContext.getResultResolvableType().getGeneric(0).toClass();
        if (Map.class.isAssignableFrom(entityClass)) {
            return new ColumnMapRowMapper();
        }
        return new CachedAnnotationRowMapper<>(entityClass);
    }

    protected RowMapper<?> createRowMapper() {
        ResolvableType resultType = methodContext.getResultResolvableType();
        // 集合类型
        if (Collection.class.isAssignableFrom(resultType.toClass())) {
            Class<?> elementType = resultType.getGeneric(0).toClass();
            if (elementType == Map.class) {
                return new ColumnMapRowMapper();
            } else if (ClassUtils.isSimpleBaseType(elementType)) {
                return new SingleColumnRowMapper<>(elementType);
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

    protected String getSqlParam(Object[] params) {
        if (ContainerUtils.isEmptyArray(params)) {
            return "[]";
        }

        List<String> paramArray = new ArrayList<>();
        for (Object param : params) {
            if (param == null) {
                paramArray.add("null");
            } else {
                paramArray.add(StringUtils.format("({}){}", ClassUtils.getClassSimpleName(param), param));
            }
        }

        return StringUtils.format("[{}]", String.join(",  ", paramArray));
    }

    protected String getBatchSqlParam(List<Object[]> batchSqlParam) {
        if (ContainerUtils.isEmptyCollection(batchSqlParam)) {
            return "[]";
        }

        List<String> paramArray = new ArrayList<>();
        for (Object[] batchParam : batchSqlParam) {
            paramArray.add(getSqlParam(batchParam));
        }

        return StringUtils.format("[\n\t{}\n]", String.join("\n\t", paramArray));
    }

    private Object[] mergeParams(Object[] params1, Object[] params2) {
        return Stream.of(
                        params1 != null ? Arrays.stream(params1) : Stream.empty(),
                        params2 != null ? Arrays.stream(params2) : Stream.empty()
                )
                .flatMap(Function.identity())
                .toArray();
    }
}
