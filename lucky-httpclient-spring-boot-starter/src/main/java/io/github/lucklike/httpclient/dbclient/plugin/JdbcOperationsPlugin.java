package io.github.lucklike.httpclient.dbclient.plugin;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.plugin.ExecuteMeta;
import com.luckyframework.httpclient.proxy.plugin.ProxyDecorator;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.dbclient.sql.SQLType;
import io.github.lucklike.httpclient.dbclient.annotation.SQL;
import io.github.lucklike.httpclient.dbclient.executor.NamedParamSQLExecutor;
import io.github.lucklike.httpclient.dbclient.executor.SQLExecutor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

        // 获取方法元信息
        ExecuteMeta meta = decorator.getMeta();
        MethodContext mc = new MethodContext(meta.getMethodMetaContext(), meta.getArgs());
        Method method = meta.getMethod();
        MethodProxy methodProxy = meta.getMethodProxy();
        Object[] args = meta.getArgs();
        Object proxy = meta.getProxy();


        // 接口的default方法
        if (method.isDefault()) {
            return MethodUtils.invokeDefault(proxy, method, args);
        }

        // hashCode方法
        if (ReflectionUtils.isHashCodeMethod(method)) {
            return proxy.getClass().hashCode();
        }

        // toString方法
        if (ReflectionUtils.isToStringMethod(method)) {
            return meta.getTargetClass().getName() + proxy.getClass().getSimpleName();
        }

        // 非抽象方法
        if (!Modifier.isAbstract(method.getModifiers())) {
            return methodProxy != null ? methodProxy.invokeSuper(proxy, args) : MethodUtils.invoke(proxy, method, args);
        }

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
        SQLExecutor sqlExecutor = new NamedParamSQLExecutor(mc, sqlType, sqlTemp);
        return sqlExecutor.execute();
    }
}
