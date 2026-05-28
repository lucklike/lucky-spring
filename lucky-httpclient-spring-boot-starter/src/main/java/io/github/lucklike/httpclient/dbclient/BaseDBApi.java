package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.function.SQLFunctions;
import io.github.lucklike.httpclient.dbclient.sql.LambdaCountBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaUpdateBuilder;
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
     * 执行COUNT类型的SQL返回结果
     *
     * @param countBuilder COUNT查询条件
     * @return 执行结果
     */
    @SQL(executor = SQL_LAMBDA)
    long count(LambdaCountBuilder<E> countBuilder);

    /**
     * 执行SELECT类型的SQL返回结果，返回单个结果
     *
     * @param queryBuilder SELECT查询条件
     * @return 执行结果
     */
    @Nullable
    @SQL(executor = SQL_LAMBDA)
    E selectOne(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 执行SELECT类型的SQL返回结果
     *
     * @param queryBuilder SELECT查询条件
     * @return 执行结果
     */
    @NonNull
    @SQL(executor = SQL_LAMBDA)
    List<E> selectList(LambdaQueryBuilder<E> queryBuilder);

    /**
     * 执行 SELECT 类型 SQL 并以流式方式返回结果。
     * <p>
     * 返回的 {@link Stream} 需要在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
     * <p>
     * 使用示例：
     * <pre>{@code
     * try (Stream<User> stream = mapper.stream(queryBuilder)) {
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
     * 使用实体类对象作为条件进行查询（将非空属性值作为条件使用 AND 进行拼接）
     *
     * @param queryEntity 查询实体类对象
     * @return 查询结果
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_ENTITY)
    List<E> selectList(@NonNull E queryEntity);

    /**
     * 根据实体对象中的非空属性作为查询条件进行查询，并以流式方式返回结果。
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
     * 执行UPDATE类型的SQL返回结果
     *
     * @param updateBuilder UPDATE查询条件
     * @return 影响行数
     */
    @SQL(executor = SQL_LAMBDA)
    int update(LambdaUpdateBuilder<E> updateBuilder);

    /**
     * 执行DELETE类型的SQL返回结果
     *
     * @param deleteBuilder DELETE查询条件
     * @return 影响行数
     */
    @SQL(executor = SQL_LAMBDA)
    int delete(LambdaDeleteBuilder<E> deleteBuilder);

    /**
     * 使用ID字段进行查询
     *
     * @param id ID字段值
     * @return 执行结果
     */
    @Nullable
    @SQL(executor = SQL_SELECT_BY_ID)
    E selectById(@NonNull Object id);

    /**
     * 使用ID字段进行删除
     *
     * @param id ID字段值
     * @return 影响行数
     */
    @SQL(executor = SQL_DELETE_BY_ID)
    int deleteById(@NonNull Object id);

    /**
     * 使用ID字段进行更新
     *
     * @param entity ID字段值
     * @return 影响行数
     */
    @SQL(executor = SQL_UPDATE_BY_ID)
    int updateById(@NonNull E entity);

    /**
     * 插入一条数据
     *
     * @param entity 数据实体
     * @return 影响行数
     */
    @SQL(executor = SQL_INSERT_SQL)
    int insert(@NonNull E entity);

    /**
     * 批量INSERT
     *
     * @param entities 要插入的实体集合
     * @return 影响行数
     */
    @SQL(executor = SQL_BATCH_INSERT_SQL)
    int[] _batchInsert_(@NonNull Collection<E> entities);

    /**
     * 批量UPDATE
     *
     * @param entities 要更新的实体集合
     * @return 影响行数
     */
    @SQL(executor = SQL_BATCH_UPDATE_BY_ID)
    int[] _batchUpdateById_(@NonNull Collection<E> entities);

    /**
     * 数据存在就更新，不存在就插入
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    default int saveOrUpdate(@NonNull E entity) {
        E e = selectById(entity);
        return e == null
                ? insert(entity)
                : updateById(entity);
    }

    /**
     * 批量INSERT，每次批量操作1000条
     *
     * @param entities 要插入的实体数组
     * @return 影响行数
     */
    default int[] batchInsert(@NonNull E[] entities) {
        return batchInsert(Arrays.asList(entities));
    }

    /**
     * 批量INSERT
     *
     * @param entities  要插入的实体数组
     * @param batchSize 每次批量操作的条数
     * @return 影响行数
     */
    default int[] batchInsert(@NonNull E[] entities, int batchSize) {
        return batchInsert(Arrays.asList(entities), batchSize);
    }

    /**
     * 批量INSERT，每次批量操作1000条
     *
     * @param entities 要插入的实体集合
     * @return 影响行数
     */
    default int[] batchInsert(@NonNull Collection<E> entities) {
        return batchInsert(entities, 1000);
    }

    /**
     * 批量INSERT，指定每次批量操作的条数
     *
     * @param entities  要插入的实体集合
     * @param batchSize 每次批量操作的条数
     * @return 影响行数
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
     * 批量INSERT，每次批量操作1000条
     *
     * @param entities 要插入的实体数组
     * @return 影响行数
     */
    default int[] batchUpdateById(@NonNull E[] entities) {
        return batchUpdateById(Arrays.asList(entities));
    }

    /**
     * 批量INSERT
     *
     * @param entities  要插入的实体数组
     * @param batchSize 每次批量操作的条数
     * @return 影响行数
     */
    default int[] batchUpdateById(@NonNull E[] entities, int batchSize) {
        return batchUpdateById(Arrays.asList(entities), batchSize);
    }

    /**
     * 批量INSERT，每次批量操作1000条
     *
     * @param entities 要插入的实体集合
     * @return 影响行数
     */
    default int[] batchUpdateById(@NonNull Collection<E> entities) {
        return batchUpdateById(entities, 1000);
    }

    /**
     * 批量UPDATE，指定每次批量操作的条数
     *
     * @param entities  要插入的实体集合
     * @param batchSize 每次批量操作的条数
     * @return 影响行数
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
