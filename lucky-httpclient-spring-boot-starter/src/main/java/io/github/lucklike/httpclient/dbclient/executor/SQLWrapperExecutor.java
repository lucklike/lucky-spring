package io.github.lucklike.httpclient.dbclient.executor;

import com.luckyframework.common.FontUtil;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.function.SqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


/**
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
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n\tSQL   : {}\n\tPARAM : {}\n>>", sqlTemp, Arrays.toString(sqlWrapper.getParams()))));
                return query(sqlTemp, sqlWrapper.getParams());
            case UPDATE:
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n\tSQL   : {}\n\tPARAM : {}\n>>", sqlTemp, Arrays.toString(sqlWrapper.getParams()))));
                return update(sqlTemp, sqlWrapper.getParams());
            default:
                logger.info(FontUtil.getWhiteStr(StringUtils.format("\n>>\n\tSQL   : {}\n\tPARAM : {}\n>>", sqlTemp,sqlWrapper.getBatchParams())));
                return batchUpdate(sqlTemp, sqlWrapper.getBatchParams());
        }
    }

}
