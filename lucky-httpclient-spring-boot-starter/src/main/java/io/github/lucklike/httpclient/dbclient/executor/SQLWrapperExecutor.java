package io.github.lucklike.httpclient.dbclient.executor;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.function.SqlBuilder;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 00:59
 */
public class SQLWrapperExecutor extends AbstractMCNamedJdbcTemplateSQLExecutor {

    private final SQLWrapper sqlWrapper;

    public SQLWrapperExecutor(MethodContext mc, SQLWrapper sqlWrapper) {
        super(mc, sqlWrapper.getType());
        this.sqlWrapper = sqlWrapper;
    }

    @Override
    public Object execute() {
        String sqlTemp = sqlWrapper.getSqlTemp();
        switch (getSqlType()) {
            case SELECT:
                return query(sqlTemp, sqlWrapper.getParams());
            case UPDATE:
                return update(sqlTemp, sqlWrapper.getParams());
            default:
                return batchUpdate(sqlTemp, sqlWrapper.getBatchParams());
        }
    }

}
