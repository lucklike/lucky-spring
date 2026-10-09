package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;

/**
 * 自带数据库客户端的恢复构建器
 * <p>
 * 该类封装了 {@link LambdaRestoreBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建恢复条件，并可直接执行恢复操作。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
 * </p>
 * <p>
 * <b>警告：</b> 如果条件为空，可能会恢复全表数据，请谨慎使用！
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取恢复构建器
 * LambdaClientRestoreBuilder<User> restoreBuilder = baseDBApi.lambdaRestore();
 *
 * // 恢复已逻辑删除的用户
 * int rows = baseDBApi.lambdaRestore()
 *     .eq(User::getId, 1L)
 *     .restore();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/9
 */
public class LambdaClientRestoreBuilder<T> extends AbstractLambdaClientBuilder<T, LambdaClientRestoreBuilder<T>, LambdaRestoreBuilder<T>> {

    /**
     * 构造恢复构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientRestoreBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        super(baseDBApi, new LambdaRestoreBuilder<>(clazz));
    }

    /**
     * 构造恢复构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientRestoreBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        super(baseDBApi, new LambdaRestoreBuilder<>(sqlBuilder));
    }

    // ==================== 执行方法 ====================

    /**
     * 执行恢复操作并返回影响行数
     * <p>
     * 根据构建器中设置的条件，将 {@code @LogicDelete} 字段的列写回"未删除值"，
     * 恢复后记录重新对常规查询可见。
     * </p>
     * <p>
     * <b>警告：</b>
     * <ul>
     *     <li>如果没有设置任何条件，可能会恢复全表数据</li>
     *     <li>建议始终添加至少一个条件来限制恢复范围</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = baseDBApi.lambdaRestore()
     *     .eq(User::getId, 1L)
     *     .restore();
     * }
     * </pre>
     * </p>
     *
     * @return 被恢复的记录行数
     */
    public int restore() {
        return this.baseDBApi.restore(this.sqlBuilder);
    }
}
