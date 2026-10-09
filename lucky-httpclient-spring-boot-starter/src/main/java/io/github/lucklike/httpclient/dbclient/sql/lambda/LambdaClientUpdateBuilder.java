package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;

/**
 * 自带数据库客户端的更新构建器
 * <p>
 * 该类封装了 {@link LambdaUpdateBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 UPDATE 更新条件和设置更新字段，并可直接执行更新操作。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
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
public class LambdaClientUpdateBuilder<T> extends AbstractLambdaClientBuilder<T, LambdaClientUpdateBuilder<T>, LambdaUpdateBuilder<T>> {

    /**
     * 构造更新构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientUpdateBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        super(baseDBApi, new LambdaUpdateBuilder<>(clazz));
    }

    /**
     * 构造更新构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientUpdateBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        super(baseDBApi, new LambdaUpdateBuilder<>(sqlBuilder));
    }

    /**
     * 获取内部委托的更新构建器
     *
     * @return 更新构建器
     */
    public LambdaUpdateBuilder<T> getUpdateBuilder() {
        return sqlBuilder;
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
    public LambdaClientUpdateBuilder<T> set(SFunction<T, ?> column, Object value) {
        sqlBuilder.set(column, value);
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
        sqlBuilder.set(column, value);
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
        return this.baseDBApi.update(this.sqlBuilder);
    }
}
