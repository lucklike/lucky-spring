package io.github.lucklike.httpclient.dbclient;

import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaSingleColumnQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.page.Page;
import io.github.lucklike.httpclient.dbclient.sql.page.PageResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * 单列查询能力接口。
 * <p>
 * 提供针对指定列的查询能力，包括：
 * <ul>
 *     <li>列表查询</li>
 *     <li>流式查询</li>
 *     <li>分页查询</li>
 *     <li>查询第一条结果</li>
 * </ul>
 * </p>
 *
 * @param <E> 实体类型泛型
 * @author fukang
 * @version 1.0.0
 */
public interface SingleColumnApi<E> extends DbApi<E> {

    /**
     * 执行单列查询，返回结果列表。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询所有年龄大于18岁的用户ID列表
     * List<Long> ids = userDBApi.columns(Lambda.column(User::getId).eq(User::getAge, 18));
     * }</pre>
     * </p>
     *
     * @param singleColumnQueryBuilder 单列查询构建器
     * @param <R>                      列类型
     * @return 指定列的值列表
     */
    @SQL(executor = SQL_LAMBDA)
    <R> List<R> columns(LambdaSingleColumnQueryBuilder<E, R> singleColumnQueryBuilder);

    /**
     * 执行单列查询，以流式方式返回结果。
     * <p>使用完毕后需关闭Stream以释放资源。</p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * try (Stream<String> stream = userDBApi.columnsStream(Lambda.column(User::getName).eq(User::getStatus, 1))) {
     *     stream.filter(name -> name.startsWith("张")).forEach(System.out::println);
     * }
     * }</pre>
     * </p>
     *
     * @param singleColumnQueryBuilder 单列查询构建器
     * @param <R>                      列类型
     * @return 指定列的值流
     */
    @SQL(executor = SQL_LAMBDA)
    <R> Stream<R> columnsStream(LambdaSingleColumnQueryBuilder<E, R> singleColumnQueryBuilder);

    /**
     * 执行单列分页查询。
     * <p>
     * 使用示例：
     * <pre>{@code
     * Page page = Page.of(1, 10).desc("age");
     * PageResult<String> result = userDBApi.columnsPage(Lambda.column(User::getName).eq(User::getStatus, 1),page);
     * List<String> names = result.getRecords();
     * long total = result.getTotalCount();
     * }</pre>
     * </p>
     *
     * @param singleColumnQueryBuilder 单列查询构建器
     * @param page                     分页参数
     * @param <R>                      列类型
     * @return 分页结果（包含列值列表和分页信息）
     */
    @SQL(executor = SQL_LAMBDA)
    <R> PageResult<R> columnsPage(LambdaSingleColumnQueryBuilder<E, R> singleColumnQueryBuilder, @NonNull Page page);

    /**
     * 简单分页查询，不进行COUNT查询，只返回记录
     *
     * @param singleColumnQueryBuilder 查询条件实体对象，仅使用其中的非空属性作为查询条件
     * @param pageNum     查询的页数
     * @param pageSize    每页的条数
     * @return 对应页码对的数据
     */
    default <R> List<R> simpleColumnsPage(LambdaSingleColumnQueryBuilder<E, R> singleColumnQueryBuilder, long pageNum, long pageSize) {
        return columnsPage(singleColumnQueryBuilder, Page.notCount(pageNum, pageSize)).getRecords();
    }

    /**
     * 执行单列查询，返回第一条结果。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 查询第一个年龄大于18岁的用户姓名
     * String name = userDBApi.column(Lambda.column(User::getName).eq(User::getAge, 18).gt(18));
     * }</pre>
     * </p>
     *
     * @param singleColumnQueryBuilder 单列查询构建器
     * @param <R>                      列类型
     * @return 第一条结果，无结果时返回null
     */
    @Nullable
    default <R> R column(LambdaSingleColumnQueryBuilder<E, R> singleColumnQueryBuilder) {
        // 通过分页（不查总数，只取1条）实现，避免全量加载
        return columnsPage(singleColumnQueryBuilder, Page.notCount(1, 1)).getRecords().stream().findFirst().orElse(null);
    }
}
