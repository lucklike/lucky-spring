// LambdaDeleteBuilder.java

package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lambda 表达式风格的 DELETE SQL 构建器
 *
 * <p>专门用于构建 DELETE 语句的构建器，提供了类型安全的 Lambda 方式
 * 来指定 WHERE 条件，自动从实体类获取表名。
 *
 * <p>使用示例：
 * <pre>
 * LambdaDeleteBuilder&lt;User&gt; builder = new LambdaDeleteBuilder&lt;&gt;(User.class);
 * builder.eq(User::getStatus, 0)
 *        .and()
 *        .lt(User::getCreateTime, LocalDate.now().minusDays(30))
 *        .build();
 * // 生成：DELETE FROM user WHERE status = ? AND create_time &lt; ?
 * </pre>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 3.0.0
 * @date 2026/5/25
 */
public class LambdaDeleteBuilder<T> extends LambdaSqlBuilder<T> {

    /**
     * 构造 DELETE 构建器
     * 会自动调用 delete().from() 设置 DELETE FROM 子句
     *
     * @param clazz 实体类类型，用于获取表名
     */
    LambdaDeleteBuilder(Class<T> clazz) {
        super(clazz);
        deleteSQL().from();
    }

    /**
     * 构造一个基于 Lambda 表达式的 DELETE 删除构建器。
     * <p>
     * 该构造函数会创建一个删除操作，默认对实体类对应的表执行删除（DELETE FROM table），
     * 并自动设置 FROM 子句为当前实体类映射的表名。
     * <p>
     * <b>注意：</b> 如果不在后续链式调用中添加 WHERE 条件，执行时将删除表中的所有数据，
     * 请务必在调用 delete() 方法前添加必要的过滤条件。
     * <p>
     * 使用示例：
     * <pre>{@code
     * LambdaDeleteBuilder<User> deleteBuilder = new LambdaDeleteBuilder<>(
     *     LambdaSqlBuilder.of(User.class)
     * );
     *
     * // 安全做法：添加条件后删除
     * deleteBuilder.eq(User::getStatus, 0);
     * }</pre>
     *
     * @param sqlBuilder Lambda SQL 构建器实例，用于提供实体类类型、表名映射等基础信息
     */
    public LambdaDeleteBuilder(LambdaSqlBuilder<T> sqlBuilder) {
        super(sqlBuilder);
        deleteSQL().from();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
         super.eq(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
         super.ne(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
         super.gt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
         super.ge(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
         super.lt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
         super.le(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
         super.like(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
         super.likeLeft(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
         super.likeRight(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        super.notLikeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        super.notLikeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
         super.notLike(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaDeleteBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
         super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
         super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaDeleteBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
         super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
         super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
         super.isNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
         super.isNotNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
         super.between(condition, column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
         super.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
         super.orderByAsc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
         super.orderByDesc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaDeleteBuilder<T> notIn(SFunction<T, R> column, R... values) {
         super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> notIn(SFunction<T, R> column, Collection<R> values) {
         super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SafeVarargs
    public final <R> LambdaDeleteBuilder<T> in(SFunction<T, R> column, R... values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaDeleteBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaDeleteBuilder<T> or(Consumer<LambdaDeleteBuilder<T>> consumer) {
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
    public LambdaDeleteBuilder<T> and(Consumer<LambdaDeleteBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> or() {
        super.or();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> and() {
        super.and();
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> andStart() {
        super.andStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> andEnd() {
        super.andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> orStart() {
        super.orStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> orEnd() {
        super.orEnd();
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaDeleteBuilder<T> print() {
        super.print();
        return this;
    }
}