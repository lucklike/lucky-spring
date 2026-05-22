package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;

/**
 * SQL并接相关的函数
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 03:09
 */
public class SQLFunctions {

    private static final String SQL_AND = "AND";
    private static final String SQL_OR = "OR";
    private static final String SQL_IN = "IN";

    @FunctionAlias(SQL_AND)
    public static String and(String sql, Object obj) {
        return sql(SQL_AND, sql, obj);
    }

    @FunctionAlias(SQL_OR)
    public static String or(String sql, Object obj) {
        return sql(SQL_OR, sql, obj);
    }

    @FunctionAlias(SQL_IN)
    public static String in(String sql, Object obj) {
        return sql(SQL_IN, sql, obj);
    }

    private static String sql(String linkSymbol, String sql, Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String && !StringUtils.hasText((String) obj)) {
            return "";
        }
        return String.format(" %s %s", linkSymbol, sql);
    }
}
