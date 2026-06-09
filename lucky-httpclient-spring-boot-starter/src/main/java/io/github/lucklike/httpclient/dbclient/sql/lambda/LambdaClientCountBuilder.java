package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 自带数据库客户端的数量统计构建器
 * <p>
 * 该类封装了 {@link LambdaCountBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 COUNT 统计查询，并可直接执行统计操作。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取统计构建器
 * LambdaClientCountBuilder<User> countBuilder = baseDBApi.lambdaCount();
 *
 * // 统计总记录数
 * long total = countBuilder.count();
 *
 * // 统计满足条件的记录数
 * long count = baseDBApi.lambdaCount()
 *     .eq(User::getStatus, 1)
 *     .count();
 *
 * // 判断是否存在满足条件的记录
 * boolean exists = baseDBApi.lambdaCount()
 *     .eq(User::getEmail, "test@example.com")
 *     .exist();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 02:04
 */
public class LambdaClientCountBuilder<T> {

    private final BaseDBApi<T> baseDBApi;
    private final LambdaCountBuilder<T> countBuilder;

    /**
     * 构造统计构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        this.countBuilder = new LambdaCountBuilder<>(clazz);
        this.baseDBApi = baseDBApi;
    }

    /**
     * 构造统计构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        this.countBuilder = new LambdaCountBuilder<>(sqlBuilder);
        this.baseDBApi = baseDBApi;
    }

    /**
     * 构造统计构建器（使用现有的 SQL 构建器和指定统计列）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     * @param column     要统计的列
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder, SFunction<T, ?> column) {
        this.countBuilder = new LambdaCountBuilder<>(sqlBuilder, column);
        this.baseDBApi = baseDBApi;
    }

    /**
     * 构造统计构建器（使用实体类和指定统计列）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     * @param column    要统计的列
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz, SFunction<T, ?> column) {
        this.countBuilder = new LambdaCountBuilder<>(clazz, column);
        this.baseDBApi = baseDBApi;
    }

    // ==================== 关联表方法 ====================

    /**
     * 添加 JOIN 关联
     * <p>
     * 支持 INNER JOIN、LEFT JOIN、RIGHT JOIN 等关联类型。
     * 添加 JOIN 后需要通过 {@link #on} 方法指定关联条件。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * baseDBApi.lambdaCount(User.class)
     *     .leftJoin(Order.class, "o")
     *     .on(User::getId, Order::getUserId)
     *     .eq(User::getStatus, 1)
     *     .count();
     * }
     * </pre>
     *
     * @param type      JOIN 类型（INNER、LEFT、RIGHT）
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        countBuilder.join(type, joinClass, alias);
        return this;
    }

    /**
     * 添加 INNER JOIN 关联
     * <p>
     * 等价于 {@code join(JoinType.INNER, joinClass, alias)}
     * </p>
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        countBuilder.innerJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 LEFT JOIN 关联
     * <p>
     * 等价于 {@code join(JoinType.LEFT, joinClass, alias)}
     * </p>
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        countBuilder.leftJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 RIGHT JOIN 关联
     * <p>
     * 等价于 {@code join(JoinType.RIGHT, joinClass, alias)}
     * </p>
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        countBuilder.rightJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 JOIN 关联条件（原生 SQL）
     * <p>
     * 使用原生 SQL 片段指定 JOIN 的 ON 条件。
     * </p>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> on(String condition) {
        countBuilder.on(condition);
        return this;
    }

    /**
     * 添加 JOIN 关联条件（Lambda 表达式）
     * <p>
     * 使用 Lambda 表达式指定 JOIN 的 ON 条件，如 left.column = right.column
     * </p>
     *
     * @param leftColumn  左表字段的 Lambda 表达式
     * @param rightColumn 右表字段的 Lambda 表达式
     * @param <E>         右表实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        countBuilder.on(leftColumn, rightColumn);
        return this;
    }

    // ==================== 条件方法 ====================

    /**
     * 添加自定义 WHERE 条件
     * <p>
     * 使用原生 SQL 片段作为条件，可用于构建复杂或 Lambda 表达式无法表达的条件。
     * </p>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @param values    占位符对应的参数值，按顺序匹配
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> where(String condition, Object... values) {
        countBuilder.where(condition, values);
        return this;
    }

    /**
     * 添加嵌套条件
     * <p>
     * 通过 Consumer 函数式接口构建嵌套的复杂条件，支持括号分组。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * builder.where(sql -> sql.eq(User::getStatus, 1)
     *                       .or()
     *                       .eq(User::getStatus, 2));
     * }
     * </pre>
     *
     * @param conditionBuilder 条件构建器函数
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        countBuilder.where(conditionBuilder);
        return this;
    }

    /**
     * 等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        countBuilder.eq(condition, column, value);
        return this;
    }

    /**
     * 不等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        countBuilder.ne(condition, column, value);
        return this;
    }

    /**
     * 大于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        countBuilder.gt(condition, column, value);
        return this;
    }

    /**
     * 大于等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        countBuilder.ge(condition, column, value);
        return this;
    }

    /**
     * 小于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        countBuilder.lt(condition, column, value);
        return this;
    }

    /**
     * 小于等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        countBuilder.le(condition, column, value);
        return this;
    }

    /**
     * 模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        countBuilder.like(condition, column, value);
        return this;
    }

    /**
     * 左模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        countBuilder.likeLeft(condition, column, value);
        return this;
    }

    /**
     * 右模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        countBuilder.likeRight(condition, column, value);
        return this;
    }

    /**
     * 非模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        countBuilder.notLike(condition, column, value);
        return this;
    }

    /**
     * IN 条件（条件性添加，可变参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final <R> LambdaClientCountBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        countBuilder.in(condition, column, values);
        return this;
    }

    /**
     * IN 条件（条件性添加，集合参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        countBuilder.in(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，可变参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final <R> LambdaClientCountBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        countBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，集合参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        countBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * IS NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        countBuilder.isNull(condition, column);
        return this;
    }

    /**
     * IS NOT NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        countBuilder.isNotNull(condition, column);
        return this;
    }

    /**
     * BETWEEN 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value1    起始值
     * @param value2    结束值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        countBuilder.between(condition, column, value1, value2);
        return this;
    }

    /**
     * 排序条件（条件性添加）
     * <p>
     * 注意：在 COUNT 查询中，ORDER BY 通常不影响统计结果，但某些数据库方言可能需要。
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        countBuilder.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * 升序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        countBuilder.orderByAsc(condition, column);
        return this;
    }

    /**
     * 降序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        countBuilder.orderByDesc(condition, column);
        return this;
    }

    /**
     * NOT IN 条件（可变参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值列表
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final <R> LambdaClientCountBuilder<T> notIn(SFunction<T, R> column, R... values) {
        countBuilder.notIn(column, values);
        return this;
    }

    /**
     * NOT IN 条件（集合参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> notIn(SFunction<T, R> column, Collection<R> values) {
        countBuilder.notIn(column, values);
        return this;
    }

    /**
     * 等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> eq(SFunction<T, R> column, Object value) {
        countBuilder.eq(column, value);
        return this;
    }

    /**
     * 不等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> ne(SFunction<T, R> column, Object value) {
        countBuilder.ne(column, value);
        return this;
    }

    /**
     * 大于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> gt(SFunction<T, R> column, Object value) {
        countBuilder.gt(column, value);
        return this;
    }

    /**
     * 大于等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> ge(SFunction<T, R> column, Object value) {
        countBuilder.ge(column, value);
        return this;
    }

    /**
     * 小于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> lt(SFunction<T, R> column, Object value) {
        countBuilder.lt(column, value);
        return this;
    }

    /**
     * 小于等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> le(SFunction<T, R> column, Object value) {
        countBuilder.le(column, value);
        return this;
    }

    /**
     * 模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> like(SFunction<T, ?> column, String value) {
        countBuilder.like(column, value);
        return this;
    }

    /**
     * 左模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        countBuilder.likeLeft(column, value);
        return this;
    }

    /**
     * 右模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        countBuilder.likeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        countBuilder.notLikeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        countBuilder.notLikeLeft(column, value);
        return this;
    }

    /**
     * 非模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> notLike(SFunction<T, ?> column, String value) {
        countBuilder.notLike(column, value);
        return this;
    }

    /**
     * IN 条件（集合参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientCountBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        countBuilder.in(column, values);
        return this;
    }

    /**
     * IS NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> isNull(SFunction<T, ?> column) {
        countBuilder.isNull(column);
        return this;
    }

    /**
     * IS NOT NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> isNotNull(SFunction<T, ?> column) {
        countBuilder.isNotNull(column);
        return this;
    }

    /**
     * BETWEEN 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        countBuilder.between(column, value1, value2);
        return this;
    }


    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> or(Consumer<LambdaClientCountBuilder<T>> consumer) {
        orStart();
        consumer.accept(this);
        orEnd();
        return this;
    }

    /**
     * 添加 AND (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> and(Consumer<LambdaClientCountBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }

    /**
     * OR 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> or() {
        countBuilder.or();
        return this;
    }

    /**
     * AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> and() {
        countBuilder.and();
        return this;
    }


    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> andStart() {
        countBuilder.andStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> andEnd() {
        countBuilder.andEnd();
        return this;
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orStart() {
        countBuilder.orStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orEnd() {
        countBuilder.orEnd();
        return this;
    }


    /**
     * 添加排序条件
     *
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        countBuilder.orderBy(column, orderType);
        return this;
    }

    /**
     * 添加升序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderByAsc(SFunction<T, ?> column) {
        countBuilder.orderByAsc(column);
        return this;
    }

    /**
     * 添加降序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderByDesc(SFunction<T, ?> column) {
        countBuilder.orderByDesc(column);
        return this;
    }

    // ==================== 调试方法 ====================

    /**
     * 打印最终生成的 SQL 语句和参数到控制台
     * <p>
     * 用于调试和开发阶段，方便查看实际执行的 SQL。
     * 生产环境建议关闭此功能。
     * </p>
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> print() {
        countBuilder.print();
        return this;
    }

    // ==================== 执行方法 ====================

    /**
     * 执行 COUNT 查询并返回统计结果
     * <p>
     * 根据构建器中设置的条件，执行 COUNT 查询并返回统计结果。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * long count = baseDBApi.lambdaCount()
     *     .eq(User::getStatus, 1)
     *     .count();
     * }
     * </pre>
     *
     * @return 统计结果（满足条件的记录数）
     */
    public long count() {
        return this.baseDBApi.count(this.countBuilder);
    }

    /**
     * 判断满足条件的记录是否存在
     * <p>
     * 根据构建器中设置的条件，判断是否存在满足条件的记录。
     * 等价于 {@code count() > 0}，但性能更优（某些数据库实现会使用 LIMIT 1 优化）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * boolean exists = baseDBApi.lambdaCount()
     *     .eq(User::getEmail, "test@example.com")
     *     .exist();
     * }
     * </pre>
     *
     * @return true 表示存在至少一条记录，false 表示不存在任何记录
     */
    public boolean exist() {
        return count() > 0;
    }
}