package io.github.lucklike.httpclient.dbclient.sql;

import com.luckyframework.common.ContainerUtils;

public abstract class Lambda {

    @SafeVarargs
    public static  <T> LambdaQueryBuilder<T> select(Class<T> clazz, SFunction<T, ?>... selectColumns) {
        return new LambdaQueryBuilder<>(clazz, selectColumns);
    }

    @SafeVarargs
    public static  <T> LambdaCountBuilder<T> count(Class<T> clazz, SFunction<T, ?>... columns) {
        if (ContainerUtils.isEmptyArray(columns)) {
            return new LambdaCountBuilder<>(clazz);
        }
        return new LambdaCountBuilder<>(clazz, columns[0]);
    }

    public static  <T> LambdaUpdateBuilder<T> update(Class<T> clazz) {
        return new LambdaUpdateBuilder<>(clazz);
    }

    public static  <T> LambdaDeleteBuilder<T> delete(Class<T> clazz) {
        return new LambdaDeleteBuilder<>(clazz);
    }


}
