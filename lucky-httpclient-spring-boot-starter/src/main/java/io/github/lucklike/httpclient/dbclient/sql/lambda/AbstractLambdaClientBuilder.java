package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lambda 客户端构建器抽象基类
 * <p>
 * 该类封装了 {@link BaseDBApi} 与具体的 {@link LambdaSqlBuilder} 实现，
 * 为所有 {@code LambdaClientXxxBuilder} 提供统一的公共条件方法与链式调用支持。
 * </p>
 * <p>
 * 子类通过 {@code SELF} 自引用泛型参数保持链式调用返回自身具体类型；
 * 通过 {@code B} 泛型参数指定内部委托的 {@link LambdaSqlBuilder} 具体实现，供子类执行方法使用。
 * </p>
 *
 * @param <T>    实体类型
 * @param <SELF> 子类自身类型（自引用泛型）
 * @param <B>    内部委托的 SQL 构建器具体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/9
 */
public abstract class AbstractLambdaClientBuilder<T, SELF extends AbstractLambdaClientBuilder<T, SELF, B>, B extends LambdaSqlBuilder<T>> {

    /**
     * 数据库客户端API
     */
    protected final BaseDBApi<T> baseDBApi;

    /**
     * 内部委托的 SQL 构建器
     */
    protected final B sqlBuilder;

    /**
     * 构造客户端构建器
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 内部委托的 SQL 构建器
     */
    protected AbstractLambdaClientBuilder(BaseDBApi<T> baseDBApi, B sqlBuilder) {
        this.baseDBApi = baseDBApi;
        this.sqlBuilder = sqlBuilder;
    }

    /**
     * 获取当前构建器实例（子类具体类型）
     *
     * @return 当前构建器实例
     */
    @SuppressWarnings("unchecked")
    protected final SELF self() {
        return (SELF) this;
    }

    // ==================== 条件方法 ====================

    /**
     * 添加自定义 WHERE 条件
     * <p>
     * 使用原生 SQL 片段作为条件，可用于构建复杂或 Lambda 表达式无法表达的条件。
     * </p>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @param values    占位符对应的参数值，按顺序匹配
     * @return 当前构建器实例，支持链式调用
     */
    public SELF where(String condition, Object... values) {
        sqlBuilder.where(condition, values);
        return self();
    }

    /**
     * 添加嵌套条件
     * <p>
     * 通过 Consumer 函数式接口构建嵌套的复杂条件，支持括号分组。
     * </p>
     *
     * @param conditionBuilder 条件构建器函数
     * @return 当前构建器实例，支持链式调用
     */
    public SELF where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        sqlBuilder.where(conditionBuilder);
        return self();
    }

    /**
     * 等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加等于条件：column = value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF eq(boolean condition, SFunction<T, ?> column, Object value) {
        sqlBuilder.eq(condition, column, value);
        return self();
    }

    /**
     * 不等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加不等于条件：column != value 或 column &lt;&gt; value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF ne(boolean condition, SFunction<T, ?> column, Object value) {
        sqlBuilder.ne(condition, column, value);
        return self();
    }

    /**
     * 大于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加大于条件：column > value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF gt(boolean condition, SFunction<T, ?> column, Object value) {
        sqlBuilder.gt(condition, column, value);
        return self();
    }

    /**
     * 大于等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加大于等于条件：column >= value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF ge(boolean condition, SFunction<T, ?> column, Object value) {
        sqlBuilder.ge(condition, column, value);
        return self();
    }

    /**
     * 小于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加小于条件：column < value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF lt(boolean condition, SFunction<T, ?> column, Object value) {
        sqlBuilder.lt(condition, column, value);
        return self();
    }

    /**
     * 小于等于条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加小于等于条件：column <= value
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF le(boolean condition, SFunction<T, ?> column, Object value) {
        sqlBuilder.le(condition, column, value);
        return self();
    }

    /**
     * 模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 LIKE 条件：column LIKE '%value%'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF like(boolean condition, SFunction<T, ?> column, String value) {
        sqlBuilder.like(condition, column, value);
        return self();
    }

    /**
     * 左模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 LIKE 条件：column LIKE '%value'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        sqlBuilder.likeLeft(condition, column, value);
        return self();
    }

    /**
     * 右模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 LIKE 条件：column LIKE 'value%'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF likeRight(boolean condition, SFunction<T, ?> column, String value) {
        sqlBuilder.likeRight(condition, column, value);
        return self();
    }

    /**
     * 非模糊匹配条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 NOT LIKE 条件：column NOT LIKE '%value%'
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF notLike(boolean condition, SFunction<T, ?> column, String value) {
        sqlBuilder.notLike(condition, column, value);
        return self();
    }

    /**
     * IN 条件（条件性添加，可变参数）
     * <p>
     * 当 condition 为 true 时，添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @return 当前构建器实例，支持链式调用
     */
    public SELF in(boolean condition, SFunction<T, ?> column, Object... values) {
        sqlBuilder.in(condition, column, values);
        return self();
    }

    /**
     * IN 条件（条件性添加，集合参数）
     * <p>
     * 当 condition 为 true 时，添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @return 当前构建器实例，支持链式调用
     */
    public SELF in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        sqlBuilder.in(condition, column, values);
        return self();
    }

    /**
     * NOT IN 条件（条件性添加，可变参数）
     * <p>
     * 当 condition 为 true 时，添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @return 当前构建器实例，支持链式调用
     */
    public SELF notIn(boolean condition, SFunction<T, ?> column, Object... values) {
        sqlBuilder.notIn(condition, column, values);
        return self();
    }

    /**
     * NOT IN 条件（条件性添加，集合参数）
     * <p>
     * 当 condition 为 true 时，添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @return 当前构建器实例，支持链式调用
     */
    public SELF notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        sqlBuilder.notIn(condition, column, values);
        return self();
    }

    /**
     * IS NULL 条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 IS NULL 条件：column IS NULL
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF isNull(boolean condition, SFunction<T, ?> column) {
        sqlBuilder.isNull(condition, column);
        return self();
    }

    /**
     * IS NOT NULL 条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 IS NOT NULL 条件：column IS NOT NULL
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF isNotNull(boolean condition, SFunction<T, ?> column) {
        sqlBuilder.isNotNull(condition, column);
        return self();
    }

    /**
     * BETWEEN 条件（条件性添加）
     * <p>
     * 当 condition 为 true 时，添加 BETWEEN 条件：column BETWEEN value1 AND value2
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value1    起始值
     * @param value2    结束值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        sqlBuilder.between(condition, column, value1, value2);
        return self();
    }

    /**
     * 排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public SELF orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        sqlBuilder.orderBy(condition, column, orderType);
        return self();
    }

    /**
     * 升序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF orderByAsc(boolean condition, SFunction<T, ?> column) {
        sqlBuilder.orderByAsc(condition, column);
        return self();
    }

    /**
     * 降序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF orderByDesc(boolean condition, SFunction<T, ?> column) {
        sqlBuilder.orderByDesc(condition, column);
        return self();
    }

    /**
     * NOT IN 条件（可变参数）
     * <p>
     * 添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值列表
     * @return 当前构建器实例，支持链式调用
     */
    public SELF notIn(SFunction<T, ?> column, Object... values) {
        sqlBuilder.notIn(column, values);
        return self();
    }

    /**
     * 等于条件
     * <p>
     * 添加等于条件：column = value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF eq(SFunction<T, ?> column, Object value) {
        sqlBuilder.eq(column, value);
        return self();
    }

    /**
     * 不等于条件
     * <p>
     * 添加不等于条件：column != value 或 column &lt;&gt; value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF ne(SFunction<T, ?> column, Object value) {
        sqlBuilder.ne(column, value);
        return self();
    }

    /**
     * 大于条件
     * <p>
     * 添加大于条件：column > value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF gt(SFunction<T, ?> column, Object value) {
        sqlBuilder.gt(column, value);
        return self();
    }

    /**
     * 大于等于条件
     * <p>
     * 添加大于等于条件：column >= value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF ge(SFunction<T, ?> column, Object value) {
        sqlBuilder.ge(column, value);
        return self();
    }

    /**
     * 小于条件
     * <p>
     * 添加小于条件：column < value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF lt(SFunction<T, ?> column, Object value) {
        sqlBuilder.lt(column, value);
        return self();
    }

    /**
     * 小于等于条件
     * <p>
     * 添加小于等于条件：column <= value
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF le(SFunction<T, ?> column, Object value) {
        sqlBuilder.le(column, value);
        return self();
    }

    /**
     * 模糊匹配条件
     * <p>
     * 添加 LIKE 条件：column LIKE '%value%'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF like(SFunction<T, ?> column, String value) {
        sqlBuilder.like(column, value);
        return self();
    }

    /**
     * 左模糊匹配条件
     * <p>
     * 添加 LIKE 条件：column LIKE '%value'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF likeLeft(SFunction<T, ?> column, String value) {
        sqlBuilder.likeLeft(column, value);
        return self();
    }

    /**
     * 右模糊匹配条件
     * <p>
     * 添加 LIKE 条件：column LIKE 'value%'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF likeRight(SFunction<T, ?> column, String value) {
        sqlBuilder.likeRight(column, value);
        return self();
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF notLikeRight(SFunction<T, ?> column, String value) {
        sqlBuilder.notLikeRight(column, value);
        return self();
    }

    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value  匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF notLikeLeft(SFunction<T, ?> column, String value) {
        sqlBuilder.notLikeLeft(column, value);
        return self();
    }

    /**
     * 非模糊匹配条件
     * <p>
     * 添加 NOT LIKE 条件：column NOT LIKE '%value%'
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public SELF notLike(SFunction<T, ?> column, String value) {
        sqlBuilder.notLike(column, value);
        return self();
    }

    /**
     * IN 条件（集合参数）
     * <p>
     * 添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @return 当前构建器实例，支持链式调用
     */
    public SELF in(SFunction<T, ?> column, Collection<?> values) {
        sqlBuilder.in(column, values);
        return self();
    }

    /**
     * IS NULL 条件
     * <p>
     * 添加 IS NULL 条件：column IS NULL
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF isNull(SFunction<T, ?> column) {
        sqlBuilder.isNull(column);
        return self();
    }

    /**
     * IS NOT NULL 条件
     * <p>
     * 添加 IS NOT NULL 条件：column IS NOT NULL
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF isNotNull(SFunction<T, ?> column) {
        sqlBuilder.isNotNull(column);
        return self();
    }

    /**
     * BETWEEN 条件
     * <p>
     * 添加 BETWEEN 条件：column BETWEEN value1 AND value2
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前构建器实例，支持链式调用
     */
    public SELF between(SFunction<T, ?> column, Object value1, Object value2) {
        sqlBuilder.between(column, value1, value2);
        return self();
    }

    // ==================== 逻辑拼接方法 ====================

    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF or(Consumer<SELF> consumer) {
        orStart();
        consumer.accept(self());
        orEnd();
        return self();
    }

    /**
     * 添加 AND (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public SELF and(Consumer<SELF> consumer) {
        andStart();
        consumer.accept(self());
        andEnd();
        return self();
    }

    /**
     * OR 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public SELF or() {
        sqlBuilder.or();
        return self();
    }

    /**
     * AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public SELF and() {
        sqlBuilder.and();
        return self();
    }

    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public SELF andStart() {
        sqlBuilder.andStart();
        return self();
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public SELF andEnd() {
        sqlBuilder.andEnd();
        return self();
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public SELF orStart() {
        sqlBuilder.orStart();
        return self();
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public SELF orEnd() {
        sqlBuilder.orEnd();
        return self();
    }

    // ==================== 调试方法 ====================

    /**
     * 打印最终生成的 SQL 语句和参数到控制台
     * <p>
     * 用于调试和开发阶段，方便查看实际执行的 SQL。
     * 生产环境建议关闭此功能。
     * </p>
     *
     * @return 当前构建器实例，支持链式调用
     */
    public SELF print() {
        sqlBuilder.print();
        return self();
    }
}
