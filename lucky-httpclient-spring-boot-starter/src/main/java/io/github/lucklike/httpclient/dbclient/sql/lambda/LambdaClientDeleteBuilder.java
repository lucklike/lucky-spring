package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;

/**
 * 自带数据库客户端的删除构建器
 * <p>
 * 该类封装了 {@link LambdaDeleteBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 DELETE 删除条件，并可直接执行删除操作。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
 * </p>
 * <p>
 * <b>警告：</b> 如果条件为空，可能会删除全表数据，请谨慎使用！
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取删除构建器
 * LambdaClientDeleteBuilder<User> deleteBuilder = baseDBApi.lambdaDelete();
 *
 * // 删除状态为 0 的用户
 * int rows = baseDBApi.lambdaDelete()
 *     .eq(User::getStatus, 0)
 *     .delete();
 *
 * // 删除年龄小于 18 岁的用户
 * int rows = baseDBApi.lambdaDelete()
 *     .lt(User::getAge, 18)
 *     .delete();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 01:59
 */
public class LambdaClientDeleteBuilder<T> extends AbstractLambdaClientBuilder<T, LambdaClientDeleteBuilder<T>, LambdaDeleteBuilder<T>> {

    /**
     * 构造删除构建器（使用实体类）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientDeleteBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        super(baseDBApi, new LambdaDeleteBuilder<>(clazz));
    }

    /**
     * 构造删除构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientDeleteBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        super(baseDBApi, new LambdaDeleteBuilder<>(sqlBuilder));
    }

    // ==================== 执行方法 ====================

    /**
     * 执行 DELETE 操作并返回影响行数
     * <p>
     * 根据构建器中设置的条件，执行 DELETE 操作。
     * </p>
     * <p>
     * <b>警告：</b>
     * <ul>
     *     <li>如果没有设置任何条件，可能会删除全表数据</li>
     *     <li>建议始终添加至少一个条件来限制删除范围</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = baseDBApi.lambdaDelete()
     *     .eq(User::getStatus, 0)
     *     .delete();
     * }
     * </pre>
     *
     * @return 被删除的记录行数
     */
    public int delete() {
        return this.baseDBApi.delete(this.sqlBuilder);
    }
}
