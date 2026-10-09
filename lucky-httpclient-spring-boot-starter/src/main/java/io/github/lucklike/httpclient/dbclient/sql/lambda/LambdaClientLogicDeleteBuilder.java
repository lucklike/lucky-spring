package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;

/**
 * 自带数据库客户端的逻辑删除构建器
 * <p>
 * 该类封装了 {@link LambdaLogicDeleteBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建逻辑删除条件，并可直接执行逻辑删除操作。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
 * </p>
 * <p>
 * <b>警告：</b> 如果条件为空，可能会"删除"全表数据，请谨慎使用！
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取逻辑删除构建器
 * LambdaClientLogicDeleteBuilder<User> logicDeleteBuilder = baseDBApi.lambdaLogicDelete();
 *
 * // 逻辑删除状态为 0 的用户
 * int rows = baseDBApi.lambdaLogicDelete()
 *     .eq(User::getStatus, 0)
 *     .logicDelete();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/9
 */
public class LambdaClientLogicDeleteBuilder<T> extends AbstractLambdaClientBuilder<T, LambdaClientLogicDeleteBuilder<T>, LambdaLogicDeleteBuilder<T>> {

    /**
     * 构造逻辑删除构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientLogicDeleteBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        super(baseDBApi, new LambdaLogicDeleteBuilder<>(clazz));
    }

    /**
     * 构造逻辑删除构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientLogicDeleteBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        super(baseDBApi, new LambdaLogicDeleteBuilder<>(sqlBuilder));
    }

    // ==================== 执行方法 ====================

    /**
     * 执行逻辑删除操作并返回影响行数
     * <p>
     * 根据构建器中设置的条件，将 {@code @LogicDelete} 字段的列更新为"已删除值"，
     * 不物理删除数据。
     * </p>
     * <p>
     * <b>警告：</b>
     * <ul>
     *     <li>如果没有设置任何条件，可能会"删除"全表数据</li>
     *     <li>建议始终添加至少一个条件来限制删除范围</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = baseDBApi.lambdaLogicDelete()
     *     .eq(User::getStatus, 0)
     *     .logicDelete();
     * }
     * </pre>
     * </p>
     *
     * @return 被"删除"的记录行数
     */
    public int logicDelete() {
        return this.baseDBApi.logicDelete(this.sqlBuilder);
    }
}
