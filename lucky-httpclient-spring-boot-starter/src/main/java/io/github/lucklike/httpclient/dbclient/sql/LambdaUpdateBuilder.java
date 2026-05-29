// LambdaUpdateBuilder.java

package io.github.lucklike.httpclient.dbclient.sql;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lambda 表达式风格的 UPDATE SQL 构建器
 *
 * <p>专门用于构建 UPDATE 语句的构建器，提供了类型安全的 Lambda 方式
 * 来指定要更新的列和 WHERE 条件，自动从实体类获取表名。
 *
 * <p>使用示例：
 * <pre>
 * // 单字段更新
 * LambdaUpdateBuilder&lt;User&gt; update = new LambdaUpdateBuilder&lt;&gt;(User.class);
 * update.set(User::getStatus, 1)
 *       .where(b -> b.eq(User::getId, 100L));
 *
 * // 多字段更新
 * update.set(User::getStatus, 1)
 *       .set(User::getUpdateTime, LocalDateTime.now())
 *       .eq(User::getAge, 25)
 *       .and()
 *       .like(User::getName, "%admin%");
 * </pre>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 3.0.0
 * @date 2026/5/25
 */
public class LambdaUpdateBuilder<T> extends LambdaSqlBuilder<T> {

    /**
     * 构造 UPDATE 构建器
     * 会自动调用 update() 设置 UPDATE 子句
     *
     * @param clazz 实体类类型，用于获取表名
     */
    LambdaUpdateBuilder(Class<T> clazz) {
        super(clazz);
        update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> set(SFunction<T, R> column, Object value) {
        super.set(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> set(String column, Object value) {
        super.set(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SafeVarargs
    public final <R> LambdaUpdateBuilder<T> in(SFunction<T, R> column, R... values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaUpdateBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> or() {
        super.or();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> and() {
        super.and();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaUpdateBuilder<T> print() {
        super.print();
        return this;
    }
}