package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.function.SQLFunctions;
import io.github.lucklike.httpclient.dbclient.sql.LambdaCountBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaUpdateBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * 提供基于实体类的CRUD操作以及批量操作
 *
 * @param <E> 实体类型泛型
 */
public interface BaseDBApi<E> {

    /**
     * {@link SQLFunctions#lambdaSql(MethodContext)}
     */
    String SQL_LAMBDA = "#{lambdaSql($mc$)}";

    /**
     * {@link SQLFunctions#selectById(MethodContext)}
     */
    String SQL_SELECT_BY_ID = "#{selectById($mc$)}";

    /**
     * {@link SQLFunctions#selectByEntity(MethodContext)}
     */
    String SQL_SELECT_BY_ENTITY = "#{selectByEntity($mc$)}";

    /**
     * {@link SQLFunctions#deleteById(MethodContext)}
     */
    String SQL_DELETE_BY_ID = "#{deleteById($mc$)}";

    /**
     * {@link SQLFunctions#updateById(MethodContext)}
     */
    String SQL_UPDATE_BY_ID = "#{updateById($mc$)}";

    /**
     * {@link SQLFunctions#insertSql(MethodContext)}
     */
    String SQL_INSERT_SQL = "#{insertSql($mc$)}";

    /**
     * {@link SQLFunctions#batchInsertSql(MethodContext)}
     */
    String SQL_BATCH_INSERT_SQL = "#{batchInsertSql($mc$)}";

    /**
     * {@link SQLFunctions#batchUpdateById(MethodContext)}
     */
    String SQL_BATCH_UPDATE_BY_ID = "#{batchUpdateById($mc$)}";

    /**
     * 执行 COUNT 类型的 SQL 并返回统计结果。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 统计年龄大于18岁的用户数量
     * long count = mapper.count(Lambda.count(User.class).gt(User::getAge, 18));
     * }</pre>
     *
     * @param countBuilder COUNT 查询条件构建器
     * @return 统计结果
     */
    @SQL(executor = SQL_LAMBDA)
    long count(LambdaCountBuilder<E> countBuilder);

    /**
     * 执行 SELECT 类型的 SQL 并返回单个结果。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接和排序。
     * 如果查询结果为空，则返回 {@code null}。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询ID为1的用户
     * User user = mapper.selectOne(Lambda.select(User.class).eq(User::getId, 1L));
     * }</pre>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @return 查询结果，可能为 {@code null}
     */
    @Nullable
    @SQL(executor = SQL_LAMBDA)
    E selectOne(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 执行 SELECT 类型的 SQL 并返回结果列表。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接、排序和分页。
     * 如果查询结果为空，则返回空列表（非 {@code null}）。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询年龄大于18岁的用户列表，按年龄降序排列
     * List<User> users = mapper.selectList(Lambda.select(User.class).eq(User::getId, 1L));
     * }</pre>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @return 查询结果列表，永远不为 {@code null}
     */
    @NonNull
    @SQL(executor = SQL_LAMBDA)
    List<E> selectList(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 执行 SELECT 类型的 SQL 并返回分页结果。
     * <p>
     * 使用 Lambda 表达式构建查询条件，支持动态条件拼接和排序，自动完成总记录数查询和分页数据查询。
     * 分页参数通过 {@link Page} 对象传递，包含当前页码、每页大小、排序字段等信息。
     * <p>
     * <b>注意：</b> 如果 {@link Page#isCountTotal()} 为 {@code true}，则会自动执行 COUNT 查询；
     * 否则只查询分页数据，总记录数为 -1。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 创建分页对象
     * Page page = Page.of(1, 10).desc("create_time");
     *
     * // 执行分页查询
     * PageResult<User> result = mapper.selectPage(Lambda.select(User.class).eq(User::getStatus, 1), page);
     *
     * // 获取分页结果
     * List<User> records = result.getRecords();
     * long total = result.getTotalCount();
     * }</pre>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @param page         分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     */
    @SQL(executor = SQL_LAMBDA)
    PageResult<E> selectPage(LambdaQueryBuilder<E> queryBuilder, @NonNull Page page);

    /**
     * 执行 SELECT 类型 SQL 并以流式方式返回结果。
     * <p>
     * 返回的 {@link Stream} 需要在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
     * <p>
     * 使用示例：
     * <pre>{@code
     * try (Stream<User> stream = mapper.stream(Lambda.select(User.class).eq(User::getId, 1L))) {
     *     stream.filter(user -> user.getAge() > 18)
     *           .forEach(System.out::println);
     * }
     * }</pre>
     *
     * @param queryBuilder SELECT 查询条件构建器
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     */
    @NonNull
    @SQL(executor = SQL_LAMBDA)
    Stream<E> stream(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 使用实体对象作为条件进行分页查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     *     <li>支持通过 {@link Page} 对象进行分页和排序</li>
     * </ul>
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
     * 使用实体对象作为条件进行查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     *     <li>如果所有属性都为 {@code null}，则会查询全表（请谨慎使用）</li>
     * </ul>
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
     *
     * @param queryEntity 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @return 查询结果列表，永远不为 {@code null}
     * @throws IllegalArgumentException 如果 queryEntity 为 null 时抛出
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_ENTITY)
    List<E> selectList(@NonNull E queryEntity);

    /**
     * 使用实体对象作为条件进行流式查询。
     * <p>
     * 查询条件规则：
     * <ul>
     *     <li>仅使用实体中 {@code 非 null} 的属性作为等值条件</li>
     *     <li>多个条件之间使用 {@code AND} 连接</li>
     *     <li>{@code null} 值属性会被自动忽略</li>
     * </ul>
     * <p>
     * <b>注意：</b> 返回的 {@link Stream} 必须在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
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
     *
     * @param queryEntity 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     * @throws IllegalArgumentException 如果 queryEntity 为 null 时抛出
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_ENTITY)
    Stream<E> stream(@NonNull E queryEntity);

    /**
     * 执行 UPDATE 类型的 SQL 并返回影响行数。
     * <p>
     * 使用 Lambda 表达式构建更新条件和更新字段，支持动态条件拼接。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 将年龄大于18岁的用户状态更新为1
     * int rows = mapper.update(
     *  Lambda.update(User.class)
     *        .set(User::getStatus, 1)
     *        .gt(User::getAge, 18)
     * );
     * }</pre>
     *
     * @param updateBuilder UPDATE 查询条件构建器
     * @return 影响的行数
     */
    @SQL(executor = SQL_LAMBDA)
    int update(LambdaUpdateBuilder<E> updateBuilder);

    /**
     * 执行 DELETE 类型的 SQL 并返回影响行数。
     * <p>
     * 使用 Lambda 表达式构建删除条件，支持动态条件拼接。
     * <p>
     * <b>注意：</b> 如果条件为空，可能会删除全表数据，请谨慎使用。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 删除状态为0的用户
     * int rows = mapper.delete(Lambda.delete(User.class).eq(User::getStatus, 0));
     * }</pre>
     *
     * @param deleteBuilder DELETE 查询条件构建器
     * @return 影响的行数
     */
    @SQL(executor = SQL_LAMBDA)
    int delete(LambdaDeleteBuilder<E> deleteBuilder);

    /**
     * 根据 ID 查询实体。
     * <p>
     * 使用实体类中标记的 {@code @TableId} 注解识别 ID 字段。
     * 如果查询结果为空，则返回 {@code null}。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询ID为1的用户
     * User user = mapper.selectById(1L);
     * }</pre>
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
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 删除ID为1的用户
     * int rows = mapper.deleteById(1L);
     * }</pre>
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
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 更新用户名称
     * User user = new User();
     * user.setId(1L);
     * user.setName("新名称");
     * int rows = mapper.updateById(user);
     * }</pre>
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
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 插入新用户
     * User user = new User();
     * user.setName("张三");
     * user.setAge(18);
     * int rows = mapper.insert(user);
     * }</pre>
     *
     * @param entity 要插入的实体对象
     * @return 影响的行数
     * @throws IllegalArgumentException 如果 entity 为 null 时抛出
     */
    @SQL(executor = SQL_INSERT_SQL)
    int insert(@NonNull E entity);

    /**
     * 批量插入数据。
     * <p>
     * 使用批量插入优化性能，根据实体中所有非空属性生成插入语句。
     * <p>
     * <b>注意：</b> 此方法为内部批量操作方法，建议使用 {@link #batchInsert(Collection)} 等封装方法。
     *
     * @param entities 要插入的实体集合
     * @return 每条记录影响的行数数组
     */
    @SQL(executor = SQL_BATCH_INSERT_SQL)
    int[] _batchInsert_(@NonNull Collection<E> entities);

    /**
     * 批量更新数据。
     * <p>
     * 根据实体中的 ID 批量更新，每个实体单独执行 UPDATE 语句。
     * <p>
     * <b>注意：</b> 此方法为内部批量操作方法，建议使用 {@link #batchUpdateById(Collection)} 等封装方法。
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
     * 批量插入数据，每次批量操作 1000 条。
     * <p>
     * 将数组分割为每 1000 条一批进行批量插入，提高大数据量插入性能。
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组
     * int[] results = mapper.batchInsert(users);
     * }</pre>
     *
     * @param entities 要插入的实体数组
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchInsert(@NonNull E[] entities) {
        return batchInsert(Arrays.asList(entities));
    }

    /**
     * 批量插入数据，指定每次批量操作的条数。
     * <p>
     * 将数组分割为指定大小进行批量插入，适用于需要自定义批处理大小的场景。
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组
     * // 每500条执行一次批量插入
     * int[] results = mapper.batchInsert(users, 500);
     * }</pre>
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
     * 批量插入数据，每次批量操作 1000 条。
     * <p>
     * 将集合分割为每 1000 条一批进行批量插入，提高大数据量插入性能。
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据
     * int[] results = mapper.batchInsert(userList);
     * }</pre>
     *
     * @param entities 要插入的实体集合
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchInsert(@NonNull Collection<E> entities) {
        return batchInsert(entities, 1000);
    }

    /**
     * 批量插入数据，指定每次批量操作的条数。
     * <p>
     * 将集合分割为指定大小进行批量插入，适用于需要自定义批处理大小的场景。
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据
     * // 每500条执行一次批量插入
     * int[] results = mapper.batchInsert(userList, 500);
     * }</pre>
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
     * 批量更新数据，每次批量操作 1000 条。
     * <p>
     * 将数组分割为每 1000 条一批进行批量更新，提高大数据量更新性能。
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组（需设置ID）
     * int[] results = mapper.batchUpdateById(users);
     * }</pre>
     *
     * @param entities 要更新的实体数组
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchUpdateById(@NonNull E[] entities) {
        return batchUpdateById(Arrays.asList(entities));
    }

    /**
     * 批量更新数据，指定每次批量操作的条数。
     * <p>
     * 将数组分割为指定大小进行批量更新，适用于需要自定义批处理大小的场景。
     * <p>
     * 使用示例：
     * <pre>{@code
     * User[] users = new User[2500];
     * // ... 初始化用户数组（需设置ID）
     * // 每500条执行一次批量更新
     * int[] results = mapper.batchUpdateById(users, 500);
     * }</pre>
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
     * 批量更新数据，每次批量操作 1000 条。
     * <p>
     * 将集合分割为每 1000 条一批进行批量更新，提高大数据量更新性能。
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据（需设置ID）
     * int[] results = mapper.batchUpdateById(userList);
     * }</pre>
     *
     * @param entities 要更新的实体集合
     * @return 每条记录影响的行数数组
     * @throws IllegalArgumentException 如果 entities 为 null 时抛出
     */
    default int[] batchUpdateById(@NonNull Collection<E> entities) {
        return batchUpdateById(entities, 1000);
    }

    /**
     * 批量更新数据，指定每次批量操作的条数。
     * <p>
     * 将集合分割为指定大小进行批量更新，适用于需要自定义批处理大小的场景。
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<User> userList = new ArrayList<>();
     * // ... 添加用户数据（需设置ID）
     * // 每500条执行一次批量更新
     * int[] results = mapper.batchUpdateById(userList, 500);
     * }</pre>
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
}