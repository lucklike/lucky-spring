package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.executor.SFunction;
import org.springframework.core.ResolvableType;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class LambdaQueryBuilder<T> extends LambdaSqlBuilder<T> {

    @SafeVarargs
    public LambdaQueryBuilder(Class<T> clazz, SFunction<T, ?>... selectColumns) {
        super(clazz);
        select(selectColumns).from();
    }

    @Override
    public LambdaQueryBuilder<T> tableName(String tableName) {
        super.tableName(tableName);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> select(SFunction<T, ?>... columns) {
        super.select(columns);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> select(String expression) {
        super.select(expression);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> selectCount() {
        super.selectCount();
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> selectCount(SFunction<T, ?> column) {
        super.selectCount(column);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> selectDistinct(SFunction<T, ?>... columns) {
        super.selectDistinct(columns);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> from() {
        super.from();
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> from(String alias) {
        super.from(alias);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> from(String tableName, String alias) {
        super.from(tableName, alias);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> from(LambdaSqlBuilder<?> subQuery, String alias) {
        super.from(subQuery, alias);
        return this;
    }

    @Override
    public <E> LambdaQueryBuilder<T> join(JoinType type, Class<E> joinClass, String alias) {
        super.join(type, joinClass, alias);
        return this;
    }

    @Override
    public <E> LambdaQueryBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        super.innerJoin(joinClass, alias);
        return this;
    }

    @Override
    public <E> LambdaQueryBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        super.leftJoin(joinClass, alias);
        return this;
    }

    @Override
    public <E> LambdaQueryBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        super.rightJoin(joinClass, alias);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> on(String condition) {
        super.on(condition);
        return this;
    }

    @Override
    public <E> LambdaQueryBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        super.on(leftColumn, rightColumn);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> insertInto(SFunction<T, ?>... columns) {
        super.insertInto(columns);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> values(Object... values) {
        super.values(values);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> valuesBatch(List<Object[]> batchValues) {
        super.valuesBatch(batchValues);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> update() {
        super.update();
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> set(SFunction<T, R> column, Object value) {
        super.set(column, value);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> set(String column, Object value) {
        super.set(column, value);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> delete() {
        super.delete();
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> in(SFunction<T, R> column, R... values) {
        super.in(column, values);
        return this;
    }

    @Override
    public <R> LambdaQueryBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> or() {
        super.or();
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> and() {
        super.and();
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> groupBy(SFunction<T, ?>... columns) {
        super.groupBy(columns);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> having(String condition, Object... values) {
        super.having(condition, values);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> orderBy(SFunction<T, ?> column, OrderType orderType) {
        super.orderBy(column, orderType);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> orderByAsc(SFunction<T, ?> column) {
        super.orderByAsc(column);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(column);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> limit(int limit) {
        super.limit(limit);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> limit(int offset, int limit) {
        super.limit(offset, limit);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> offset(int offset) {
        super.offset(offset);
        return this;
    }

    @Override
    public LambdaQueryBuilder<T> print() {
        super.print();
        return this;
    }
}