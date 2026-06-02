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

    @Override
    public LambdaConditionBuilder<T> on(String condition) {
        return super.on(condition);
    }

    @Override
    public <E> LambdaConditionBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        return super.on(leftColumn, rightColumn);
    }

    @Override
    public LambdaConditionBuilder<T> where(String condition, Object... values) {
        return super.where(condition, values);
    }

    @Override
    public LambdaConditionBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        return super.where(conditionBuilder);
    }

    @Override
    public <R> LambdaConditionBuilder<T> eq(SFunction<T, R> column, Object value) {
        return super.eq(column, value);
    }

    @Override
    public <R> LambdaConditionBuilder<T> ne(SFunction<T, R> column, Object value) {
        return super.ne(column, value);
    }

    @Override
    public <R> LambdaConditionBuilder<T> gt(SFunction<T, R> column, Object value) {
        return super.gt(column, value);
    }

    @Override
    public <R> LambdaConditionBuilder<T> ge(SFunction<T, R> column, Object value) {
        return super.ge(column, value);
    }

    @Override
    public <R> LambdaConditionBuilder<T> lt(SFunction<T, R> column, Object value) {
        return super.lt(column, value);
    }

    @Override
    public <R> LambdaConditionBuilder<T> le(SFunction<T, R> column, Object value) {
        return super.le(column, value);
    }

    @Override
    public LambdaConditionBuilder<T> like(SFunction<T, ?> column, String value) {
        return super.like(column, value);
    }

    @Override
    public LambdaConditionBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        return super.likeLeft(column, value);
    }

    @Override
    public LambdaConditionBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        return super.likeRight(column, value);
    }

    @Override
    public LambdaConditionBuilder<T> notLike(SFunction<T, ?> column, String value) {
        return super.notLike(column, value);
    }

    @Override
    public <R> LambdaConditionBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        return super.in(column, values);
    }

    @Override
    public LambdaConditionBuilder<T> isNull(SFunction<T, ?> column) {
        return super.isNull(column);
    }

    @Override
    public LambdaConditionBuilder<T> isNotNull(SFunction<T, ?> column) {
        return super.isNotNull(column);
    }

    @Override
    public LambdaConditionBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        return super.between(column, value1, value2);
    }

    @Override
    public LambdaConditionBuilder<T> or() {
        return super.or();
    }

    @Override
    public LambdaConditionBuilder<T> and() {
        return super.and();
    }


    // ==================== 类型转换方法 ====================

    /**
     * 转换为查询构建器
     *
     *
     * @param selectColumns 要查询的列
     * @return 查询构建器
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
     * @return 统计构建器
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
     * @return 删除构建器
     */
    public final LambdaClientDeleteBuilder<T> becomeDelete() {
        return new LambdaClientDeleteBuilder<>(this.baseDBApi,this);
    }

    /**
     * 转换为更新构建器
     *
     * @return 更新构建器
     */
    public final LambdaClientUpdateBuilder<T> becomeUpdate() {
        return new LambdaClientUpdateBuilder<>(this.baseDBApi, this);
    }
}
