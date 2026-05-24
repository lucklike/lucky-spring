package io.github.lucklike.httpclient.dbclient.executor;

import io.github.lucklike.httpclient.dbclient.SQLType;

import java.util.List;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 02:11
 */
public interface SQLWrapper {

    String getSqlTemp();

    SQLType getType();

    Object[] getParams();

    List<Object[]> getBatchParams();
}
