package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.StringUtils;
import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.dbclient.SQLType;
import io.github.lucklike.httpclient.dbclient.annotation.Table;
import io.github.lucklike.httpclient.dbclient.executor.SFunction;
import io.github.lucklike.httpclient.dbclient.executor.SQLWrapper;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 基于 Lambda 表达式的 SQL 构建器 - 优化版
 * 支持任意顺序调用，自动处理 SQL 语句结构
 *
 * @author fukang
 * @version 2.0.0
 * @date 2026/5/25
 */
public class LambdaSqlBuilder<T> implements SQLWrapper {

    private final Class<T> entityClass;
    private String tableName;
    private SQLType sqlType;
    private boolean useBatch;
    private boolean isBuilt;

    // SQL 各部分的构建器
    private final StringBuilder selectBuilder;
    private final StringBuilder fromBuilder;
    private final StringBuilder joinBuilder;
    private final StringBuilder setBuilder;
    private final StringBuilder whereBuilder;
    private final StringBuilder groupByBuilder;
    private final StringBuilder havingBuilder;
    private final StringBuilder orderByBuilder;
    private final StringBuilder limitBuilder;
    private final StringBuilder insertIntoBuilder;
    private final StringBuilder insertColumnsBuilder;
    private final StringBuilder insertValuesBuilder;

    // 参数存储
    private final List<Object> params;
    private final List<Object[]> batchParams;

    // WHERE 条件状态
    private boolean hasWhere;
    private boolean needAndPrefix;

    // SQL关键字
    private static final String SELECT = "SELECT ";
    private static final String FROM = " FROM ";
    private static final String WHERE = " WHERE ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String ON = " ON ";
    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String VALUES = " VALUES ";
    private static final String UPDATE = "UPDATE ";
    private static final String SET = " SET ";
    private static final String DELETE = "DELETE FROM ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String GROUP_BY = " GROUP BY ";
    private static final String HAVING = " HAVING ";
    private static final String LIMIT = " LIMIT ";
    private static final String OFFSET = " OFFSET ";
    private static final String INNER_JOIN = " INNER JOIN ";
    private static final String LEFT_JOIN = " LEFT JOIN ";
    private static final String RIGHT_JOIN = " RIGHT JOIN ";
    private static final String AS = " AS ";
    private static final String ASC = " ASC";
    private static final String DESC = " DESC";

    public enum OrderType { ASC, DESC }
    public enum JoinType { INNER, LEFT, RIGHT }

    // ==================== 构造方法 ====================

    private LambdaSqlBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.selectBuilder = new StringBuilder();
        this.fromBuilder = new StringBuilder();
        this.joinBuilder = new StringBuilder();
        this.setBuilder = new StringBuilder();
        this.whereBuilder = new StringBuilder();
        this.groupByBuilder = new StringBuilder();
        this.havingBuilder = new StringBuilder();
        this.orderByBuilder = new StringBuilder();
        this.limitBuilder = new StringBuilder();
        this.insertIntoBuilder = new StringBuilder();
        this.insertColumnsBuilder = new StringBuilder();
        this.insertValuesBuilder = new StringBuilder();
        this.params = new ArrayList<>();
        this.batchParams = new ArrayList<>();
        this.hasWhere = false;
        this.needAndPrefix = false;
        this.useBatch = false;
        this.isBuilt = false;
        this.sqlType = SQLType.NON;
    }

    public static <T> LambdaSqlBuilder<T> lambda(Class<T> entityClass) {
        return new LambdaSqlBuilder<>(entityClass);
    }

    public static <T> LambdaSqlBuilder<T> select(Class<T> entityClass){
        return lambda(entityClass).select().from();
    }

    public static <T> LambdaSqlBuilder<T> update(Class<T> entityClass){
        return lambda(entityClass).update().from();
    }

    public static <T> LambdaSqlBuilder<T> delete(Class<T> entityClass){
        return lambda(entityClass).delete().from();
    }


    // ==================== 表名和列名辅助方法 ====================

    public LambdaSqlBuilder<T> tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    private String getTableName() {
        if (tableName != null) return tableName;
        Table tableAnn = AnnotationUtils.findMergedAnnotation(entityClass, Table.class);
        if (tableAnn != null && StringUtils.hasText(tableAnn.value())) {
            return tableAnn.value();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    private String getTableName(Class<?> entityClass) {
        Table tableAnn = AnnotationUtils.findMergedAnnotation(entityClass, Table.class);
        if (tableAnn != null && StringUtils.hasText(tableAnn.value())) {
            return tableAnn.value();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    private <R> String getColumn(SFunction<T, R> function) {
        return LambdaUtils.getColumnName(entityClass, function);
    }

    // ==================== SELECT 相关方法 ====================

    @SafeVarargs
    public final LambdaSqlBuilder<T> select(SFunction<T, ?>... columns) {
        if (sqlType == SQLType.NON) setSqlType(SQLType.SELECT);

        if (selectBuilder.length() > 0) {
            selectBuilder.append(", ");
        }

        if (columns == null || columns.length == 0) {
            selectBuilder.append("*");
        } else {
            String columnStr = Arrays.stream(columns)
                    .map(this::getColumn)
                    .collect(Collectors.joining(", "));
            selectBuilder.append(columnStr);
        }
        return this;
    }

    public LambdaSqlBuilder<T> select(String expression) {
        if (sqlType == SQLType.NON) setSqlType(SQLType.SELECT);

        if (selectBuilder.length() > 0) {
            selectBuilder.append(", ");
        }
        selectBuilder.append(expression);
        return this;
    }

    public LambdaSqlBuilder<T> selectCount() {
        return select("COUNT(*)");
    }

    public LambdaSqlBuilder<T> selectCount(SFunction<T, ?> column) {
        return select("COUNT(" + getColumn(column) + ")");
    }

    public LambdaSqlBuilder<T> selectDistinct(SFunction<T, ?>... columns) {
        select(columns);
        String current = selectBuilder.toString();
        selectBuilder.setLength(0);
        selectBuilder.append("DISTINCT ").append(current);
        return this;
    }

    // ==================== FROM 相关方法 ====================

    public LambdaSqlBuilder<T> from() {
        fromBuilder.append(getTableName());
        return this;
    }

    public LambdaSqlBuilder<T> from(String alias) {
        fromBuilder.append(getTableName()).append(AS).append(alias);
        return this;
    }

    public LambdaSqlBuilder<T> from(String tableName, String alias) {
        fromBuilder.append(tableName);
        if (alias != null && !alias.isEmpty()) {
            fromBuilder.append(AS).append(alias);
        }
        return this;
    }

    public LambdaSqlBuilder<T> from(LambdaSqlBuilder<?> subQuery, String alias) {
        fromBuilder.append("(").append(subQuery.buildSql()).append(")");
        if (alias != null && !alias.isEmpty()) {
            fromBuilder.append(AS).append(alias);
        }
        // 注意：这里不能直接添加参数，因为 subQuery 的参数需要合并到主查询
        // 参数会在 build() 时统一处理
        return this;
    }

    // ==================== JOIN 相关方法 ====================

    public <E> LambdaSqlBuilder<T> join(JoinType type, Class<E> joinClass, String alias) {
        String joinTableName = getTableName(joinClass);
        String joinKeyword;
        switch (type) {
            case INNER: joinKeyword = INNER_JOIN; break;
            case LEFT: joinKeyword = LEFT_JOIN; break;
            case RIGHT: joinKeyword = RIGHT_JOIN; break;
            default: joinKeyword = INNER_JOIN;
        }
        joinBuilder.append(joinKeyword).append(joinTableName);
        if (alias != null && !alias.isEmpty()) {
            joinBuilder.append(AS).append(alias);
        }
        return this;
    }

    public <E> LambdaSqlBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        return join(JoinType.INNER, joinClass, alias);
    }

    public <E> LambdaSqlBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        return join(JoinType.LEFT, joinClass, alias);
    }

    public <E> LambdaSqlBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        return join(JoinType.RIGHT, joinClass, alias);
    }

    public LambdaSqlBuilder<T> on(String condition) {
        joinBuilder.append(ON).append(condition);
        return this;
    }

    public <E> LambdaSqlBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        joinBuilder.append(ON).append(getColumn(leftColumn)).append(" = ")
                .append(LambdaUtils.getColumnName(null, rightColumn));
        return this;
    }

    // ==================== INSERT 相关方法 ====================

    @SafeVarargs
    public final LambdaSqlBuilder<T> insertInto(SFunction<T, ?>... columns) {
        setSqlType(SQLType.UPDATE);
        insertIntoBuilder.append(getTableName());

        if (columns != null && columns.length > 0) {
            String columnStr = Arrays.stream(columns)
                    .map(this::getColumn)
                    .collect(Collectors.joining(", "));
            insertColumnsBuilder.append("(").append(columnStr).append(")");
        }
        return this;
    }

    public LambdaSqlBuilder<T> values(Object... values) {
        insertValuesBuilder.append("(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) insertValuesBuilder.append(", ");
            insertValuesBuilder.append("?");
            params.add(values[i]);
        }
        insertValuesBuilder.append(")");
        return this;
    }

    public LambdaSqlBuilder<T> valuesBatch(List<Object[]> batchValues) {
        if (batchValues == null || batchValues.isEmpty()) return this;

        this.useBatch = true;
        setSqlType(SQLType.BATCH);

        for (int i = 0; i < batchValues.size(); i++) {
            if (i > 0) insertValuesBuilder.append(", ");
            insertValuesBuilder.append("(");
            Object[] values = batchValues.get(i);
            for (int j = 0; j < values.length; j++) {
                if (j > 0) insertValuesBuilder.append(", ");
                insertValuesBuilder.append("?");
            }
            insertValuesBuilder.append(")");
        }

        this.batchParams.addAll(batchValues);
        return this;
    }

    // ==================== UPDATE 相关方法 ====================

    public LambdaSqlBuilder<T> update() {
        setSqlType(SQLType.UPDATE);
        return this;
    }

    public <R> LambdaSqlBuilder<T> set(SFunction<T, R> column, Object value) {
        if (setBuilder.length() > 0) {
            setBuilder.append(", ");
        }
        setBuilder.append(getColumn(column)).append(" = ?");
        params.add(value);
        return this;
    }

    public LambdaSqlBuilder<T> set(String column, Object value) {
        if (setBuilder.length() > 0) {
            setBuilder.append(", ");
        }
        setBuilder.append(column).append(" = ?");
        params.add(value);
        return this;
    }

    // ==================== DELETE 相关方法 ====================

    public LambdaSqlBuilder<T> delete() {
        setSqlType(SQLType.UPDATE);
        return this;
    }

    // ==================== WHERE 条件方法 ====================

    public LambdaSqlBuilder<T> where(String condition, Object... values) {
        if (!hasWhere) {
            whereBuilder.append(WHERE);
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereBuilder.append(AND);
        }
        whereBuilder.append(condition);
        if (values != null) {
            Collections.addAll(params, values);
        }
        needAndPrefix = true;
        return this;
    }

    public LambdaSqlBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        if (!hasWhere) {
            whereBuilder.append(WHERE);
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereBuilder.append(AND);
        }
        whereBuilder.append("(");
        boolean oldNeedAndPrefix = this.needAndPrefix;
        boolean oldHasWhere = this.hasWhere;
        this.needAndPrefix = false;
        this.hasWhere = true;
        conditionBuilder.accept(this);
        this.needAndPrefix = oldNeedAndPrefix;
        this.hasWhere = oldHasWhere;
        whereBuilder.append(")");
        needAndPrefix = true;
        return this;
    }

    // 基础条件方法
    public <R> LambdaSqlBuilder<T> eq(SFunction<T, R> column, Object value) {
        return condition(getColumn(column), " = ?", value);
    }

    public <R> LambdaSqlBuilder<T> ne(SFunction<T, R> column, Object value) {
        return condition(getColumn(column), " <> ?", value);
    }

    public <R> LambdaSqlBuilder<T> gt(SFunction<T, R> column, Object value) {
        return condition(getColumn(column), " > ?", value);
    }

    public <R> LambdaSqlBuilder<T> ge(SFunction<T, R> column, Object value) {
        return condition(getColumn(column), " >= ?", value);
    }

    public <R> LambdaSqlBuilder<T> lt(SFunction<T, R> column, Object value) {
        return condition(getColumn(column), " < ?", value);
    }

    public <R> LambdaSqlBuilder<T> le(SFunction<T, R> column, Object value) {
        return condition(getColumn(column), " <= ?", value);
    }

    public LambdaSqlBuilder<T> like(SFunction<T, ?> column, String value) {
        return condition(getColumn(column), " LIKE ?", "%" + value + "%");
    }

    public LambdaSqlBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        return condition(getColumn(column), " LIKE ?", "%" + value);
    }

    public LambdaSqlBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        return condition(getColumn(column), " LIKE ?", value + "%");
    }

    public LambdaSqlBuilder<T> notLike(SFunction<T, ?> column, String value) {
        return condition(getColumn(column), " NOT LIKE ?", "%" + value + "%");
    }

    @SafeVarargs
    public final <R> LambdaSqlBuilder<T> in(SFunction<T, R> column, R... values) {
        if (values == null || values.length == 0) return this;
        String placeholders = Arrays.stream(values).map(v -> "?").collect(Collectors.joining(", "));
        return condition(getColumn(column), " IN (" + placeholders + ")", (Object[]) values);
    }

    public <R> LambdaSqlBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        if (values == null || values.isEmpty()) return this;
        String placeholders = values.stream().map(v -> "?").collect(Collectors.joining(", "));
        return condition(getColumn(column), " IN (" + placeholders + ")", values.toArray());
    }

    public LambdaSqlBuilder<T> isNull(SFunction<T, ?> column) {
        return condition(getColumn(column), " IS NULL");
    }

    public LambdaSqlBuilder<T> isNotNull(SFunction<T, ?> column) {
        return condition(getColumn(column), " IS NOT NULL");
    }

    public LambdaSqlBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        condition(getColumn(column), " BETWEEN ? AND ?", value1);
        params.add(value2);
        return this;
    }

    public LambdaSqlBuilder<T> or() {
        if (hasWhere) {
            whereBuilder.append(OR);
            needAndPrefix = false;
        }
        return this;
    }

    public LambdaSqlBuilder<T> and() {
        if (hasWhere) {
            whereBuilder.append(AND);
            needAndPrefix = false;
        }
        return this;
    }

    private LambdaSqlBuilder<T> condition(String column, String operator, Object... values) {
        if (!hasWhere) {
            whereBuilder.append(WHERE);
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereBuilder.append(AND);
        }
        whereBuilder.append(column).append(operator);
        if (values != null) {
            Collections.addAll(params, values);
        }
        needAndPrefix = true;
        return this;
    }

    // ==================== 分组和排序 ====================

    @SafeVarargs
    public final LambdaSqlBuilder<T> groupBy(SFunction<T, ?>... columns) {
        if (groupByBuilder.length() > 0) {
            groupByBuilder.append(", ");
        }
        String columnStr = Arrays.stream(columns)
                .map(this::getColumn)
                .collect(Collectors.joining(", "));
        groupByBuilder.append(columnStr);
        return this;
    }

    public LambdaSqlBuilder<T> having(String condition, Object... values) {
        havingBuilder.append(condition);
        if (values != null) {
            Collections.addAll(params, values);
        }
        return this;
    }

    public LambdaSqlBuilder<T> orderBy(SFunction<T, ?> column, OrderType orderType) {
        if (orderByBuilder.length() > 0) {
            orderByBuilder.append(", ");
        }
        orderByBuilder.append(getColumn(column));
        orderByBuilder.append(orderType == OrderType.DESC ? DESC : ASC);
        return this;
    }

    public LambdaSqlBuilder<T> orderByAsc(SFunction<T, ?> column) {
        return orderBy(column, OrderType.ASC);
    }

    public LambdaSqlBuilder<T> orderByDesc(SFunction<T, ?> column) {
        return orderBy(column, OrderType.DESC);
    }

    public LambdaSqlBuilder<T> limit(int limit) {
        limitBuilder.append("?");
        params.add(limit);
        return this;
    }

    public LambdaSqlBuilder<T> limit(int offset, int limit) {
        limitBuilder.append("? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return this;
    }

    public LambdaSqlBuilder<T> offset(int offset) {
        limitBuilder.append("OFFSET ?");
        params.add(offset);
        return this;
    }

    // ==================== 构建 SQL ====================

    private String buildSql() {
        StringBuilder sql = new StringBuilder();

        // 构建 SELECT 语句
        if (sqlType == SQLType.SELECT) {
            if (selectBuilder.length() == 0) {
                selectBuilder.append("*");
            }
            sql.append(SELECT).append(selectBuilder);

            if (fromBuilder.length() > 0) {
                sql.append(FROM).append(fromBuilder);
            }

            if (joinBuilder.length() > 0) {
                sql.append(joinBuilder);
            }

            if (whereBuilder.length() > 0) {
                sql.append(whereBuilder);
            }

            if (groupByBuilder.length() > 0) {
                sql.append(GROUP_BY).append(groupByBuilder);
            }

            if (havingBuilder.length() > 0) {
                sql.append(HAVING).append(havingBuilder);
            }

            if (orderByBuilder.length() > 0) {
                sql.append(ORDER_BY).append(orderByBuilder);
            }

            if (limitBuilder.length() > 0) {
                sql.append(LIMIT).append(limitBuilder);
            }
        }

        // 构建 INSERT 语句
        else if (insertIntoBuilder.length() > 0) {
            sql.append(INSERT_INTO).append(insertIntoBuilder);
            if (insertColumnsBuilder.length() > 0) {
                sql.append(" ").append(insertColumnsBuilder);
            }
            if (insertValuesBuilder.length() > 0) {
                sql.append(VALUES).append(insertValuesBuilder);
            }
        }

        // 构建 UPDATE 语句
        else if (sqlType == SQLType.UPDATE) {
            if (setBuilder.length() > 0) {
                sql.append(UPDATE).append(getTableName());
                sql.append(SET).append(setBuilder);

                if (whereBuilder.length() > 0) {
                    sql.append(whereBuilder);
                }

                if (limitBuilder.length() > 0) {
                    sql.append(LIMIT).append(limitBuilder);
                }
            }
            // DELETE 语句
            else if (whereBuilder.length() > 0 || limitBuilder.length() > 0) {
                sql.append(DELETE).append(getTableName());
                if (whereBuilder.length() > 0) {
                    sql.append(whereBuilder);
                }
                if (limitBuilder.length() > 0) {
                    sql.append(LIMIT).append(limitBuilder);
                }
            }
        }

        return sql.toString();
    }

    public QueryResult build() {
        if (isBuilt) {
            throw new IllegalStateException("SQL already built");
        }
        isBuilt = true;

        String sql = buildSql();

        if (useBatch) {
            return new BatchQueryResult(sql, batchParams, sqlType);
        } else {
            return new SingleQueryResult(sql, params.toArray(), sqlType);
        }
    }

    // 调试方法
    public LambdaSqlBuilder<T> print() {
        System.out.println("SQL Type: " + sqlType);
        System.out.println("SQL: " + buildSql());
        System.out.println("Params: " + params);
        if (useBatch) {
            System.out.println("Batch Params: ");
            for (int i = 0; i < batchParams.size(); i++) {
                System.out.println("  Row " + (i + 1) + ": " + Arrays.toString(batchParams.get(i)));
            }
        }
        return this;
    }

    // ==================== 接口实现 ====================

    @Override
    public String getSqlTemp() {
        return buildSql();
    }

    @Override
    public SQLType getType() {
        return sqlType;
    }

    @Override
    public Object[] getParams() {
        return params.toArray();
    }

    @Override
    public List<Object[]> getBatchParams() {
        return useBatch ? new ArrayList<>(batchParams) : null;
    }

    private void setSqlType(SQLType type) {
        if (this.sqlType == SQLType.BATCH && type != SQLType.BATCH) return;
        if (this.sqlType == SQLType.UPDATE && type == SQLType.SELECT) return;
        if (this.sqlType == SQLType.NON) {
            this.sqlType = type;
        }
    }

    // ==================== 查询结果类 ====================

    public interface QueryResult {
        String getSql();
        SQLType getSqlType();
        boolean isBatch();
    }

    public static class SingleQueryResult implements QueryResult {
        private final String sql;
        private final Object[] params;
        private final SQLType sqlType;

        public SingleQueryResult(String sql, Object[] params, SQLType sqlType) {
            this.sql = sql;
            this.params = params;
            this.sqlType = sqlType;
        }

        @Override
        public String getSql() { return sql; }
        @Override
        public SQLType getSqlType() { return sqlType; }
        @Override
        public boolean isBatch() { return false; }
        public Object[] getParams() { return params; }

        @Override
        public String toString() {
            return "SingleQueryResult{sqlType=" + sqlType + ", sql='" + sql + "', params=" + Arrays.toString(params) + "}";
        }
    }

    public static class BatchQueryResult implements QueryResult {
        private final String sql;
        private final List<Object[]> batchParams;
        private final SQLType sqlType;

        public BatchQueryResult(String sql, List<Object[]> batchParams, SQLType sqlType) {
            this.sql = sql;
            this.batchParams = batchParams;
            this.sqlType = sqlType;
        }

        @Override
        public String getSql() { return sql; }
        @Override
        public SQLType getSqlType() { return sqlType; }
        @Override
        public boolean isBatch() { return true; }
        public List<Object[]> getBatchParams() { return new ArrayList<>(batchParams); }

        @Override
        public String toString() {
            return "BatchQueryResult{sqlType=" + sqlType + ", sql='" + sql + "', batchParams.size=" + batchParams.size() + "}";
        }
    }
}