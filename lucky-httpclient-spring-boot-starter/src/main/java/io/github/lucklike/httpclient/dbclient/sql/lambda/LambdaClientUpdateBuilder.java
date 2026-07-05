package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 自带数据库客户端的更新构建器
 * <p>
 * 该类封装了 {@link LambdaUpdateBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 UPDATE 更新条件和设置更新字段，并可直接执行更新操作。
 * </p>
 * <p>
 * <b>警告：</b> 如果条件为空，可能会更新全表数据，请谨慎使用！
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取更新构建器
 * LambdaClientUpdateBuilder<User> updateBuilder = baseDBApi.lambdaUpdate();
 *
 * // 更新单个字段
 * int rows = baseDBApi.lambdaUpdate()
 *     .set(User::getStatus, 1)
 *     .eq(User::getStatus, 0)
 *     .update();
 *
 * // 更新多个字段
 * int rows = baseDBApi.lambdaUpdate()
 *     .set(User::getStatus, 1)
 *     .set(User::getUpdateTime, new Date())
 *     .eq(User::getId, 1L)
 *     .update();
 *
 * // 使用原生字段名更新
 * int rows = baseDBApi.lambdaUpdate()
 *     .set("status", 1)
 *     .where("id = ?", 1L)
 *     .update();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 01:47
 */
public class LambdaClientUpdateBuilder<T> {

    private final BaseDBApi<T> baseDBApi;
    private final LambdaUpdateBuilder<T> updateBuilder;

    /**
     * 构造更新构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientUpdateBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        this.updateBuilder = new LambdaUpdateBuilder<>(clazz);
        this.baseDBApi = baseDBApi;
    }

    /**
     * 构造更新构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi   数据库客户端API
     * @param sqlBuilder  现有的 SQL 构建器
     */
    public LambdaClientUpdateBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        this.updateBuilder = new LambdaUpdateBuilder<>(sqlBuilder);
        this.baseDBApi = baseDBApi;
    }

    public LambdaUpdateBuilder<T> getUpdateBuilder() {
        return updateBuilder;
    }

    // ==================== SET 方法 ====================

    /**
     * 设置要更新的字段值（使用 Lambda 表达式）
     * <p>
     * 指定要更新的列及其新值。可以多次调用以设置多个字段。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * builder.set(User::getStatus, 1)
     *        .set(User::getUpdateTime, new Date());
     * }
     * </pre>
     *
     * @param column 要更新的字段（Lambda 表达式）
     * @param value  新值
     * @return 当前构建器实例，支持链式调用
     */
    public  LambdaClientUpdateBuilder<T> set(SFunction<T, ?> column, Object value) {
        updateBuilder.set(column, value);
        return this;
    }

    /**
     * 设置要更新的字段值（使用原生字段名）
     * <p>
     * 当 Lambda 表达式无法表达或需要使用数据库特定函数时，可使用原生字段名。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * builder.set("status", 1)
     *        .set("update_time", "NOW()");
     * }
     * </pre>
     *
     * @param column 字段名（数据库列名）
     * @param value  新值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> set(String column, Object value) {
        updateBuilder.set(column, value);
        return this;
    }

    // ==================== 条件方法 ====================

    /**
     * 添加自定义 WHERE 条件
     * <p>
     * 使用原生 SQL 片段作为条件，用于限制更新的记录范围。
     * </p>
     * <p>
     * <b>警告：</b> 如果不添加任何条件，将会更新全表数据！
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * builder.where("status = 0")
     *        .where("age BETWEEN ? AND ?", 18, 30);
     * }
     * </pre>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @param values    占位符对应的参数值，按顺序匹配
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> where(String condition, Object... values) {
        updateBuilder.where(condition, values);
        return this;
    }

    /**
     * 添加嵌套条件
     * <p>
     * 通过 Consumer 函数式接口构建嵌套的复杂条件，支持括号分组。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * builder.where(sql -> sql.eq(User::getStatus, 0)
     *                       .or()
     *                       .isNull(User::getDeletedAt));
     * }
     * </pre>
     *
     * @param conditionBuilder 条件构建器函数
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> where(Consumer<LambdaSqlBuilder<T>> conditionBuilder) {
        updateBuilder.where(conditionBuilder);
        return this;
    }

    /**
     * 等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> eq(boolean condition, SFunction<T, ?> column, Object value) {
        updateBuilder.eq(condition, column, value);
        return this;
    }

    /**
     * 不等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> ne(boolean condition, SFunction<T, ?> column, Object value) {
        updateBuilder.ne(condition, column, value);
        return this;
    }

    /**
     * 大于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> gt(boolean condition, SFunction<T, ?> column, Object value) {
        updateBuilder.gt(condition, column, value);
        return this;
    }

    /**
     * 大于等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> ge(boolean condition, SFunction<T, ?> column, Object value) {
        updateBuilder.ge(condition, column, value);
        return this;
    }

    /**
     * 小于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> lt(boolean condition, SFunction<T, ?> column, Object value) {
        updateBuilder.lt(condition, column, value);
        return this;
    }

    /**
     * 小于等于条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> le(boolean condition, SFunction<T, ?> column, Object value) {
        updateBuilder.le(condition, column, value);
        return this;
    }

    /**
     * 模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> like(boolean condition, SFunction<T, ?> column, String value) {
        updateBuilder.like(condition, column, value);
        return this;
    }

    /**
     * 左模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> likeLeft(boolean condition, SFunction<T, ?> column, String value) {
        updateBuilder.likeLeft(condition, column, value);
        return this;
    }

    /**
     * 右模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> likeRight(boolean condition, SFunction<T, ?> column, String value) {
        updateBuilder.likeRight(condition, column, value);
        return this;
    }

    /**
     * 非模糊匹配条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value     匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notLike(boolean condition, SFunction<T, ?> column, String value) {
        updateBuilder.notLike(condition, column, value);
        return this;
    }

    /**
     * IN 条件（条件性添加，可变参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> in(boolean condition, SFunction<T, ?> column, Object... values) {
        updateBuilder.in(condition, column, values);
        return this;
    }

    /**
     * IN 条件（条件性添加，集合参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        updateBuilder.in(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，可变参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值列表
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notIn(boolean condition, SFunction<T, ?> column, Object... values) {
        updateBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * IS NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> isNull(boolean condition, SFunction<T, ?> column) {
        updateBuilder.isNull(condition, column);
        return this;
    }

    /**
     * IS NOT NULL 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        updateBuilder.isNotNull(condition, column);
        return this;
    }

    /**
     * BETWEEN 条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param value1    起始值
     * @param value2    结束值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> between(boolean condition, SFunction<T, ?> column, Object value1, Object value2) {
        updateBuilder.between(condition, column, value1, value2);
        return this;
    }

    /**
     * 排序条件（条件性添加）
     * <p>
     * 注意：在 UPDATE 查询中，ORDER BY 通常不影响更新结果，但某些数据库方言可能需要。
     * </p>
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> orderBy(boolean condition, SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        updateBuilder.orderBy(condition, column, orderType);
        return this;
    }

    /**
     * 升序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        updateBuilder.orderByAsc(condition, column);
        return this;
    }

    /**
     * 降序排序条件（条件性添加）
     *
     * @param condition 是否添加此条件
     * @param column    排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        updateBuilder.orderByDesc(condition, column);
        return this;
    }

    /**
     * NOT IN 条件（可变参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值列表
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notIn(SFunction<T, ?> column, Object... values) {
        updateBuilder.notIn(column, values);
        return this;
    }

    /**
     * NOT IN 条件（条件性添加，集合参数）
     *
     * @param condition 是否添加此条件
     * @param column    表字段的 Lambda 表达式
     * @param values    值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        updateBuilder.notIn(condition, column, values);
        return this;
    }

    /**
     * NOT IN 条件（集合参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notIn(SFunction<T, ?> column, Collection<?> values) {
        updateBuilder.notIn(column, values);
        return this;
    }

    /**
     * 等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> eq(SFunction<T, ?> column, Object value) {
        updateBuilder.eq(column, value);
        return this;
    }

    /**
     * 不等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> ne(SFunction<T, ?> column, Object value) {
        updateBuilder.ne(column, value);
        return this;
    }

    /**
     * 大于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> gt(SFunction<T, ?> column, Object value) {
        updateBuilder.gt(column, value);
        return this;
    }

    /**
     * 大于等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> ge(SFunction<T, ?> column, Object value) {
        updateBuilder.ge(column, value);
        return this;
    }

    /**
     * 小于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> lt(SFunction<T, ?> column, Object value) {
        updateBuilder.lt(column, value);
        return this;
    }

    /**
     * 小于等于条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  比较值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> le(SFunction<T, ?> column, Object value) {
        updateBuilder.le(column, value);
        return this;
    }

    /**
     * 模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> like(SFunction<T, ?> column, String value) {
        updateBuilder.like(column, value);
        return this;
    }

    /**
     * 左模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在前面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> likeLeft(SFunction<T, ?> column, String value) {
        updateBuilder.likeLeft(column, value);
        return this;
    }

    /**
     * 右模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动在后面添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> likeRight(SFunction<T, ?> column, String value) {
        updateBuilder.likeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE 'value%'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value 匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notLikeRight(SFunction<T, ?> column, String value) {
        updateBuilder.notLikeRight(column, value);
        return this;
    }

    /**
     * 不匹配条件（NOT LIKE '%value'）
     *
     * @param column 列对应的 Lambda 函数
     * @param value 匹配模式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notLikeLeft(SFunction<T, ?> column, String value) {
        updateBuilder.notLikeLeft(column, value);
        return this;
    }


    /**
     * 非模糊匹配条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value  匹配值（会自动添加 % 通配符）
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> notLike(SFunction<T, ?> column, String value) {
        updateBuilder.notLike(column, value);
        return this;
    }

    /**
     * IN 条件（集合参数）
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> in(SFunction<T, ?> column, Collection<?> values) {
        updateBuilder.in(column, values);
        return this;
    }

    /**
     * IS NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> isNull(SFunction<T, ?> column) {
        updateBuilder.isNull(column);
        return this;
    }

    /**
     * IS NOT NULL 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> isNotNull(SFunction<T, ?> column) {
        updateBuilder.isNotNull(column);
        return this;
    }

    /**
     * BETWEEN 条件
     *
     * @param column 表字段的 Lambda 表达式
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> between(SFunction<T, ?> column, Object value1, Object value2) {
        updateBuilder.between(column, value1, value2);
        return this;
    }


    /**
     * 添加 OR (xxx) 逻辑表达式
     *
     * @param consumer 括号中的表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> or(Consumer<LambdaClientUpdateBuilder<T>> consumer) {
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
    public LambdaClientUpdateBuilder<T> and(Consumer<LambdaClientUpdateBuilder<T>> consumer) {
        andStart();
        consumer.accept(this);
        andEnd();
        return this;
    }

    /**
     * OR 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> or() {
        updateBuilder.or();
        return this;
    }

    /**
     * AND 逻辑运算符
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> and() {
        updateBuilder.and();
        return this;
    }

    /**
     * 拼接一个['AND ( ']，必须和andEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> andStart() {
        updateBuilder.andStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和andStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> andEnd() {
        updateBuilder.andEnd();
        return this;
    }

    /**
     * 拼接一个['OR ( ']，必须和orEnd方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> orStart() {
        updateBuilder.orStart();
        return this;
    }

    /**
     * 拼接一个[')']，必须和orStart方法配套使用
     *
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientUpdateBuilder<T> orEnd() {
        updateBuilder.orEnd();
        return this;
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
    public LambdaClientUpdateBuilder<T> print() {
        updateBuilder.print();
        return this;
    }

    // ==================== 执行方法 ====================

    /**
     * 执行 UPDATE 操作并返回影响行数
     * <p>
     * 根据构建器中设置的更新字段和条件，执行 UPDATE 操作。
     * </p>
     * <p>
     * <b>警告：</b>
     * <ul>
     *     <li>如果没有设置任何条件，可能会更新全表数据</li>
     *     <li>建议始终添加至少一个条件来限制更新范围</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = baseDBApi.lambdaUpdate()
     *     .set(User::getStatus, 1)
     *     .eq(User::getStatus, 0)
     *     .update();
     * }
     * </pre>
     *
     * @return 被更新的记录行数
     */
    public int update() {
        return this.baseDBApi.update(this.updateBuilder);
    }
}