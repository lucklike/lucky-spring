package io.github.lucklike.httpclient.dbclient;

import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientConditionBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientCountBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientSingleColumnQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientUpdateBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.SFunction;
import org.springframework.lang.NonNull;

/**
 * Lambda 构建器工厂能力接口。
 * <p>
 * 提供各类 Lambda 客户端构建器的创建入口，包括：
 * <ul>
 *     <li>查询构建器</li>
 *     <li>更新构建器</li>
 *     <li>删除构建器</li>
 *     <li>统计构建器</li>
 *     <li>条件构建器</li>
 *     <li>单列查询构建器</li>
 * </ul>
 * </p>
 *
 * @param <E> 实体类型泛型
 * @author fukang
 * @version 1.0.0
 */
public interface LambdaBuilderApi<E> extends DbApi<E> {

    /**
     * 获取当前实例的 {@link BaseDBApi} 视图。
     * <p>
     * 用于创建各类 Lambda 客户端构建器（只应在由 {@link BaseDBApi} 继承后使用）。
     * </p>
     *
     * @return 当前实例的 BaseDBApi 视图
     */
    @SuppressWarnings("unchecked")
    default BaseDBApi<E> dbApi() {
        return (BaseDBApi<E>) this;
    }

    /**
     * 创建 Lambda 查询构建器（查询所有列）。
     * <p>
     * 返回一个基于当前数据库客户端的 Lambda 查询构建器，
     * 用于构建动态查询条件并执行查询操作。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> users = userDBApi.lambdaQuery()
     *     .where(User::getStatus).eq(1)
     *     .list();
     * }</pre>
     * </p>
     *
     * @return Lambda 查询构建器
     */
    default LambdaClientQueryBuilder<E> lambdaQuery() {
        return new LambdaClientQueryBuilder<>(dbApi(), entityClass());
    }

    /**
     * 创建 Lambda 查询构建器（查询所有列）。
     * <p>
     * 返回一个基于当前数据库客户端的 Lambda 查询构建器，
     * 用于构建动态查询条件并执行查询操作。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * User user = new User();
     * user.setName("Jack");
     * List<User> users = userDBApi.lambdaQuery(user)
     *     .where(User::getStatus).eq(1)
     *     .list();
     * }</pre>
     * </p>
     *
     * @return Lambda 查询构建器
     */
    default LambdaClientQueryBuilder<E> lambdaQuery(@NonNull E entity) {
        return new LambdaClientQueryBuilder<>(dbApi(), entity);
    }


    /**
     * 创建 Lambda 更新构建器。
     * <p>
     * 返回一个基于当前数据库客户端的 Lambda 更新构建器，
     * 用于构建动态更新条件并执行更新操作。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * userDBApi.lambdaUpdate()
     *     .set(User::getStatus, 1)
     *     .where(User::getId).eq(1L)
     *     .update();
     * }</pre>
     * </p>
     *
     * @return Lambda 更新构建器
     */
    default LambdaClientUpdateBuilder<E> lambdaUpdate() {
        return new LambdaClientUpdateBuilder<>(dbApi(), entityClass());
    }

    /**
     * 创建 Lambda 删除构建器。
     * <p>
     * 返回一个基于当前数据库客户端的 Lambda 删除构建器，
     * 用于构建动态删除条件并执行删除操作。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * userDBApi.lambdaDelete()
     *     .where(User::getStatus).eq(0)
     *     .delete();
     * }</pre>
     * </p>
     *
     * @return Lambda 删除构建器
     */
    default LambdaClientDeleteBuilder<E> lambdaDelete() {
        return new LambdaClientDeleteBuilder<>(dbApi(), entityClass());
    }

    /**
     * 创建 Lambda 统计构建器（COUNT(*)）。
     * <p>
     * 返回一个基于当前数据库客户端的 Lambda 统计构建器，
     * 用于构建动态统计条件并执行 COUNT 操作。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * long count = userDBApi.lambdaCount()
     *     .where(User::getStatus).eq(1)
     *     .count();
     * }</pre>
     * </p>
     *
     * @return Lambda 统计构建器
     */
    default LambdaClientCountBuilder<E> lambdaCount() {
        return new LambdaClientCountBuilder<>(dbApi(), entityClass());
    }

    /**
     * 创建 Lambda 统计构建器（统计指定列的非空值数量）。
     * <p>
     * 统计指定列的非空值数量，而不是 COUNT(*)。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 统计有邮箱地址的用户数量
     * long count = userDBApi.lambdaCount(User::getEmail)
     *     .where(User::getStatus).eq(1)
     *     .count();
     * }</pre>
     * </p>
     *
     * @param countColumn 要统计的列
     * @return Lambda 统计构建器
     */
    default LambdaClientCountBuilder<E> lambdaCount(SFunction<E, ?> countColumn) {
        return new LambdaClientCountBuilder<>(dbApi(), entityClass(), countColumn);
    }

    /**
     * 创建 Lambda 条件构建器。
     * <p>
     * 返回一个基于当前数据库客户端的 Lambda 条件构建器，
     * 用于构建动态条件，后续可转换为查询、更新、删除、统计等操作。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 构建条件
     * LambdaClientConditionBuilder<User> condition = userDBApi.lambdaCondition()
     *     .where(User::getStatus).eq(1)
     *     .orderByDesc(User::getCreateTime);
     *
     * // 转换为查询
     * List<User> users = condition.toSelect().list();
     *
     * // 转换为统计
     * long count = condition.toCount().count();
     * }</pre>
     * </p>
     *
     * @return Lambda 条件构建器
     */
    default LambdaClientConditionBuilder<E> lambdaCondition() {
        return new LambdaClientConditionBuilder<>(dbApi(), entityClass());
    }

    /**
     * 创建单列查询构建器，用于查询指定列。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询所有用户的姓名列表
     * List<String> names = userDBApi.lambdaColumn(User::getName).list();
     *
     * // 分页查询用户ID列表
     * PageResult<Long> ids = userDBApi.lambdaColumn(User::getId).eq(User::getStatus, 1).page(Page.of(1, 10));
     *
     * // 流式查询
     * try(Stream<Long> stream = userDBApi.lambdaColumn(User::getId).stream()){
     *     stream.filter(name -> name.startsWith("张")).forEach(System.out::println);
     * }
     *
     * // 查询单个列
     * Long id = userDBApi.lambdaColumn(User::getId).eq(User::getStatus, 1).one()
     * }</pre>
     * </p>
     *
     * @param selectColumn 要查询的列（Lambda表达式）
     * @param <R>          列类型
     * @return 单列查询构建器
     */
    default <R> LambdaClientSingleColumnQueryBuilder<E, R> lambdaColumn(SFunction<E, R> selectColumn) {
        return new LambdaClientSingleColumnQueryBuilder<>(dbApi(), entityClass(), selectColumn);
    }
}
