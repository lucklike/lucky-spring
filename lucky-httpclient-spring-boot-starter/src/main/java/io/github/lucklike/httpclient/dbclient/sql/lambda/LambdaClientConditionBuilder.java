package io.github.lucklike.httpclient.dbclient.sql.lambda;

import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 自带数据库客户端的条件构建器
 * <p>
 * 该类封装了 {@link LambdaConditionBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建查询条件，并可通过 {@link #toSelect}, {@link #toCount},
 * {@link #toUpdate}, {@link #toDelete} 方法转换为对应的操作构建器。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取条件构建器
 * LambdaClientConditionBuilder<User> condition = baseDBApi.lambdaCondition();
 *
 * // 构建条件并执行查询
 * List<User> users = condition.eq(User::getStatus, 1)
 *     .orderByDesc(User::getCreateTime)
 *     .toSelect()
 *     .list();
 *
 * // 构建条件并执行更新
 * int rows = condition.eq(User::getStatus, 0)
 *     .toUpdate()
 *     .set(User::getStatus, 1)
 *     .update();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 02:11
 */
public class LambdaClientConditionBuilder<T> {

    private final BaseDBApi<T> baseDBApi;
    private final LambdaConditionBuilder<T> conditionBuilder;

    /**
     * 构造条件构建器
     *
     * @param baseDBApi   数据库客户端API
     * @param entityClass 实体类类型
     */
    public LambdaClientConditionBuilder(BaseDBApi<T> baseDBApi, Class<T> entityClass) {
        this.conditionBuilder = new LambdaConditionBuilder<>(entityClass);
        this.baseDBApi = baseDBApi;
    }

    // ==================== 条件方法（返回自身类型） ====================

    /**
     * 添加自定义 WHERE 条件
     * <p>
     * 使用原生 SQL 片段作为条件，可用于构建复杂或 Lambda 表达式无法表达的条件。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * condition.where("DATE(create_time) = CURDATE()")
     *          .where("age BETWEEN ? AND ?", 18, 30);
     * }</pre>
     * </p>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @param values    占位符对应的参数值，按顺序匹配
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> where(String condition, Object... values) {
        conditionBuilder.where(condition, values);
        return this;
    }

    /**
     * 等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加等于条件：column = value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        conditionBuilder.eq(condition, column, value);
        return this;
    }

    /**
     * 不等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加不等于条件：column != value 或 column &lt;&gt; value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        conditionBuilder.ne(condition, column, value);
        return this;
    }

    /**
     * 大于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加大于条件：column > value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        conditionBuilder.gt(condition, column, value);
        return this;
    }

    /**
     * 大于等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加大于等于条件：column >= value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        conditionBuilder.ge(condition, column, value);
        return this;
    }

    /**
     * 小于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加小于条件：column < value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        conditionBuilder.lt(condition, column, value);
        return this;
    }

    /**
     * 小于等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加小于等于条件：column <= value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        conditionBuilder.le(condition, column, value);
        return this;
    }

    /**
     * 模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 LIKE 条件：column LIKE '%value%'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        conditionBuilder.like(condition, column, value);
        return this;
    }

    /**
     * 左模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 LIKE 条件：column LIKE '%value'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        conditionBuilder.likeLeft(condition, column, value);
        return this;
    }

    /**
     * 右模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 LIKE 条件：column LIKE 'value%'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        conditionBuilder.likeRight(condition, column, value);
        return this;
    }

    /**
     * 非模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 NOT LIKE 条件：column NOT LIKE '%value%'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        conditionBuilder.notLike(condition, column, value);
        return this;
    }

    /**
     * IN 条件（条件性添加，可变参数）
     * <p>
     * 当 condition 为 true 时，添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final <R> LambdaClientConditionBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        conditionBuilder.in(condition, column, values);
        return this;
    }

    /**
     * IN 条件（条件性添加，集合参数）
     * <p>
     * 当 condition 为 true 时，添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        conditionBuilder.in(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，可变参数）
     * <p>
     * 当 condition 为 true 时，添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final <R> LambdaClientConditionBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        conditionBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，集合参数）
     * <p>
     * 当 condition 为 true 时，添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @param <R>       字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        conditionBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * IS NULL 条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 IS NULL 条件：column IS NULL
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        conditionBuilder.isNull(condition, column);
        return this;
    }

    /**
     * IS NOT NULL 条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 IS NOT NULL 条件：column IS NOT NULL
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        conditionBuilder.isNotNull(condition, column);
        return this;
    }

    /**
     * BETWEEN 条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 BETWEEN 条件：column BETWEEN value1 AND value2
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value1    起始值
     * @param value2    结束值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        conditionBuilder.between(condition, column, value1, value2);
        return this;
    }

    /**
     * 排序条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 ORDER BY 排序条件
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型（ASC 升序 / DESC 降序）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        conditionBuilder.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * 升序排序条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 ORDER BY column ASC 排序条件
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        conditionBuilder.orderByAsc(condition, column);
        return this;
    }

    /**
     * 降序排序条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 ORDER BY column DESC 排序条件
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        conditionBuilder.orderByDesc(condition, column);
        return this;
    }

    /**
     * NOT IN 条件（可变参数）
     * <p>
     * 添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值列表
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final <R> LambdaClientConditionBuilder<T> notIn(SFunction<T, R> column, R... values) {
        conditionBuilder.notIn(column, values);
        return this;
    }

    /**
     * NOT IN 条件（集合参数）
     * <p>
     * 添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> notIn(SFunction<T, R> column, Collection<R> values) {
        conditionBuilder.notIn(column, values);
        return this;
    }

    /**
     * 等于条件
     * <p>
     * 添加等于条件：column = value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> eq(SFunction<T, R> column, Object value) {
        conditionBuilder.eq(column, value);
        return this;
    }

    /**
     * 不等于条件
     * <p>
     * 添加不等于条件：column != value 或 column &lt;&gt; value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> ne(SFunction<T, R> column, Object value) {
        conditionBuilder.ne(column, value);
        return this;
    }

    /**
     * 大于条件
     * <p>
     * 添加大于条件：column > value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> gt(SFunction<T, R> column, Object value) {
        conditionBuilder.gt(column, value);
        return this;
    }

    /**
     * 大于等于条件
     * <p>
     * 添加大于等于条件：column >= value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> ge(SFunction<T, R> column, Object value) {
        conditionBuilder.ge(column, value);
        return this;
    }

    /**
     * 小于条件
     * <p>
     * 添加小于条件：column < value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> lt(SFunction<T, R> column, Object value) {
        conditionBuilder.lt(column, value);
        return this;
    }

    /**
     * 小于等于条件
     * <p>
     * 添加小于等于条件：column <= value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> le(SFunction<T, R> column, Object value) {
        conditionBuilder.le(column, value);
        return this;
    }

    /**
     * 模糊匹配条件
     * <p>
     * 添加 LIKE 条件：column LIKE '%value%'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> like(SFunction<T, ?> column, String value) {
        conditionBuilder.like(column, value);
        return this;
    }

    /**
     * 左模糊匹配条件
     * <p>
     * 添加 LIKE 条件：column LIKE '%value'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        conditionBuilder.likeLeft(column, value);
        return this;
    }

    /**
     * 右模糊匹配条件
     * <p>
     * 添加 LIKE 条件：column LIKE 'value%'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        conditionBuilder.likeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        conditionBuilder.notLikeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        conditionBuilder.notLikeLeft(column, value);
        return this;
    }

    /**
     * 非模糊匹配条件
     * <p>
     * 添加 NOT LIKE 条件：column NOT LIKE '%value%'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> notLike(SFunction<T, ?> column, String value) {
        conditionBuilder.notLike(column, value);
        return this;
    }

    /**
     * IN 条件（可变参数）
     * <p>
     * 添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值列表
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final <R> LambdaClientConditionBuilder<T> in(SFunction<T, R> column, R... values) {
        conditionBuilder.in(column, values);
        return this;
    }

    /**
     * IN 条件（集合参数）
     * <p>
     * 添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @param <R>    字段类型
     * @return 当前构建器实例，支持链式调用
     */
    public <R> LambdaClientConditionBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        conditionBuilder.in(column, values);
        return this;
    }

    /**
     * IS NULL 条件
     * <p>
     * 添加 IS NULL 条件：column IS NULL
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> isNull(SFunction<T, ?> column) {
        conditionBuilder.isNull(column);
        return this;
    }

    /**
     * IS NOT NULL 条件
     * <p>
     * 添加 IS NOT NULL 条件：column IS NOT NULL
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> isNotNull(SFunction<T, ?> column) {
        conditionBuilder.isNotNull(column);
        return this;
    }

    /**
     * BETWEEN 条件
     * <p>
     * 添加 BETWEEN 条件：column BETWEEN value1 AND value2
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        conditionBuilder.between(column, value1, value2);
        return this;
    }


    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> or(Consumer<LambdaClientConditionBuilder<T>> consumer) {
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
    public LambdaClientConditionBuilder<T> and(Consumer<LambdaClientConditionBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }


    /**
     * OR 逻辑运算符
     * <p>
     * 添加 OR 关键字，将后续条件与前一个条件进行 OR 连接
     * </p>
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> or() {
        conditionBuilder.or();
        return this;
    }

    /**
     * AND 逻辑运算符
     * <p>
     * 添加 AND 关键字，将后续条件与前一个条件进行 AND 连接（默认行为，通常无需显式调用）
     * </p>
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> and() {
        conditionBuilder.and();
        return this;
    }


    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> andStart() {
        conditionBuilder.andStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> andEnd() {
        conditionBuilder.andEnd();
        return this;
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> orStart() {
        conditionBuilder.orStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> orEnd() {
        conditionBuilder.orEnd();
        return this;
    }

    // ==================== 类型转换方法 ====================

    /**
     * 将当前的条件构建器转换为查询构建器
     * <p>
     * 转换后可用于执行 SELECT 查询操作。如果不指定查询列，则默认查询所有列。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询所有列
     * List<User> users = condition.toSelect().list();
     * }</pre>
     * </p>
     *
     * @return 查询构建器
     */
    public final LambdaClientQueryBuilder<T> toSelect() {
        return new LambdaClientQueryBuilder<>(this.baseDBApi, this.conditionBuilder);
    }

    /**
     * 将当前的条件构建器转换为统计构建器
     * <p>
     * 转换后可用于执行 COUNT 统计查询。如果不指定统计列，则执行 COUNT(*) 统计总记录数；
     * 如果指定了统计列，则统计该列的非空值数量。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 统计总记录数
     * long total = condition.toCount().count();
     *
     * // 统计指定列的非空值数量
     * long count = condition.toCount(User::getEmail).count();
     * }</pre>
     * </p>
     *
     * @param column 要统计的列（可选），使用 Lambda 表达式指定
     * @return 统计构建器
     */
    @SafeVarargs
    public final LambdaClientCountBuilder<T> toCount(SFunction<T, ?>... column) {
        if (ContainerUtils.isEmptyArray(column)) {
            return new LambdaClientCountBuilder<>(this.baseDBApi, this.conditionBuilder);
        }
        return new LambdaClientCountBuilder<>(this.baseDBApi, this.conditionBuilder, column[0]);
    }

    /**
     * 将当前的条件构建器转换为删除构建器
     * <p>
     * 转换后可用于执行 DELETE 删除操作。
     * </p>
     * <p>
     * <b>注意：</b> 如果条件为空，可能会删除全表数据，请谨慎使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = condition.toDelete().delete();
     * }</pre>
     * </p>
     *
     * @return 删除构建器
     */
    public final LambdaClientDeleteBuilder<T> toDelete() {
        return new LambdaClientDeleteBuilder<>(this.baseDBApi, this.conditionBuilder);
    }

    /**
     * 将当前的条件构建器转换为更新构建器
     * <p>
     * 转换后可用于执行 UPDATE 更新操作。转换后需要调用 {@link LambdaClientUpdateBuilder#set(SFunction, Object)}
     * 方法设置要更新的字段。
     * </p>
     * <p>
     * <b>注意：</b> 如果条件为空，可能会更新全表数据，请谨慎使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = condition.toUpdate()
     *     .set(User::getStatus, 1)
     *     .update();
     * }</pre>
     * </p>
     *
     * @return 更新构建器
     */
    public final LambdaClientUpdateBuilder<T> toUpdate() {
        return new LambdaClientUpdateBuilder<>(this.baseDBApi, this.conditionBuilder);
    }

    // ==================== 执行方法 ====================

    /**
     * 执行查询并返回结果列表
     * <p>
     * 根据构建器中设置的条件、排序、关联表等，执行 SELECT 查询并返回结果列表。
     * 如果查询结果为空，返回空列表（非 null）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> users = baseDBApi.lambdaQuery()
     *     .eq(User::getStatus, 1)
     *     .orderByDesc(User::getCreateTime)
     *     .list();
     * }
     * </pre>
     *
     * @return 查询结果列表，永远不为 null
     */
    public List<T> list() {
        return toSelect().list();
    }

    /**
     * 执行查询并返回单条结果
     * <p>
     * 根据构建器中设置的条件，执行 SELECT 查询并返回第一条结果。
     * 如果查询结果为空，返回 null。
     * </p>
     * <p>
     * <b>注意：</b> 如果查询结果有多条，只返回第一条。建议配合 limit(1) 使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * User user = baseDBApi.lambdaQuery()
     *     .eq(User::getId, 1L)
     *     .one();
     * }
     * </pre>
     *
     * @return 查询结果，可能为 null
     */
    public T one() {
        return toSelect().one();
    }

    /**
     * 以流式方式执行查询并返回结果流
     * <p>
     * 返回的 {@link Stream} 需要在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
     * 适用于处理大量数据，避免一次性加载所有结果到内存。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * try (Stream<User> stream = baseDBApi.lambdaQuery()
     *         .gt(User::getAge, 18)
     *         .stream()) {
     *     stream.filter(user -> user.getName().startsWith("张"))
     *           .forEach(System.out::println);
     * }
     * }
     * </pre>
     *
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     */
    public Stream<T> stream() {
        return toSelect().stream();
    }

    /**
     * 执行分页查询
     * <p>
     * 根据构建器中设置的条件和分页参数，执行分页查询。
     * 分页参数通过 {@link Page} 对象传递，包含当前页码、每页大小、排序字段等信息。
     * </p>
     * <p>
     * <b>注意：</b> 如果 {@link Page#isCountTotal()} 为 {@code true}，则会自动执行 COUNT 查询；
     * 否则只查询分页数据，总记录数为 -1。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * Page page = Page.of(1, 10).desc("create_time");
     * PageResult<User> result = baseDBApi.lambdaQuery()
     *     .eq(User::getStatus, 1)
     *     .page(page);
     * }
     * </pre>
     *
     * @param page 分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     */
    public PageResult<T> page(Page page) {
        return toSelect().page(page);
    }

    /**
     * 执行 DELETE 操作并返回影响行数
     * <p>
     * 根据构建器中设置的条件，执行 DELETE 操作。
     * </p>
     * <p>
     * <b>警告：</b>
     * <ul>
     *     <li>如果没有设置任何条件，可能会删除全表数据</li>
     *     <li>建议始终添加至少一个条件来限制删除范围</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = baseDBApi.lambdaDelete()
     *     .eq(User::getStatus, 0)
     *     .delete();
     * }
     * </pre>
     *
     * @return 被删除的记录行数
     */
    public int delete() {
        return toDelete().delete();
    }

    /**
     * 执行 UPDATE 操作并返回影响行数
     * <p>
     * 根据构建器中设置的更新字段和条件，执行 UPDATE 操作。
     * </p>
     * <p>
     * <b>警告：</b>
     * <ul>
     *     <li>如果没有设置任何条件，可能会更新全表数据</li>
     *     <li>建议始终添加至少一个条件来限制更新范围</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = baseDBApi.lambdaUpdate()
     *     .set(User::getStatus, 1)
     *     .eq(User::getStatus, 0)
     *     .update();
     * }
     * </pre>
     * @param consumer 需要使用这个消费接口来提供set相关的信息
     *
     * @return 被更新的记录行数
     */
    public int update(Consumer<LambdaUpdateBuilder<T>> consumer) {
        LambdaClientUpdateBuilder<T> clientUpdate = toUpdate();
        consumer.accept(clientUpdate.getUpdateBuilder());
        return clientUpdate.update();
    }

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
        return toCount().count();
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
        return toCount().exist();
    }
}