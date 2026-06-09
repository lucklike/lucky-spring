// LambdaSqlBuilder.java

package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.function.EntityUtils;
import io.github.lucklike.httpclient.dbclient.function.LambdaUtils;
import io.github.lucklike.httpclient.dbclient.sql.SQLType;
import io.github.lucklike.httpclient.dbclient.sql.SQLWrapper;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

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
 * <p>该构建器提供类型安全的 Lambda 表达式方式来构建 SQL 语句，
 * 避免了字符串硬编码列名，提高了代码的可维护性和重构安全性。
 *
 * @param <T> 实体类型，用于获取表名和列信息
 * @author fukang
 * @version 3.0.0
 * @date 2026/5/25
 */
public class LambdaSqlBuilder<T> implements SQLWrapper {

    /**
     * 实体类类型，用于通过反射获取表名和列名映射关系
     */
    private final Class<T> entityClass;

    /**
     * 表名（可自定义，若不设置则从实体类注解中获取）
     */
    private String tableName;

    /**
     * 底层 SQL 构建器，实际执行 SQL 语句的构建
     */
    private final SqlBuilder sqlBuilder;

    /**
     * 标记是否已经构建过 SQL，防止重复构建
     */
    private boolean isBuilt;

    /**
     * 是否处于嵌套条件构建中（用于处理括号内的复合条件）
     */
    private boolean inNestedCondition;

    /**
     * 嵌套条件中的参数列表
     */
    private List<Object> nestedParams;

    // ==================== 构造方法 ====================

    /**
     * 受保护的构造方法，通过子类或静态工厂方法创建实例
     *
     * @param entityClass 实体类类型
     */
    protected LambdaSqlBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.sqlBuilder = SqlBuilder.builder();
        this.isBuilt = false;
        this.inNestedCondition = false;
        this.nestedParams = new ArrayList<>();
    }

    /**
     * 拷贝构造函数。
     * <p>
     * 基于现有的 LambdaSqlBuilder 实例创建一个深拷贝副本。
     * 复制后的新实例与原实例相互独立，修改一个不会影响另一个。
     * <p>
     * 复制内容包括：实体类类型、表名、底层 SqlBuilder（深拷贝）、嵌套条件状态及参数列表。
     * 特别地，构建状态 isBuilt 会被重置为 false，确保新实例可以正常使用。
     *
     * @param lambdaSqlBuilder 要复制的源 LambdaSqlBuilder 实例
     */
    protected LambdaSqlBuilder(LambdaSqlBuilder<T> lambdaSqlBuilder) {
        this.entityClass = lambdaSqlBuilder.entityClass;
        this.tableName = lambdaSqlBuilder.tableName;
        this.sqlBuilder = lambdaSqlBuilder.sqlBuilder.copy();
        this.isBuilt = false;
        this.inNestedCondition = lambdaSqlBuilder.inNestedCondition;
        this.nestedParams = new ArrayList<>(lambdaSqlBuilder.nestedParams);
    }

    /**
     * 创建 LambdaSqlBuilder 实例的静态工厂方法
     *
     * @param entityClass 实体类类型
     * @param <T>         实体类型泛型
     * @return LambdaSqlBuilder 实例
     */
    public static <T> LambdaSqlBuilder<T> of(Class<T> entityClass) {
        return new LambdaSqlBuilder<>(entityClass);
    }

    public SqlBuilder getSqlBuilder() {
        return sqlBuilder;
    }

    // ==================== 表名和列名辅助方法 ====================

    /**
     * 设置自定义表名（覆盖实体类注解中配置的表名）
     *
     * @param tableName 自定义表名
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * 获取表名（优先使用自定义表名，否则从实体类注解获取）
     *
     * @return 表名
     */
    private String getTableName() {
        if (tableName != null) return tableName;
        return EntityUtils.getTableName(entityClass);
    }

    /**
     * 通过 Lambda 函数获取对应的数据库列名
     *
     * @param function Lambda 函数，如 User::getName
     * @param <R>      属性类型
     * @return 列名
     */
    private <R> String getColumn(SFunction<T, R> function) {
        return LambdaUtils.getColumnName(entityClass, function);
    }

    // ==================== SELECT 相关方法 ====================

    /**
     * 设置要查询的列
     *
     * @param columns 要查询的列对应的 Lambda 函数，若为空则表示查询所有列（SELECT *）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> select(SFunction<T, ?>... columns) {
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

    /**
     * 设置要查询的表达式（支持复杂查询，如函数、计算等）
     *
     * @param expression SQL 表达式，如 "COUNT(*)" 或 "YEAR(create_time)"
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> select(String expression) {
        sqlBuilder.select(expression);
        return this;
    }

    /**
     * 设置查询总记录数（SELECT COUNT(*)）
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> selectCount() {
        sqlBuilder.count();
        return this;
    }

    /**
     * 设置查询指定列的非空记录数（SELECT COUNT(column)）
     *
     * @param column 要统计的列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> selectCount(SFunction<T, ?> column) {
        sqlBuilder.count(getColumn(column));
        return this;
    }

    /**
     * 设置去重查询（SELECT DISTINCT）
     *
     * @param columns 要去重的列对应的 Lambda 函数，若为空则表示 SELECT DISTINCT *
     * @return 当前构建器实例，支持链式调用
     */
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

    /**
     * 设置 FROM 子句，使用实体类映射的表名
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> from() {
        sqlBuilder.from(getTableName());
        return this;
    }

    /**
     * 设置 FROM 子句并为表指定别名
     *
     * @param alias 表别名
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> from(String alias) {
        sqlBuilder.from(getTableName(), alias);
        return this;
    }

    /**
     * 设置 FROM 子句，使用指定的表名和别名
     *
     * @param tableName 表名
     * @param alias     表别名
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> from(String tableName, String alias) {
        sqlBuilder.from(tableName, alias);
        return this;
    }

    /**
     * 设置 FROM 子句，使用子查询作为数据源
     *
     * @param subQuery 子查询构建器
     * @param alias    子查询别名
     * @return 当前构建器实例，支持链式调用
     */
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

    /**
     * 添加 JOIN 关联
     *
     * @param type      JOIN 类型（INNER、LEFT、RIGHT）
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <E> LambdaSqlBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        String joinTableName = EntityUtils.getTableName(joinClass);
        sqlBuilder.join(
                type,
                joinTableName,
                alias
        );
        return this;
    }

    /**
     * 添加 INNER JOIN
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <E> LambdaSqlBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        return join(SqlBuilder.JoinType.INNER, joinClass, alias);
    }

    /**
     * 添加 LEFT JOIN
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <E> LambdaSqlBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        return join(SqlBuilder.JoinType.LEFT, joinClass, alias);
    }

    /**
     * 添加 RIGHT JOIN
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <E> LambdaSqlBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        return join(SqlBuilder.JoinType.RIGHT, joinClass, alias);
    }

    /**
     * 设置 JOIN 条件（原始 SQL 表达式）
     *
     * @param condition JOIN 条件表达式，如 "a.id = b.user_id"
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> on(String condition) {
        sqlBuilder.on(condition);
        return this;
    }

    /**
     * 设置 JOIN 条件（使用 Lambda 表达式，主表列等于关联表列）
     *
     * @param leftColumn  主表的列
     * @param rightColumn 关联表的列
     * @param <E>         关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <E> LambdaSqlBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        String condition = getColumn(leftColumn) + " = " + LambdaUtils.getColumnName(null, rightColumn);
        sqlBuilder.on(condition);
        return this;
    }

    // ==================== INSERT 相关方法 ====================

    /**
     * 设置 INSERT INTO 子句
     *
     * @param columns 要插入的列对应的 Lambda 函数，若为空则表示插入所有列
     * @return 当前构建器实例，支持链式调用
     */
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

    /**
     * 设置插入的值
     *
     * @param values 要插入的值，顺序需与 insertInto 中指定的列顺序一致
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> values(Object... values) {
        sqlBuilder.values(values);
        return this;
    }

    /**
     * 设置批量插入的值（用于批量插入操作）
     *
     * @param batchValues 批量值列表，每个元素对应一条记录的值数组
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> valuesBatch(List<Object[]> batchValues) {
        sqlBuilder.valuesBatch(batchValues);
        return this;
    }

// ==================== UPDATE 相关方法 ====================

    /**
     * 设置要更新的列和值（使用 Lambda 表达式指定列），支持条件判断
     *
     * @param condition 条件判断，为 true 时才执行此设置
     * @param column    要更新的列对应的 Lambda 函数
     * @param value     更新的值
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> set(boolean condition, SFunction<T, R> column, Object value) {
        if (condition) {
            return set(column, value);
        }
        return this;
    }

    /**
     * 设置要更新的列和值（直接使用列名字符串），支持条件判断
     *
     * @param condition 条件判断，为 true 时才执行此设置
     * @param column    列名
     * @param value     更新的值
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> set(boolean condition, String column, Object value) {
        if (condition) {
            return set(column, value);
        }
        return this;
    }

    /**
     * 设置 UPDATE 子句，使用实体类映射的表名
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> updateSQL() {
        sqlBuilder.update(getTableName());
        return this;
    }

    /**
     * 设置要更新的列和值（使用 Lambda 表达式指定列）
     *
     * @param column 要更新的列对应的 Lambda 函数
     * @param value  更新的值
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> set(SFunction<T, R> column, Object value) {
        sqlBuilder.set(getColumn(column), value);
        return this;
    }

    /**
     * 设置要更新的列和值（直接使用列名字符串）
     *
     * @param column 列名
     * @param value  更新的值
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> set(String column, Object value) {
        sqlBuilder.set(column, value);
        return this;
    }

    /**
     * 批量设置要更新的列值对
     *
     * @param columnValues 列名与值的映射
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> set(Map<String, Object> columnValues) {
        sqlBuilder.set(columnValues);
        return this;
    }

    // ==================== DELETE 相关方法 ====================

    /**
     * 设置 DELETE 语句（不带 FROM 关键字）
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> deleteSQL() {
        sqlBuilder.delete();
        sqlBuilder.from(getTableName());
        return this;
    }

    /**
     * 设置 DELETE FROM 语句
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> deleteFrom() {
        sqlBuilder.deleteFrom(getTableName());
        return this;
    }

    // ==================== WHERE 条件方法 ====================

    /**
     * 添加 WHERE 条件（原始 SQL 表达式）
     *
     * @param condition 条件表达式，如 "age > ?" 或 "name = ?"
     * @param values    条件参数值，用于替换表达式中的占位符
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> where(String condition, Object... values) {
        sqlBuilder.where(condition, values);
        return this;
    }

    /**
     * 添加 WHERE 条件（通过 Lambda 构建器嵌套构建复合条件）
     *
     * <p>示例：
     * <pre>
     * builder.where(b -> {
     *     b.eq(User::getStatus, 1).or().eq(User::getStatus, 2);
     * });
     * </pre>
     *
     * @param conditionBuilder 条件构建器函数，接收 LambdaSqlBuilder 实例进行条件构建
     * @return 当前构建器实例，支持链式调用
     */
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

// ==================== 基础条件方法 ====================

    /**
     * 等于条件（=），支持条件判断
     * <p>
     * 只有当 condition 为 true 时，才会添加等于条件。
     * 适用于动态条件构建场景。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * boolean filterByAge = true;
     * builder.eq(() -> filterByAge, User::getAge, 18);
     * }</pre>
     * </p>
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     比较的值
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        if (condition) {
            return eq(column, value);
        }
        return this;
    }

    /**
     * 不等于条件（&lt;&gt; 或 !=），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     比较的值
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        if (condition) {
            return ne(column, value);
        }
        return this;
    }

    /**
     * 大于条件（&gt;），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     比较的值
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        if (condition) {
            return gt(column, value);
        }
        return this;
    }

    /**
     * 大于等于条件（&gt;=），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     比较的值
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        if (condition) {
            return ge(column, value);
        }
        return this;
    }

    /**
     * 小于条件（&lt;），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     比较的值
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        if (condition) {
            return lt(column, value);
        }
        return this;
    }

    /**
     * 小于等于条件（&lt;=），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     比较的值
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        if (condition) {
            return le(column, value);
        }
        return this;
    }

    /**
     * 模糊匹配条件（LIKE），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     匹配模式（需自行包含 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        if (condition) {
            return like(column, value);
        }
        return this;
    }

    /**
     * 左模糊匹配条件（LIKE '%value'），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     匹配值（会自动在左侧添加 %）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        if (condition) {
            return likeLeft(column, value);
        }
        return this;
    }

    /**
     * 右模糊匹配条件（LIKE 'value%'），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     匹配值（会自动在右侧添加 %）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        if (condition) {
            return likeRight(column, value);
        }
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value     匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        if (condition) {
            return notLike(column, value);
        }
        return this;
    }

    /**
     * 包含于条件（IN），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param values    值列表
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        if (condition) {
            return in(column, values);
        }
        return this;
    }

    /**
     * 包含于条件（IN），使用集合参数，支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param values    值集合
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        if (condition) {
            return in(column, values);
        }
        return this;
    }

    /**
     * 不包含于条件（NOT IN），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param values    值列表
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        if (condition) {
            return notIn(column, values);
        }
        return this;
    }

    /**
     * 不包含于条件（NOT IN），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param values    值集合
     * @param <R>       列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        if (condition) {
            return notIn(column, values);
        }
        return this;
    }

    /**
     * 为空条件（IS NULL），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        if (condition) {
            return isNull(column);
        }
        return this;
    }

    /**
     * 非空条件（IS NOT NULL），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        if (condition) {
            return isNotNull(column);
        }
        return this;
    }

    /**
     * 区间条件（BETWEEN），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此条件
     * @param column    列对应的 Lambda 函数
     * @param value1    区间起始值
     * @param value2    区间结束值
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        if (condition) {
            return between(column, value1, value2);
        }
        return this;
    }


    /**
     * 添加 ORDER BY 排序，支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此排序逻辑
     * @param column    排序列对应的 Lambda 函数
     * @param orderType 排序类型（ASC 升序 / DESC 降序）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        if (condition) {
            return orderBy(column, orderType);
        }
        return this;
    }

    /**
     * 添加升序排序（ORDER BY column ASC），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此排序逻辑
     * @param column    排序列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        if (condition) {
            return orderByAsc(column);
        }
        return this;
    }

    /**
     * 添加降序排序（ORDER BY column DESC），支持条件判断
     *
     * @param condition 条件判断，为 true 时才添加此排序逻辑
     * @param column    排序列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        if (condition) {
            return orderByDesc(column);
        }
        return this;
    }

    /**
     * 等于条件（=）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  比较的值
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> eq(SFunction<T, R> column, Object value) {
        sqlBuilder.eq(getColumn(column), value);
        return this;
    }

    /**
     * 不等于条件（&lt;&gt; 或 !=）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  比较的值
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> ne(SFunction<T, R> column, Object value) {
        sqlBuilder.ne(getColumn(column), value);
        return this;
    }

    /**
     * 大于条件（&gt;）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  比较的值
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> gt(SFunction<T, R> column, Object value) {
        sqlBuilder.gt(getColumn(column), value);
        return this;
    }

    /**
     * 大于等于条件（&gt;=）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  比较的值
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> ge(SFunction<T, R> column, Object value) {
        sqlBuilder.ge(getColumn(column), value);
        return this;
    }

    /**
     * 小于条件（&lt;）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  比较的值
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> lt(SFunction<T, R> column, Object value) {
        sqlBuilder.lt(getColumn(column), value);
        return this;
    }

    /**
     * 小于等于条件（&lt;=）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  比较的值
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> le(SFunction<T, R> column, Object value) {
        sqlBuilder.le(getColumn(column), value);
        return this;
    }

    /**
     * 模糊匹配条件（LIKE）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式（需自行包含 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> like(SFunction<T, ?> column, String value) {
        sqlBuilder.like(getColumn(column), value);
        return this;
    }

    /**
     * 左模糊匹配条件（LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配值（会自动在左侧添加 %）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        sqlBuilder.likeLeft(getColumn(column), value);
        return this;
    }

    /**
     * 右模糊匹配条件（LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配值（会自动在右侧添加 %）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        sqlBuilder.likeRight(getColumn(column), value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        sqlBuilder.notLikeRight(getColumn(column), value);
        return this;
    }


    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        sqlBuilder.notLikeLeft(getColumn(column), value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> notLike(SFunction<T, ?> column, String value) {
        sqlBuilder.notLike(getColumn(column), value);
        return this;
    }

    /**
     * 包含于条件（IN）
     *
     * @param column 列对应的 Lambda 函数
     * @param values 值列表
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> in(SFunction<T, R> column, R... values) {
        sqlBuilder.in(getColumn(column), values);
        return this;
    }

    /**
     * 包含于条件（IN），使用集合参数
     *
     * @param column 列对应的 Lambda 函数
     * @param values 值集合
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        sqlBuilder.in(getColumn(column), values);
        return this;
    }

    /**
     * 不包含于条件（NOT IN）
     *
     * @param column 列对应的 Lambda 函数
     * @param values 值列表
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> notIn(SFunction<T, R> column, R... values) {
        sqlBuilder.notIn(getColumn(column), values);
        return this;
    }

    /**
     * 不包含于条件（NOT IN）
     *
     * @param column 列对应的 Lambda 函数
     * @param values 值集合
     * @param <R>    列类型
     * @return 当前构建器实例，支持链式调用
     */
    protected <R> LambdaSqlBuilder<T> notIn(SFunction<T, R> column, Collection<R> values) {
        sqlBuilder.notIn(getColumn(column), values);
        return this;
    }

    /**
     * 为空条件（IS NULL）
     *
     * @param column 列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> isNull(SFunction<T, ?> column) {
        sqlBuilder.isNull(getColumn(column));
        return this;
    }

    /**
     * 非空条件（IS NOT NULL）
     *
     * @param column 列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> isNotNull(SFunction<T, ?> column) {
        sqlBuilder.isNotNull(getColumn(column));
        return this;
    }

    /**
     * 区间条件（BETWEEN）
     *
     * @param column 列对应的 Lambda 函数
     * @param value1 区间起始值
     * @param value2 区间结束值
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        sqlBuilder.between(getColumn(column), value1, value2);
        return this;
    }

    /**
     * 添加 OR 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> or() {
        sqlBuilder.or();
        return this;
    }

    /**
     * 添加 AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> and() {
        sqlBuilder.and();
        return this;
    }

    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> andStart() {
        return and().bracketStart();
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> andEnd() {
        return bracketEnd();
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orStart() {
        return or().bracketStart();
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orEnd() {
        return bracketEnd();
    }

    /**
     * 添加一个左括号'('
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> bracketStart() {
        sqlBuilder.bracketStart();
        return this;
    }

    /**
     * 添加一个右括号')'
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> bracketEnd() {
        sqlBuilder.bracketEnd();
        return this;
    }


    // 子查询条件

    /**
     * 存在条件（EXISTS）
     *
     * @param subQuery 子查询构建器
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> exists(LambdaSqlBuilder<?> subQuery) {
        SqlBuilder subBuilder = SqlBuilder.builder();
        // 构建子查询
        subBuilder.exists(convertToSqlBuilder(subQuery));
        return this;
    }

    /**
     * 不存在条件（NOT EXISTS）
     *
     * @param subQuery 子查询构建器
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> notExists(LambdaSqlBuilder<?> subQuery) {
        SqlBuilder subBuilder = SqlBuilder.builder();
        subBuilder.notExists(convertToSqlBuilder(subQuery));
        return this;
    }

    // ==================== 分组和排序 ====================

    /**
     * 添加 GROUP BY 分组
     *
     * @param columns 分组列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> groupBy(SFunction<T, ?>... columns) {
        String[] columnNames = Arrays.stream(columns)
                .map(this::getColumn)
                .toArray(String[]::new);
        sqlBuilder.groupBy(columnNames);
        return this;
    }

    /**
     * 添加 HAVING 条件（用于分组后的过滤）
     *
     * @param condition HAVING 条件表达式
     * @param values    条件参数值
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> having(String condition, Object... values) {
        sqlBuilder.having(condition, values);
        return this;
    }

    /**
     * 添加 ORDER BY 排序
     *
     * @param column    排序列对应的 Lambda 函数
     * @param orderType 排序类型（ASC 升序 / DESC 降序）
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        sqlBuilder.orderBy(getColumn(column), orderType);
        return this;
    }

    /**
     * 添加升序排序（ORDER BY column ASC）
     *
     * @param column 排序列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orderByAsc(SFunction<T, ?> column) {
        return orderBy(column, SqlBuilder.OrderType.ASC);
    }

    /**
     * 添加降序排序（ORDER BY column DESC）
     *
     * @param column 排序列对应的 Lambda 函数
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> orderByDesc(SFunction<T, ?> column) {
        return orderBy(column, SqlBuilder.OrderType.DESC);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 LambdaSqlBuilder 转换为 SqlBuilder
     *
     * @param lambdaBuilder Lambda 构建器实例
     * @return SqlBuilder 实例
     */
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
     *
     * @return 查询结果对象
     */
    private SqlBuilder.QueryResult buildInternal() {
        return sqlBuilder.build();
    }

    /**
     * 构建查询结果
     *
     * @return 查询结果对象，包含 SQL 和参数
     * @throws IllegalStateException 如果 SQL 已经构建过
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

    /**
     * 打印当前构建的 SQL 语句（用于调试）
     *
     * @return 当前构建器实例，支持链式调用
     */
    protected LambdaSqlBuilder<T> print() {
        sqlBuilder.print();
        return this;
    }

    /**
     * 清空当前构建的 SQL，重置构建器状态
     *
     * @return 当前构建器实例，支持链式调用
     */
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

    /**
     * 查询结果接口，封装 SQL 构建结果
     */
    protected interface QueryResult {
        /**
         * 获取 SQL 语句
         *
         * @return SQL 字符串
         */
        String getSql();

        /**
         * 获取 SQL 类型
         *
         * @return SQL 类型（SELECT、INSERT、UPDATE、DELETE）
         */
        SQLType getSqlType();

        /**
         * 判断是否为批量操作
         *
         * @return true 表示批量操作，false 表示单条操作
         */
        boolean isBatch();
    }

    /**
     * 单条查询结果，对应非批量操作
     */
    protected static class SingleQueryResult implements QueryResult {
        /**
         * SQL 语句
         */
        private final String sql;

        /**
         * SQL 参数数组
         */
        private final Object[] params;

        /**
         * SQL 类型
         */
        private final SQLType sqlType;

        /**
         * 构造单条查询结果
         *
         * @param sql     SQL 语句
         * @param params  参数数组
         * @param sqlType SQL 类型
         */
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

        /**
         * 获取参数数组（受保护访问，供子类使用）
         *
         * @return 参数数组
         */
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

    /**
     * 批量查询结果，对应批量操作（如批量插入）
     */
    protected static class BatchQueryResult implements QueryResult {
        /**
         * SQL 语句模板
         */
        private final String sql;

        /**
         * 批量参数列表
         */
        private final List<Object[]> batchParams;

        /**
         * SQL 类型
         */
        private final SQLType sqlType;

        /**
         * 构造批量查询结果
         *
         * @param sql         SQL 语句模板
         * @param batchParams 批量参数列表
         * @param sqlType     SQL 类型
         */
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

        /**
         * 获取批量参数列表（受保护访问，供子类使用）
         *
         * @return 批量参数列表
         */
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