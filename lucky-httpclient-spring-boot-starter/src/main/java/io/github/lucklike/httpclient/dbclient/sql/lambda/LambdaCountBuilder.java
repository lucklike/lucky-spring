package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lambda 表达式风格的 COUNT 查询构建器
 *
 * <p>专门用于构建统计查询语句的构建器，自动生成 SELECT COUNT(*) 或 SELECT COUNT(column) FROM table
 * 支持添加 JOIN、WHERE、GROUP BY、HAVING、ORDER BY、LIMIT 等子句。
 *
 * <p>使用示例：
 * <pre>
 * // 统计所有用户数量
 * LambdaCountBuilder&lt;User&gt; countBuilder = new LambdaCountBuilder&lt;&gt;(User.class);
 *
 * // 统计指定条件的用户数量
 * LambdaCountBuilder&lt;User&gt; countBuilder = new LambdaCountBuilder&lt;&gt;(User.class);
 * countBuilder.eq(User::getStatus, 1).like(User::getName, "%张%");
 *
 * // 统计指定列的非空值数量
 * LambdaCountBuilder&lt;User&gt; countBuilder = new LambdaCountBuilder&lt;&gt;(User.class, User::getEmail);
 * </pre>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 3.0.0
 * @date 2026/5/25
 */
public class LambdaCountBuilder<T> extends LambdaSqlBuilder<T> {

    /**
     * 构造统计构建器，使用 COUNT(*)
     *
     * @param clazz 实体类类型，用于获取表名
     */
    LambdaCountBuilder(Class<T> clazz) {
        super(clazz);
        selectCount().from();
    }

    /**
     * 构造一个基于 Lambda 表达式的 COUNT 统计构建器。
     * <p>
     * 该构造函数会创建一个统计查询，默认对实体类对应的表进行全表统计（COUNT(*)），
     * 并自动设置 FROM 子句为当前实体类映射的表名。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 统计用户表中所有记录数
     * LambdaCountBuilder<User> countBuilder = new LambdaCountBuilder<>(
     *     LambdaSqlBuilder.of(User.class)
     * );
     *
     * // 最终生成的 SQL 类似于：SELECT COUNT(*) FROM user
     * }</pre>
     *
     * @param sqlBuilder Lambda SQL 构建器实例，用于提供实体类类型、表名映射等基础信息
     */
    public LambdaCountBuilder(LambdaSqlBuilder<T> sqlBuilder) {
        super(sqlBuilder);
        selectCount().from();
    }

    /**
     * 构造一个基于 Lambda 表达式的 COUNT 统计构建器。
     * <p>
     * 该构造函数会创建一个统计查询，统计指定列的非空值数量（COUNT(column)），
     * 并自动设置 FROM 子句为当前实体类映射的表名。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 统计用户表中 status 列非空的记录数
     * LambdaCountBuilder<User> countBuilder = new LambdaCountBuilder<>(
     *     LambdaSqlBuilder.of(User.class),
     *     User::getStatus
     * );
     *
     * // 最终生成的 SQL 类似于：SELECT COUNT(status) FROM user
     * }</pre>
     *
     * @param sqlBuilder Lambda SQL 构建器实例，用于提供实体类类型、表名映射等基础信息
     * @param column     要统计的列对应的 Lambda 函数（如 User::getStatus），
     *                   该方法会统计该列非空值的数量
     */
    public LambdaCountBuilder(LambdaSqlBuilder<T> sqlBuilder, SFunction<T, ?> column) {
        super(sqlBuilder);
        selectCount(column).from();
    }

    /**
     * 构造统计构建器，统计指定列的非空值数量 COUNT(column)
     *
     * @param clazz  实体类类型，用于获取表名
     * @param column 要统计的列对应的 Lambda 函数
     */
    public LambdaCountBuilder(Class<T> clazz, SFunction<T, ?> column) {
        super(clazz);
        selectCount(column).from();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaCountBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        super.join(type, joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaCountBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        super.innerJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaCountBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        super.leftJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaCountBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        super.rightJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> on(String condition) {
        super.on(condition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaCountBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        super.on(leftColumn, rightColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        super.eq(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        super.ne(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        super.gt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        super.ge(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        super.lt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        super.le(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        super.like(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        super.likeLeft(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        super.likeRight(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        super.notLikeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        super.notLikeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        super.notLike(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaCountBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaCountBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        super.isNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        super.isNotNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        super.between(condition, column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        super.orderByAsc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        super.orderByDesc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaCountBuilder<T> notIn(SFunction<T, R> column, R... values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> notIn(SFunction<T, R> column, Collection<R> values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SafeVarargs
    public final <R> LambdaCountBuilder<T> in(SFunction<T, R> column, R... values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaCountBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> or() {
        super.or();
        return this;
    }

    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaCountBuilder<T> or(Consumer<LambdaCountBuilder<T>> consumer) {
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
    public LambdaCountBuilder<T> and(Consumer<LambdaCountBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> and() {
        super.and();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> andStart() {
        super.andStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> andEnd() {
        super.andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orStart() {
        super.orStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orEnd() {
        super.orEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orderByAsc(SFunction<T, ?> column) {
        super.orderByAsc(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaCountBuilder<T> print() {
        super.print();
        return this;
    }
}