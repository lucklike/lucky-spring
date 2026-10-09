package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.function.EntityUtils;
import io.github.lucklike.httpclient.dbclient.function.IdField;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaConditionBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaUpdateBuilder;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 写操作能力接口。
 * <p>
 * 在 {@link QueryApi} 查询能力的基础上，提供数据的写入能力，包括：
 * <ul>
 *     <li>根据 ID 查询、更新、删除</li>
 *     <li>Lambda 更新与删除</li>
 *     <li>单条/批量插入</li>
 *     <li>批量更新</li>
 *     <li>保存或更新（saveOrUpdate）语义</li>
 * </ul>
 * </p>
 *
 * @param <E> 实体类型泛型
 * @author fukang
 * @version 1.0.0
 */
public interface WriteApi<E> extends QueryApi<E> {

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
     * @param keyHolder 自增 ID 持有者
     * @return 影响的行数
     * @throws IllegalArgumentException 如果 entity 为 null 时抛出
     */
    @SQL(executor = SQL_INSERT_SQL)
    int _insert_(@NonNull E entity, KeyHolder keyHolder);


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
    default int insert(@NonNull E entity) {
        List<IdField> idFields = EntityUtils.getIdFields(entityClass());
        IdField autoIncrementField = null;
        for (IdField idField : idFields) {
            if (idField.isAutoIncrement()) {
                // 自增ID由数据库生成，插入后通过KeyHolder回填（复合主键场景下回填第一个自增字段）
                if (autoIncrementField == null) {
                    autoIncrementField = idField;
                }
            } else if (!idField.isManualSettings()) {
                // UUID/NANOID/雪花ID在插入前自动生成
                idField.setId(entity);
            }
        }
        if (autoIncrementField != null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int row = _insert_(entity, keyHolder);
            autoIncrementField.setId(entity, keyHolder);
            return row;
        }
        return _insert_(entity, null);
    }

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
        // 取实体的第一个非空ID值进行存在性判断，没有ID值时直接执行插入
        Object id = EntityUtils.getIdValue(entity);
        E e = id == null ? null : selectById(id);
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
}
