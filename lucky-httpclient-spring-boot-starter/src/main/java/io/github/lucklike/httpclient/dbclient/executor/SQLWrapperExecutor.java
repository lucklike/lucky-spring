package io.github.lucklike.httpclient.dbclient.executor;

import com.luckyframework.common.FontUtil;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.sql.SQLWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SQL包装器执行器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 00:59
 */
public class SQLWrapperExecutor extends AbstractMCNamedJdbcTemplateSQLExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SQLWrapperExecutor.class);

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
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n{}\n{}\n>>", sqlTemp, getSqlParam(sqlWrapper.getParams()))));
                return queryAutoSelectModel(sqlTemp, sqlWrapper.getParams());
            case UPDATE:
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n{}\n{}\n>>", sqlTemp, getSqlParam(sqlWrapper.getParams()))));
                return update(sqlTemp, sqlWrapper.getParams());
            default:
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n{}\n{}\n>>", sqlTemp, getBatchSqlParam(sqlWrapper.getBatchParams()))));
                return batchUpdate(sqlTemp, sqlWrapper.getBatchParams());
        }
    }

}
