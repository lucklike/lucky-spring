package io.github.lucklike.httpclient.dbclient.executor;

/**
 * SQL执行器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 00:07
 */
public interface SQLExecutor {

    /**
     * 执行 SQL 返回执行结果
     *
     * @return 执行结果
     */
    Object execute();
}
