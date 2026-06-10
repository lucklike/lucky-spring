package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.function.LambdaUtils;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/10 00:58
 */
public class LambdaSingleColumnQueryBuilder<T, R> extends LambdaSqlBuilder<T> {

    private final SFunction<T, R> selectColumn;

    /**
     * 构造查询构建器，默认查询所有列（SELECT *）
     *
     * @param clazz 实体类类型，用于获取表名和列信息
     */
    @SuppressWarnings("unchecked")
    public LambdaSingleColumnQueryBuilder(Class<T> clazz, SFunction<T, R> selectColumn) {
        super(clazz);
        this.selectColumn = selectColumn;
        select(selectColumn).from();
    }

    /**
     * 构造一个查询所有列的 SELECT 查询构建器。
     *
     * @param sqlBuilder Lambda SQL 构建器实例
     */
    @SuppressWarnings("unchecked")
    public LambdaSingleColumnQueryBuilder(LambdaSqlBuilder<T> sqlBuilder, SFunction<T, R> selectColumn) {
        super(sqlBuilder);
        this.selectColumn = selectColumn;
        select(selectColumn).from();
    }

    /**
     * 获取查询列的类型
     *
     * @return 查询列的类型
     */
    public Class<?> getSelectColumnType() {
        return LambdaUtils.getField(selectColumn).getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaSingleColumnQueryBuilder<T, R> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        super.join(type, joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaSingleColumnQueryBuilder<T, R> innerJoin(Class<E> joinClass, String alias) {
        super.innerJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaSingleColumnQueryBuilder<T, R> leftJoin(Class<E> joinClass, String alias) {
        super.leftJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaSingleColumnQueryBuilder<T, R> rightJoin(Class<E> joinClass, String alias) {
        super.rightJoin(joinClass, alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> on(String condition) {
        super.on(condition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaSingleColumnQueryBuilder<T, R> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        super.on(leftColumn, rightColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> eq(boolean condition, SFunction<T, ?> column, Object value) {
        super.eq(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> ne(boolean condition, SFunction<T, ?> column, Object value) {
        super.ne(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> gt(boolean condition, SFunction<T, ?> column, Object value) {
        super.gt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> ge(boolean condition, SFunction<T, ?> column, Object value) {
        super.ge(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> lt(boolean condition, SFunction<T, ?> column, Object value) {
        super.lt(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> le(boolean condition, SFunction<T, ?> column, Object value) {
        super.le(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> like(boolean condition, SFunction<T, ?> column, String value) {
        super.like(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        super.likeLeft(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        super.likeRight(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notLikeRight(SFunction<T, ?> column, String value) {
        super.notLikeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notLikeLeft(SFunction<T, ?> column, String value) {
        super.notLikeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notLike(boolean condition, SFunction<T, ?> column, String value) {
        super.notLike(condition, column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> in(boolean condition, SFunction<T, ?> column, Object... values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        super.in(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notIn(boolean condition, SFunction<T, ?> column, Object... values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        super.notIn(condition, column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notIn(SFunction<T, ?> column, Collection<?> values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> isNull(boolean condition, SFunction<T, ?> column) {
        super.isNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> isNotNull(boolean condition, SFunction<T, ?> column) {
        super.isNotNull(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        super.between(condition, column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orderByAsc(boolean condition, SFunction<T, ?> column) {
        super.orderByAsc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orderByDesc(boolean condition, SFunction<T, ?> column) {
        super.orderByDesc(condition, column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notIn(SFunction<T, ?> column, Object... values) {
        super.notIn(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> eq(SFunction<T, ?> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> ne(SFunction<T, ?> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> gt(SFunction<T, ?> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> ge(SFunction<T, ?> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> lt(SFunction<T, ?> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> le(SFunction<T, ?> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> in(SFunction<T, ?> column, Object... values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> in(SFunction<T, ?> column, Collection<?> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaSingleColumnQueryBuilder<T, R> or(Consumer<LambdaSingleColumnQueryBuilder<T, R>> consumer) {
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
    public LambdaSingleColumnQueryBuilder<T, R> and(Consumer<LambdaSingleColumnQueryBuilder<T, R>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> or() {
        super.or();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> and() {
        super.and();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> andStart() {
        super.andStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> andEnd() {
        super.andEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orStart() {
        super.orStart();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orEnd() {
        super.orEnd();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        super.orderBy(column, orderType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orderByAsc(SFunction<T, ?> column) {
        super.orderByAsc(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaSingleColumnQueryBuilder<T, R> print() {
        super.print();
        return this;
    }
}
