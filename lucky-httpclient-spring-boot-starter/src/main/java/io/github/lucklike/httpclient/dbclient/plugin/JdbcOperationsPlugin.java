package io.github.lucklike.httpclient.dbclient.plugin;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.plugin.ProxyDecorator;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import io.github.lucklike.httpclient.dbclient.SQLType;
import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.executor.AnnotationSQLExecutor;
import io.github.lucklike.httpclient.dbclient.executor.SQLExecutor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.SQLException;

/**
 * 基于{@link NamedParameterJdbcTemplate}实现的数据库通讯客户端插件
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 03:07
 */
public class JdbcOperationsPlugin implements ProxyPlugin {

    @Override
    public Object decorate(ProxyDecorator decorator) throws Throwable {
        // 构建方法上下文
        MethodContext mc = decorator.getMeta().getMethodContext();

        // 参数列表中存在 SQL 执行器时直接使用参数列表中的执行器
        SQLExecutor argExecutor = mc.getArgument(SQLExecutor.class);
        if (argExecutor != null) {
            argExecutor.execute();
        }

        /* 尝试从@SQL注解中获取 SQL 执行器 */
        // 没有被 SQL 注解标注时直接返回 null
        SQL sqlAnn = mc.getMergedAnnotation(SQL.class);
        if (sqlAnn == null) {
            return null;
        }

        // 配置了 SQL 执行器时优先使用 SQL 执行器
        String executor = sqlAnn.executor();
        if (StringUtils.hasText(executor)) {
            return mc.parseExpression(executor, SQLExecutor.class).execute();
        }

        // 执行 SQL
        return executeSQL(mc, sqlAnn);
    }

    /**
     * 执行 SQL 返回结果
     * @param mc 方法上下文
     * @param sqlAnn SQL 注解示例
     * @return SQL 执行结果
     * @throws SQLException 执行过程中可能出现 SQL 异常
     */
    private Object executeSQL(MethodContext mc, SQL sqlAnn) throws SQLException {
        // 计算 SQL 模板
        String sqlTemp = mc.parseExpression(sqlAnn.sql(), String.class);
        SQLType sqlType = sqlAnn.type();

        // 构建 SQL 执行器
        SQLExecutor sqlExecutor = new AnnotationSQLExecutor(mc, sqlType, sqlTemp);
        return sqlExecutor.execute();
    }
}
