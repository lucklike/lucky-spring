package io.github.lucklike.httpclient.dbclient;

import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.function.LambdaDeleteBuilder;
import io.github.lucklike.httpclient.dbclient.function.LambdaQueryBuilder;
import io.github.lucklike.httpclient.dbclient.function.LambdaUpdateBuilder;

import java.util.List;

public interface BaseDBApi<E> {

    String SQL_EXECUTOR_EXPRESSION = "#{sqlWrapper($mc$)}";

    @SQL(executor = SQL_EXECUTOR_EXPRESSION)
    E selectOne(LambdaQueryBuilder<E> queryBuilder);

    @SQL(executor = SQL_EXECUTOR_EXPRESSION)
    List<E> selectList(LambdaQueryBuilder<E> queryBuilder);

    @SQL(executor = SQL_EXECUTOR_EXPRESSION)
    int update(LambdaUpdateBuilder<E> updateBuilder);

    @SQL(executor = SQL_EXECUTOR_EXPRESSION)
    int delete(LambdaDeleteBuilder<E> deleteBuilder);

}
