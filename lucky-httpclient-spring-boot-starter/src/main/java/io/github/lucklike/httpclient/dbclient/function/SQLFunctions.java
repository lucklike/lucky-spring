package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.annotation.Column;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.executor.SQLExecutor;
import io.github.lucklike.httpclient.dbclient.executor.SQLWrapperExecutor;
import io.github.lucklike.httpclient.dbclient.sql.SQLWrapper;
import io.github.lucklike.httpclient.dbclient.sql.SimpleSqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * SQL拼接相关的函数，提供动态SQL构建和数据库操作的支持
 * <p>该类包含了一系列用于构建SQL语句的静态方法，支持条件拼接、增删改查等操作</p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 03:09
 */
public class SQLFunctions {

    private static final String SQL_AND = "AND";
    private static final String SQL_OR = "OR";
    private static final String SQL_IN = "IN";

    /**
     * 构建AND条件连接的SQL片段
     * <p>如果传入的obj对象为null或空字符串，则返回空字符串，否则返回" AND {sql}"</p>
     *
     * @param sql SQL条件语句
     * @param obj 条件值对象，用于判断是否需要拼接该条件
     * @return 拼接后的SQL条件片段，如果obj无效则返回空字符串
     */
    @FunctionAlias(SQL_AND)
    public static String and(String sql, Object obj) {
        return sql(SQL_AND, sql, obj);
    }

    /**
     * 构建OR条件连接的SQL片段
     * <p>如果传入的obj对象为null或空字符串，则返回空字符串，否则返回" OR {sql}"</p>
     *
     * @param sql SQL条件语句
     * @param obj 条件值对象，用于判断是否需要拼接该条件
     * @return 拼接后的SQL条件片段，如果obj无效则返回空字符串
     */
    @FunctionAlias(SQL_OR)
    public static String or(String sql, Object obj) {
        return sql(SQL_OR, sql, obj);
    }

    /**
     * 构建IN条件连接的SQL片段
     * <p>如果传入的obj对象为null或空字符串，则返回空字符串，否则返回" IN {sql}"</p>
     *
     * @param sql SQL条件语句
     * @param obj 条件值对象，用于判断是否需要拼接该条件
     * @return 拼接后的SQL条件片段，如果obj无效则返回空字符串
     */
    @FunctionAlias(SQL_IN)
    public static String in(String sql, Object obj) {
        return sql(SQL_IN, sql, obj);
    }

    /**
     * 通用SQL条件拼接方法
     *
     * @param linkSymbol 连接符号（AND/OR/IN）
     * @param sql        SQL条件语句
     * @param obj        条件值对象
     * @return 拼接后的SQL条件片段
     */
    private static String sql(String linkSymbol, String sql, Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String && !StringUtils.hasText((String) obj)) {
            return "";
        }
        return String.format(" %s %s", linkSymbol, sql);
    }

    /**
     * 执行Lambda表达式构建的SQL
     * <p>从方法参数中获取SQLWrapper对象，并将其包装为SQLExecutor执行器</p>
     *
     * @param mc 方法上下文对象，包含方法参数、返回值类型等信息
     * @return SQL执行器，用于执行Lambda构建的SQL语句
     */
    public static SQLExecutor lambdaSql(MethodContext mc) {
        return new SQLWrapperExecutor(mc, Objects.requireNonNull(mc.getArgument(SQLWrapper.class)));
    }

    /**
     * 根据主键ID查询实体
     * <p>自动从方法返回值类型中获取实体类，从参数中获取主键值，构建SELECT语句</p>
     * <p><b>注意：</b>主键ID值不能为null，否则会抛出异常</p>
     *
     * @param mc 方法上下文对象，包含返回值类型和参数信息
     * @return SQL执行器，用于执行根据ID查询的SQL语句
     * @throws IllegalArgumentException 如果主键ID为null或实体中没有@Id字段时抛出此异常
     */
    public static SQLExecutor selectById(MethodContext mc) {
        // 获取主键值
        Object idValue = mc.getArguments()[0];
        if (idValue == null) {
            throw new IllegalArgumentException("Primary key ID value cannot be null in selectById operation");
        }

        Class<?> entityClass = mc.getResultResolvableType().toClass();
        SqlBuilder sqlBuilder = SqlBuilder.builder()
                .select()
                .from(EntityUtils.getTableName(entityClass))
                .eq(EntityUtils.getIdColumn(entityClass, "Entity [" + entityClass.getName() + "] has no @Id field defined, selectById requires at least one @Id field"), idValue);
        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    /**
     * 根据实体对象中的非空字段作为条件进行查询
     * <p>遍历实体对象的所有字段，将值不为空的字段作为WHERE条件构建SELECT语句</p>
     *
     * @param mc 方法上下文对象，包含实体参数信息
     * @return SQL执行器，用于执行根据实体条件查询的SQL语句
     */
    public static SQLExecutor selectByEntity(MethodContext mc) {
        Object entity = mc.getArguments()[0];
        Class<?> entityClass = entity.getClass();

        SqlBuilder sqlBuilder = SqlBuilder.builder().select().from(EntityUtils.getTableName(entityClass));
        columnHandler(mc, entity, co -> {
            if (co.getValue() != null) {
                co.getCondition().additionCondition(sqlBuilder, co);
            }
        });
        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    /**
     * 根据主键ID删除实体
     * <p>自动从BaseDBApi的泛型参数中获取实体类，从方法参数中获取主键值，构建DELETE语句</p>
     *
     * @param mc 方法上下文对象，包含泛型信息和参数信息
     * @return SQL执行器，用于执行根据ID删除的SQL语句
     */
    public static SQLExecutor deleteById(MethodContext mc) {
        // 获取主键值
        Object idValue = mc.getArguments()[0];
        if (idValue == null) {
            throw new IllegalArgumentException("Primary key ID value cannot be null in deleteById operation");
        }

        Class<?> entityClass = ResolvableType.forClass(BaseDBApi.class, mc.getClassContext().getCurrentAnnotatedElement()).getGeneric(0).toClass();
        SqlBuilder sqlBuilder = SqlBuilder.builder()
                .delete()
                .from(EntityUtils.getTableName(entityClass))
                .eq(EntityUtils.getIdColumn(entityClass, "Entity [" + entityClass.getName() + "] has no @Id field defined, deleteById requires at least one @Id field"), idValue);
        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    /**
     * 根据主键ID更新实体
     * <p>遍历实体对象的所有字段，将@Id注解的字段作为WHERE条件，其他非空字段作为SET子句构建UPDATE语句</p>
     * <p><b>注意：</b></p>
     * <ul>
     *     <li>实体中必须至少有一个字段标注了@Id注解</li>
     *     <li>ID字段的值不能为null</li>
     * </ul>
     *
     * @param mc 方法上下文对象，包含实体参数信息
     * @return SQL执行器，用于执行根据ID更新的SQL语句
     * @throws IllegalArgumentException 如果实体中没有@Id字段或所有ID字段的值都为null时抛出此异常
     */
    public static SQLExecutor updateById(MethodContext mc) {
        Object entity = mc.getArguments()[0];
        Class<?> entityClass = entity.getClass();

        // 分别记录是否有ID字段、是否有有效的ID值
        final AtomicBoolean hasIdField = new AtomicBoolean(false);
        final AtomicBoolean hasValidIdValue = new AtomicBoolean(false);
        // 记录第一个ID字段的名称，用于错误提示
        final String[] idFieldName = new String[1];

        SqlBuilder sqlBuilder = SqlBuilder.builder().update(EntityUtils.getTableName(entityClass));

        columnHandler(mc, entity, co -> {
            if (co.isId()) {
                hasIdField.set(true);
                if (idFieldName[0] == null) {
                    idFieldName[0] = co.getName();
                }
                if (co.getValue() != null) {
                    sqlBuilder.eq(co.getName(), co.getValue());
                    hasValidIdValue.set(true);
                }
            } else if (co.getValue() != null) {
                sqlBuilder.set(co.getName(), co.getValue());
            }
        });

        // 情况1：没有ID字段
        if (!hasIdField.get()) {
            throw new IllegalArgumentException(
                    String.format("Entity [%s] has no @Id field defined, updateById requires at least one @Id field",
                            entityClass.getName())
            );
        }

        // 情况2：有ID字段但值都为null
        if (!hasValidIdValue.get()) {
            throw new IllegalArgumentException(
                    String.format("Entity [%s] has @Id field(s) but all ID values are null, " +
                                    "cannot execute updateById operation. Please ensure at least one ID field has a non-null value. " +
                                    "First ID field name: [%s]",
                            entityClass.getName(), idFieldName[0])
            );
        }

        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    /**
     * 插入单个实体
     * <p>遍历实体对象的所有字段，将值不为空的字段作为插入列和值构建INSERT语句</p>
     *
     * @param mc 方法上下文对象，包含实体参数信息
     * @return SQL执行器，用于执行插入实体的SQL语句
     */
    public static SQLExecutor insertSql(MethodContext mc) {
        Object entity = mc.getArguments()[0];
        Class<?> entityClass = entity.getClass();

        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        columnHandler(mc, entity, co -> {
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

    /**
     * 批量插入实体集合
     * <p>遍历实体类的所有字段和实体集合，构建批量INSERT语句</p>
     * <p><b>注意：</b>该方法会为每个字段生成占位符"?"，并使用批量参数方式执行</p>
     *
     * @param mc 方法上下文对象，包含实体集合参数信息
     * @return SQL执行器，用于执行批量插入的SQL语句
     */
    public static SQLExecutor batchInsertSql(MethodContext mc) {
        Class<?> entityClass = mc.getParameterContexts()[0].getType().getGeneric(0).toClass();
        Collection<?> entityList = (Collection<?>) mc.getArguments()[0];

        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("Batch insert entity collection must not be null or empty");
        }

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
            for (Object entity : entityList) {
                List<Object> values;
                if (valuesList.size() < i) {
                    values = new ArrayList<>();
                    valuesList.add(values);
                } else {
                    values = valuesList.get(i - 1);
                }
                values.add(FieldUtils.getValue(entity, field));
                i++;
            }
        }
        String iColumn = String.join(",", columnNames);
        String iValue = columnNames.stream().map(n -> "?").collect(Collectors.joining(","));

        String sql = String.format(sqlTemp, EntityUtils.getTableName(entityClass), iColumn, iValue);
        List<Object[]> batchParams = valuesList.stream().filter(Objects::nonNull).map(list -> list.toArray(new Object[0])).collect(Collectors.toList());

        return new SQLWrapperExecutor(mc, SimpleSqlBuilder.ofBatch(sql, batchParams));
    }

    /**
     * 批量根据主键ID更新实体集合
     * <p>遍历实体集合，将所有非@Id且exist()为true的字段作为SET子句，
     *
     * @param mc 方法上下文对象，包含实体集合参数信息
     * @return SQL执行器，用于执行批量根据ID更新的SQL语句
     * @throws IllegalArgumentException 如果实体集合为空、没有@Id字段或没有可更新字段时抛出此异常
     * {@link Id @Id}字段作为WHERE条件构建批量UPDATE语句</p> <p><b>注意：</b></p>
     * <ul>
     *     <li>实体集合不能为null或空</li>
     *     <li>实体中必须至少有一个字段标注了@Id注解</li>
     *     <li>实体中必须至少有一个可更新的非ID字段</li>
     *     <li>支持复合主键（多个@Id字段）</li>
     * </ul>
     */
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

    /**
     * 获取字段对应的数据库列名
     * <p>优先级：@Column.value() > @Id.value() > 字段名</p>
     *
     * @param field 字段对象
     * @return 数据库列名
     */
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

    /**
     * 处理实体对象的所有字段，提取列信息
     * <p>遍历实体的所有非静态字段，过滤掉@Column(exist=false)的字段，
     * 并将每个字段封装为ColumnInfo对象后传递给消费者处理</p>
     *
     * @param mc       方法上下文
     * @param entity   实体对象
     * @param consumer 列信息消费者，用于处理每个字段的列信息
     */
    private static void columnHandler(MethodContext mc, Object entity, Consumer<ColumnInfo> consumer) {
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
            Condition condition =
                    columnAnn == null
                            ? mc.generateObject(Condition.Eq.class, Scope.SINGLETON)
                            : mc.generateObject(columnAnn.condition(), Scope.SINGLETON);
            if (AnnotationUtils.isAnnotated(field, Id.class)) {
                columnInfo = new ColumnInfo(columnName, fieldValue, true, condition);
            } else {
                columnInfo = new ColumnInfo(columnName, fieldValue, false, condition);
            }

            consumer.accept(columnInfo);
        }
    }

    /**
     * 处理实体对象的所有字段，提取列信息
     * <p>遍历实体的所有非静态字段，过滤掉@Column(exist=false)的字段，
     * 并将每个字段封装为ColumnInfo对象后传递给消费者处理</p>
     *
     * @param entity   实体对象
     * @param consumer 列信息消费者，用于处理每个字段的列信息
     */
    public static void columnHandler(Object entity, Consumer<ColumnInfo> consumer) {
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
            Condition condition =
                    columnAnn == null
                            ? ClassUtils.newObject(Condition.Eq.class)
                            : ClassUtils.newObject(columnAnn.condition());
            if (AnnotationUtils.isAnnotated(field, Id.class)) {
                columnInfo = new ColumnInfo(columnName, fieldValue, true, condition);
            } else {
                columnInfo = new ColumnInfo(columnName, fieldValue, false, condition);
            }

            consumer.accept(columnInfo);
        }
    }

}