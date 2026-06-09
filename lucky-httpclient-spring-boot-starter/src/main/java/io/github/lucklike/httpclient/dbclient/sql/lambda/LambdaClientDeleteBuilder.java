package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 自带数据库客户端的删除构建器
 * <p>
 * 该类封装了 {@link LambdaDeleteBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 DELETE 删除条件，并可直接执行删除操作。
 * </p>
 * <p>
 * <b>警告：</b> 如果条件为空，可能会删除全表数据，请谨慎使用！
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取删除构建器
 * LambdaClientDeleteBuilder<User> deleteBuilder = baseDBApi.lambdaDelete();
 *
 * // 删除状态为 0 的用户
 * int rows = baseDBApi.lambdaDelete()
 *     .eq(User::getStatus, 0)
 *     .delete();
 *
 * // 删除年龄小于 18 岁的用户
 * int rows = baseDBApi.lambdaDelete()
 *     .lt(User::getAge, 18)
 *     .delete();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 01:59
 */
public class LambdaClientDeleteBuilder<T> {

    private final BaseDBApi<T> baseDBApi;
    private final LambdaDeleteBuilder<T> deleteBuilder;

    /**
     * 构造删除构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientDeleteBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        this.deleteBuilder = new LambdaDeleteBuilder<>(clazz);
        this.baseDBApi = baseDBApi;
    }

    /**
     * 构造删除构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientDeleteBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        this.deleteBuilder = new LambdaDeleteBuilder<>(sqlBuilder);
        this.baseDBApi = baseDBApi;
    }

    // ==================== 条件方法 ====================

    /**
     * 添加自定义 WHERE 条件
     * <p>
     * 使用原生 SQL 片段作为条件，可用于构建复杂或 Lambda 表达式无法表达的条件。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * deleteBuilder.where("DATE(create_time) < '2024-01-01'")
     *              .delete();
     * }
     * </pre>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @param values    占位符对应的参数值，按顺序匹配
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> where(String condition, Object... values) {
        deleteBuilder.where(condition, values);
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
     * deleteBuilder.where(sql -> sql.eq(User::getStatus, 0)
     *                             .or()
     *                             .isNull(User::getDeletedAt))
     *              .delete();
     * }
     * </pre>
     *
     * @param conditionBuilder 条件构建器函数
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        deleteBuilder.where(conditionBuilder);
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
    public <R> LambdaClientDeleteBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        deleteBuilder.eq(condition, column, value);
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
    public <R> LambdaClientDeleteBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        deleteBuilder.ne(condition, column, value);
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
    public <R> LambdaClientDeleteBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        deleteBuilder.gt(condition, column, value);
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
    public <R> LambdaClientDeleteBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        deleteBuilder.ge(condition, column, value);
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
    public <R> LambdaClientDeleteBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        deleteBuilder.lt(condition, column, value);
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
    public <R> LambdaClientDeleteBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        deleteBuilder.le(condition, column, value);
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
    public LambdaClientDeleteBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        deleteBuilder.like(condition, column, value);
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
    public LambdaClientDeleteBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        deleteBuilder.likeLeft(condition, column, value);
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
    public LambdaClientDeleteBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        deleteBuilder.likeRight(condition, column, value);
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
    public LambdaClientDeleteBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        deleteBuilder.notLike(condition, column, value);
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
    public final <R> LambdaClientDeleteBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        deleteBuilder.in(condition, column, values);
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
    public <R> LambdaClientDeleteBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        deleteBuilder.in(condition, column, values);
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
    public final <R> LambdaClientDeleteBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        deleteBuilder.notIn(condition, column, values);
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
    public <R> LambdaClientDeleteBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        deleteBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * IS NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        deleteBuilder.isNull(condition, column);
        return this;
    }

    /**
     * IS NOT NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        deleteBuilder.isNotNull(condition, column);
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
    public LambdaClientDeleteBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        deleteBuilder.between(condition, column, value1, value2);
        return this;
    }

    /**
     * 排序条件（条件性添加）
     * <p>
     * 注意：在 DELETE 查询中，ORDER BY 通常不影响删除结果，但某些数据库方言可能需要。
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        deleteBuilder.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * 升序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        deleteBuilder.orderByAsc(condition, column);
        return this;
    }

    /**
     * 降序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        deleteBuilder.orderByDesc(condition, column);
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
    public final <R> LambdaClientDeleteBuilder<T> notIn(SFunction<T, R> column, R... values) {
        deleteBuilder.notIn(column, values);
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
    public <R> LambdaClientDeleteBuilder<T> notIn(SFunction<T, R> column, Collection<R> values) {
        deleteBuilder.notIn(column, values);
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
    public <R> LambdaClientDeleteBuilder<T> eq(SFunction<T, R> column, Object value) {
        deleteBuilder.eq(column, value);
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
    public <R> LambdaClientDeleteBuilder<T> ne(SFunction<T, R> column, Object value) {
        deleteBuilder.ne(column, value);
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
    public <R> LambdaClientDeleteBuilder<T> gt(SFunction<T, R> column, Object value) {
        deleteBuilder.gt(column, value);
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
    public <R> LambdaClientDeleteBuilder<T> ge(SFunction<T, R> column, Object value) {
        deleteBuilder.ge(column, value);
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
    public <R> LambdaClientDeleteBuilder<T> lt(SFunction<T, R> column, Object value) {
        deleteBuilder.lt(column, value);
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
    public <R> LambdaClientDeleteBuilder<T> le(SFunction<T, R> column, Object value) {
        deleteBuilder.le(column, value);
        return this;
    }

    /**
     * 模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> like(SFunction<T, ?> column, String value) {
        deleteBuilder.like(column, value);
        return this;
    }

    /**
     * 左模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        deleteBuilder.likeLeft(column, value);
        return this;
    }

    /**
     * 右模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        deleteBuilder.likeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        deleteBuilder.notLikeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        deleteBuilder.notLikeLeft(column, value);
        return this;
    }

    /**
     * 非模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> notLike(SFunction<T, ?> column, String value) {
        deleteBuilder.notLike(column, value);
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
    public <R> LambdaClientDeleteBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        deleteBuilder.in(column, values);
        return this;
    }

    /**
     * IS NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> isNull(SFunction<T, ?> column) {
        deleteBuilder.isNull(column);
        return this;
    }

    /**
     * IS NOT NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> isNotNull(SFunction<T, ?> column) {
        deleteBuilder.isNotNull(column);
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
    public LambdaClientDeleteBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        deleteBuilder.between(column, value1, value2);
        return this;
    }


    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> or(Consumer<LambdaClientDeleteBuilder<T>> consumer) {
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
    public LambdaClientDeleteBuilder<T> and(Consumer<LambdaClientDeleteBuilder<T>> consumer) {
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
    public LambdaClientDeleteBuilder<T> or() {
        deleteBuilder.or();
        return this;
    }

    /**
     * AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> and() {
        deleteBuilder.and();
        return this;
    }

    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> andStart() {
        deleteBuilder.andStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> andEnd() {
        deleteBuilder.andEnd();
        return this;
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> orStart() {
        deleteBuilder.orStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientDeleteBuilder<T> orEnd() {
        deleteBuilder.orEnd();
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
    public LambdaClientDeleteBuilder<T> print() {
        deleteBuilder.print();
        return this;
    }

    // ==================== 执行方法 ====================

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
        return this.baseDBApi.delete(this.deleteBuilder);
    }
}