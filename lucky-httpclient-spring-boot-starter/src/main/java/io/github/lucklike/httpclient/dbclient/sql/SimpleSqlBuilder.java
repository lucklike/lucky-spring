package io.github.lucklike.httpclient.dbclient.sql;

import java.util.List;

/**
 * 简单SQL构建器
 */
public class SimpleSqlBuilder implements SQLWrapper {

    private final String sqlTemp;
    private final SQLType sqlType;
    private final Object[] params;
    private final List<Object[]> batchParams;

    private SimpleSqlBuilder(String sqlTemp, SQLType sqlType, Object[] params, List<Object[]> batchParams) {
        this.sqlTemp = sqlTemp;
        this.sqlType = sqlType;
        this.params = params;
        this.batchParams = batchParams;
    }

    public static SimpleSqlBuilder of(SQLType sqlType, String sqlTemp, Object[] params) {
        return new SimpleSqlBuilder(sqlTemp, sqlType, params, null);
    }

    public static SimpleSqlBuilder ofBatch(String sqlTemp, List<Object[]> batchParams) {
        return new SimpleSqlBuilder(sqlTemp, SQLType.BATCH, null, batchParams);
    }

    @Override
    public String getSqlTemp() {
        return sqlTemp;
    }

    @Override
    public SQLType getType() {
        return sqlType;
    }

    @Override
    public Object[] getParams() {
        return params;
    }

    @Override
    public List<Object[]> getBatchParams() {
        return batchParams;
    }

}
