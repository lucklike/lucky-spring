package io.github.lucklike.httpclient.dbclient.sql.lambda;

import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lambda 表达式条件构建器。
 * <p>
 * 专门用于构建 WHERE 条件片段，不包含完整的 SQL 语句。
 * 可作为独立条件构造器使用，也可转换为完整的 CRUD 操作构建器。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 创建条件构建器
 * LambdaConditionBuilder<User> condition = Lambda.condition(User.class);
 *
 * // 构建复合条件
 * condition.where(b -> b
 *     .eq(User::getStatus, 1)
 *     .and()
 *     .gt(User::getAge, 18)
 * );
 *
 * // 转换为查询
 * LambdaQueryBuilder<User> users = condition.toSelect(User::getName, User::getAge);
 *
 * // 转换为删除
 * LambdaDeleteBuilder<User> = condition.toDelete();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31
 */
public class LambdaConditionBuilder<T> extends LambdaSqlBuilder<T> {

    /**
     * 构造条件构建器
     *
     * @param entityClass 实体类类型
     */
    public LambdaConditionBuilder(Class<T> entityClass) {
        super(entityClass);
    }

    // ==================== 条件方法（返回自身类型） ====================


    /**
     * {@inheritDoc}
     */
    public LambdaConditionBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public LambdaConditionBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> eq(boolean condition, SFunction<T, R> column, Object value) {
        super.eq(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> ne(boolean condition, SFunction<T, R> column, Object value) {
        super.ne(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> gt(boolean condition, SFunction<T, R> column, Object value) {
        super.gt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> ge(boolean condition, SFunction<T, R> column, Object value) {
        super.ge(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> lt(boolean condition, SFunction<T, R> column, Object value) {
        super.lt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> le(boolean condition, SFunction<T, R> column, Object value) {
        super.le(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        super.like(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        super.likeLeft(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        super.likeRight(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        super.notLikeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        super.notLikeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        super.notLike(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaConditionBuilder<T> in(boolean condition, SFunction<T, R> column, R... values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> in(boolean condition, SFunction<T, R> column, Collection<R> values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaConditionBuilder<T> notIn(boolean condition, SFunction<T, R> column, R... values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> notIn(boolean condition, SFunction<T, R> column, Collection<R> values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        super.isNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        super.isNotNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        super.between(condition, column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        super.orderByAsc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        super.orderByDesc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SafeVarargs
    @Override
    public final <R> LambdaConditionBuilder<T> notIn(SFunction<T, R> column, R... values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> notIn(SFunction<T, R> column, Collection<R> values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SafeVarargs
    public final <R> LambdaConditionBuilder<T> in(SFunction<T, R> column, R... values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaConditionBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaConditionBuilder<T> or(Consumer<LambdaConditionBuilder<T>> consumer) {
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
    public LambdaConditionBuilder<T> and(Consumer<LambdaConditionBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> or() {
        super.or();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> and() {
        super.and();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> andStart() {
        super.andStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> andEnd() {
        super.andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> orStart() {
        super.orStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaConditionBuilder<T> orEnd() {
        super.orEnd();
        return this;
    }


    // ==================== 类型转换方法 ====================

    /**
     * 转换为查询构建器
     *
     * @return 查询构建器
     */
    public final LambdaQueryBuilder<T> toSelect() {
        return new LambdaQueryBuilder<>(this);
    }

    /**
     * 转换为统计构建器
     *
     * @param column 要统计的列（不传则 COUNT(*)）
     * @return 统计构建器
     */
    @SafeVarargs
    public final LambdaCountBuilder<T> toCount(SFunction<T, ?>... column) {
        if (ContainerUtils.isEmptyArray(column)) {
            return new LambdaCountBuilder<>(this);
        }
        return new LambdaCountBuilder<>(this, column[0]);
    }

    /**
     * 转换为删除构建器
     *
     * @return 删除构建器
     */
    public final LambdaDeleteBuilder<T> toDelete() {
        return new LambdaDeleteBuilder<>(this);
    }

    /**
     * 转换为更新构建器
     *
     * @return 更新构建器
     */
    public final LambdaUpdateBuilder<T> toUpdate() {
        return new LambdaUpdateBuilder<>(this);
    }
}