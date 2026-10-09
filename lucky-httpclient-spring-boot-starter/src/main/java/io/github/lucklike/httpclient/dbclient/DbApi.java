package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.function.SQLFunctions;

/**
 * 数据库访问能力的根接口。
 * <p>
 * 该接口定义所有 SQL 执行模板常量，以及实体类型解析能力，
 * 是 {@link BaseDBApi} 接口体系的根，所有数据库访问能力接口
 * （{@link QueryApi}、{@link WriteApi}、{@link SingleColumnApi}、{@link LambdaBuilderApi}）
 * 均继承自本接口。
 * </p>
 *
 * @param <E> 实体类型泛型
 * @author fukang
 * @version 1.0.0
 */
public interface DbApi<E> {

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
     * {@link SQLFunctions#selectByMap(MethodContext)} 执行的 SQL 模板
     * <p>
     * 用于根据Map的非空元素作为等值条件进行查询。
     * </p>
     */
    String SQL_SELECT_BY_MAP = "#{selectByMap($mc$)}";

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
     * 获取实体类类型。
     * <p>
     * 通过泛型参数自动解析实体类的 Class 对象。
     * </p>
     *
     * @return 实体类 Class 对象
     */
    Class<E> entityClass();
}
