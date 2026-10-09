package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

/**
 * 自带数据库客户端的数量统计构建器
 * <p>
 * 该类封装了 {@link LambdaCountBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 COUNT 统计查询，并可直接执行统计操作。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取统计构建器
 * LambdaClientCountBuilder<User> countBuilder = baseDBApi.lambdaCount();
 *
 * // 统计总记录数
 * long total = countBuilder.count();
 *
 * // 统计满足条件的记录数
 * long count = baseDBApi.lambdaCount()
 *     .eq(User::getStatus, 1)
 *     .count();
 *
 * // 判断是否存在满足条件的记录
 * boolean exists = baseDBApi.lambdaCount()
 *     .eq(User::getEmail, "test@example.com")
 *     .exist();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 02:04
 */
public class LambdaClientCountBuilder<T> extends AbstractLambdaClientBuilder<T, LambdaClientCountBuilder<T>, LambdaCountBuilder<T>> {

    /**
     * 构造统计构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        super(baseDBApi, new LambdaCountBuilder<>(clazz));
    }

    /**
     * 构造统计构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        super(baseDBApi, new LambdaCountBuilder<>(sqlBuilder));
    }

    /**
     * 构造统计构建器（使用现有的 SQL 构建器和指定统计列）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     * @param column     要统计的列
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder, SFunction<T, ?> column) {
        super(baseDBApi, new LambdaCountBuilder<>(sqlBuilder, column));
    }

    /**
     * 构造统计构建器（使用实体类和指定统计列）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     * @param column    要统计的列
     */
    public LambdaClientCountBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz, SFunction<T, ?> column) {
        super(baseDBApi, new LambdaCountBuilder<>(clazz, column));
    }

    /**
     * 统计时包含已删除记录
     * <p>
     * 调用后本次统计不追加"不等于已删除值"的过滤条件，统计结果同时包含未删除与已删除记录；
     * 与查询路径的 includeDeleted() 口径一致。实体未标注 {@code @LogicDelete} 字段时抛出明确异常。
     * </p>
     *
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 实体未标注 {@code @LogicDelete} 字段时
     */
    public LambdaClientCountBuilder<T> includeDeleted() {
        sqlBuilder.includeDeleted();
        return this;
    }

    // ==================== 关联表方法 ====================

    /**
     * 添加 JOIN 关联
     * <p>
     * 支持 INNER JOIN、LEFT JOIN、RIGHT JOIN 等关联类型。
     * 添加 JOIN 后需要通过 {@link #on} 方法指定关联条件。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * baseDBApi.lambdaCount(User.class)
     *     .leftJoin(Order.class, "o")
     *     .on(User::getId, Order::getUserId)
     *     .eq(User::getStatus, 1)
     *     .count();
     * }
     * </pre>
     *
     * @param type      JOIN 类型（INNER、LEFT、RIGHT）
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        sqlBuilder.join(type, joinClass, alias);
        return this;
    }

    /**
     * 添加 INNER JOIN 关联
     * <p>
     * 等价于 {@code join(JoinType.INNER, joinClass, alias)}
     * </p>
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        sqlBuilder.innerJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 LEFT JOIN 关联
     * <p>
     * 等价于 {@code join(JoinType.LEFT, joinClass, alias)}
     * </p>
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        sqlBuilder.leftJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 RIGHT JOIN 关联
     * <p>
     * 等价于 {@code join(JoinType.RIGHT, joinClass, alias)}
     * </p>
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        sqlBuilder.rightJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 JOIN 关联条件（原生 SQL）
     * <p>
     * 使用原生 SQL 片段指定 JOIN 的 ON 条件。
     * </p>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> on(String condition) {
        sqlBuilder.on(condition);
        return this;
    }

    /**
     * 添加 JOIN 关联条件（Lambda 表达式）
     * <p>
     * 使用 Lambda 表达式指定 JOIN 的 ON 条件，如 left.column = right.column
     * </p>
     *
     * @param leftColumn  左表字段的 Lambda 表达式
     * @param rightColumn 右表字段的 Lambda 表达式
     * @param <E>         右表实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientCountBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        sqlBuilder.on(leftColumn, rightColumn);
        return this;
    }

    // ==================== 排序方法 ====================

    /**
     * 添加排序条件
     *
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        sqlBuilder.orderBy(column, orderType);
        return this;
    }

    /**
     * 添加升序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderByAsc(SFunction<T, ?> column) {
        sqlBuilder.orderByAsc(column);
        return this;
    }

    /**
     * 添加降序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientCountBuilder<T> orderByDesc(SFunction<T, ?> column) {
        sqlBuilder.orderByDesc(column);
        return this;
    }

    // ==================== 执行方法 ====================

    /**
     * 执行 COUNT 查询并返回统计结果
     * <p>
     * 根据构建器中设置的条件，执行 COUNT 查询并返回统计结果。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * long count = baseDBApi.lambdaCount()
     *     .eq(User::getStatus, 1)
     *     .count();
     * }
     * </pre>
     *
     * @return 统计结果（满足条件的记录数）
     */
    public long count() {
        return this.baseDBApi.count(this.sqlBuilder);
    }

    /**
     * 判断满足条件的记录是否存在
     * <p>
     * 根据构建器中设置的条件，判断是否存在满足条件的记录。
     * 等价于 {@code count() > 0}，但性能更优（某些数据库实现会使用 LIMIT 1 优化）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * boolean exists = baseDBApi.lambdaCount()
     *     .eq(User::getEmail, "test@example.com")
     *     .exist();
     * }
     * </pre>
     *
     * @return true 表示存在至少一条记录，false 表示不存在任何记录
     */
    public boolean exist() {
        return count() > 0;
    }
}
