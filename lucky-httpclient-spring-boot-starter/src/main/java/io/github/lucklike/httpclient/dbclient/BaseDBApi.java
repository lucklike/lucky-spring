package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.function.SQLFunctions;
import io.github.lucklike.httpclient.dbclient.sql.lambda.Lambda;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientConditionBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientCountBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaClientUpdateBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaConditionBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaCountBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaUpdateBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.SFunction;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * 提供基于实体类的 CRUD 操作以及批量操作的核心数据库访问接口。
 * <p>
 * 该接口是数据库客户端的核心抽象，提供了丰富的数据访问方法，包括：
 * <ul>
 *     <li>Lambda 表达式构建动态查询（推荐使用 {@link Lambda} 入口类）</li>
 *     <li>实体对象作为查询条件（零 SQL 配置）</li>
 *     <li>分页查询与流式查询</li>
 *     <li>单条/批量插入、更新、删除</li>
 *     <li>根据 ID 的快捷操作</li>
 *     <li>保存或更新（saveOrUpdate）语义</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 1. 通过依赖注入获取实例
 * \@Autowired
 * private BaseDBApi<User> userDBApi;
 *
 * // 2. Lambda 条件查询（使用 Lambda 入口类）
 * List<User> users = userDBApi.selectList(
 *     Lambda.select(User.class)
 *         .where(User::getStatus).eq(1)
 *         .orderByDesc(User::getCreateTime)
 * );
 *
 * // 3. 实体对象条件查询
 * User condition = new User();
 * condition.setStatus(1);
 * condition.setAge(18);
 * List<User> users = userDBApi.selectList(condition);
 *
 * // 4. 分页查询
 * Page page = Page.of(1, 10).desc("create_time");
 * PageResult<User> result = userDBApi.selectPage(condition, page);
 *
 * // 5. 插入/更新
 * User user = new User();
 * user.setName("张三");
 * user.setAge(18);
 * userDBApi.insert(user);
 * userDBApi.updateById(user);
 *
 * // 6. 批量操作
 * List<User> userList = getUsers();
 * userDBApi.batchInsert(userList);
 * }
 * </pre>
 * </p>
 *
 * @param <E> 实体类型泛型
 * @author fukang
 * @version 1.0.0
 */
public interface BaseDBApi<E> {

    /**
     * {@link SQLFunctions#lambdaSql(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于执行 Lambda 构建器生成的动态 SQL，支持：
     * <ul>
     *     <li>SELECT 查询</li>
     *     <li>COUNT 统计</li>
     *     <li>UPDATE 更新</li>
     *     <li>DELETE 删除</li>
     * </ul>
     * </p>
     */
    String SQL_LAMBDA = "#{lambdaSql($mc$)}";

    /**
     * {@link SQLFunctions#selectById(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于根据 ID 查询单条记录。
     * </p>
     */
    String SQL_SELECT_BY_ID = "#{selectById($mc$)}";

    /**
     * {@link SQLFunctions#selectByEntity(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于根据实体对象的非空属性作为等值条件进行查询。
     * </p>
     */
    String SQL_SELECT_BY_ENTITY = "#{selectByEntity($mc$)}";

    /**
     * {@link SQLFunctions#deleteById(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于根据 ID 删除记录。
     * </p>
     */
    String SQL_DELETE_BY_ID = "#{deleteById($mc$)}";

    /**
     * {@link SQLFunctions#updateById(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于根据 ID 更新记录，仅更新实体中非空的字段。
     * </p>
     */
    String SQL_UPDATE_BY_ID = "#{updateById($mc$)}";

    /**
     * {@link SQLFunctions#insertSql(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于插入单条记录。
     * </p>
     */
    String SQL_INSERT_SQL = "#{insertSql($mc$)}";

    /**
     * {@link SQLFunctions#batchInsertSql(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于批量插入记录。
     * </p>
     */
    String SQL_BATCH_INSERT_SQL = "#{batchInsertSql($mc$)}";

    /**
     * {@link SQLFunctions#batchUpdateById(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于批量根据 ID 更新记录。
     * </p>
     */
    String SQL_BATCH_UPDATE_BY_ID = "#{batchUpdateById($mc$)}";

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
     * 执行 UPDATE 类型的 SQL 并返回影响行数。
     * <p>
     * 使用 Lambda 表达式构建更新条件和更新字段，支持动态条件拼接。
     * </p>
     * <p>
     * <b>警告：</b> 如果没有设置任何条件，可能会更新全表数据，请谨慎使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 将年龄大于18岁的用户状态更新为1
     * int rows = mapper.update(Lambda.update(User.class)
     *     .set(User::getStatus, 1)
     *     .where(User::getAge).gt(18));
     * }</pre>
     * </p>
     *
     * @param updateBuilder UPDATE 查询条件构建器
     * @return 影响的行数
     */
    @SQL(executor = SQL_LAMBDA)
    int update(LambdaUpdateBuilder<E> updateBuilder);

    /**
     * 执行 UPDATE 类型的 SQL 并返回影响行数（使用条件构建器）
     * <p>
     * 便捷方法，将条件构建器转换为更新构建器后执行。
     * </p>
     *
     * @param conditionBuilder 条件构建器
     * @return 影响的行数
     */
    default int update(LambdaConditionBuilder<E> conditionBuilder) {
        return update(conditionBuilder.toUpdate());
    }

    /**
     * 执行 DELETE 类型的 SQL 并返回影响行数。
     * <p>
     * 使用 Lambda 表达式构建删除条件，支持动态条件拼接。
     * </p>
     * <p>
     * <b>警告：</b> 如果条件为空，可能会删除全表数据，请谨慎使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 删除状态为0的用户
     * int rows = mapper.delete(Lambda.delete(User.class)
     *     .where(User::getStatus).eq(0));
     * }</pre>
     * </p>
     *
     * @param deleteBuilder DELETE 查询条件构建器
     * @return 影响的行数
     */
    @SQL(executor = SQL_LAMBDA)
    int delete(LambdaDeleteBuilder<E> deleteBuilder);

    /**
     * 执行 DELETE 类型的 SQL 并返回影响行数（使用条件构建器）
     *
     * @param conditionBuilder 条件构建器
     * @return 影响的行数
     */
    default int delete(LambdaConditionBuilder<E> conditionBuilder) {
        return delete(conditionBuilder.toDelete());
    }

    /**
     * 根据 ID 查询实体。
     * <p>
     * 使用实体类中标记的 {@code @TableId} 注解识别 ID 字段。
     * 如果查询结果为空，则返回 {@code null}。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询ID为1的用户
     * User user = mapper.selectById(1L);
     * }</pre>
     * </p>
     *
     * @param id ID 字段值
     * @return 查询结果，可能为 {@code null}
     * @throws IllegalArgumentException 如果 id 为 null 时抛出
     */
    @Nullable
    @SQL(executor = SQL_SELECT_BY_ID)
    E selectById(@NonNull Object id);

    /**
     * 根据 ID 删除实体。
     * <p>
     * 使用实体类中标记的 {@code @TableId} 注解识别 ID 字段。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 删除ID为1的用户
     * int rows = mapper.deleteById(1L);
     * }</pre>
     * </p>
     *
     * @param id ID 字段值
     * @return 影响的行数
     * @throws IllegalArgumentException 如果 id 为 null 时抛出
     */
    @SQL(executor = SQL_DELETE_BY_ID)
    int deleteById(@NonNull Object id);

    /**
     * 根据 ID 更新实体。
     * <p>
     * 使用实体中标记的 {@code @TableId} 注解识别 ID 字段，
     * 使用实体中其他非空属性作为更新字段（如果字段值为 null，则不会更新该字段）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 更新用户名称
     * User user = new User();
     * user.setId(1L);
     * user.setName("新名称");
     * int rows = mapper.updateById(user);
     * }</pre>
     * </p>
     *
     * @param entity 实体对象，必须包含 ID 字段值
     * @return 影响的行数
     * @throws IllegalArgumentException 如果 entity 为 null 时抛出
     */
    @SQL(executor = SQL_UPDATE_BY_ID)
    int updateById(@NonNull E entity);

    /**
     * 插入单条数据。
     * <p>
     * 使用实体中所有非空属性作为插入字段，
     * 如果字段值为 null，则使用数据库默认值或不插入该字段（取决于配置）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 插入新用户
     * User user = new User();
     * user.setName("张三");
     * user.setAge(18);
     * int rows = mapper.insert(user);
     * }</pre>
     * </p>
     *
     * @param entity 要插入的实体对象
     * @return 影响的行数
     * @throws IllegalArgumentException 如果 entity 为 null 时抛出
     */
    @SQL(executor = SQL_INSERT_SQL)
    int insert(@NonNull E entity);

    /**
     * 批量插入数据（内部批量操作方法）。
     * <p>
     * 使用批量插入优化性能，根据实体中所有非空属性生成插入语句。
     * </p>
     * <p>
     * <b>注意：</b> 此方法为内部批量操作方法，建议使用 {@link #batchInsert(Collection)} 等封装方法。
     * </p>
     *
     * @param entities 要插入的实体集合
     * @return 每条记录影响的行数数组
     */
    @SQL(executor = SQL_BATCH_INSERT_SQL)
    int[] _batchInsert_(@NonNull Collection<E> entities);

    /**
     * 批量更新数据（内部批量操作方法）。
     * <p>
     * 根据实体中的 ID 批量更新，每个实体单独执行 UPDATE 语句。
     * </p>
     * <p>
     * <b>注意：</b> 此方法为内部批量操作方法，建议使用 {@link #batchUpdateById(Collection)} 等封装方法。
     * </p>
     *
     * @param entities 要更新的实体集合
     * @return 每条记录影响的行数数组
     */
    @SQL(executor = SQL_BATCH_UPDATE_BY_ID)
    int[] _batchUpdateById_(@NonNull Collection<E> entities);

    /**
     * 数据存在则更新，不存在则插入。
     * <p>
     * 根据 ID 查询实体是否存在：
     * <ul>
     *     <li>如果存在，则执行更新操作（根据 ID 更新非空字段）</li>
     *     <li>如果不存在，则执行插入操作</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 保存或更新用户
     * User user = new User();
     * user.setId(1L);
     * user.setName("张三");
     * user.setAge(18);
     * int rows = mapper.saveOrUpdate(user);
     * }</pre>
     * </p>
     *
     * @param entity 实体对象
     * @return 影响的行数
     * @throws IllegalArgumentException 如果 entity 为 null 时抛出
     */
    default int saveOrUpdate(@NonNull E entity) {
        E e = selectById(entity);
        return e == null
                ? insert(entity)
                : updateById(entity);
    }

    /**
     * 批量插入数据，每次批量操作 1000 条（数组版本）。
     * <p>
     * 将数组分割为每 1000 条一批进行批量插入，提高大数据量插入性能。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组
     * int[] results = mapper.batchInsert(users);
     * }</pre>
     * </p>
     *
     * @param entities 要插入的实体数组
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchInsert(@NonNull E[] entities) {
        return batchInsert(Arrays.asList(entities));
    }

    /**
     * 批量插入数据，指定每次批量操作的条数（数组版本）。
     * <p>
     * 将数组分割为指定大小进行批量插入，适用于需要自定义批处理大小的场景。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组
     * // 每500条执行一次批量插入
     * int[] results = mapper.batchInsert(users, 500);
     * }</pre>
     * </p>
     *
     * @param entities  要插入的实体数组
     * @param batchSize 每次批量操作的条数
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchInsert(@NonNull E[] entities, int batchSize) {
        return batchInsert(Arrays.asList(entities), batchSize);
    }

    /**
     * 批量插入数据，每次批量操作 1000 条（集合版本）。
     * <p>
     * 将集合分割为每 1000 条一批进行批量插入，提高大数据量插入性能。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据
     * int[] results = mapper.batchInsert(userList);
     * }</pre>
     * </p>
     *
     * @param entities 要插入的实体集合
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchInsert(@NonNull Collection<E> entities) {
        return batchInsert(entities, 1000);
    }

    /**
     * 批量插入数据，指定每次批量操作的条数（集合版本）。
     * <p>
     * 将集合分割为指定大小进行批量插入，适用于需要自定义批处理大小的场景。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据
     * // 每500条执行一次批量插入
     * int[] results = mapper.batchInsert(userList, 500);
     * }</pre>
     * </p>
     *
     * @param entities  要插入的实体集合
     * @param batchSize 每次批量操作的条数
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchInsert(@NonNull Collection<E> entities, int batchSize) {
        if (ContainerUtils.isEmptyCollection(entities)) {
            return new int[0];
        }

        List<Integer> result = new ArrayList<>();
        for (List<E> partitionList : ContainerUtils.partition(entities, batchSize)) {
            int[] ints = _batchInsert_(partitionList);
            for (int i : ints) {
                result.add(i);
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 批量更新数据，每次批量操作 1000 条（数组版本）。
     * <p>
     * 将数组分割为每 1000 条一批进行批量更新，提高大数据量更新性能。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组（需设置ID）
     * int[] results = mapper.batchUpdateById(users);
     * }</pre>
     * </p>
     *
     * @param entities 要更新的实体数组
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchUpdateById(@NonNull E[] entities) {
        return batchUpdateById(Arrays.asList(entities));
    }

    /**
     * 批量更新数据，指定每次批量操作的条数（数组版本）。
     * <p>
     * 将数组分割为指定大小进行批量更新，适用于需要自定义批处理大小的场景。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组（需设置ID）
     * // 每500条执行一次批量更新
     * int[] results = mapper.batchUpdateById(users, 500);
     * }</pre>
     * </p>
     *
     * @param entities  要更新的实体数组
     * @param batchSize 每次批量操作的条数
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchUpdateById(@NonNull E[] entities, int batchSize) {
        return batchUpdateById(Arrays.asList(entities), batchSize);
    }

    /**
     * 批量更新数据，每次批量操作 1000 条（集合版本）。
     * <p>
     * 将集合分割为每 1000 条一批进行批量更新，提高大数据量更新性能。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据（需设置ID）
     * int[] results = mapper.batchUpdateById(userList);
     * }</pre>
     * </p>
     *
     * @param entities 要更新的实体集合
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchUpdateById(@NonNull Collection<E> entities) {
        return batchUpdateById(entities, 1000);
    }

    /**
     * 批量更新数据，指定每次批量操作的条数（集合版本）。
     * <p>
     * 将集合分割为指定大小进行批量更新，适用于需要自定义批处理大小的场景。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据（需设置ID）
     * // 每500条执行一次批量更新
     * int[] results = mapper.batchUpdateById(userList, 500);
     * }</pre>
     * </p>
     *
     * @param entities  要更新的实体集合
     * @param batchSize 每次批量操作的条数
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchUpdateById(@NonNull Collection<E> entities, int batchSize) {
        if (ContainerUtils.isEmptyCollection(entities)) {
            return new int[0];
        }

        List<Integer> result = new ArrayList<>();
        for (List<E> partitionList : ContainerUtils.partition(entities, batchSize)) {
            int[] ints = _batchUpdateById_(partitionList);
            for (int i : ints) {
                result.add(i);
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
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
        return new LambdaClientQueryBuilder<>(this, entityClass());
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
        return new LambdaClientQueryBuilder<>(this,entity);
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
        return new LambdaClientUpdateBuilder<>(this, entityClass());
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
        return new LambdaClientDeleteBuilder<>(this, entityClass());
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
        return new LambdaClientCountBuilder<>(this, entityClass());
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
        return new LambdaClientCountBuilder<>(this, entityClass(), countColumn);
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
        return new LambdaClientConditionBuilder<>(this, entityClass());
    }

    /**
     * 获取实体类类型。
     * <p>
     * 通过泛型参数自动解析实体类的 Class 对象。
     * </p>
     *
     * @return 实体类 Class 对象
     */
    @SuppressWarnings("unchecked")
    default Class<E> entityClass() {
        return (Class<E>) ResolvableType.forClass(BaseDBApi.class, getClass()).getGeneric(0).toClass();
    }
}