// LambdaQueryBuilder.java

package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lambda 表达式风格的 SELECT 查询构建器
 *
 * <p>专门用于构建 SELECT 查询语句的构建器，提供了类型安全的 Lambda 方式
 * 来指定查询列、WHERE 条件、JOIN、GROUP BY、ORDER BY、LIMIT 等子句。
 *
 * <p>使用示例：
 * <pre>
 * // 查询所有列
 * LambdaQueryBuilder&lt;User&gt; query = new LambdaQueryBuilder&lt;&gt;(User.class);
 *
 * // 查询指定列
 * LambdaQueryBuilder&lt;User&gt; query = new LambdaQueryBuilder&lt;&gt;(User.class, User::getId, User::getName);
 *
 * // 带条件的查询
 * LambdaQueryBuilder&lt;User&gt; query = new LambdaQueryBuilder&lt;&gt;(User.class);
 * query.eq(User::getStatus, 1)
 *      .like(User::getName, "%test%")
 *      .orderByDesc(User::getCreateTime)
 *      .limit(10);
 *
 * // 复杂条件（嵌套括号）
 * query.where(b -> {
 *     b.eq(User::getStatus, 1).or().eq(User::getStatus, 2);
 * }).and().gt(User::getAge, 18);
 * </pre>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 3.0.0
 * @date 2026/5/25
 */
public class LambdaQueryBuilder<T> extends LambdaSqlBuilder<T> {

    /**
     * 构造查询构建器，默认查询所有列（SELECT *）
     *
     * @param clazz 实体类类型，用于获取表名和列信息
     */
    LambdaQueryBuilder(Class<T> clazz) {
        super(clazz);
        select().from();
    }

    /**
     * 构造一个查询所有列的 SELECT 查询构建器。
     *
     * @param sqlBuilder Lambda SQL 构建器实例
     */
    public LambdaQueryBuilder(LambdaSqlBuilder<T> sqlBuilder) {
        super(sqlBuilder);
        select().from();
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final LambdaQueryBuilder<T> select(SFunction<T, ?>... columns) {
         super.select(columns);
         return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaQueryBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        super.join(type, joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaQueryBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        super.innerJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaQueryBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        super.leftJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaQueryBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        super.rightJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> on(String condition) {
        super.on(condition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaQueryBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        super.on(leftColumn, rightColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> eq(boolean condition, SFunction<T, ?> column, Object value) {
        super.eq(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> ne(boolean condition, SFunction<T, ?> column, Object value) {
        super.ne(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> gt(boolean condition, SFunction<T, ?> column, Object value) {
        super.gt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> ge(boolean condition, SFunction<T, ?> column, Object value) {
        super.ge(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> lt(boolean condition, SFunction<T, ?> column, Object value) {
        super.lt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> le(boolean condition, SFunction<T, ?> column, Object value) {
        super.le(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        super.like(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        super.likeLeft(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        super.likeRight(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        super.notLikeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        super.notLikeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        super.notLike(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> in(boolean condition, SFunction<T, ?> column, Object... values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notIn(boolean condition, SFunction<T, ?> column, Object... values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notIn(SFunction<T, ?> column, Collection<?> values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        super.isNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        super.isNotNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        super.between(condition, column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        super.orderByAsc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        super.orderByDesc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notIn(SFunction<T, ?> column, Object... values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> eq(SFunction<T, ?> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> ne(SFunction<T, ?> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> gt(SFunction<T, ?> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> ge(SFunction<T, ?> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> lt(SFunction<T, ?> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> le(SFunction<T, ?> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> in(SFunction<T, ?> column, Object... values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> in(SFunction<T, ?> column, Collection<?> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaQueryBuilder<T> or(Consumer<LambdaQueryBuilder<T>> consumer) {
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
    public LambdaQueryBuilder<T> and(Consumer<LambdaQueryBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> or() {
        super.or();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> and() {
        super.and();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> andStart() {
        super.andStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> andEnd() {
        super.andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orStart() {
        super.orStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orEnd() {
        super.orEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orderByAsc(SFunction<T, ?> column) {
        super.orderByAsc(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaQueryBuilder<T> print() {
        super.print();
        return this;
    }
}