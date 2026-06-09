package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 自带数据库客户端的查询构建器
 * <p>
 * 该类封装了 {@link LambdaQueryBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 SELECT 查询条件，并可直接执行查询操作。
 * </p>
 * <p>
 * 支持的功能：
 * <ul>
 *     <li>条件过滤（WHERE）</li>
 *     <li>关联查询（JOIN）</li>
 *     <li>排序（ORDER BY）</li>
 *     <li>分页查询（PAGE）</li>
 *     <li>流式查询（STREAM）</li>
 *     <li>指定查询列（SELECT）</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取查询构建器
 * LambdaClientQueryBuilder<User> query = baseDBApi.lambdaQuery();
 *
 * // 查询所有用户
 * List<User> allUsers = query.list();
 *
 * // 条件查询
 * List<User> activeUsers = baseDBApi.lambdaQuery()
 *     .eq(User::getStatus, 1)
 *     .orderByDesc(User::getCreateTime)
 *     .list();
 *
 * // 查询单条记录
 * User user = baseDBApi.lambdaQuery()
 *     .eq(User::getId, 1L)
 *     .one();
 *
 * // 分页查询
 * Page page = Page.of(1, 10).desc("create_time");
 * PageResult<User> pageResult = baseDBApi.lambdaQuery()
 *     .eq(User::getStatus, 1)
 *     .page(page);
 *
 * // 关联查询
 * List<User> users = baseDBApi.lambdaQuery()
 *     .leftJoin(Order.class, "o")
 *     .on(User::getId, Order::getUserId)
 *     .eq(User::getStatus, 1)
 *     .list();
 *
 * // 指定查询列
 * List<User> users = baseDBApi.lambdaQuery(User::getId, User::getName)
 *     .eq(User::getStatus, 1)
 *     .list();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 00:53
 */
public class LambdaClientQueryBuilder<T> {

    private final BaseDBApi<T> baseDBApi;
    private final LambdaQueryBuilder<T> queryBuilder;

    /**
     * 构造查询构建器（使用实体类，查询所有列）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientQueryBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        this.queryBuilder = new LambdaQueryBuilder<>(clazz);
        this.baseDBApi = baseDBApi;
    }

    /**
     * 构造查询构建器（使用现有的 SQL 构建器，查询所有列）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientQueryBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        this.queryBuilder = new LambdaQueryBuilder<>(sqlBuilder);
        this.baseDBApi = baseDBApi;
    }


    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    public final LambdaClientQueryBuilder<T> select(SFunction<T, ?>... columns) {
        this.queryBuilder.select(columns);
        return this;
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
    public <E> LambdaClientQueryBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        queryBuilder.join(type, joinClass, alias);
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
    public <E> LambdaClientQueryBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        queryBuilder.innerJoin(joinClass, alias);
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
    public <E> LambdaClientQueryBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        queryBuilder.leftJoin(joinClass, alias);
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
    public <E> LambdaClientQueryBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        queryBuilder.rightJoin(joinClass, alias);
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
    public LambdaClientQueryBuilder<T> on(String condition) {
        queryBuilder.on(condition);
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
    public <E> LambdaClientQueryBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        queryBuilder.on(leftColumn, rightColumn);
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
    public LambdaClientQueryBuilder<T> where(String condition, Object... values) {
        queryBuilder.where(condition, values);
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
    public LambdaClientQueryBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        queryBuilder.where(conditionBuilder);
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
    public <R> LambdaClientQueryBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        queryBuilder.eq(condition, column, value);
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
    public <R> LambdaClientQueryBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        queryBuilder.ne(condition, column, value);
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
    public <R> LambdaClientQueryBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        queryBuilder.gt(condition, column, value);
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
    public <R> LambdaClientQueryBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        queryBuilder.ge(condition, column, value);
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
    public <R> LambdaClientQueryBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        queryBuilder.lt(condition, column, value);
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
    public <R> LambdaClientQueryBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        queryBuilder.le(condition, column, value);
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
    public LambdaClientQueryBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        queryBuilder.like(condition, column, value);
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
    public LambdaClientQueryBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        queryBuilder.likeLeft(condition, column, value);
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
    public LambdaClientQueryBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        queryBuilder.likeRight(condition, column, value);
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
    public LambdaClientQueryBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        queryBuilder.notLike(condition, column, value);
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
    public final <R> LambdaClientQueryBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        queryBuilder.in(condition, column, values);
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
    public <R> LambdaClientQueryBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        queryBuilder.in(condition, column, values);
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
    public final <R> LambdaClientQueryBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        queryBuilder.notIn(condition, column, values);
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
    public <R> LambdaClientQueryBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        queryBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * IS NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        queryBuilder.isNull(condition, column);
        return this;
    }

    /**
     * IS NOT NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        queryBuilder.isNotNull(condition, column);
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
    public LambdaClientQueryBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        queryBuilder.between(condition, column, value1, value2);
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
    public LambdaClientQueryBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        queryBuilder.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * 升序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        queryBuilder.orderByAsc(condition, column);
        return this;
    }

    /**
     * 降序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        queryBuilder.orderByDesc(condition, column);
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
    public final <R> LambdaClientQueryBuilder<T> notIn(SFunction<T, R> column, R... values) {
        queryBuilder.notIn(column, values);
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
    public <R> LambdaClientQueryBuilder<T> eq(SFunction<T, R> column, Object value) {
        queryBuilder.eq(column, value);
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
    public <R> LambdaClientQueryBuilder<T> ne(SFunction<T, R> column, Object value) {
        queryBuilder.ne(column, value);
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
    public <R> LambdaClientQueryBuilder<T> gt(SFunction<T, R> column, Object value) {
        queryBuilder.gt(column, value);
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
    public <R> LambdaClientQueryBuilder<T> ge(SFunction<T, R> column, Object value) {
        queryBuilder.ge(column, value);
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
    public <R> LambdaClientQueryBuilder<T> lt(SFunction<T, R> column, Object value) {
        queryBuilder.lt(column, value);
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
    public <R> LambdaClientQueryBuilder<T> le(SFunction<T, R> column, Object value) {
        queryBuilder.le(column, value);
        return this;
    }

    /**
     * 模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> like(SFunction<T, ?> column, String value) {
        queryBuilder.like(column, value);
        return this;
    }

    /**
     * 左模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        queryBuilder.likeLeft(column, value);
        return this;
    }

    /**
     * 右模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        queryBuilder.likeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        queryBuilder.notLikeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        queryBuilder.notLikeLeft(column, value);
        return this;
    }

    /**
     * 非模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> notLike(SFunction<T, ?> column, String value) {
        queryBuilder.notLike(column, value);
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
    public <R> LambdaClientQueryBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        queryBuilder.in(column, values);
        return this;
    }

    /**
     * IS NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> isNull(SFunction<T, ?> column) {
        queryBuilder.isNull(column);
        return this;
    }

    /**
     * IS NOT NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> isNotNull(SFunction<T, ?> column) {
        queryBuilder.isNotNull(column);
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
    public LambdaClientQueryBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        queryBuilder.between(column, value1, value2);
        return this;
    }


    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> or(Consumer<LambdaClientQueryBuilder<T>> consumer) {
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
    public LambdaClientQueryBuilder<T> and(Consumer<LambdaClientQueryBuilder<T>> consumer) {
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
    public LambdaClientQueryBuilder<T> or() {
        queryBuilder.or();
        return this;
    }

    /**
     * AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> and() {
        queryBuilder.and();
        return this;
    }

    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> andStart() {
        queryBuilder.andStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> andEnd() {
        queryBuilder.andEnd();
        return this;
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orStart() {
        queryBuilder.orStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orEnd() {
        queryBuilder.orEnd();
        return this;
    }


    /**
     * 添加排序条件
     *
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        queryBuilder.orderBy(column, orderType);
        return this;
    }

    /**
     * 添加升序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderByAsc(SFunction<T, ?> column) {
        queryBuilder.orderByAsc(column);
        return this;
    }

    /**
     * 添加降序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderByDesc(SFunction<T, ?> column) {
        queryBuilder.orderByDesc(column);
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
    public LambdaClientQueryBuilder<T> print() {
        queryBuilder.print();
        return this;
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
        return this.baseDBApi.selectList(this.queryBuilder);
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
        return this.baseDBApi.selectOne(this.queryBuilder);
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
        return this.baseDBApi.stream(this.queryBuilder);
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
        return this.baseDBApi.selectPage(this.queryBuilder, page);
    }
}