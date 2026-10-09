package io.github.lucklike.httpclient.dbclient.sql.lambda;

import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * 自带数据库客户端的单列查询构建器
 * <p>
 * 该类封装了 {@link LambdaSingleColumnQueryBuilder} 和 {@link BaseDBApi}，
 * 提供流式 API 构建单列 SELECT 查询条件，并可直接执行查询操作，返回指定列的值。
 * 公共条件方法继承自 {@link AbstractLambdaClientBuilder}。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 查询所有用户的名称列
 * List<String> names = baseDBApi.lambdaQuery(User.class, User::getName)
 *     .eq(User::getStatus, 1)
 *     .list();
 *
 * // 查询单个用户的年龄
 * Integer age = baseDBApi.lambdaQuery(User.class, User::getAge)
 *     .eq(User::getId, 1L)
 *     .one();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 实体类型
 * @param <R> 查询列的类型
 * @author fukang
 * @version 1.0.0
 * @date 2026/6/10 01:43
 */
public class LambdaClientSingleColumnQueryBuilder<T, R> extends AbstractLambdaClientBuilder<T, LambdaClientSingleColumnQueryBuilder<T, R>, LambdaSingleColumnQueryBuilder<T, R>> {

    /**
     * 构造单列查询构建器（使用实体类）
     *
     * @param baseDBApi    数据库客户端API
     * @param clazz        实体类类型
     * @param selectColumn 要查询的列（Lambda 表达式）
     */
    public LambdaClientSingleColumnQueryBuilder(BaseDBApi<T> baseDBApi, Class<T> clazz, SFunction<T, R> selectColumn) {
        super(baseDBApi, new LambdaSingleColumnQueryBuilder<>(clazz, selectColumn));
    }

    /**
     * 构造单列查询构建器（使用现有的 SQL 构建器）
     *
     * @param baseDBApi    数据库客户端API
     * @param sqlBuilder   现有的 SQL 构建器
     * @param selectColumn 要查询的列（Lambda 表达式）
     */
    public LambdaClientSingleColumnQueryBuilder(BaseDBApi<T> baseDBApi, LambdaSqlBuilder<T> sqlBuilder, SFunction<T, R> selectColumn) {
        super(baseDBApi, new LambdaSingleColumnQueryBuilder<>(sqlBuilder, selectColumn));
    }

    /**
     * 获取查询列的类型
     *
     * @return 查询列的类型
     */
    public Class<?> getSelectColumnType() {
        return sqlBuilder.getSelectColumnType();
    }

    /**
     * 查询时包含已删除记录
     * <p>
     * 调用后本次查询不追加"不等于已删除值"的过滤条件，列值结果中同时包含来自未删除与已删除记录的数据；
     * 分页查询（含总数统计）口径一致。实体未标注 {@code @LogicDelete} 字段时抛出明确异常。
     * </p>
     *
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 实体未标注 {@code @LogicDelete} 字段时
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> includeDeleted() {
        sqlBuilder.includeDeleted();
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> join(SqlBuilder.JoinType type, Class<E> joinClass, String alias) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> innerJoin(Class<E> joinClass, String alias) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> leftJoin(Class<E> joinClass, String alias) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> rightJoin(Class<E> joinClass, String alias) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> on(String condition) {
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
    public <E> LambdaClientSingleColumnQueryBuilder<T, R> on(SFunction<T, ?> leftColumn, SFunction<E, ?> rightColumn) {
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
    public LambdaClientSingleColumnQueryBuilder<T, R> orderBy(SFunction<T, ?> column, SqlBuilder.OrderType orderType) {
        sqlBuilder.orderBy(column, orderType);
        return this;
    }

    /**
     * 添加升序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> orderByAsc(SFunction<T, ?> column) {
        sqlBuilder.orderByAsc(column);
        return this;
    }

    /**
     * 添加降序排序条件
     *
     * @param column 排序字段的 Lambda 表达式
     * @return 当前构建器实例，支持链式调用
     */
    public LambdaClientSingleColumnQueryBuilder<T, R> orderByDesc(SFunction<T, ?> column) {
        sqlBuilder.orderByDesc(column);
        return this;
    }

    // ==================== 执行方法 ====================

    /**
     * 执行查询并返回指定列的值列表
     *
     * @return 指定列的值列表，永远不为 null
     */
    public List<R> list() {
        return this.baseDBApi.columns(this.sqlBuilder);
    }

    /**
     * 以流式方式执行查询并返回指定列的值流
     * <p>
     * 返回的 {@link Stream} 需要在使用完毕后关闭，以避免数据库连接和游标资源泄漏。
     * </p>
     *
     * @return 指定列的值流，必须在使用完毕后关闭
     */
    public Stream<R> stream() {
        return this.baseDBApi.columnsStream(this.sqlBuilder);
    }

    /**
     * 执行指定列的分页查询
     *
     * @param page 分页参数对象
     * @return 分页结果，包含指定列的值列表和分页信息
     */
    public PageResult<R> page(@NonNull Page page) {
        return this.baseDBApi.columnsPage(this.sqlBuilder, page);
    }

    /**
     * 简单分页查询，不进行COUNT查询，只返回指定列的值
     *
     * @param pageNum  查询的页数
     * @param pageSize 每页的条数
     * @return 对应页码的指定列的值列表
     */
    public List<R> simplePage(long pageNum, long pageSize) {
        return this.baseDBApi.simpleColumnsPage(this.sqlBuilder, pageNum, pageSize);
    }

    /**
     * 执行查询并返回指定列的单条值
     * <p>
     * 如果查询结果为空，返回 null。
     * </p>
     *
     * @return 指定列的单条值，可能为 null
     */
    @Nullable
    public R one() {
        return this.baseDBApi.column(this.sqlBuilder);
    }
}
