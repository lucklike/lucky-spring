package io.github.lucklike.httpclient.dbclient;

import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaConditionBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaCountBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 查询能力接口。
 * <p>
 * 提供基于 Lambda 构建器、实体对象以及 Map 条件的查询能力，包括：
 * <ul>
 *     <li>COUNT 统计</li>
 *     <li>单条查询</li>
 *     <li>列表查询</li>
 *     <li>分页查询</li>
 *     <li>流式查询</li>
 * </ul>
 * </p>
 *
 * @param <E> 实体类型泛型
 * @author fukang
 * @version 1.0.0
 */
public interface QueryApi<E> extends DbApi<E> {

    /**
     * 执行 COUNT 类型的 SQL 并返回统计结果。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 统计年龄大于18岁的用户数量
     * long count = mapper.count(Lambda.count(User.class)
     *     .where(User::getAge).gt(18));
     * }</pre>
     * </p>
     *
     * @param countBuilder COUNT 查询条件构建器
     * @return 统计结果（满足条件的记录数）
     */
    @SQL(executor = SQL_LAMBDA)
    long count(LambdaCountBuilder<E> countBuilder);

    /**
     * 执行 COUNT 类型的 SQL 并返回统计结果（使用条件构建器）
     * <p>
     * 便捷方法，将条件构建器转换为统计构建器后执行。
     * </p>
     *
     * @param conditionBuilder 条件构建器
     * @return 统计结果
     */
    default long count(LambdaConditionBuilder<E> conditionBuilder) {
        return count(conditionBuilder.toCount());
    }

    /**
     * 执行 SELECT 类型的 SQL 并返回单个结果。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接和排序。
     * 如果查询结果为空，则返回 {@code null}。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询ID为1的用户
     * User user = mapper.selectOne(Lambda.select(User.class)
     *     .where(User::getId).eq(1L));
     * }</pre>
     * </p>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @return 查询结果，可能为 {@code null}
     */
    @Nullable
    @SQL(executor = SQL_LAMBDA)
    E selectOne(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 执行 SELECT 类型的 SQL 并返回单个结果（使用条件构建器）
     * <p>
     * 便捷方法，将条件构建器转换为查询构建器后执行。
     * </p>
     *
     * @param conditionBuilder 条件构建器
     * @return 查询结果，可能为 {@code null}
     */
    default E selectOne(LambdaConditionBuilder<E> conditionBuilder) {
        return selectOne(conditionBuilder.toSelect());
    }

    /**
     * 执行 SELECT 类型的 SQL 并返回结果列表。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接、排序和分页。
     * 如果查询结果为空，则返回空列表（非 {@code null}）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询年龄大于18岁的用户列表，按年龄降序排列
     * List<User> users = mapper.selectList(Lambda.select(User.class)
     *     .where(User::getAge).gt(18)
     *     .orderByDesc(User::getAge));
     * }</pre>
     * </p>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @return 查询结果列表，永远不为 {@code null}
     */
    @NonNull
    @SQL(executor = SQL_LAMBDA)
    List<E> selectList(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 执行 SELECT 类型的 SQL 并返回结果列表（使用条件构建器）
     * <p>
     * 便捷方法，将条件构建器转换为查询构建器后执行。
     * </p>
     *
     * @param conditionBuilder 条件构建器
     * @return 查询结果列表，永远不为 {@code null}
     */
    default List<E> selectList(LambdaConditionBuilder<E> conditionBuilder) {
        return selectList(conditionBuilder.toSelect());
    }

    /**
     * 执行 SELECT 类型的 SQL 并返回分页结果。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接和排序，自动完成总记录数查询和分页数据查询。
     * 分页参数通过 {@link Page} 对象传递，包含当前页码、每页大小、排序字段等信息。
     * </p>
     * <p>
     * <b>注意：</b> 如果 {@link Page#isCountTotal()} 为 {@code true}，则会自动执行 COUNT 查询；
     * 否则只查询分页数据，总记录数为 -1。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 创建分页对象
     * Page page = Page.of(1, 10).desc("create_time");
     *
     * // 执行分页查询
     * PageResult<User> result = mapper.selectPage(Lambda.select(User.class)
     *     .where(User::getStatus).eq(1), page);
     *
     * // 获取分页结果
     * List<User> records = result.getRecords();
     * long total = result.getTotalCount();
     * }</pre>
     * </p>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @param page         分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     */
    @SQL(executor = SQL_LAMBDA)
    PageResult<E> selectPage(LambdaQueryBuilder<E> queryBuilder, @NonNull Page page);

    /**
     * 执行 SELECT 类型的 SQL 并返回分页结果（使用条件构建器）
     *
     * @param conditionBuilder 条件构建器
     * @param page             分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     */
    default PageResult<E> selectPage(LambdaConditionBuilder<E> conditionBuilder, @NonNull Page page) {
        return selectPage(conditionBuilder.toSelect(), page);
    }

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @param pageNum      查询的页数
     * @param pageSize     每页的条数
     * @return 对应页码对的数据
     */
    default List<E> simplePage(LambdaQueryBuilder<E> queryBuilder, long pageNum, long pageSize) {
        return selectPage(queryBuilder, Page.notCount(pageNum, pageSize)).getRecords();
    }

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param conditionBuilder 条件构建器
     * @param pageNum          查询的页数
     * @param pageSize         每页的条数
     * @return 对应页码对的数据
     */
    default List<E> simplePage(LambdaConditionBuilder<E> conditionBuilder, long pageNum, long pageSize) {
        return selectPage(conditionBuilder, Page.notCount(pageNum, pageSize)).getRecords();
    }

    /**
     * 执行 SELECT 类型 SQL 并以流式方式返回结果。
     * <p>
     * 返回的 {@link Stream} 需要在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
     * 适用于处理大量数据，避免一次性加载所有结果到内存。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * try (Stream<User> stream = mapper.stream(Lambda.select(User.class)
     *         .where(User::getAge).gt(18))) {
     *     stream.filter(user -> user.getName().startsWith("张"))
     *           .forEach(System.out::println);
     * }
     * }</pre>
     * </p>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     */
    @NonNull
    @SQL(executor = SQL_LAMBDA)
    Stream<E> stream(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 执行 SELECT 类型 SQL 并以流式方式返回结果（使用条件构建器）
     *
     * @param conditionBuilder 条件构建器
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     */
    default Stream<E> stream(LambdaConditionBuilder<E> conditionBuilder) {
        return stream(conditionBuilder.toSelect());
    }

    /**
     * 使用实体对象作为条件进行分页查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     *     <li>支持通过 {@link Page} 对象进行分页和排序</li>
     *     <li>支持通过字段上的 {@code @Column} 和 {@code @Id} 注解自定义列名</li>
     *     <li>支持通过字段上的 {@code @Column(condition = XxxCondition.class)} 注解自定义条件类型</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 创建查询条件实体
     * User condition = new User();
     * condition.setStatus(1);
     * condition.setAge(18);
     *
     * // 创建分页对象
     * Page page = Page.of(1, 10).desc("create_time");
     *
     * // 执行分页查询
     * PageResult<User> result = mapper.selectPage(condition, page);
     * }</pre>
     * </p>
     *
     * @param queryEntity 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @param page        分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     * @throws IllegalArgumentException 如果 queryEntity 为 null 时抛出
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_ENTITY)
    PageResult<E> selectPage(@NonNull E queryEntity, @NonNull Page page);


    /**
     * 使用Map作为条件进行分页查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     *     <li>支持通过 {@link Page} 对象进行分页和排序</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 创建查询条件实体
     * Map<String, Object> conditionMap = new HashMap();
     * conditionMap.put("status", 1);
     * conditionMap.put("age", 18);
     *
     * // 创建分页对象
     * Page page = Page.of(1, 10).desc("create_time");
     *
     * // 执行分页查询
     * PageResult<User> result = mapper.selectPage(conditionMap, page);
     * }</pre>
     * </p>
     *
     * @param queryMap 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @param page        分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     * @throws IllegalArgumentException 如果 queryEntity 为 null 时抛出
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_MAP)
    PageResult<E> selectPage(@NonNull Map<String, Object> queryMap, @NonNull Page page);

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param queryEntity 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @param pageNum     查询的页数
     * @param pageSize    每页的条数
     * @return 对应页码对的数据
     */
    default List<E> simplePage(@NonNull E queryEntity, long pageNum, long pageSize) {
        return selectPage(queryEntity, Page.notCount(pageNum, pageSize)).getRecords();
    }

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param queryMap 查询条件Map，仅使用其中的非空属性作为查询条件
     * @param pageNum     查询的页数
     * @param pageSize    每页的条数
     * @return 对应页码对的数据
     */
    default List<E> simplePage(@NonNull Map<String, Object> queryMap, long pageNum, long pageSize) {
        return selectPage(queryMap, Page.notCount(pageNum, pageSize)).getRecords();
    }

    /**
     * 使用实体对象作为条件进行流式查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     *     <li>支持通过字段上的 {@code @Column} 和 {@code @Id} 注解自定义列名</li>
     *     <li>支持通过字段上的 {@code @Column(condition = XxxCondition.class)} 注解自定义条件类型</li>
     * </ul>
     * </p>
     * <p>
     * <b>注意：</b> 返回的 {@link Stream} 必须在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询年龄为 18 岁的用户（name 为 null 会被忽略）
     * User condition = new User();
     * condition.setAge(18);
     *
     * try (Stream<User> stream = mapper.stream(condition)) {
     *     stream.forEach(System.out::println);
     * }
     * }</pre>
     * </p>
     *
     * @param queryEntity 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     * @throws IllegalArgumentException 如果 queryEntity 为 null 时抛出
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_ENTITY)
    Stream<E> stream(@NonNull E queryEntity);

    /**
     * 使用Map作为条件进行流式查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     * </ul>
     * </p>
     * <p>
     * <b>注意：</b> 返回的 {@link Stream} 必须在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询年龄为 18 岁的用户（name 为 null 会被忽略）
     * Map<String, Object> conditionMap = new HashMaap();
     * conditionMap.put("age", 18);
     *
     * try (Stream<User> stream = mapper.stream(conditionMap)) {
     *     stream.forEach(System.out::println);
     * }
     * }</pre>
     * </p>
     *
     * @param queryMap 查询条件Map，仅使用其中的非空属性作为查询条件
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_MAP)
    Stream<E> stream(@NonNull Map<String, Object> queryMap);

    /**
     * 使用实体对象作为条件进行查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     *     <li>如果所有属性都为 {@code null}，则会查询全表（请谨慎使用）</li>
     *     <li>支持通过字段上的 {@code @Column} 和 {@code @Id} 注解自定义列名</li>
     *     <li>支持通过字段上的 {@code @Column(condition = XxxCondition.class)} 注解自定义条件类型</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询状态为1且年龄为18岁的用户列表
     * User condition = new User();
     * condition.setStatus(1);
     * condition.setAge(18);
     *
     * List<User> users = mapper.selectList(condition);
     * }</pre>
     * </p>
     *
     * @param queryEntity 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @return 查询结果列表，永远不为 {@code null}
     * @throws IllegalArgumentException 如果 queryEntity 为 null 时抛出
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_ENTITY)
    List<E> selectList(@NonNull E queryEntity);


    /**
     * 使用Map作为条件进行查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用Map中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     *     <li>如果所有属性都为 {@code null}，则会查询全表（请谨慎使用）</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询状态为1且年龄为18岁的用户列表
     * Map<String, Object> conditionMap = new HashMap<>();
     * conditionMap.put("status", 1);
     * conditionMap.put("age", 18);
     *
     * List<User> users = mapper.selectList(conditionMap);
     * }</pre>
     * </p>
     *
     * @param queryMap 查询条件Map，仅使用其中的非空属性作为查询条件
     * @return 查询结果列表，永远不为 {@code null}
     * @throws IllegalArgumentException 如果 queryEntity 为 null 时抛出
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_MAP)
    List<E> selectList(@NonNull Map<String, Object> queryMap);
}
