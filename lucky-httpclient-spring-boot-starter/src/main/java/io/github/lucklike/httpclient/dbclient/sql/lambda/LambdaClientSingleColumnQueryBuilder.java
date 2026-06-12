package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.function.LambdaUtils;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/10 01:43
 */
public class LambdaClientSingleColumnQueryBuilder<T, R> {

    private final BaseDBApi<T> baseDBApi;
    private final LambdaSingleColumnQueryBuilder<T, R> singleColumnQueryBuilder;

    public LambdaClientSingleColumnQueryBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz, SFunction<T, R> selectColumn) {
        this.singleColumnQueryBuilder = new LambdaSingleColumnQueryBuilder<>(clazz, selectColumn);
        this.baseDBApi = baseDBApi;
    }

    public LambdaClientSingleColumnQueryBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder, SFunction<T, R> selectColumn) {
        this.singleColumnQueryBuilder = new LambdaSingleColumnQueryBuilder<>(sqlBuilder, selectColumn);
        this.baseDBApi = baseDBApi;
    }

    /**
     * 获取查询列的类型
     *
     * @return 查询列的类型
     */
    public Class<?> getSelectColumnType() {
        return singleColumnQueryBuilder.getSelectColumnType();
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> innerJoin(Class<E> joinClass, String alias) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> leftJoin(Class<E> joinClass, String alias) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> rightJoin(Class<E> joinClass, String alias) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> on(String condition) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> where(String condition, Object... values) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        singleColumnQueryBuilder.where(conditionBuilder);
        return this;
    }

    /**
     * 等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> eq(boolean condition, SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.eq(condition, column, value);
        return this;
    }

    /**
     * 不等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> ne(boolean condition, SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.ne(condition, column, value);
        return this;
    }

    /**
     * 大于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> gt(boolean condition, SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.gt(condition, column, value);
        return this;
    }

    /**
     * 大于等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> ge(boolean condition, SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.ge(condition, column, value);
        return this;
    }

    /**
     * 小于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> lt(boolean condition, SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.lt(condition, column, value);
        return this;
    }

    /**
     * 小于等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> le(boolean condition, SFunction<T, ?> column, Object value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> like(boolean condition, SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> likeRight(boolean condition, SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> notLike(boolean condition, SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.notLike(condition, column, value);
        return this;
    }

    /**
     * IN 条件（条件性添加，可变参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> in(boolean condition, SFunction<T, ?> column, Object... values) {
        singleColumnQueryBuilder.in(condition, column, values);
        return this;
    }

    /**
     * IN 条件（条件性添加，集合参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        singleColumnQueryBuilder.in(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，可变参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> notIn(boolean condition, SFunction<T, ?> column, Object... values) {
        singleColumnQueryBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，集合参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> isNull(boolean condition, SFunction<T, ?> column) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> isNotNull(boolean condition, SFunction<T, ?> column) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> orderByAsc(boolean condition, SFunction<T, ?> column) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> orderByDesc(boolean condition, SFunction<T, ?> column) {
        singleColumnQueryBuilder.orderByDesc(condition, column);
        return this;
    }

    /**
     * NOT IN 条件（可变参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值列表
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> notIn(SFunction<T, ?> column, Object... values) {
        singleColumnQueryBuilder.notIn(column, values);
        return this;
    }

    /**
     * 等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> eq(SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.eq(column, value);
        return this;
    }

    /**
     * 不等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> ne(SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.ne(column, value);
        return this;
    }

    /**
     * 大于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> gt(SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.gt(column, value);
        return this;
    }

    /**
     * 大于等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> ge(SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.ge(column, value);
        return this;
    }

    /**
     * 小于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> lt(SFunction<T, ?> column, Object value) {
        singleColumnQueryBuilder.lt(column, value);
        return this;
    }

    /**
     * 小于等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> le(SFunction<T, ?> column, Object value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> like(SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> likeLeft(SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> likeRight(SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> notLikeRight(SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> notLikeLeft(SFunction<T, ?> column, String value) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> notLike(SFunction<T, ?> column, String value) {
        singleColumnQueryBuilder.notLike(column, value);
        return this;
    }

    /**
     * IN 条件（集合参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> in(SFunction<T, ?> column, Collection<?> values) {
        singleColumnQueryBuilder.in(column, values);
        return this;
    }

    /**
     * IS NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> isNull(SFunction<T, ?> column) {
        singleColumnQueryBuilder.isNull(column);
        return this;
    }

    /**
     * IS NOT NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> isNotNull(SFunction<T, ?> column) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> between(SFunction<T, ?> column, Object value1, Object value2) {
        singleColumnQueryBuilder.between(column, value1, value2);
        return this;
    }


    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> or(Consumer<LambdaClientSingleColumnQueryBuilder<T, R>> consumer) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> and(Consumer<LambdaClientSingleColumnQueryBuilder<T, R>> consumer) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> or() {
        singleColumnQueryBuilder.or();
        return this;
    }

    /**
     * AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> and() {
        singleColumnQueryBuilder.and();
        return this;
    }

    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> andStart() {
        singleColumnQueryBuilder.andStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> andEnd() {
        singleColumnQueryBuilder.andEnd();
        return this;
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> orStart() {
        singleColumnQueryBuilder.orStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> orEnd() {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        singleColumnQueryBuilder.orderBy(column, orderType);
        return this;
    }

    /**
     * 添加升序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> orderByAsc(SFunction<T, ?> column) {
        singleColumnQueryBuilder.orderByAsc(column);
        return this;
    }

    /**
     * 添加降序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> orderByDesc(SFunction<T, ?> column) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> print() {
        singleColumnQueryBuilder.print();
        return this;
    }

    // ==================== 执行方法 ====================

    public List<R> list() {
        return this.baseDBApi.columns(this.singleColumnQueryBuilder);
    }


    public Stream<R> stream() {
        return this.baseDBApi.columnsStream(this.singleColumnQueryBuilder);
    }

    public PageResult<R> page(@NonNull Page page) {
        return this.baseDBApi.columnsPage(this.singleColumnQueryBuilder, page);
    }

    public List<R> simplePage(long pageNum, long pageSize) {
        return this.baseDBApi.simpleColumnsPage(this.singleColumnQueryBuilder, pageNum, pageSize);
    }

    @Nullable
    public R one() {
        return this.baseDBApi.column(this.singleColumnQueryBuilder);
    }
}
