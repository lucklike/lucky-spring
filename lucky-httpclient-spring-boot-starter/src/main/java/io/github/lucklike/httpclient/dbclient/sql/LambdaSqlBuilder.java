package io.github.lucklike.httpclient.dbclient.sql;

import io.github.lucklike.httpclient.dbclient.function.EntityUtils;
import io.github.lucklike.httpclient.dbclient.function.LambdaUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 基于 Lambda 表达式的 SQL 构建器
 * 使用 SqlBuilder 作为底层实现，支持任意顺序调用
 *
 * @author fukang
 * @version 3.0.0
 * @date 2026/5/25
 */
public class LambdaSqlBuilder<T> implements SQLWrapper {

    private final Class<T> entityClass;
    private String tableName;
    private final SqlBuilder sqlBuilder;
    private boolean isBuilt;

    // 用于 WHERE 条件的嵌套构建状态
    private boolean inNestedCondition;
    private List<Object> nestedParams;

    // ==================== 构造方法 ====================

    protected LambdaSqlBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.sqlBuilder = SqlBuilder.builder();
        this.isBuilt = false;
        this.inNestedCondition = false;
        this.nestedParams = new ArrayList<>();
    }

    public static <T> LambdaSqlBuilder<T> of(Class<T> entityClass) {
        return new LambdaSqlBuilder<>(entityClass);
    }

    // ==================== 表名和列名辅助方法 ====================

    protected LambdaSqlBuilder<T> tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    private String getTableName() {
        if (tableName != null) return tableName;
        return EntityUtils.getTableName(entityClass);
    }

    private <R> String getColumn(SFunction<T, R> function) {
        return LambdaUtils.getColumnName(entityClass, function);
    }

    // ==================== SELECT 相关方法 ====================

    @SafeVarargs
    protected final LambdaSqlBuilder<T> select(SFunction<T, ?>... columns) {
        if (columns == null || columns.length == 0) {
            sqlBuilder.select();
        } else {
            String[] columnNames = Arrays.stream(columns)
                    .map(this::getColumn)
                    .toArray(String[]::new);
            sqlBuilder.select(columnNames);
        }
        return this;
    }

    protected LambdaSqlBuilder<T> select(String expression) {
        sqlBuilder.select(expression);
        return this;
    }

    protected LambdaSqlBuilder<T> selectCount() {
        sqlBuilder.count();
        return this;
    }

    protected LambdaSqlBuilder<T> selectCount(SFunction<T, ?> column) {
        sqlBuilder.count(getColumn(column));
        return this;
    }

    @SafeVarargs
    protected final LambdaSqlBuilder<T> selectDistinct(SFunction<T, ?>... columns) {
        if (columns == null || columns.length == 0) {
            sqlBuilder.selectDistinct();
        } else {
            String[] columnNames = Arrays.stream(columns)
                    .map(this::getColumn)
                    .toArray(String[]::new);
            sqlBuilder.selectDistinct(columnNames);
        }
        return this;
    }

    // ==================== FROM 相关方法 ====================

    protected LambdaSqlBuilder<T> from() {
        sqlBuilder.from(getTableName());
        return this;
    }

    protected LambdaSqlBuilder<T> from(String alias) {
        sqlBuilder.from(getTableName(), alias);
        return this;
    }

    protected LambdaSqlBuilder<T> from(String tableName, String alias) {
        sqlBuilder.from(tableName, alias);
        return this;
    }

    protected LambdaSqlBuilder<T> from(LambdaSqlBuilder<?> subQuery, String alias) {
        // 先构建子查询获取 SQL 和参数
        SqlBuilder.QueryResult subResult = subQuery.buildInternal();
        String subSql = subResult.getSql();
        Object[] subParams = subResult instanceof SqlBuilder.SingleQueryResult
                ? ((SqlBuilder.SingleQueryResult) subResult).getParams()
                : new Object[0];

        // 构建子查询片段
        SqlBuilder tempBuilder = SqlBuilder.builder();
        tempBuilder.from(subSql, alias);
        // 手动添加子查询参数
        for (Object param : subParams) {
            tempBuilder.where("1=1", param); // 技巧：通过 where 添加参数
        }

        return this;
    }

    // ==================== JOIN 相关方法 ====================

    protected <E> LambdaSqlBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        String joinTableName = EntityUtils.getTableName(joinClass);
        sqlBuilder.join(
                type,
                joinTableName,
                alias
        );
        return this;
    }

    protected <E> LambdaSqlBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        return join(SqlBuilder.JoinType.INNER, joinClass, alias);
    }

    protected <E> LambdaSqlBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        return join(SqlBuilder.JoinType.LEFT, joinClass, alias);
    }

    protected <E> LambdaSqlBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        return join(SqlBuilder.JoinType.RIGHT, joinClass, alias);
    }

    protected LambdaSqlBuilder<T> on(String condition) {
        sqlBuilder.on(condition);
        return this;
    }

    protected <E> LambdaSqlBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        String condition = getColumn(leftColumn) + " = " + LambdaUtils.getColumnName(null, rightColumn);
        sqlBuilder.on(condition);
        return this;
    }

    // ==================== INSERT 相关方法 ====================

    @SafeVarargs
    protected final LambdaSqlBuilder<T> insertInto(SFunction<T, ?>... columns) {
        if (columns == null || columns.length == 0) {
            sqlBuilder.insertInto(getTableName());
        } else {
            String[] columnNames = Arrays.stream(columns)
                    .map(this::getColumn)
                    .toArray(String[]::new);
            sqlBuilder.insertInto(getTableName(), columnNames);
        }
        return this;
    }

    protected LambdaSqlBuilder<T> values(Object... values) {
        sqlBuilder.values(values);
        return this;
    }

    protected LambdaSqlBuilder<T> valuesBatch(List<Object[]> batchValues) {
        sqlBuilder.valuesBatch(batchValues);
        return this;
    }

    // ==================== UPDATE 相关方法 ====================

    protected LambdaSqlBuilder<T> update() {
        sqlBuilder.update(getTableName());
        return this;
    }

    protected <R> LambdaSqlBuilder<T> set(SFunction<T, R> column, Object value) {
        sqlBuilder.set(getColumn(column), value);
        return this;
    }

    protected LambdaSqlBuilder<T> set(String column, Object value) {
        sqlBuilder.set(column, value);
        return this;
    }

    protected LambdaSqlBuilder<T> set(Map<String, Object> columnValues) {
        sqlBuilder.set(columnValues);
        return this;
    }

    // ==================== DELETE 相关方法 ====================

    protected LambdaSqlBuilder<T> delete() {
        sqlBuilder.delete();
        sqlBuilder.from(getTableName());
        return this;
    }

    protected LambdaSqlBuilder<T> deleteFrom() {
        sqlBuilder.deleteFrom(getTableName());
        return this;
    }

    // ==================== WHERE 条件方法 ====================

    protected LambdaSqlBuilder<T> where(String condition, Object... values) {
        sqlBuilder.where(condition, values);
        return this;
    }

    protected LambdaSqlBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        sqlBuilder.bracketStart();

        // 保存当前状态
        boolean oldInNested = this.inNestedCondition;
        List<Object> oldNestedParams = this.nestedParams;

        // 设置嵌套状态
        this.inNestedCondition = true;
        this.nestedParams = new ArrayList<>();

        // 执行嵌套条件构建
        conditionBuilder.accept(this);

        // 恢复状态
        this.inNestedCondition = oldInNested;
        this.nestedParams = oldNestedParams;

        sqlBuilder.bracketEnd();
        return this;
    }

    // 基础条件方法
    protected <R> LambdaSqlBuilder<T> eq(SFunction<T, R> column, Object value) {
        if (inNestedCondition) {
            sqlBuilder.eq(getColumn(column), value);
        } else {
            sqlBuilder.eq(getColumn(column), value);
        }
        return this;
    }

    protected <R> LambdaSqlBuilder<T> ne(SFunction<T, R> column, Object value) {
        sqlBuilder.ne(getColumn(column), value);
        return this;
    }

    protected <R> LambdaSqlBuilder<T> gt(SFunction<T, R> column, Object value) {
        sqlBuilder.gt(getColumn(column), value);
        return this;
    }

    protected <R> LambdaSqlBuilder<T> ge(SFunction<T, R> column, Object value) {
        sqlBuilder.ge(getColumn(column), value);
        return this;
    }

    protected <R> LambdaSqlBuilder<T> lt(SFunction<T, R> column, Object value) {
        sqlBuilder.lt(getColumn(column), value);
        return this;
    }

    protected <R> LambdaSqlBuilder<T> le(SFunction<T, R> column, Object value) {
        sqlBuilder.le(getColumn(column), value);
        return this;
    }

    protected LambdaSqlBuilder<T> like(SFunction<T, ?> column, String value) {
        sqlBuilder.like(getColumn(column), value);
        return this;
    }

    protected LambdaSqlBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        sqlBuilder.likeLeft(getColumn(column), value);
        return this;
    }

    protected LambdaSqlBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        sqlBuilder.likeRight(getColumn(column), value);
        return this;
    }

    protected LambdaSqlBuilder<T> notLike(SFunction<T, ?> column, String value) {
        sqlBuilder.notLike(getColumn(column), value);
        return this;
    }

    protected <R> LambdaSqlBuilder<T> in(SFunction<T, R> column, R... values) {
        sqlBuilder.in(getColumn(column), values);
        return this;
    }

    protected <R> LambdaSqlBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        sqlBuilder.in(getColumn(column), values);
        return this;
    }

    @SafeVarargs
    protected final <R> LambdaSqlBuilder<T> notIn(SFunction<T, R> column, R... values) {
        sqlBuilder.notIn(getColumn(column), values);
        return this;
    }

    protected LambdaSqlBuilder<T> isNull(SFunction<T, ?> column) {
        sqlBuilder.isNull(getColumn(column));
        return this;
    }

    protected LambdaSqlBuilder<T> isNotNull(SFunction<T, ?> column) {
        sqlBuilder.isNotNull(getColumn(column));
        return this;
    }

    protected LambdaSqlBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        sqlBuilder.between(getColumn(column), value1, value2);
        return this;
    }

    protected LambdaSqlBuilder<T> or() {
        sqlBuilder.or();
        return this;
    }

    protected LambdaSqlBuilder<T> and() {
        sqlBuilder.and();
        return this;
    }

    // 子查询条件
    protected LambdaSqlBuilder<T> exists(LambdaSqlBuilder<?> subQuery) {
        SqlBuilder subBuilder = SqlBuilder.builder();
        // 构建子查询
        SqlBuilder.QueryResult subResult = subQuery.buildInternal();
        subBuilder.exists(convertToSqlBuilder(subQuery));
        return this;
    }

    protected LambdaSqlBuilder<T> notExists(LambdaSqlBuilder<?> subQuery) {
        SqlBuilder subBuilder = SqlBuilder.builder();
        subBuilder.notExists(convertToSqlBuilder(subQuery));
        return this;
    }

    // ==================== 分组和排序 ====================

    @SafeVarargs
    protected final LambdaSqlBuilder<T> groupBy(SFunction<T, ?>... columns) {
        String[] columnNames = Arrays.stream(columns)
                .map(this::getColumn)
                .toArray(String[]::new);
        sqlBuilder.groupBy(columnNames);
        return this;
    }

    protected LambdaSqlBuilder<T> having(String condition, Object... values) {
        sqlBuilder.having(condition, values);
        return this;
    }

    protected LambdaSqlBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        sqlBuilder.orderBy(getColumn(column), orderType);
        return this;
    }

    protected LambdaSqlBuilder<T> orderByAsc(SFunction<T, ?> column) {
        return orderBy(column, SqlBuilder.OrderType.ASC);
    }

    protected LambdaSqlBuilder<T> orderByDesc(SFunction<T, ?> column) {
        return orderBy(column, SqlBuilder.OrderType.DESC);
    }

    protected LambdaSqlBuilder<T> limit(int limit) {
        sqlBuilder.limit(limit);
        return this;
    }

    protected LambdaSqlBuilder<T> limit(int offset, int limit) {
        sqlBuilder.limit(offset, limit);
        return this;
    }

    protected LambdaSqlBuilder<T> offset(int offset) {
        sqlBuilder.offset(offset);
        return this;
    }

    // ==================== 辅助方法 ====================

    private SqlBuilder convertToSqlBuilder(LambdaSqlBuilder<?> lambdaBuilder) {
        SqlBuilder.QueryResult result = lambdaBuilder.buildInternal();
        SqlBuilder builder = SqlBuilder.builder();

        // 根据 SQL 类型设置构建器
        if (result.getSqlType() == SQLType.SELECT) {
            // 这里需要解析 SQL 来设置，简化处理：直接使用原始 SQL
            // 更好的做法是让 LambdaSqlBuilder 直接暴露 SqlBuilder
            builder.select("*");
            builder.from("(" + result.getSql() + ")");
        }

        return builder;
    }

    /**
     * 内部构建方法，不改变 isBuilt 状态
     */
    private SqlBuilder.QueryResult buildInternal() {
        return sqlBuilder.build();
    }

    /**
     * 构建查询结果
     */
    protected QueryResult build() {
        if (isBuilt) {
            throw new IllegalStateException("SQL already built");
        }
        isBuilt = true;

        SqlBuilder.QueryResult result = sqlBuilder.build();

        if (result.isBatch()) {
            return new BatchQueryResult(
                    result.getSql(),
                    result instanceof SqlBuilder.BatchQueryResult
                            ? ((SqlBuilder.BatchQueryResult) result).getBatchParams()
                            : null,
                    result.getSqlType()
            );
        } else {
            return new SingleQueryResult(
                    result.getSql(),
                    result instanceof SqlBuilder.SingleQueryResult
                            ? ((SqlBuilder.SingleQueryResult) result).getParams()
                            : new Object[0],
                    result.getSqlType()
            );
        }
    }

    protected LambdaSqlBuilder<T> print() {
        sqlBuilder.print();
        return this;
    }

    protected LambdaSqlBuilder<T> clear() {
        sqlBuilder.clear();
        isBuilt = false;
        return this;
    }

    // ==================== 接口实现 ====================

    @Override
    public String getSqlTemp() {
        return sqlBuilder.getSqlTemp();
    }

    @Override
    public SQLType getType() {
        return sqlBuilder.getType();
    }

    @Override
    public Object[] getParams() {
        return sqlBuilder.getParams();
    }

    @Override
    public List<Object[]> getBatchParams() {
        return sqlBuilder.getBatchParams();
    }

    // ==================== 查询结果类 ====================

    protected interface QueryResult {
        String getSql();

        SQLType getSqlType();

        boolean isBatch();
    }

    protected static class SingleQueryResult implements QueryResult {
        private final String sql;
        private final Object[] params;
        private final SQLType sqlType;

        protected SingleQueryResult(String sql, Object[] params, SQLType sqlType) {
            this.sql = sql;
            this.params = params;
            this.sqlType = sqlType;
        }

        @Override
        public String getSql() {
            return sql;
        }

        @Override
        public SQLType getSqlType() {
            return sqlType;
        }

        @Override
        public boolean isBatch() {
            return false;
        }

        protected Object[] getParams() {
            return params;
        }

        @Override
        public String toString() {
            return "SingleQueryResult{sqlType=" + sqlType +
                    ", sql='" + sql + '\'' +
                    ", params=" + Arrays.toString(params) + "}";
        }
    }

    protected static class BatchQueryResult implements QueryResult {
        private final String sql;
        private final List<Object[]> batchParams;
        private final SQLType sqlType;

        protected BatchQueryResult(String sql, List<Object[]> batchParams, SQLType sqlType) {
            this.sql = sql;
            this.batchParams = batchParams;
            this.sqlType = sqlType;
        }

        @Override
        public String getSql() {
            return sql;
        }

        @Override
        public SQLType getSqlType() {
            return sqlType;
        }

        @Override
        public boolean isBatch() {
            return true;
        }

        protected List<Object[]> getBatchParams() {
            return batchParams;
        }

        @Override
        public String toString() {
            return "BatchQueryResult{sqlType=" + sqlType +
                    ", sql='" + sql + '\'' +
                    ", batchParams.size=" + (batchParams != null ? batchParams.size() : 0) + "}";
        }
    }
}