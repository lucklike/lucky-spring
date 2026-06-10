package io.github.lucklike.httpclient.dbclient.sql.lambda;

import com.luckyframework.common.ContainerUtils;

/**
 * Lambda 查询构造器入口类。
 * <p>
 * 提供基于 Lambda 表达式的类型安全 SQL 构造方法，支持：
 * <ul>
 *     <li>SELECT 查询：{@link #select(Class)}</li>
 *     <li>COUNT 统计：{@link #count(Class, SFunction[])}</li>
 *     <li>UPDATE 更新：{@link #update(Class)}</li>
 *     <li>DELETE 删除：{@link #delete(Class)}</li>
 *     <li>条件构造：{@link #condition(Class)}</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 查询指定列
 * Lambda.select(User.class, User::getName, User::getAge)
 *       .where(User::getStatus).eq(1)
 *       .list();
 *
 * // 统计数量
 * Lambda.count(User.class, User::getStatus)
 *       .where(User::getStatus).eq(1)
 *       .count();
 *
 * // 更新操作
 * Lambda.update(User.class)
 *       .set(User::getStatus, 0)
 *       .where(User::getAge).lt(18)
 *       .update();
 *
 * // 删除操作
 * Lambda.delete(User.class)
 *       .where(User::getStatus).eq(0)
 *       .delete();
 *
 * // 单独的条件构造器
 * Lambda.condition(User.class)
 *       .and(User::getAge).gt(18)
 *       .or(User::getStatus).eq(0);
 * }</pre>
 *
 * @author lucklike
 * @see LambdaQueryBuilder
 * @see LambdaCountBuilder
 * @see LambdaUpdateBuilder
 * @see LambdaDeleteBuilder
 * @see LambdaConditionBuilder
 * @see SFunction
 */
public abstract class Lambda {

    /**
     * 构造 SELECT 查询。
     *
     * @param clazz 实体类类型，用于生成表名和字段映射
     * @param <T>   实体类型
     * @return {@link LambdaQueryBuilder} 查询构造器实例
     */
    public static <T> LambdaQueryBuilder<T> select(Class<T> clazz) {
        return new LambdaQueryBuilder<>(clazz);
    }

    public static <T, R> LambdaSingleColumnQueryBuilder<T, R> column(Class<T> clazz, SFunction<T, R> selectColumn) {
        return new LambdaSingleColumnQueryBuilder<>(clazz, selectColumn);
    }

    /**
     * 构造 COUNT 统计查询。
     * <p>
     * 如果不传入列，默认对所有行进行统计（COUNT(*)）；
     * 如果传入一列，则统计该列非空值的数量。
     *
     * @param clazz   实体类类型
     * @param columns 要统计的列（最多一列，多列只取第一列）
     * @param <T>     实体类型
     * @return {@link LambdaCountBuilder} 统计构造器实例
     */
    @SafeVarargs
    public static <T> LambdaCountBuilder<T> count(Class<T> clazz, SFunction<T, ?>... columns) {
        if (ContainerUtils.isEmptyArray(columns)) {
            return new LambdaCountBuilder<>(clazz);
        }
        return new LambdaCountBuilder<>(clazz, columns[0]);
    }

    /**
     * 构造 UPDATE 更新操作。
     *
     * @param clazz 实体类类型
     * @param <T>   实体类型
     * @return {@link LambdaUpdateBuilder} 更新构造器实例
     */
    public static <T> LambdaUpdateBuilder<T> update(Class<T> clazz) {
        return new LambdaUpdateBuilder<>(clazz);
    }

    /**
     * 构造 DELETE 删除操作。
     *
     * @param clazz 实体类类型
     * @param <T>   实体类型
     * @return {@link LambdaDeleteBuilder} 删除构造器实例
     */
    public static <T> LambdaDeleteBuilder<T> delete(Class<T> clazz) {
        return new LambdaDeleteBuilder<>(clazz);
    }

    /**
     * 构造独立的 WHERE 条件构造器，可用于复杂条件的复用或组装。
     *
     * @param clazz 实体类类型
     * @param <T>   实体类型
     * @return {@link LambdaConditionBuilder} 条件构造器实例
     */
    public static <T> LambdaConditionBuilder<T> condition(Class<T> clazz) {
        return new LambdaConditionBuilder<>(clazz);
    }
}