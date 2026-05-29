// LambdaDeleteBuilder.java

package io.github.lucklike.httpclient.dbclient.sql;

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
        delete().from();
    }

    @Override
    public LambdaDeleteBuilder<T> where(String condition, Object... values) {
        super.where(condition, values);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        super.where(conditionBuilder);
        return this;
    }

    @Override
    public <R> LambdaDeleteBuilder<T> eq(SFunction<T, R> column, Object value) {
        super.eq(column, value);
        return this;
    }

    @Override
    public <R> LambdaDeleteBuilder<T> ne(SFunction<T, R> column, Object value) {
        super.ne(column, value);
        return this;
    }

    @Override
    public <R> LambdaDeleteBuilder<T> gt(SFunction<T, R> column, Object value) {
        super.gt(column, value);
        return this;
    }

    @Override
    public <R> LambdaDeleteBuilder<T> ge(SFunction<T, R> column, Object value) {
        super.ge(column, value);
        return this;
    }

    @Override
    public <R> LambdaDeleteBuilder<T> lt(SFunction<T, R> column, Object value) {
        super.lt(column, value);
        return this;
    }

    @Override
    public <R> LambdaDeleteBuilder<T> le(SFunction<T, R> column, Object value) {
        super.le(column, value);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> like(SFunction<T, ?> column, String value) {
        super.like(column, value);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        super.likeLeft(column, value);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        super.likeRight(column, value);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> notLike(SFunction<T, ?> column, String value) {
        super.notLike(column, value);
        return this;
    }

    @Override
    @SafeVarargs
    public final <R> LambdaDeleteBuilder<T> in(SFunction<T, R> column, R... values) {
        super.in(column, values);
        return this;
    }

    @Override
    public <R> LambdaDeleteBuilder<T> in(SFunction<T, R> column, Collection<R> values) {
        super.in(column, values);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        super.between(column, value1, value2);
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> or() {
        super.or();
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> and() {
        super.and();
        return this;
    }

    @Override
    public LambdaDeleteBuilder<T> print() {
        super.print();
        return this;
    }
}