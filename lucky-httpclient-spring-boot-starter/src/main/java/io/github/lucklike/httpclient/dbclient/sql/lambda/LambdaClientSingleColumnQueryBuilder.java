package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/10 01:43
 */
public class LambdaClientSingleColumnQueryBuilder<T> {

    private final BaseDBApi<T> baseDBApi;
    private final LambdaSingleColumnQueryBuilder<T> singleColumnQueryBuilder;

    public LambdaClientSingleColumnQueryBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz, SFunction<T, ?> selectColumn) {
        this.singleColumnQueryBuilder = new LambdaSingleColumnQueryBuilder<>(clazz, selectColumn);
        this.baseDBApi = baseDBApi;
    }

    public LambdaClientSingleColumnQueryBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder, SFunction<T, ?> selectColumn) {
        this.singleColumnQueryBuilder = new LambdaSingleColumnQueryBuilder<>(sqlBuilder, selectColumn);
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
     * baseDBApi.lambdaQuery(User.class)
     *     .leftJoin(Order.class, "o")
     *     .on(User::getId, Order::getUserId)
     *     .list();
     * }
     * </pre>
     *
     * @param type      JOIN 类型（INNER、LEFT、RIGHT）
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientSingleColumnQueryBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        singleColumnQueryBuilder.join(type, joinClass, alias);
        return this;
    }

    /**
     * 添加 INNER JOIN 关联
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientSingleColumnQueryBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        singleColumnQueryBuilder.innerJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 LEFT JOIN 关联
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientSingleColumnQueryBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        singleColumnQueryBuilder.leftJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 RIGHT JOIN 关联
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientSingleColumnQueryBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        singleColumnQueryBuilder.rightJoin(joinClass, alias);
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
    public LambdaClientSingleColumnQueryBuilder<T> on(String condition) {
        singleColumnQueryBuilder.on(condition);
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
    public <E> LambdaClientSingleColumnQueryBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        singleColumnQueryBuilder.on(leftColumn, rightColumn);
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
    public LambdaClientSingleColumnQueryBuilder<T> where(String condition, Object... values) {
        singleColumnQueryBuilder.where(condition, values);
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
    public LambdaClientSingleColumnQueryBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        singleColumnQueryBuilder.where(conditionBuilder);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.eq(condition, column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.ne(condition, column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.gt(condition, column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.ge(condition, column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.lt(condition, column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.le(condition, column, value);
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
    public LambdaClientSingleColumnQueryBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.like(condition, column, value);
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
    public LambdaClientSingleColumnQueryBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.likeLeft(condition, column, value);
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
    public LambdaClientSingleColumnQueryBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.likeRight(condition, column, value);
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
    public LambdaClientSingleColumnQueryBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.notLike(condition, column, value);
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
    public final <R> LambdaClientSingleColumnQueryBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        singleColumnQueryBuilder.in(condition, column, values);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        singleColumnQueryBuilder.in(condition, column, values);
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
    public final <R> LambdaClientSingleColumnQueryBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        singleColumnQueryBuilder.notIn(condition, column, values);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        singleColumnQueryBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * IS NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        singleColumnQueryBuilder.isNull(condition, column);
        return this;
    }

    /**
     * IS NOT NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        singleColumnQueryBuilder.isNotNull(condition, column);
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
    public LambdaClientSingleColumnQueryBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        singleColumnQueryBuilder.between(condition, column, value1, value2);
        return this;
    }

    /**
     * 排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        singleColumnQueryBuilder.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * 升序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        singleColumnQueryBuilder.orderByAsc(condition, column);
        return this;
    }

    /**
     * 降序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        singleColumnQueryBuilder.orderByDesc(condition, column);
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
    public final <R> LambdaClientSingleColumnQueryBuilder<T> notIn(SFunction<T, R> column, R... values) {
        singleColumnQueryBuilder.notIn(column, values);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> eq(SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.eq(column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> ne(SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.ne(column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> gt(SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.gt(column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> ge(SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.ge(column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> lt(SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.lt(column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> le(SFunction<T, R> column, Object value) {
        singleColumnQueryBuilder.le(column, value);
        return this;
    }

    /**
     * 模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> like(SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.like(column, value);
        return this;
    }

    /**
     * 左模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.likeLeft(column, value);
        return this;
    }

    /**
     * 右模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.likeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.notLikeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.notLikeLeft(column, value);
        return this;
    }

    /**
     * 非模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> notLike(SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.notLike(column, value);
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
    public <R> LambdaClientSingleColumnQueryBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        singleColumnQueryBuilder.in(column, values);
        return this;
    }

    /**
     * IS NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> isNull(SFunction<T, ?> column) {
        singleColumnQueryBuilder.isNull(column);
        return this;
    }

    /**
     * IS NOT NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> isNotNull(SFunction<T, ?> column) {
        singleColumnQueryBuilder.isNotNull(column);
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
    public LambdaClientSingleColumnQueryBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        singleColumnQueryBuilder.between(column, value1, value2);
        return this;
    }


    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> or(Consumer<LambdaClientSingleColumnQueryBuilder<T>> consumer) {
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
    public LambdaClientSingleColumnQueryBuilder<T> and(Consumer<LambdaClientSingleColumnQueryBuilder<T>> consumer) {
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
    public LambdaClientSingleColumnQueryBuilder<T> or() {
        singleColumnQueryBuilder.or();
        return this;
    }

    /**
     * AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> and() {
        singleColumnQueryBuilder.and();
        return this;
    }

    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> andStart() {
        singleColumnQueryBuilder.andStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> andEnd() {
        singleColumnQueryBuilder.andEnd();
        return this;
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orStart() {
        singleColumnQueryBuilder.orStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orEnd() {
        singleColumnQueryBuilder.orEnd();
        return this;
    }


    /**
     * 添加排序条件
     *
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        singleColumnQueryBuilder.orderBy(column, orderType);
        return this;
    }

    /**
     * 添加升序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orderByAsc(SFunction<T, ?> column) {
        singleColumnQueryBuilder.orderByAsc(column);
        return this;
    }

    /**
     * 添加降序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T> orderByDesc(SFunction<T, ?> column) {
        singleColumnQueryBuilder.orderByDesc(column);
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
    public LambdaClientSingleColumnQueryBuilder<T> print() {
        singleColumnQueryBuilder.print();
        return this;
    }

    // ==================== 执行方法 ====================

    public List<String> strs() {
        return this.baseDBApi.strColumns(this.singleColumnQueryBuilder);
    }

    public List<Integer> ints() {
        return this.baseDBApi.intColumns(this.singleColumnQueryBuilder);
    }

    public List<Long> longs() {
        return this.baseDBApi.longColumns(this.singleColumnQueryBuilder);
    }

    public List<Double> doubles() {
        return this.baseDBApi.doubleColumns(this.singleColumnQueryBuilder);
    }

    public List<Date> dates() {
        return this.baseDBApi.dateColumns(this.singleColumnQueryBuilder);
    }

    public List<BigDecimal> bigDecimals() {
        return this.baseDBApi.bigDecimalColumns(this.singleColumnQueryBuilder);
    }

    public List<BigInteger> bigInts() {
        return this.baseDBApi.bigIntColumns(this.singleColumnQueryBuilder);
    }


    //-------------

    public Stream<String> strStream() {
        return this.baseDBApi.strStream(this.singleColumnQueryBuilder);
    }

    public Stream<Integer> intStream() {
        return this.baseDBApi.intStream(this.singleColumnQueryBuilder);
    }

    public Stream<Long> longStream() {
        return this.baseDBApi.longStream(this.singleColumnQueryBuilder);
    }

    public Stream<Double> doubleStream() {
        return this.baseDBApi.doubleStream(this.singleColumnQueryBuilder);
    }

    public Stream<Date> dateStream() {
        return this.baseDBApi.dateStream(this.singleColumnQueryBuilder);
    }

    public Stream<BigDecimal> bigDecimalStream() {
        return this.baseDBApi.bigDecimalStream(this.singleColumnQueryBuilder);
    }

    public Stream<BigInteger> bigIntStream() {
        return this.baseDBApi.bigIntStream(this.singleColumnQueryBuilder);
    }

    //-------------

    public PageResult<String> strPage(@NonNull Page page) {
        return this.baseDBApi.strPage(this.singleColumnQueryBuilder, page);
    }

    public PageResult<Integer> intPage(@NonNull Page page) {
        return this.baseDBApi.intPage(this.singleColumnQueryBuilder, page);
    }

    public PageResult<Long> longPage(@NonNull Page page) {
        return this.baseDBApi.longPage(this.singleColumnQueryBuilder, page);
    }

    public PageResult<Double> doublePage(@NonNull Page page) {
        return this.baseDBApi.doublePage(this.singleColumnQueryBuilder, page);
    }

    public PageResult<Date> datePage(@NonNull Page page) {
        return this.baseDBApi.datePage(this.singleColumnQueryBuilder, page);
    }

    public PageResult<BigDecimal> bigDecimalPage(@NonNull Page page) {
        return this.baseDBApi.bigDecimalPage(this.singleColumnQueryBuilder, page);
    }

    public PageResult<BigInteger> bigIntPage(@NonNull Page page) {
        return this.baseDBApi.bigIntPage(this.singleColumnQueryBuilder, page);
    }

    @Nullable
    public String str() {
        return strs().stream().findFirst().orElse(null);
    }

    @Nullable
    public Integer aInt() {
        return ints().stream().findFirst().orElse(null);
    }

    @Nullable
    public Long aLong() {
        return longs().stream().findFirst().orElse(null);
    }

    @Nullable
    public Double aDouble() {
        return doubles().stream().findFirst().orElse(null);
    }

    @Nullable
    public Date aDate() {
        return dates().stream().findFirst().orElse(null);
    }

    @Nullable
    public BigDecimal bigDecimal() {
        return bigDecimals().stream().findFirst().orElse(null);
    }

    @Nullable
    public BigInteger bigInt() {
        return bigInts().stream().findFirst().orElse(null);
    }
}
