package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.annotation.Column;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.executor.SQLExecutor;
import io.github.lucklike.httpclient.dbclient.executor.SQLWrapper;
import io.github.lucklike.httpclient.dbclient.executor.SQLWrapperExecutor;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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

    public static SQLExecutor lambdaSql(MethodContext mc) {
        return new SQLWrapperExecutor(mc, Objects.requireNonNull(mc.getArgument(SQLWrapper.class)));
    }

    public static SQLExecutor selectById(MethodContext mc) {
        Class<?> entityClass = mc.getResultResolvableType().toClass();
        SqlBuilder sqlBuilder = SqlBuilder.builder().select().from(EntityUtils.getTableName(entityClass)).eq(EntityUtils.getIdColumn(entityClass), mc.getArguments()[0]);
        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    public static SQLExecutor deleteById(MethodContext mc) {
        Class<?> entityClass = ResolvableType.forClass(BaseDBApi.class, mc.getClassContext().getCurrentAnnotatedElement()).getGeneric(0).toClass();
        SqlBuilder sqlBuilder = SqlBuilder.builder().delete().from(EntityUtils.getTableName(entityClass)).eq(EntityUtils.getIdColumn(entityClass), mc.getArguments()[0]);
        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    public static SQLExecutor updateById(MethodContext mc) {
        Object entity = mc.getArguments()[0];
        Class<?> entityClass = entity.getClass();
        final AtomicBoolean hasId = new  AtomicBoolean(false);
        SqlBuilder sqlBuilder = SqlBuilder.builder().update(EntityUtils.getTableName(entityClass));

        columnHandler(entity, co -> {
            if (co.getValue() != null) {
                if (co.isId()) {
                    sqlBuilder.eq(co.getName(), co.getValue());
                    hasId.set(true);
                } else {
                    sqlBuilder.set(co.getName(), co.getValue());
                }
            }
        });

        if (!hasId.get()) {
            throw new IllegalArgumentException("The ID attribute was not found in class '" + ClassUtils.getClassName(entityClass) + "'");
        }

        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    public static SQLExecutor insertSql(MethodContext mc) {
        Object entity = mc.getArguments()[0];
        Class<?> entityClass = entity.getClass();

        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        columnHandler(entity, co -> {
            if (co.getValue() != null) {
                columnNames.add(co.getName());
                values.add(co.getValue());
            }
        });

        SqlBuilder sqlBuilder = SqlBuilder
                .builder()
                .insertInto(EntityUtils.getTableName(entityClass), columnNames.toArray(new String[0]))
                .values(values.toArray(new Object[0]));

        return new SQLWrapperExecutor(mc, sqlBuilder);
    }


    private static void columnHandler(Object entity, Consumer<ColumnInfo> consumer) {
        for (Field field : ClassUtils.getAllFields(entity.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Column columnAnn = AnnotationUtils.findMergedAnnotation(field, Column.class);
            if (columnAnn != null && !columnAnn.exist()) {
                continue;
            }

            ColumnInfo columnInfo;
            Object fieldValue = FieldUtils.getValue(entity, field);
            String columnName = (columnAnn != null && StringUtils.hasText(columnAnn.value())) ? columnAnn.value() : field.getName();
            if (AnnotationUtils.isAnnotated(field, Id.class)) {
                columnInfo = new ColumnInfo(columnName, fieldValue, true);
            } else {
                columnInfo = new ColumnInfo(columnName, fieldValue, false);
            }

            consumer.accept(columnInfo);
        }
    }

    static class ColumnInfo {
        private final String name;
        private final Object value;
        private final boolean isId;

        private ColumnInfo(String name, Object value, boolean isId) {
            this.name = name;
            this.value = value;
            this.isId = isId;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public boolean isId() {
            return isId;
        }
    }
}
