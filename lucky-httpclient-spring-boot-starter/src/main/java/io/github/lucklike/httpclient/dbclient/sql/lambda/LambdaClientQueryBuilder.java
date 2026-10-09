package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.function.SQLFunctions;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 自带数据库客户端的查询构建器
 * <p>
 * 该类封装了 {@link LambdaQueryBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建 SELECT 查询条件，并可直接执行查询操作。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
 * </p>
 * <p>
 * 支持的功能：
 * <ul>
 *     <li>条件过滤（WHERE）</li>
 *     <li>关联查询（JOIN）</li>
 *     <li>排序（ORDER BY）</li>
 *     <li>分页查询（PAGE）</li>
 *     <li>流式查询（STREAM）</li>
 *     <li>指定查询列（SELECT）</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 通过 BaseDBApi 获取查询构建器
 * LambdaClientQueryBuilder<User> query = baseDBApi.lambdaQuery();
 *
 * // 查询所有用户
 * List<User> allUsers = query.list();
 *
 * // 条件查询
 * List<User> activeUsers = baseDBApi.lambdaQuery()
 *     .eq(User::getStatus, 1)
 *     .orderByDesc(User::getCreateTime)
 *     .list();
 *
 * // 查询单条记录
 * User user = baseDBApi.lambdaQuery()
 *     .eq(User::getId, 1L)
 *     .one();
 *
 * // 分页查询
 * Page page = Page.of(1, 10).desc("create_time");
 * PageResult<User> pageResult = baseDBApi.lambdaQuery()
 *     .eq(User::getStatus, 1)
 *     .page(page);
 *
 * // 关联查询
 * List<User> users = baseDBApi.lambdaQuery()
 *     .leftJoin(Order.class, "o")
 *     .on(User::getId, Order::getUserId)
 *     .eq(User::getStatus, 1)
 *     .list();
 *
 * // 指定查询列
 * List<User> users = baseDBApi.lambdaQuery(User::getId, User::getName)
 *     .eq(User::getStatus, 1)
 *     .list();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/3 00:53
 */
public class LambdaClientQueryBuilder<T> extends AbstractLambdaClientBuilder<T, LambdaClientQueryBuilder<T>, LambdaQueryBuilder<T>> {

    /**
     * 构造查询构建器（使用实体对象，查询所有列）
     * <p>
     * 实体的非空字段会自动作为相等条件加入查询。
     * </p>
     *
     * @param baseDBApi 数据库客户端API
     * @param entity    实体对象
     */
    @SuppressWarnings("unchecked")
    public LambdaClientQueryBuilder(BaseDBApi<T> baseDBApi, @Nullable T entity) {
        this(baseDBApi, (Class<T>) Objects.requireNonNull(entity).getClass());
        SQLFunctions.columnHandler(entity, co -> {
            if (co.getValue() != null) {
                co.getCondition().additionCondition(sqlBuilder.getSqlBuilder(), co);
            }
        });
    }

    /**
     * 构造查询构建器（使用实体类，查询所有列）
     *
     * @param baseDBApi 数据库客户端API
     * @param clazz     实体类类型
     */
    public LambdaClientQueryBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz) {
        super(baseDBApi, new LambdaQueryBuilder<>(clazz));
    }

    /**
     * 构造查询构建器（使用现有的 SQL 构建器，查询所有列）
     *
     * @param baseDBApi  数据库客户端API
     * @param sqlBuilder 现有的 SQL 构建器
     */
    public LambdaClientQueryBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder) {
        super(baseDBApi, new LambdaQueryBuilder<>(sqlBuilder));
    }

    /**
     * 指定查询列
     * <p>
     * 覆盖默认的全列查询，仅查询指定的列。支持多次调用以追加查询列。
     * </p>
     *
     * @param columns 要查询的列（Lambda 表达式）
     * @return 当前构建器实例，支持链式调用
     */
    @SafeVarargs
    public final LambdaClientQueryBuilder<T> select(SFunction<T, ?>... columns) {
        sqlBuilder.select(columns);
        return this;
    }

    // ==================== 关联表方法 ====================

    /**
     * 添加 JOIN 关联
     * <p>
     * 支持 INNER JOIN、LEFT JOIN、RIGHT JOIN 等关联类型。
     * 添加 JOIN 后需要通过 {@link #on} 方法指定关联条件。
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * baseDBApi.lambdaQuery(User.class)
     *     .leftJoin(Order.class, "o")
     *     .on(User::getId, Order::getUserId)
     *     .list();
     * }
     * </pre>
     *
     * @param type      JOIN 类型（INNER、LEFT、RIGHT）
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientQueryBuilder<T> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
        sqlBuilder.join(type, joinClass, alias);
        return this;
    }

    /**
     * 添加 INNER JOIN 关联
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientQueryBuilder<T> innerJoin(Class<E> joinClass, String alias) {
        sqlBuilder.innerJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 LEFT JOIN 关联
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientQueryBuilder<T> leftJoin(Class<E> joinClass, String alias) {
        sqlBuilder.leftJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 RIGHT JOIN 关联
     *
     * @param joinClass 要关联的实体类
     * @param alias     关联表的别名
     * @param <E>       关联实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientQueryBuilder<T> rightJoin(Class<E> joinClass, String alias) {
        sqlBuilder.rightJoin(joinClass, alias);
        return this;
    }

    /**
     * 添加 JOIN 关联条件（原生 SQL）
     * <p>
     * 使用原生 SQL 片段指定 JOIN 的 ON 条件。
     * </p>
     *
     * @param condition SQL 条件片段，可使用 ? 作为参数占位符
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> on(String condition) {
        sqlBuilder.on(condition);
        return this;
    }

    /**
     * 添加 JOIN 关联条件（Lambda 表达式）
     * <p>
     * 使用 Lambda 表达式指定 JOIN 的 ON 条件，如 left.column = right.column
     * </p>
     *
     * @param leftColumn  左表字段的 Lambda 表达式
     * @param rightColumn 右表字段的 Lambda 表达式
     * @param <E>         右表实体类型
     * @return 当前构建器实例，支持链式调用
     */
    public <E> LambdaClientQueryBuilder<T> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
        sqlBuilder.on(leftColumn, rightColumn);
        return this;
    }

    // ==================== 排序方法 ====================

    /**
     * 添加排序条件
     *
     * @param column    排序字段的 Lambda 表达式
     * @param orderType 排序类型
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        sqlBuilder.orderBy(column, orderType);
        return this;
    }

    /**
     * 添加升序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderByAsc(SFunction<T, ?> column) {
        sqlBuilder.orderByAsc(column);
        return this;
    }

    /**
     * 添加降序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientQueryBuilder<T> orderByDesc(SFunction<T, ?> column) {
        sqlBuilder.orderByDesc(column);
        return this;
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
     * List<User> users = baseDBApi.lambdaQuery()
     *     .eq(User::getStatus, 1)
     *     .orderByDesc(User::getCreateTime)
     *     .list();
     * }
     * </pre>
     *
     * @return 查询结果列表，永远不为 null
     */
    public List<T> list() {
        return this.baseDBApi.selectList(this.sqlBuilder);
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
        return this.baseDBApi.selectOne(this.sqlBuilder);
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
     * try (Stream<User> stream = baseDBApi.lambdaQuery()
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
        return this.baseDBApi.stream(this.sqlBuilder);
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
     * PageResult<User> result = baseDBApi.lambdaQuery()
     *     .eq(User::getStatus, 1)
     *     .page(page);
     * }
     * </pre>
     *
     * @param page 分页参数对象
     * @return 分页结果，包含数据列表和分页信息
     */
    public PageResult<T> page(Page page) {
        return this.baseDBApi.selectPage(this.sqlBuilder, page);
    }

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param pageNum  查询的页数
     * @param pageSize 每页的条数
     * @return 对应页码对的数据
     */
    public List<T> simplePage(long pageNum, long pageSize) {
        return this.baseDBApi.simplePage(this.sqlBuilder, pageNum, pageSize);
    }
}
