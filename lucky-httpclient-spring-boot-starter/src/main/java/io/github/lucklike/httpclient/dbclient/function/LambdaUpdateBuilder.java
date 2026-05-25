package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.executor.SFunction;

import java.util.Collection;
import java.util.function.Consumer;

public class LambdaUpdateBuilder<T> extends LambdaSqlBuilder<T> {

    LambdaUpdateBuilder(Class<T> clazz) {
        super(clazz);
        update().from();
    }

    @Override
    public <R> LambdaUpdateBuilder<T> set(SFunction<T, R> column, Object value) {
        super.set(column, value);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> set(String column, Object value) {
        super.set(column, value);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    @Override
    public <R> LambdaUpdateBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    @Override
    public <R> LambdaUpdateBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    @Override
    public <R> LambdaUpdateBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    @Override
    public <R> LambdaUpdateBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    @Override
    public <R> LambdaUpdateBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    @Override
    public <R> LambdaUpdateBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    @SafeVarargs
    @Override
    public final <R> LambdaUpdateBuilder<T> in(SFunction<T, R> column, R... values) {
        super.in(column, values);
        return this;
    }

    @Override
    public <R> LambdaUpdateBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> or() {
        super.or();
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> and() {
        super.and();
        return this;
    }

    @Override
    public LambdaUpdateBuilder<T> print() {
        super.print();
        return this;
    }
}