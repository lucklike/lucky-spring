package io.github.lucklike.httpclient.dbclient.sql.lambda;

import com.luckyframework.common.ContainerUtils;
import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 自带数据库客户端的条件构建器
 * <p>
 * 该类封装了 {@link LambdaConditionBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建查询条件，并可通过 {@link #toSelect}, {@link #toCount},
 * {@link #toUpdate}, {@link #toDelete} 方法转换为对应的操作构建器。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取条件构建器
 * LambdaClientConditionBuilder<Use?> condition = baseDBApi.lambdaCondition();
 *
 * // 构建条件并执行查询
 * List<Use?> users = condition.eq(User::getStatus, 1)
 *     .orderByDesc(User::getCreateTime)
 *     .toSelect()
 *     .list();
 *
 * // 构建条件并执行更新
 * int rows = condition.eq(User::getStatus, 0)
 *     .toUpdate()
 *     .set(User::getStatus, 1)
 *     .update();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 02:11
 */
public class LambdaClientConditionBuilder<T> extends AbstractLambdaClientBuilder<T, LambdaClientConditionBuilder<T>, LambdaConditionBuilder<T>> {

    /**
     * 构造条件构建器
     *
     * @param baseDBApi   数据库客户端API
     * @param entityClass 实体类类型
     */
    public LambdaClientConditionBuilder(BaseDBApi<T> baseDBApi, Class<T> entityClass) {
        super(baseDBApi, new LambdaConditionBuilder<>(entityClass));
    }

    // ==================== 条件方法 ====================

    /**
     * IN 条件（可变参数）
     * <p>
     * 添加 IN 条件：column IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值列表
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final LambdaClientConditionBuilder<T> in(SFunction<T, ?> column, Object... values) {
        sqlBuilder.in(column, values);
        return this;
    }

    /**
     * NOT IN 条件（集合参数）
     * <p>
     * 添加 NOT IN 条件：column NOT IN (value1, value2, ...)
     * </p>
     *
     * @param column 表字段的 Lambda 表达式
     * @param values 值集合
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientConditionBuilder<T> notIn(SFunction<T, ?> column, Collection<?> values) {
        sqlBuilder.notIn(column, values);
        return this;
    }

    // ==================== 类型转换方法 ====================

    /**
     * 将当前的条件构建器转换为查询构建器
     * <p>
     * 转换后可用于执行 SELECT 查询操作。如果不指定查询列，则默认查询所有列。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询所有列
     * List<Use?> users = condition.toSelect().list();
     * }</pre>
     * </p>
     *
     * @return 查询构建器
     */
    public final LambdaClientQueryBuilder<T> toSelect() {
        return new LambdaClientQueryBuilder<>(this.baseDBApi, this.sqlBuilder);
    }

    /**
     * 将当前的条件构建器转换为统计构建器
     * <p>
     * 转换后可用于执行 COUNT 统计查询。如果不指定统计列，则执行 COUNT(*) 统计总记录数；
     * 如果指定了统计列，则统计该列的非空值数量。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 统计总记录数
     * long total = condition.toCount().count();
     *
     * // 统计指定列的非空值数量
     * long count = condition.toCount(User::getEmail).count();
     * }</pre>
     * </p>
     *
     * @param column 要统计的列（可选），使用 Lambda 表达式指定
     * @return 统计构建器
     */
    @SafeVarargs
    public final LambdaClientCountBuilder<T> toCount(SFunction<T, ?>... column) {
        if (ContainerUtils.isEmptyArray(column)) {
            return new LambdaClientCountBuilder<>(this.baseDBApi, this.sqlBuilder);
        }
        return new LambdaClientCountBuilder<>(this.baseDBApi, this.sqlBuilder, column[0]);
    }

    /**
     * 将当前的条件构建器转换为删除构建器
     * <p>
     * 转换后可用于执行 DELETE 删除操作。
     * </p>
     * <p>
     * <b>注意：</b> 如果条件为空，可能会删除全表数据，请谨慎使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = condition.toDelete().delete();
     * }</pre>
     * </p>
     *
     * @return 删除构建器
     */
    public final LambdaClientDeleteBuilder<T> toDelete() {
        return new LambdaClientDeleteBuilder<>(this.baseDBApi, this.sqlBuilder);
    }

    /**
     * 将当前的条件构建器转换为更新构建器
     * <p>
     * 转换后可用于执行 UPDATE 更新操作。转换后需要调用 {@link LambdaClientUpdateBuilder#set(SFunction, Object)}
     * 方法设置要更新的字段。
     * </p>
     * <p>
     * <b>注意：</b> 如果条件为空，可能会更新全表数据，请谨慎使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = condition.toUpdate()
     *     .set(User::getStatus, 1)
     *     .update();
     * }</pre>
     * </p>
     *
     * @return 更新构建器
     */
    public final LambdaClientUpdateBuilder<T> toUpdate() {
        return new LambdaClientUpdateBuilder<>(this.baseDBApi, this.sqlBuilder);
    }

    /**
     * 将当前的条件构建器转换为单列查询构建器
     *
     * @param selectColumn 要查询的列（Lambda 表达式）
     * @param <R>          列类型
     * @return 单列查询构建器
     */
    public final <R> LambdaClientSingleColumnQueryBuilder<T, R> toColumn(SFunction<T, R> selectColumn) {
        return new LambdaClientSingleColumnQueryBuilder<>(this.baseDBApi, this.sqlBuilder, selectColumn);
    }

    // ==================== 执行方法 ====================

    /**
     * 执行查询并返回结果列表
     * <p>
     * 根据构建器中设置的条件、排序、关联表等，执行 SELECT 查询并返回结果列表。
     * 如果查询结果为空，返回空列表（非 null）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * List<Use?> users = baseDBApi.lambdaQuery()
     *     .eq(User::getStatus, 1)
     *     .orderByDesc(User::getCreateTime)
     *     .list();
     * }
     * </pre>
     *
     * @return 查询结果列表，永远不为 null
     */
    public List<T> list() {
        return toSelect().list();
    }

    /**
     * 执行查询并返回单条结果
     * <p>
     * 根据构建器中设置的条件，执行 SELECT 查询并返回第一条结果。
     * 如果查询结果为空，返回 null。
     * </p>
     * <p>
     * <b>注意：</b> 如果查询结果有多条，只返回第一条。建议配合 limit(1) 使用。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * User user = baseDBApi.lambdaQuery()
     *     .eq(User::getId, 1L)
     *     .one();
     * }
     * </pre>
     *
     * @return 查询结果，可能为 null
     */
    public T one() {
        return toSelect().one();
    }

    /**
     * 以流式方式执行查询并返回结果流
     * <p>
     * 返回的 {@link Stream} 需要在使用完毕后关闭（例如通过 try-with-resources 语句），
     * 以避免数据库连接和游标资源泄漏。
     * 适用于处理大量数据，避免一次性加载所有结果到内存。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * try (Stream<Use?> stream = baseDBApi.lambdaQuery()
     *         .gt(User::getAge, 18)
     *         .stream()) {
     *     stream.filter(user -> user.getName().startsWith("张"))
     *           .forEach(System.out::println);
     * }
     * }
     * </pre>
     *
     * @return 包含映射对象的 Stream，必须在使用完毕后关闭
     */
    public Stream<T> stream() {
        return toSelect().stream();
    }

    /**
     * 执行分页查询
     * <p>
     * 根据构建器中设置的条件和分页参数，执行分页查询。
     * 分页参数通过 {@link Page} 对象传递，包含当前页码、每页大小、排序字段等信息。
     * </p>
     * <p>
     * <b>注意：</b> 如果 {@link Page#isCountTotal()} 为 {@code true}，则会自动执行 COUNT 查询；
     * 否则只查询分页数据，总记录数为 -1。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * Page page = Page.of(1, 10).desc("create_time");
     * PageResult<Use?> result = baseDBApi.lambdaQuery()
     *     .eq(User::getStatus, 1)
     *     .page(page);
     * }
     * </pre>
     *
     * @param page 分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     */
    public PageResult<T> page(Page page) {
        return toSelect().page(page);
    }

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param pageNum  查询的页数
     * @param pageSize 每页的条数
     * @return 对应页码对的数据
     */
    public List<T> simplePage(long pageNum, long pageSize) {
        return toSelect().simplePage(pageNum, pageSize);
    }

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
        return toDelete().delete();
    }

    /**
     * 执行 UPDATE 操作并返回影响行数
     * <p>
     * 根据构建器中设置的更新字段和条件，执行 UPDATE 操作。
     * </p>
     * <p>
     * <b>警告：</b>
     * <ul>
     *     <li>如果没有设置任何条件，可能会更新全表数据</li>
     *     <li>建议始终添加至少一个条件来限制更新范围</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * int rows = baseDBApi.lambdaUpdate()
     *     .set(User::getStatus, 1)
     *     .eq(User::getStatus, 0)
     *     .update();
     * }
     * </pre>
     *
     * @param consumer 需要使用这个消费接口来提供set相关的信息
     * @return 被更新的记录行数
     */
    public int update(Consumer<LambdaUpdateBuilder<T>> consumer) {
        LambdaClientUpdateBuilder<T> clientUpdate = toUpdate();
        consumer.accept(clientUpdate.getUpdateBuilder());
        return clientUpdate.update();
    }

    /**
     * 执行 COUNT 查询并返回统计结果
     * <p>
     * 根据构建器中设置的条件，执行 COUNT 查询并返回统计结果。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * long count = baseDBApi.lambdaCount()
     *     .eq(User::getStatus, 1)
     *     .count();
     * }
     * </pre>
     *
     * @return 统计结果（满足条件的记录数）
     */
    public long count() {
        return toCount().count();
    }

    /**
     * 判断满足条件的记录是否存在
     * <p>
     * 根据构建器中设置的条件，判断是否存在满足条件的记录。
     * 等价于 {@code count() > 0}，但性能更优（某些数据库实现会使用 LIMIT 1 优化）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * boolean exists = baseDBApi.lambdaCount()
     *     .eq(User::getEmail, "test@example.com")
     *     .exist();
     * }
     * </pre>
     *
     * @return true 表示存在至少一条记录，false 表示不存在任何记录
     */
    public boolean exist() {
        return toCount().exist();
    }

    /**
     * 执行单列查询，返回指定列的结果列表。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询所有用户的姓名列表
     * List<String> names = baseDBApi.lambdaCondition()
     *     .eq(User::getStatus, 1)
     *     .columns(User::getName);
     * }</pre>
     * </p>
     *
     * @param selectColumn 要查询的列（Lambda表达式）
     * @param <R>          列类型
     * @return 指定列的值列表
     */
    public <R> List<R> columns(SFunction<T, R> selectColumn) {
        return toColumn(selectColumn).list();
    }

    /**
     * 执行单列查询，以流式方式返回指定列的结果。
     * <p>使用完毕后需关闭Stream以释放资源。</p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * try (Stream<String> stream = baseDBApi.lambdaCondition()
     *         .eq(User::getStatus, 1)
     *         .columnsStream(User::getName)) {
     *     stream.filter(name -> name.startsWith("张")).forEach(System.out::println);
     * }
     * }</pre>
     * </p>
     *
     * @param selectColumn 要查询的列（Lambda表达式）
     * @param <R>          列类型
     * @return 指定列的值流，使用完毕后需关闭
     */
    public <R> Stream<R> columnsStream(SFunction<T, R> selectColumn) {
        return toColumn(selectColumn).stream();
    }

    /**
     * 执行单列分页查询，返回指定列的分页结果。
     * <p>
     * 使用示例：
     * <pre>{@code
     * Page page = Page.of(1, 10).desc("age");
     * PageResult<String> result = baseDBApi.lambdaCondition()
     *     .eq(User::getStatus, 1)
     *     .columnsPage(User::getName, page);
     * List<String> names = result.getRecords();
     * long total = result.getTotalCount();
     * }</pre>
     * </p>
     *
     * @param selectColumn 要查询的列（Lambda表达式）
     * @param page         分页参数
     * @param <R>          列类型
     * @return 分页结果，包含列值列表和分页信息
     */
    public <R> PageResult<R> columnsPage(SFunction<T, R> selectColumn, @NonNull Page page) {
        return toColumn(selectColumn).page(page);
    }

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param pageNum  查询的页数
     * @param pageSize 每页的条数
     * @return 对应页码对的数据
     */
    public <R> List<R> simpleColumnsPage(SFunction<T, R> selectColumn, long pageNum, long pageSize) {
        return toColumn(selectColumn).simplePage(pageNum, pageSize);
    }

    /**
     * 执行单列查询，返回指定列的第一条结果。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询第一个状态为1的用户姓名
     * String name = baseDBApi.lambdaCondition()
     *     .eq(User::getStatus, 1)
     *     .column(User::getName);
     * }</pre>
     * </p>
     *
     * @param selectColumn 要查询的列（Lambda表达式）
     * @param <R>          列类型
     * @return 第一条结果，无结果时返回null
     */
    @Nullable
    public <R> R column(SFunction<T, R> selectColumn) {
        return toColumn(selectColumn).one();
    }
}
