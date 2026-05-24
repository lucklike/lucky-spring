package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.SQLType;
import io.github.lucklike.httpclient.dbclient.executor.SQLWrapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL构建工具类 - 优化版
 * 支持任意顺序调用，自动处理 SQL 语句结构
 * 支持批量SQL操作，参数为List<Object[]>格式
 * 支持SQL类型标识
 *
 * @author fukang
 * @version 2.0.0
 * @date 2026/5/25
 */
public class SqlBuilder implements SQLWrapper {

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

    // 状态
    private boolean useBatch;
    private boolean isBuilt;
    private SQLType sqlType;

    // WHERE 条件状态
    private boolean hasWhere;
    private boolean needAndPrefix;

    // 用于标记当前构建的是 DELETE 语句
    private boolean isDeleteStatement;

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
    private static final String DELETE_FROM = "DELETE FROM ";
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

    private SqlBuilder() {
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
        this.useBatch = false;
        this.isBuilt = false;
        this.sqlType = null;
        this.hasWhere = false;
        this.needAndPrefix = false;
        this.isDeleteStatement = false;
    }

    public static SqlBuilder builder() {
        return new SqlBuilder();
    }

    // ==================== SELECT 相关方法 ====================

    public SqlBuilder select(String... columns) {
        setSqlType(SQLType.SELECT);

        if (selectBuilder.length() > 0) {
            selectBuilder.append(", ");
        }

        if (columns == null || columns.length == 0) {
            selectBuilder.append("*");
        } else {
            selectBuilder.append(String.join(", ", columns));
        }
        return this;
    }

    public SqlBuilder selectDistinct(String... columns) {
        select(columns);
        String current = selectBuilder.toString();
        selectBuilder.setLength(0);
        selectBuilder.append("DISTINCT ").append(current);
        return this;
    }

    public SqlBuilder count(String column) {
        return select("COUNT(" + column + ")");
    }

    public SqlBuilder count() {
        return select("COUNT(*)");
    }

    // ==================== FROM 相关方法 ====================

    public SqlBuilder from(String table) {
        fromBuilder.append(table);
        return this;
    }

    public SqlBuilder from(String table, String alias) {
        fromBuilder.append(table).append(AS).append(alias);
        return this;
    }

    public SqlBuilder from(SqlBuilder subQuery, String alias) {
        fromBuilder.append("(").append(subQuery.getSqlTemp()).append(")");
        if (alias != null && !alias.isEmpty()) {
            fromBuilder.append(AS).append(alias);
        }
        params.addAll(subQuery.getParamsList());
        return this;
    }

    // ==================== JOIN 相关方法 ====================

    public SqlBuilder join(JoinType type, String table, String alias) {
        String joinKeyword;
        switch (type) {
            case INNER: joinKeyword = INNER_JOIN; break;
            case LEFT: joinKeyword = LEFT_JOIN; break;
            case RIGHT: joinKeyword = RIGHT_JOIN; break;
            default: joinKeyword = INNER_JOIN;
        }
        joinBuilder.append(joinKeyword).append(table);
        if (alias != null && !alias.isEmpty()) {
            joinBuilder.append(AS).append(alias);
        }
        return this;
    }

    public SqlBuilder innerJoin(String table, String alias) {
        return join(JoinType.INNER, table, alias);
    }

    public SqlBuilder leftJoin(String table, String alias) {
        return join(JoinType.LEFT, table, alias);
    }

    public SqlBuilder rightJoin(String table, String alias) {
        return join(JoinType.RIGHT, table, alias);
    }

    public SqlBuilder on(String condition) {
        joinBuilder.append(ON).append(condition);
        return this;
    }

    // ==================== INSERT 相关方法 ====================

    public SqlBuilder insertInto(String table, String... columns) {
        setSqlType(SQLType.UPDATE);
        insertIntoBuilder.append(table);

        if (columns != null && columns.length > 0) {
            insertColumnsBuilder.append("(").append(String.join(", ", columns)).append(")");
        }
        return this;
    }

    public SqlBuilder values(Object... values) {
        insertValuesBuilder.append("(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) insertValuesBuilder.append(", ");
            insertValuesBuilder.append("?");
            params.add(values[i]);
        }
        insertValuesBuilder.append(")");
        return this;
    }

    public SqlBuilder valuesBatch(List<Object[]> batchValues) {
        if (batchValues == null || batchValues.isEmpty()) {
            return this;
        }

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

    public SqlBuilder valuesBatchTemplate(List<Object[]> batchValues, String valueTemplate) {
        if (batchValues == null || batchValues.isEmpty()) {
            return this;
        }

        this.useBatch = true;
        setSqlType(SQLType.BATCH);

        for (int i = 0; i < batchValues.size(); i++) {
            if (i > 0) insertValuesBuilder.append(", ");
            insertValuesBuilder.append(valueTemplate);
        }

        this.batchParams.addAll(batchValues);
        return this;
    }

    // ==================== UPDATE 相关方法 ====================

    public SqlBuilder update(String table) {
        setSqlType(SQLType.UPDATE);
        fromBuilder.append(table);
        return this;
    }

    public SqlBuilder set(String column, Object value) {
        if (setBuilder.length() > 0) {
            setBuilder.append(", ");
        }
        setBuilder.append(column).append(" = ?");
        params.add(value);
        return this;
    }

    public SqlBuilder set(Map<String, Object> columnValues) {
        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
            if (setBuilder.length() > 0) {
                setBuilder.append(", ");
            }
            setBuilder.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
        }
        return this;
    }

    public SqlBuilder batchUpdate(String table) {
        update(table);
        this.useBatch = true;
        setSqlType(SQLType.BATCH);
        return this;
    }

    // ==================== DELETE 相关方法 ====================

    public SqlBuilder delete() {
        setSqlType(SQLType.UPDATE);
        this.isDeleteStatement = true;
        return this;
    }

    public SqlBuilder deleteFrom(String table) {
        setSqlType(SQLType.UPDATE);
        this.isDeleteStatement = true;
        fromBuilder.append(table);
        return this;
    }

    public SqlBuilder batchDelete() {
        delete();
        this.useBatch = true;
        setSqlType(SQLType.BATCH);
        return this;
    }

    public SqlBuilder batchDeleteFrom(String table) {
        deleteFrom(table);
        this.useBatch = true;
        setSqlType(SQLType.BATCH);
        return this;
    }

    // ==================== WHERE 条件方法 ====================

    public SqlBuilder where(String condition, Object... values) {
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

    public SqlBuilder eq(String column, Object value) {
        return condition(column, " = ?", value);
    }

    public SqlBuilder ne(String column, Object value) {
        return condition(column, " <> ?", value);
    }

    public SqlBuilder gt(String column, Object value) {
        return condition(column, " > ?", value);
    }

    public SqlBuilder ge(String column, Object value) {
        return condition(column, " >= ?", value);
    }

    public SqlBuilder lt(String column, Object value) {
        return condition(column, " < ?", value);
    }

    public SqlBuilder le(String column, Object value) {
        return condition(column, " <= ?", value);
    }

    public SqlBuilder like(String column, String value) {
        return condition(column, " LIKE ?", "%" + value + "%");
    }

    public SqlBuilder likeLeft(String column, String value) {
        return condition(column, " LIKE ?", "%" + value);
    }

    public SqlBuilder likeRight(String column, String value) {
        return condition(column, " LIKE ?", value + "%");
    }

    public SqlBuilder notLike(String column, String value) {
        return condition(column, " NOT LIKE ?", "%" + value + "%");
    }

    public SqlBuilder in(String column, Object... values) {
        if (values == null || values.length == 0) return this;
        String placeholders = Arrays.stream(values).map(v -> "?").collect(Collectors.joining(", "));
        return condition(column, " IN (" + placeholders + ")", values);
    }

    public SqlBuilder in(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) return this;
        String placeholders = values.stream().map(v -> "?").collect(Collectors.joining(", "));
        return condition(column, " IN (" + placeholders + ")", values.toArray());
    }

    public SqlBuilder notIn(String column, Object... values) {
        if (values == null || values.length == 0) return this;
        String placeholders = Arrays.stream(values).map(v -> "?").collect(Collectors.joining(", "));
        return condition(column, " NOT IN (" + placeholders + ")", values);
    }

    public SqlBuilder between(String column, Object value1, Object value2) {
        condition(column, " BETWEEN ? AND ?", value1);
        params.add(value2);
        return this;
    }

    public SqlBuilder isNull(String column) {
        return condition(column, " IS NULL");
    }

    public SqlBuilder isNotNull(String column) {
        return condition(column, " IS NOT NULL");
    }

    public SqlBuilder exists(SqlBuilder subQuery) {
        if (!hasWhere) {
            whereBuilder.append(WHERE);
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereBuilder.append(AND);
        }
        whereBuilder.append("EXISTS (");
        whereBuilder.append(subQuery.getSqlTemp());
        whereBuilder.append(")");
        params.addAll(subQuery.getParamsList());
        needAndPrefix = true;
        return this;
    }

    public SqlBuilder notExists(SqlBuilder subQuery) {
        if (!hasWhere) {
            whereBuilder.append(WHERE);
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereBuilder.append(AND);
        }
        whereBuilder.append("NOT EXISTS (");
        whereBuilder.append(subQuery.getSqlTemp());
        whereBuilder.append(")");
        params.addAll(subQuery.getParamsList());
        needAndPrefix = true;
        return this;
    }

    public SqlBuilder whereSql(String sqlFragment, Object... values) {
        if (!hasWhere) {
            whereBuilder.append(WHERE);
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereBuilder.append(AND);
        }
        whereBuilder.append(sqlFragment);
        if (values != null) {
            Collections.addAll(params, values);
        }
        needAndPrefix = true;
        return this;
    }

    public SqlBuilder or() {
        if (hasWhere) {
            whereBuilder.append(OR);
            needAndPrefix = false;
        }
        return this;
    }

    public SqlBuilder and() {
        if (hasWhere) {
            whereBuilder.append(AND);
            needAndPrefix = false;
        }
        return this;
    }

    public SqlBuilder bracketStart() {
        if (!hasWhere) {
            whereBuilder.append(WHERE);
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereBuilder.append(AND);
        }
        whereBuilder.append("(");
        needAndPrefix = false;
        return this;
    }

    public SqlBuilder bracketEnd() {
        whereBuilder.append(")");
        needAndPrefix = true;
        return this;
    }

    private SqlBuilder condition(String column, String operator, Object... values) {
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

    public SqlBuilder groupBy(String... columns) {
        if (groupByBuilder.length() > 0) {
            groupByBuilder.append(", ");
        }
        groupByBuilder.append(String.join(", ", columns));
        return this;
    }

    public SqlBuilder having(String condition, Object... values) {
        havingBuilder.append(condition);
        if (values != null) {
            Collections.addAll(params, values);
        }
        return this;
    }

    public SqlBuilder orderBy(String column, OrderType orderType) {
        if (orderByBuilder.length() > 0) {
            orderByBuilder.append(", ");
        }
        orderByBuilder.append(column);
        orderByBuilder.append(orderType == OrderType.DESC ? DESC : ASC);
        return this;
    }

    public SqlBuilder orderBy(Map<String, OrderType> orderMap) {
        for (Map.Entry<String, OrderType> entry : orderMap.entrySet()) {
            if (orderByBuilder.length() > 0) {
                orderByBuilder.append(", ");
            }
            orderByBuilder.append(entry.getKey());
            orderByBuilder.append(entry.getValue() == OrderType.DESC ? DESC : ASC);
        }
        return this;
    }

    public SqlBuilder limit(int limit) {
        limitBuilder.append("?");
        params.add(limit);
        return this;
    }

    public SqlBuilder limit(int offset, int limit) {
        limitBuilder.append("? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return this;
    }

    public SqlBuilder offset(int offset) {
        limitBuilder.append("OFFSET ?");
        params.add(offset);
        return this;
    }

    // ==================== 其他方法 ====================

    public SqlBuilder append(String sqlFragment, Object... values) {
        // 智能追加，根据当前 SQL 类型决定追加到哪里
        if (sqlType == SQLType.SELECT) {
            // 对于 SELECT，简单追加到末尾（用户需自行确保语法正确）
            // 这里简单处理，直接追加到主 SQL
            if (fromBuilder.length() == 0 && joinBuilder.length() == 0 && whereBuilder.length() == 0) {
                // 可能是在 SELECT 阶段，暂时不做处理
            }
        }
        // 默认追加到末尾
        if (whereBuilder.length() > 0) {
            whereBuilder.append(" ").append(sqlFragment);
        } else if (fromBuilder.length() > 0) {
            fromBuilder.append(" ").append(sqlFragment);
        } else {
            // 简单追加，可能导致问题，建议直接使用具体方法
        }
        if (values != null) {
            Collections.addAll(params, values);
        }
        return this;
    }

    public SqlBuilder clear() {
        selectBuilder.setLength(0);
        fromBuilder.setLength(0);
        joinBuilder.setLength(0);
        setBuilder.setLength(0);
        whereBuilder.setLength(0);
        groupByBuilder.setLength(0);
        havingBuilder.setLength(0);
        orderByBuilder.setLength(0);
        limitBuilder.setLength(0);
        insertIntoBuilder.setLength(0);
        insertColumnsBuilder.setLength(0);
        insertValuesBuilder.setLength(0);
        params.clear();
        batchParams.clear();
        useBatch = false;
        isBuilt = false;
        sqlType = null;
        hasWhere = false;
        needAndPrefix = false;
        isDeleteStatement = false;
        return this;
    }

    public SqlBuilder copy() {
        SqlBuilder copy = new SqlBuilder();
        copy.selectBuilder.append(this.selectBuilder);
        copy.fromBuilder.append(this.fromBuilder);
        copy.joinBuilder.append(this.joinBuilder);
        copy.setBuilder.append(this.setBuilder);
        copy.whereBuilder.append(this.whereBuilder);
        copy.groupByBuilder.append(this.groupByBuilder);
        copy.havingBuilder.append(this.havingBuilder);
        copy.orderByBuilder.append(this.orderByBuilder);
        copy.limitBuilder.append(this.limitBuilder);
        copy.insertIntoBuilder.append(this.insertIntoBuilder);
        copy.insertColumnsBuilder.append(this.insertColumnsBuilder);
        copy.insertValuesBuilder.append(this.insertValuesBuilder);
        copy.params.addAll(this.params);
        for (Object[] batch : this.batchParams) {
            copy.batchParams.add(batch.clone());
        }
        copy.useBatch = this.useBatch;
        copy.sqlType = this.sqlType;
        copy.hasWhere = this.hasWhere;
        copy.needAndPrefix = this.needAndPrefix;
        copy.isDeleteStatement = this.isDeleteStatement;
        return copy;
    }

    public SqlBuilder setSqlType(SQLType type) {
        if (this.sqlType == SQLType.BATCH && type != SQLType.BATCH) {
            return this;
        }
        if (this.sqlType == SQLType.UPDATE && type == SQLType.SELECT) {
            return this;
        }
        if (this.sqlType == null) {
            this.sqlType = type;
        }
        return this;
    }

    public SqlBuilder forceSqlType(SQLType type) {
        this.sqlType = type;
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
        else if (sqlType == SQLType.UPDATE && !isDeleteStatement) {
            if (fromBuilder.length() > 0 && setBuilder.length() > 0) {
                sql.append(UPDATE).append(fromBuilder);
                sql.append(SET).append(setBuilder);

                if (whereBuilder.length() > 0) {
                    sql.append(whereBuilder);
                }

                if (limitBuilder.length() > 0) {
                    sql.append(LIMIT).append(limitBuilder);
                }
            }
        }

        // 构建 DELETE 语句
        else if (sqlType == SQLType.UPDATE && isDeleteStatement) {
            sql.append(DELETE_FROM);
            if (fromBuilder.length() > 0) {
                sql.append(fromBuilder);
            }

            if (whereBuilder.length() > 0) {
                sql.append(whereBuilder);
            }

            if (limitBuilder.length() > 0) {
                sql.append(LIMIT).append(limitBuilder);
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

    public SqlBuilder print() {
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

    public List<Object> getParamsList() {
        return params;
    }

    @Override
    public List<Object[]> getBatchParams() {
        return useBatch ? new ArrayList<>(batchParams) : null;
    }

    public boolean isBatch() {
        return useBatch;
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
            return "SingleQueryResult{" +
                    "sqlType=" + sqlType +
                    ", sql='" + sql + '\'' +
                    ", params=" + Arrays.toString(params) +
                    '}';
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
            return "BatchQueryResult{" +
                    "sqlType=" + sqlType +
                    ", sql='" + sql + '\'' +
                    ", batchParams.size=" + batchParams.size() +
                    '}';
        }
    }
}