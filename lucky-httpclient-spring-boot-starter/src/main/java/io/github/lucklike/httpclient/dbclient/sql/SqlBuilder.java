package io.github.lucklike.httpclient.dbclient.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL构建工具类 - 优化版
 * 支持任意顺序调用，自动处理 SQL 语句结构
 * 支持批量SQL操作，参数为List<Object[]>格式
 * 支持SQL类型标识
 *
 * @author fukang
 * @version 3.0.0
 * @date 2026/5/25
 */
public class SqlBuilder implements SQLWrapper {

    // SQL 各部分的构建器 - 使用带参数的片段
    private final List<SqlFragment> selectFragments;
    private final List<SqlFragment> fromFragments;
    private final List<SqlFragment> joinFragments;
    private final List<SqlFragment> setFragments;
    private final List<SqlFragment> whereFragments;
    private final List<SqlFragment> groupByFragments;
    private final List<SqlFragment> havingFragments;
    private final List<SqlFragment> orderByFragments;
    private final List<SqlFragment> insertIntoFragments;
    private final List<SqlFragment> insertColumnsFragments;
    private final List<SqlFragment> insertValuesFragments;

    // 批量参数存储
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
    private static final String INNER_JOIN = " INNER JOIN ";
    private static final String LEFT_JOIN = " LEFT JOIN ";
    private static final String RIGHT_JOIN = " RIGHT JOIN ";
    private static final String AS = " AS ";
    private static final String ASC = " ASC";
    private static final String DESC = " DESC";

    public enum OrderType { ASC, DESC }
    public enum JoinType { INNER, LEFT, RIGHT }

    /**
     * SQL片段，包含SQL文本和对应的参数
     */
    private static class SqlFragment {
        final String sql;
        final List<Object> params;

        SqlFragment(String sql, Object... params) {
            this.sql = sql;
            this.params = params == null ? Collections.emptyList() : Arrays.asList(params);
        }

        SqlFragment(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params == null ? Collections.emptyList() : new ArrayList<>(params);
        }

        String getSql() {
            return sql;
        }

        List<Object> getParams() {
            return params;
        }
    }

    // ==================== 构造方法 ====================

    private SqlBuilder() {
        this.selectFragments = new ArrayList<>();
        this.fromFragments = new ArrayList<>();
        this.joinFragments = new ArrayList<>();
        this.setFragments = new ArrayList<>();
        this.whereFragments = new ArrayList<>();
        this.groupByFragments = new ArrayList<>();
        this.havingFragments = new ArrayList<>();
        this.orderByFragments = new ArrayList<>();
        this.insertIntoFragments = new ArrayList<>();
        this.insertColumnsFragments = new ArrayList<>();
        this.insertValuesFragments = new ArrayList<>();
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

    // ==================== 辅助方法 ====================

    private void addFragment(List<SqlFragment> fragments, String sql, Object... values) {
        fragments.add(new SqlFragment(sql, values));
    }

    private void addFragmentWithPrefix(List<SqlFragment> fragments, String prefix, String sql, Object... values) {
        if (fragments.isEmpty()) {
            fragments.add(new SqlFragment(prefix + sql, values));
        } else {
            fragments.add(new SqlFragment(sql, values));
        }
    }

    // ==================== SELECT 相关方法 ====================

    public SqlBuilder select(String... columns) {
        setSqlType(SQLType.SELECT);

        String sql;
        if (columns == null || columns.length == 0) {
            sql = "*";
        } else {
            sql = String.join(", ", columns);
        }

        if (selectFragments.isEmpty()) {
            selectFragments.add(new SqlFragment(sql));
        } else {
            selectFragments.add(new SqlFragment(", " + sql));
        }
        return this;
    }

    public SqlBuilder selectDistinct(String... columns) {
        String sql;
        if (columns == null || columns.length == 0) {
            sql = "DISTINCT *";
        } else {
            sql = "DISTINCT " + String.join(", ", columns);
        }

        selectFragments.clear();
        selectFragments.add(new SqlFragment(sql));
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
        addFragment(fromFragments, table);
        return this;
    }

    public SqlBuilder from(String table, String alias) {
        addFragment(fromFragments, table + AS + alias);
        return this;
    }

    public SqlBuilder from(SqlBuilder subQuery, String alias) {
        String sql = "(" + subQuery.getSqlTemp() + ")";
        if (alias != null && !alias.isEmpty()) {
            sql += AS + alias;
        }
        List<Object> subParams = subQuery.getAllParams();
        fromFragments.add(new SqlFragment(sql, subParams));
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
        String sql = joinKeyword + table;
        if (alias != null && !alias.isEmpty()) {
            sql += AS + alias;
        }
        addFragment(joinFragments, sql);
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

    public SqlBuilder on(String condition, Object... values) {
        addFragment(joinFragments, ON + condition, values);
        return this;
    }

    // ==================== INSERT 相关方法 ====================

    public SqlBuilder insertInto(String table, String... columns) {
        setSqlType(SQLType.UPDATE);
        insertIntoFragments.clear();
        insertIntoFragments.add(new SqlFragment(table));

        if (columns != null && columns.length > 0) {
            insertColumnsFragments.clear();
            insertColumnsFragments.add(new SqlFragment("(" + String.join(", ", columns) + ")"));
        }
        return this;
    }

    public SqlBuilder values(Object... values) {
        StringBuilder sb = new StringBuilder("(");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
            params.add(values[i]);
        }
        sb.append(")");

        if (insertValuesFragments.isEmpty()) {
            insertValuesFragments.add(new SqlFragment(sb.toString(), params));
        } else {
            insertValuesFragments.add(new SqlFragment(", " + sb, params));
        }
        return this;
    }

    public SqlBuilder valuesBatch(List<Object[]> batchValues) {
        if (batchValues == null || batchValues.isEmpty()) {
            return this;
        }

        this.useBatch = true;
        setSqlType(SQLType.BATCH);
        insertValuesFragments.clear();

        for (int i = 0; i < batchValues.size(); i++) {
            StringBuilder sb = new StringBuilder();
            if (i > 0) sb.append(", ");
            sb.append("(");
            Object[] values = batchValues.get(i);
            for (int j = 0; j < values.length; j++) {
                if (j > 0) sb.append(", ");
                sb.append("?");
            }
            sb.append(")");
            insertValuesFragments.add(new SqlFragment(sb.toString()));
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
        insertValuesFragments.clear();

        for (int i = 0; i < batchValues.size(); i++) {
            if (i > 0) {
                insertValuesFragments.add(new SqlFragment(", " + valueTemplate));
            } else {
                insertValuesFragments.add(new SqlFragment(valueTemplate));
            }
        }

        this.batchParams.addAll(batchValues);
        return this;
    }

    // ==================== UPDATE 相关方法 ====================

    public SqlBuilder update(String table) {
        setSqlType(SQLType.UPDATE);
        fromFragments.clear();
        addFragment(fromFragments, table);
        return this;
    }

    public SqlBuilder set(String column, Object value) {
        if (setFragments.isEmpty()) {
            addFragment(setFragments, column + " = ?", value);
        } else {
            addFragment(setFragments, ", " + column + " = ?", value);
        }
        return this;
    }

    public SqlBuilder set(Map<String, Object> columnValues) {
        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
            set(entry.getKey(), entry.getValue());
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
        fromFragments.clear();
        addFragment(fromFragments, table);
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
            whereFragments.clear();
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            addFragment(whereFragments, AND + condition, values);
        } else {
            addFragment(whereFragments, condition, values);
            needAndPrefix = true;
        }
        return this;
    }

    public SqlBuilder eq(String column, Object value) {
        return condition(column + " = ?", value);
    }

    public SqlBuilder ne(String column, Object value) {
        return condition(column + " <> ?", value);
    }

    public SqlBuilder gt(String column, Object value) {
        return condition(column + " > ?", value);
    }

    public SqlBuilder ge(String column, Object value) {
        return condition(column + " >= ?", value);
    }

    public SqlBuilder lt(String column, Object value) {
        return condition(column + " < ?", value);
    }

    public SqlBuilder le(String column, Object value) {
        return condition(column + " <= ?", value);
    }

    public SqlBuilder like(String column, String value) {
        return condition(column + " LIKE ?", "%" + value + "%");
    }

    public SqlBuilder likeLeft(String column, String value) {
        return condition(column + " LIKE ?", "%" + value);
    }

    public SqlBuilder likeRight(String column, String value) {
        return condition(column + " LIKE ?", value + "%");
    }

    public SqlBuilder notLike(String column, String value) {
        return condition(column + " NOT LIKE ?", "%" + value + "%");
    }

    public SqlBuilder in(String column, Object... values) {
        if (values == null || values.length == 0) return this;
        String placeholders = Arrays.stream(values).map(v -> "?").collect(Collectors.joining(", "));
        return condition(column + " IN (" + placeholders + ")", values);
    }

    public SqlBuilder in(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) return this;
        String placeholders = values.stream().map(v -> "?").collect(Collectors.joining(", "));
        return condition(column + " IN (" + placeholders + ")", values.toArray());
    }

    public SqlBuilder notIn(String column, Object... values) {
        if (values == null || values.length == 0) return this;
        String placeholders = Arrays.stream(values).map(v -> "?").collect(Collectors.joining(", "));
        return condition(column + " NOT IN (" + placeholders + ")", values);
    }

    public SqlBuilder between(String column, Object value1, Object value2) {
        condition(column + " BETWEEN ? AND ?", value1, value2);
        return this;
    }

    public SqlBuilder isNull(String column) {
        return condition(column + " IS NULL");
    }

    public SqlBuilder isNotNull(String column) {
        return condition(column + " IS NOT NULL");
    }

    public SqlBuilder exists(SqlBuilder subQuery) {
        if (!hasWhere) {
            whereFragments.clear();
            hasWhere = true;
            needAndPrefix = false;
        }
        String sql = "EXISTS (" + subQuery.getSqlTemp() + ")";
        if (needAndPrefix) {
            whereFragments.add(new SqlFragment(AND + sql, subQuery.getAllParams()));
        } else {
            whereFragments.add(new SqlFragment(sql, subQuery.getAllParams()));
            needAndPrefix = true;
        }
        return this;
    }

    public SqlBuilder notExists(SqlBuilder subQuery) {
        if (!hasWhere) {
            whereFragments.clear();
            hasWhere = true;
            needAndPrefix = false;
        }
        String sql = "NOT EXISTS (" + subQuery.getSqlTemp() + ")";
        if (needAndPrefix) {
            whereFragments.add(new SqlFragment(AND + sql, subQuery.getAllParams()));
        } else {
            whereFragments.add(new SqlFragment(sql, subQuery.getAllParams()));
            needAndPrefix = true;
        }
        return this;
    }

    public SqlBuilder whereSql(String sqlFragment, Object... values) {
        if (!hasWhere) {
            whereFragments.clear();
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            addFragment(whereFragments, AND + sqlFragment, values);
        } else {
            addFragment(whereFragments, sqlFragment, values);
            needAndPrefix = true;
        }
        return this;
    }

    public SqlBuilder or() {
        if (hasWhere && !whereFragments.isEmpty()) {
            // 标记下一个条件使用 OR
            needAndPrefix = false;
            // 修改最后一个片段的前缀逻辑比较复杂，这里简单处理：添加一个 OR 标记片段
            whereFragments.add(new SqlFragment(OR));
        }
        return this;
    }

    public SqlBuilder and() {
        if (hasWhere && !whereFragments.isEmpty()) {
            needAndPrefix = false;
            whereFragments.add(new SqlFragment(AND));
        }
        return this;
    }

    public SqlBuilder bracketStart() {
        if (!hasWhere) {
            whereFragments.clear();
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            whereFragments.add(new SqlFragment(AND + "("));
        } else {
            whereFragments.add(new SqlFragment("("));
        }
        needAndPrefix = false;
        return this;
    }

    public SqlBuilder bracketEnd() {
        whereFragments.add(new SqlFragment(")"));
        needAndPrefix = true;
        return this;
    }

    private SqlBuilder condition(String conditionSql, Object... values) {
        if (!hasWhere) {
            whereFragments.clear();
            hasWhere = true;
            needAndPrefix = false;
        }
        if (needAndPrefix) {
            addFragment(whereFragments, AND + conditionSql, values);
        } else {
            addFragment(whereFragments, conditionSql, values);
            needAndPrefix = true;
        }
        return this;
    }

    // ==================== 分组和排序 ====================

    public SqlBuilder groupBy(String... columns) {
        String sql = String.join(", ", columns);
        if (groupByFragments.isEmpty()) {
            addFragment(groupByFragments, sql);
        } else {
            addFragment(groupByFragments, ", " + sql);
        }
        return this;
    }

    public SqlBuilder having(String condition, Object... values) {
        addFragment(havingFragments, condition, values);
        return this;
    }

    public SqlBuilder orderBy(String column, OrderType orderType) {
        String sql = column + (orderType == OrderType.DESC ? DESC : ASC);
        if (orderByFragments.isEmpty()) {
            addFragment(orderByFragments, sql);
        } else {
            addFragment(orderByFragments, ", " + sql);
        }
        return this;
    }

    public SqlBuilder orderBy(Map<String, OrderType> orderMap) {
        for (Map.Entry<String, OrderType> entry : orderMap.entrySet()) {
            orderBy(entry.getKey(), entry.getValue());
        }
        return this;
    }

    // ==================== 其他方法 ====================

    public SqlBuilder clear() {
        selectFragments.clear();
        fromFragments.clear();
        joinFragments.clear();
        setFragments.clear();
        whereFragments.clear();
        groupByFragments.clear();
        havingFragments.clear();
        orderByFragments.clear();
        insertIntoFragments.clear();
        insertColumnsFragments.clear();
        insertValuesFragments.clear();
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

        // 深拷贝所有 SqlFragment
        copy.selectFragments.addAll(copyFragments(this.selectFragments));
        copy.fromFragments.addAll(copyFragments(this.fromFragments));
        copy.joinFragments.addAll(copyFragments(this.joinFragments));
        copy.setFragments.addAll(copyFragments(this.setFragments));
        copy.whereFragments.addAll(copyFragments(this.whereFragments));
        copy.groupByFragments.addAll(copyFragments(this.groupByFragments));
        copy.havingFragments.addAll(copyFragments(this.havingFragments));
        copy.orderByFragments.addAll(copyFragments(this.orderByFragments));
        copy.insertIntoFragments.addAll(copyFragments(this.insertIntoFragments));
        copy.insertColumnsFragments.addAll(copyFragments(this.insertColumnsFragments));
        copy.insertValuesFragments.addAll(copyFragments(this.insertValuesFragments));

        // 深拷贝 batchParams（注意内部对象的深浅取决于使用场景）
        for (Object[] batch : this.batchParams) {
            copy.batchParams.add(batch.clone());
        }

        copy.useBatch = this.useBatch;
        copy.sqlType = this.sqlType;
        copy.hasWhere = this.hasWhere;
        copy.needAndPrefix = this.needAndPrefix;
        copy.isDeleteStatement = this.isDeleteStatement;
        copy.isBuilt = false;  // 关键：重置构建状态

        return copy;
    }

    private List<SqlFragment> copyFragments(List<SqlFragment> fragments) {
        List<SqlFragment> copies = new ArrayList<>();
        for (SqlFragment fragment : fragments) {
            copies.add(new SqlFragment(fragment.sql, new ArrayList<>(fragment.params)));
        }
        return copies;
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

    /**
     * 获取所有参数（按顺序）
     */
    private List<Object> getAllParams() {
        List<Object> allParams = new ArrayList<>();

        // SELECT 参数
        for (SqlFragment fragment : selectFragments) {
            allParams.addAll(fragment.getParams());
        }
        // FROM 参数
        for (SqlFragment fragment : fromFragments) {
            allParams.addAll(fragment.getParams());
        }
        // JOIN 参数
        for (SqlFragment fragment : joinFragments) {
            allParams.addAll(fragment.getParams());
        }
        // SET 参数
        for (SqlFragment fragment : setFragments) {
            allParams.addAll(fragment.getParams());
        }
        // WHERE 参数
        for (SqlFragment fragment : whereFragments) {
            allParams.addAll(fragment.getParams());
        }
        // GROUP BY 参数
        for (SqlFragment fragment : groupByFragments) {
            allParams.addAll(fragment.getParams());
        }
        // HAVING 参数
        for (SqlFragment fragment : havingFragments) {
            allParams.addAll(fragment.getParams());
        }
        // ORDER BY 参数
        for (SqlFragment fragment : orderByFragments) {
            allParams.addAll(fragment.getParams());
        }
        // INSERT 参数
        for (SqlFragment fragment : insertIntoFragments) {
            allParams.addAll(fragment.getParams());
        }
        for (SqlFragment fragment : insertColumnsFragments) {
            allParams.addAll(fragment.getParams());
        }
        for (SqlFragment fragment : insertValuesFragments) {
            allParams.addAll(fragment.getParams());
        }

        return allParams;
    }

    // ==================== 构建 SQL ====================

    private String buildSql() {
        StringBuilder sql = new StringBuilder();

        // 构建 SELECT 语句
        if (sqlType == SQLType.SELECT) {
            if (selectFragments.isEmpty()) {
                sql.append(SELECT).append("*");
            } else {
                sql.append(SELECT);
                for (SqlFragment selectFragment : selectFragments) {
                    sql.append(selectFragment.getSql());
                }
            }

            if (!fromFragments.isEmpty()) {
                sql.append(FROM);
                for (SqlFragment fragment : fromFragments) {
                    sql.append(fragment.getSql());
                }
            }

            if (!joinFragments.isEmpty()) {
                for (SqlFragment fragment : joinFragments) {
                    sql.append(fragment.getSql());
                }
            }

            if (!whereFragments.isEmpty()) {
                sql.append(WHERE);
                for (SqlFragment fragment : whereFragments) {
                    sql.append(fragment.getSql());
                }
            }

            if (!groupByFragments.isEmpty()) {
                sql.append(GROUP_BY);
                for (SqlFragment fragment : groupByFragments) {
                    sql.append(fragment.getSql());
                }
            }

            if (!havingFragments.isEmpty()) {
                sql.append(HAVING);
                for (SqlFragment fragment : havingFragments) {
                    sql.append(fragment.getSql());
                }
            }

            if (!orderByFragments.isEmpty()) {
                sql.append(ORDER_BY);
                for (SqlFragment fragment : orderByFragments) {
                    sql.append(fragment.getSql());
                }
            }
        }

        // 构建 INSERT 语句
        else if (!insertIntoFragments.isEmpty()) {
            sql.append(INSERT_INTO);
            for (SqlFragment fragment : insertIntoFragments) {
                sql.append(fragment.getSql());
            }

            if (!insertColumnsFragments.isEmpty()) {
                sql.append(" ");
                for (SqlFragment fragment : insertColumnsFragments) {
                    sql.append(fragment.getSql());
                }
            }

            if (!insertValuesFragments.isEmpty()) {
                sql.append(VALUES);
                for (SqlFragment fragment : insertValuesFragments) {
                    sql.append(fragment.getSql());
                }
            }
        }

        // 构建 UPDATE 语句
        else if (sqlType == SQLType.UPDATE && !isDeleteStatement) {
            if (!fromFragments.isEmpty()) {
                sql.append(UPDATE);
                for (SqlFragment fragment : fromFragments) {
                    sql.append(fragment.getSql());
                }

                sql.append(SET);
                for (SqlFragment fragment : setFragments) {
                    sql.append(fragment.getSql());
                }

                if (!whereFragments.isEmpty()) {
                    sql.append(WHERE);
                    for (SqlFragment fragment : whereFragments) {
                        sql.append(fragment.getSql());
                    }
                }
            }
        }

        // 构建 DELETE 语句
        else if (sqlType == SQLType.UPDATE) {
            sql.append(DELETE_FROM);

            if (!fromFragments.isEmpty()) {
                for (SqlFragment fragment : fromFragments) {
                    sql.append(fragment.getSql());
                }
            }

            if (!whereFragments.isEmpty()) {
                sql.append(WHERE);
                for (SqlFragment fragment : whereFragments) {
                    sql.append(fragment.getSql());
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
        List<Object> allParams = getAllParams();

        if (useBatch) {
            return new BatchQueryResult(sql, batchParams, sqlType);
        } else {
            return new SingleQueryResult(sql, allParams.toArray(), sqlType);
        }
    }

    public SqlBuilder print() {
        System.out.println("SQL Type: " + sqlType);
        System.out.println("SQL: " + buildSql());
        System.out.println("Params: " + getAllParams());
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
        return getAllParams().toArray();
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