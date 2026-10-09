// LambdaRestoreBuilder.java

package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.function.EntityUtils;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lambda 表达式风格的恢复构建器
 *
 * <p>专门用于构建恢复语句的构建器，基于 UPDATE 语句实现：构造时自动将
 * {@code @LogicDelete} 字段的列写回"未删除值"，并提供类型安全的 Lambda 方式
 * 指定 WHERE 条件，自动从实体类获取表名。恢复后记录重新对常规查询可见。
 *
 * <p>使用示例：
 * <pre>
 * // 按条件恢复
 * LambdaRestoreBuilder&lt;User&gt; builder = new LambdaRestoreBuilder&lt;&gt;(User.class);
 * builder.eq(User::getId, 100L);
 * // 生成：UPDATE user SET deleted = ? WHERE id = ?
 * </pre>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 3.0.0
 * @date 2026/10/9
 */
public class LambdaRestoreBuilder<T> extends LambdaSqlBuilder<T> {

    /**
     * 构造恢复构建器
     * 会自动调用 update() 设置 UPDATE 子句，并将逻辑删除列写回"未删除值"
     *
     * @param clazz 实体类类型，用于获取表名与逻辑删除规则
     */
    LambdaRestoreBuilder(Class<T> clazz) {
        super(clazz);
        initRestoreUpdate();
    }

    /**
     * 构造一个基于 Lambda 表达式的恢复构建器。
     * <p>
     * 该构造函数会创建一个恢复操作，默认对实体类对应的表执行更新（UPDATE table），
     * 并自动将 {@code @LogicDelete} 字段的列写回"未删除值"。
     * <p>
     * <b>注意：</b> 如果不在后续链式调用中添加 WHERE 条件，执行时将恢复表中的所有数据，
     * 请务必在调用恢复方法前添加必要的过滤条件。
     * <p>
     * 使用示例：
     * <pre>{@code
     * LambdaRestoreBuilder<User> builder = new LambdaRestoreBuilder<>(
     *     LambdaSqlBuilder.of(User.class)
     * );
     *
     * // 安全做法：添加条件后执行恢复
     * builder.eq(User::getId, 100L);
     * }</pre>
     *
     * @param sqlBuilder Lambda SQL 构建器实例，用于提供实体类类型、表名映射等基础信息
     * @throws IllegalArgumentException 实体未标注 {@code @LogicDelete} 字段、标注了多个或取值非法时
     */
    public LambdaRestoreBuilder(LambdaSqlBuilder<T> sqlBuilder) {
        super(sqlBuilder);
        initRestoreUpdate();
    }

    /**
     * 初始化恢复的 UPDATE 语句：SET 逻辑删除列 = 未删除值
     */
    private void initRestoreUpdate() {
        EntityUtils.LogicDeleteRule rule = EntityUtils.getLogicDeleteRule(getEntityClass());
        if (rule == null) {
            throw new IllegalArgumentException(String.format(
                    "Entity [%s] has no @LogicDelete field defined, restore requires a @LogicDelete field",
                    getEntityClass().getName()));
        }
        updateSQL();
        set(rule.getColumnName(), rule.getNotDeletedValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> eq(boolean condition, SFunction<T, ?> column, Object value) {
        super.eq(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> ne(boolean condition, SFunction<T, ?> column, Object value) {
        super.ne(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> gt(boolean condition, SFunction<T, ?> column, Object value) {
        super.gt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> ge(boolean condition, SFunction<T, ?> column, Object value) {
        super.ge(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> lt(boolean condition, SFunction<T, ?> column, Object value) {
        super.lt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> le(boolean condition, SFunction<T, ?> column, Object value) {
        super.le(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        super.like(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        super.likeLeft(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        super.likeRight(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        super.notLikeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        super.notLikeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        super.notLike(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> in(boolean condition, SFunction<T, ?> column, Object... values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notIn(boolean condition, SFunction<T, ?> column, Object... values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        super.isNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        super.isNotNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        super.between(condition, column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        super.orderByAsc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        super.orderByDesc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notIn(SFunction<T, ?> column, Object... values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notIn(SFunction<T, ?> column, Collection<?> values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> eq(SFunction<T, ?> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> ne(SFunction<T, ?> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> gt(SFunction<T, ?> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> ge(SFunction<T, ?> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> lt(SFunction<T, ?> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> le(SFunction<T, ?> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> in(SFunction<T, ?> column, Object... values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> in(SFunction<T, ?> column, Collection<?> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaRestoreBuilder<T> or(Consumer<LambdaRestoreBuilder<T>> consumer) {
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
    public LambdaRestoreBuilder<T> and(Consumer<LambdaRestoreBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> or() {
        super.or();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> and() {
        super.and();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> andStart() {
        super.andStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> andEnd() {
        super.andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> orStart() {
        super.orStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> orEnd() {
        super.orEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaRestoreBuilder<T> print() {
        super.print();
        return this;
    }
}
