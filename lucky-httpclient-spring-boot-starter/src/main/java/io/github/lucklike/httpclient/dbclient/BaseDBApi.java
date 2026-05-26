package io.github.lucklike.httpclient.dbclient;

import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.sql.LambdaCountBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaQueryBuilder;
import io.github.lucklike.httpclient.dbclient.sql.LambdaUpdateBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 提供基于Lambda表达式查询功能的基本API
 *
 * @param <E> 实体类型泛型
 */
public interface BaseDBApi<E> {

    String SQL_LAMBDA = "#{lambdaSql($mc$)}";
    String SQL_SELECT_BY_ID = "#{selectById($mc$)}";
    String SQL_SELECT_BY_ENTITY = "#{selectByEntity($mc$)}";
    String SQL_DELETE_BY_ID = "#{deleteById($mc$)}";
    String SQL_UPDATE_BY_ID = "#{updateById($mc$)}";
    String SQL_INSERT_SQL = "#{insertSql($mc$)}";
    String SQL_BATCH_INSERT_SQL = "#{batchInsertSql($mc$)}";

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
     * 使用实体类对象作为条件进行查询（将非空属性值作为条件使用 AND 进行拼接）
     *
     * @param queryEntity 查询实体类对象
     * @return 查询结果
     */
    @NonNull
    @SQL(executor = SQL_SELECT_BY_ENTITY)
    List<E> selectList(@NonNull E queryEntity);

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
    int[] batchInsert(@NonNull Collection<E> entities);

}
