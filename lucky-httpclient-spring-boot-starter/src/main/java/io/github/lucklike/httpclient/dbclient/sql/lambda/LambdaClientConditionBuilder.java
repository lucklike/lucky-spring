package io.github.lucklike.httpclient.dbclient.sql.lambda;

import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.dbclient.BaseDBApi;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 自带数据库客户端的条件构建器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 02:11
 */
public class LambdaClientConditionBuilder<T> extends LambdaConditionBuilder<T> {

    private final BaseDBApi<T> baseDBApi;

    /**
     * 构造条件构建器
     *
     * @param entityClass 实体类类型
     */
    public LambdaClientConditionBuilder(BaseDBApi<T> baseDBApi, Class<T> entityClass) {
        super(entityClass);
        this.baseDBApi = baseDBApi;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> on(String condition) {
        super.on(condition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> LambdaClientConditionBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        super.on(leftColumn, rightColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaClientConditionBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaClientConditionBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaClientConditionBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaClientConditionBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaClientConditionBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaClientConditionBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> LambdaClientConditionBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> or() {
        super.or();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaClientConditionBuilder<T> and() {
        super.and();
        return this;
    }


    // ==================== 类型转换方法 ====================

    /**
     * 转换为查询构建器
     *
     * @param selectColumns 要查询的列
     * @ 查询构建器
     */
    @SafeVarargs
    public final LambdaClientQueryBuilder<T> becomeSelect(SFunction<T, ?>... selectColumns) {
        if (ContainerUtils.isNotEmptyArray(selectColumns)) {
            return new LambdaClientQueryBuilder<>(this.baseDBApi, this);
        }
        return new LambdaClientQueryBuilder<>(this.baseDBApi, this, selectColumns);
    }

    /**
     * 转换为统计构建器
     *
     * @param column 要统计的列（不传则 COUNT(*)）
     * @ 统计构建器
     */
    @SafeVarargs
    public final LambdaClientCountBuilder<T> becomeCount(SFunction<T, ?>... column) {
        if (ContainerUtils.isEmptyArray(column)) {
            return new LambdaClientCountBuilder<>(this.baseDBApi, this);
        }
        return new LambdaClientCountBuilder<>(this.baseDBApi, this, column[0]);
    }

    /**
     * 转换为删除构建器
     *
     * @ 删除构建器
     */
    public final LambdaClientDeleteBuilder<T> becomeDelete() {
        return new LambdaClientDeleteBuilder<>(this.baseDBApi, this);
    }

    /**
     * 转换为更新构建器
     *
     * @ 更新构建器
     */
    public final LambdaClientUpdateBuilder<T> becomeUpdate() {
        return new LambdaClientUpdateBuilder<>(this.baseDBApi, this);
    }
}
