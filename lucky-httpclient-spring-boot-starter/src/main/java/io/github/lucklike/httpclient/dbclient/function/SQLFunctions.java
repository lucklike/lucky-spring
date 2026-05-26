package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.StringUtils;
import com.luckyframework.common.TempPair;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.annotation.Column;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.executor.SQLExecutor;
import io.github.lucklike.httpclient.dbclient.sql.SQLWrapper;
import io.github.lucklike.httpclient.dbclient.executor.SQLWrapperExecutor;
import io.github.lucklike.httpclient.dbclient.sql.SimpleSqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    public static SQLExecutor selectByEntity(MethodContext mc) {
        Object entity = mc.getArguments()[0];
        Class<?> entityClass = entity.getClass();

        SqlBuilder sqlBuilder = SqlBuilder.builder().select().from(EntityUtils.getTableName(entityClass));
        columnHandler(entity, co -> {
            if (co.getValue() != null) {
                sqlBuilder.eq(co.getName(), co.getValue());
            }
        });
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
        final AtomicBoolean hasId = new AtomicBoolean(false);
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
            throw new IllegalArgumentException("Entity [" + entityClass.getName() + "] has no @Id field defined, updateById requires at least one @Id field");
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

    public static SQLExecutor batchInsertSql(MethodContext mc) {
        Class<?> entityClass = mc.getParameterContexts()[0].getType().getGeneric(0).toClass();
        Collection<?> entityObject = (Collection<?>) mc.getArguments()[0];

        List<String> columnNames = new ArrayList<>();
        List<List<Object>> valuesList = new ArrayList<>();

        String sqlTemp = "INSERT INTO %s (%s) VALUES (%s)";

        for (Field field : ClassUtils.getAllFields(entityClass)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Column columnAnn = AnnotationUtils.findMergedAnnotation(field, Column.class);
            if (columnAnn != null && !columnAnn.exist()) {
                continue;
            }

            String columnName = (columnAnn != null && StringUtils.hasText(columnAnn.value())) ? columnAnn.value() : field.getName();
            columnNames.add(columnName);
            int i = 1;
            for (Object entity : entityObject) {
                List<Object> values;
                if (valuesList.size() < i) {
                    values = new ArrayList<>();
                    valuesList.add(values);
                } else {
                    values =  valuesList.get(i - 1);
                }
                values.add(FieldUtils.getValue(entity, field));
                i++;
            }
        }
        String iColumn =  String.join(",", columnNames);
        String iValue = columnNames.stream().map(n -> "?").collect(Collectors.joining(","));

        String sql =  String.format(sqlTemp, EntityUtils.getTableName(entityClass),iColumn, iValue);
        List<Object[]> batchParams = valuesList.stream().filter(Objects::nonNull).map(list -> list.toArray(new Object[0])).collect(Collectors.toList());


        return new SQLWrapperExecutor(mc, SimpleSqlBuilder.ofBatch(sql, batchParams));
    }

    public static SQLExecutor batchUpdateById(MethodContext mc) {
        // 获取实体类型和集合
        Class<?> entityClass = mc.getParameterContexts()[0].getType().getGeneric(0).toClass();
        Collection<?> entityList = (Collection<?>) mc.getArguments()[0];
        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("Batch update entity collection must not be null or empty");
        }

        // 找出所有带有 @Column 且 exist() 为 true 的字段，并区分 ID 字段和普通字段
        List<Field> idFields = new ArrayList<>();
        List<Field> normalFields = new ArrayList<>();

        for (Field field : ClassUtils.getAllFields(entityClass)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Column columnAnn = AnnotationUtils.findMergedAnnotation(field, Column.class);
            if (columnAnn != null && !columnAnn.exist()) {
                continue;
            }
            // 判断是否为 ID 字段
            Id idAnn = AnnotationUtils.findMergedAnnotation(field, Id.class);
            if (idAnn != null) {
                idFields.add(field);
            } else {
                normalFields.add(field);
            }
        }

        if (idFields.isEmpty()) {
            throw new IllegalArgumentException("Entity [" + entityClass.getName() + "] has no @Id field defined, batchUpdateById requires at least one @Id field");
        }

        // 构建列名（用于 SET 和 WHERE）
        String tableName = EntityUtils.getTableName(entityClass);
        List<String> setColumns = new ArrayList<>();
        List<String> whereColumns = new ArrayList<>();

        for (Field field : normalFields) {
            String columnName = getColumnName(field);
            setColumns.add(columnName + " = ?");
        }
        for (Field field : idFields) {
            String columnName = getColumnName(field);
            whereColumns.add(columnName + " = ?");
        }

        if (setColumns.isEmpty()) {
            throw new IllegalArgumentException("Entity [" + entityClass.getName() + "] has no updatable column except @Id fields");
        }

        String sqlTemplate = "UPDATE %s SET %s WHERE %s";
        String setClause = String.join(", ", setColumns);
        String whereClause = String.join(" AND ", whereColumns);
        String finalSql = String.format(sqlTemplate, tableName, setClause, whereClause);

        // 准备批量参数：每条记录按顺序包含 [普通字段值, ID字段值]
        List<Object[]> batchParams = new ArrayList<>();
        for (Object entity : entityList) {
            List<Object> paramList = new ArrayList<>();
            // SET 部分的值
            for (Field field : normalFields) {
                paramList.add(FieldUtils.getValue(entity, field));
            }
            // WHERE 部分的值
            for (Field field : idFields) {
                paramList.add(FieldUtils.getValue(entity, field));
            }
            batchParams.add(paramList.toArray());
        }

        return new SQLWrapperExecutor(mc, SimpleSqlBuilder.ofBatch(finalSql, batchParams));
    }

    // 辅助方法：获取字段对应的列名
    private static String getColumnName(Field field) {
        Column columnAnn = AnnotationUtils.findMergedAnnotation(field, Column.class);
        if (columnAnn != null && StringUtils.hasText(columnAnn.value())) {
            return columnAnn.value();
        }
        // 如果 @Id 本身有 value，也会通过 @AliasFor 传递到 @Column.value()
        Id idAnn = AnnotationUtils.findMergedAnnotation(field, Id.class);
        if (idAnn != null && StringUtils.hasText(idAnn.value())) {
            return idAnn.value();
        }
        return field.getName();
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
