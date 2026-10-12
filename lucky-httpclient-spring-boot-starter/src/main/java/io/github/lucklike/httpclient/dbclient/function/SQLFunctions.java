package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.BaseDBApi;
import io.github.lucklike.httpclient.dbclient.annotation.FillScene;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.executor.SQLExecutor;
import io.github.lucklike.httpclient.dbclient.executor.SQLWrapperExecutor;
import io.github.lucklike.httpclient.dbclient.metadata.ColumnMetadata;
import io.github.lucklike.httpclient.dbclient.metadata.EntityMetadataFactory;
import io.github.lucklike.httpclient.dbclient.sql.SQLType;
import io.github.lucklike.httpclient.dbclient.sql.SQLWrapper;
import io.github.lucklike.httpclient.dbclient.sql.SimpleSqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import io.github.lucklike.httpclient.dbclient.sql.lambda.LambdaSqlBuilder;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
     * <p>
     * 本方法是所有 Lambda 链路（lambdaUpdate/lambdaLogicDelete/lambdaRestore/
     * lambdaCondition.toUpdate 等）的执行收口点，在此对 UPDATE 类语句
     * 统一应用 @Column 自动填充（fillScene/fillValue）的审计列 SET 注入（无实体上下文）。
     * </p>
     *
     * @param mc 方法上下文对象，包含方法参数、返回值类型等信息
     * @return SQL执行器，用于执行Lambda构建的SQL语句
     */
    public static SQLExecutor lambdaSql(MethodContext mc) {
        SQLWrapper wrapper = Objects.requireNonNull(mc.getArgument(SQLWrapper.class));
        applyLambdaAuditFill(mc, wrapper);
        return new SQLWrapperExecutor(mc, wrapper);
    }

    /**
     * 对 Lambda 构建器应用 UPDATE 场景的审计列 SET 注入。
     * <p>
     * 仅处理携带实体类型的 {@link LambdaSqlBuilder} 且 SQL 类型为 UPDATE 的构建器；
     * 物理 DELETE 语句（isDeleteStatement）与查询构建器不受影响。
     * Lambda 路径无实体对象，表达式中不可引用 entity 变量。
     * </p>
     *
     * @param mc      方法上下文，供 SpEL 表达式求值
     * @param wrapper 待执行的 SQL 包装器
     */
    private static void applyLambdaAuditFill(MethodContext mc, SQLWrapper wrapper) {
        if (!(wrapper instanceof LambdaSqlBuilder)) {
            return;
        }
        LambdaSqlBuilder<?> builder = (LambdaSqlBuilder<?>) wrapper;
        if (builder.getType() != SQLType.UPDATE) {
            return;
        }
        SqlBuilder inner = builder.getSqlBuilder();
        if (inner.isDeleteStatement()) {
            return; // 物理删除无 SET 子句，不注入
        }
        AuditFillSupport.applyUpdateFill(mc, inner, builder.getEntityClass(), null);
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
        // 标注 @LogicDelete 的实体：自动追加“不等于已删除值”的过滤条件
        EntityUtils.applyLogicDeleteFilter(sqlBuilder, entityClass);
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
        // 标注 @LogicDelete 的实体：自动追加“不等于已删除值”的过滤条件
        EntityUtils.applyLogicDeleteFilter(sqlBuilder, entityClass);
        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    /**
     * 根据Map中的非空值作为条件进行查询
     * <p>遍历Map的所有元素，将值不为空的字段作为WHERE条件构建SELECT语句</p>
     *
     * @param mc 方法上下文对象，包含实体参数信息
     * @return SQL执行器，用于执行根据实体条件查询的SQL语句
     */
    @SuppressWarnings("unchecked")
    public static SQLExecutor selectByMap(MethodContext mc) {
        Map<String, Object> queryMap = (Map<String, Object>) mc.getArguments()[0];
        Class<?> entityClass = mc.getResultResolvableType().getGeneric(0).toClass();
        SqlBuilder sqlBuilder = SqlBuilder.builder().select().from(EntityUtils.getTableName(entityClass));
        queryMap.forEach((key, value) -> {
            if (value != null) {
                sqlBuilder.eq(key, value);
            }
        });
        // 标注 @LogicDelete 的实体：自动追加“不等于已删除值”的过滤条件
        EntityUtils.applyLogicDeleteFilter(sqlBuilder, entityClass);
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
     * 根据主键ID逻辑删除实体
     * <p>将实体@LogicDelete字段的列更新为"已删除值"，不物理删除数据</p>
     * <p><b>注意：</b>主键ID值不能为null，实体必须标注了@LogicDelete字段，否则会抛出异常</p>
     *
     * @param mc 方法上下文对象，包含泛型信息和参数信息
     * @return SQL执行器，用于执行逻辑删除的SQL语句
     * @throws IllegalArgumentException 如果主键ID为null或实体未标注@LogicDelete字段时抛出此异常
     */
    public static SQLExecutor logicDeleteById(MethodContext mc) {
        // 获取主键值
        Object idValue = mc.getArguments()[0];
        if (idValue == null) {
            throw new IllegalArgumentException("Primary key ID value cannot be null in logicDeleteById operation");
        }

        Class<?> entityClass = ResolvableType.forClass(BaseDBApi.class, mc.getClassContext().getCurrentAnnotatedElement()).getGeneric(0).toClass();
        EntityUtils.LogicDeleteRule rule = EntityUtils.getLogicDeleteRule(entityClass);
        if (rule == null) {
            throw new IllegalArgumentException(
                    String.format("Entity [%s] has no @LogicDelete field defined, logicDeleteById requires a @LogicDelete field",
                            entityClass.getName())
            );
        }
        SqlBuilder sqlBuilder = SqlBuilder.builder()
                .update(EntityUtils.getTableName(entityClass))
                .set(rule.getColumnName(), rule.getDeletedValue())
                .eq(EntityUtils.getIdColumn(entityClass, "Entity [" + entityClass.getName() + "] has no @Id field defined, logicDeleteById requires at least one @Id field"), idValue);
        // 逻辑删除本质是 UPDATE，统一应用 UPDATE 场景审计列 SET 注入（无实体上下文）
        AuditFillSupport.applyUpdateFill(mc, sqlBuilder, entityClass, null);
        return new SQLWrapperExecutor(mc, sqlBuilder);
    }

    /**
     * 根据主键ID恢复已逻辑删除的实体
     * <p>将实体@LogicDelete字段的列写回"未删除值"；对未处于删除状态的记录执行恢复不改变其数据</p>
     * <p><b>注意：</b>主键ID值不能为null，实体必须标注了@LogicDelete字段，否则会抛出异常</p>
     *
     * @param mc 方法上下文对象，包含泛型信息和参数信息
     * @return SQL执行器，用于执行恢复的SQL语句
     * @throws IllegalArgumentException 如果主键ID为null或实体未标注@LogicDelete字段时抛出此异常
     */
    public static SQLExecutor restoreById(MethodContext mc) {
        // 获取主键值
        Object idValue = mc.getArguments()[0];
        if (idValue == null) {
            throw new IllegalArgumentException("Primary key ID value cannot be null in restoreById operation");
        }

        Class<?> entityClass = ResolvableType.forClass(BaseDBApi.class, mc.getClassContext().getCurrentAnnotatedElement()).getGeneric(0).toClass();
        EntityUtils.LogicDeleteRule rule = EntityUtils.getLogicDeleteRule(entityClass);
        if (rule == null) {
            throw new IllegalArgumentException(
                    String.format("Entity [%s] has no @LogicDelete field defined, restoreById requires a @LogicDelete field",
                            entityClass.getName())
            );
        }
        SqlBuilder sqlBuilder = SqlBuilder.builder()
                .update(EntityUtils.getTableName(entityClass))
                .set(rule.getColumnName(), rule.getNotDeletedValue())
                .eq(EntityUtils.getIdColumn(entityClass, "Entity [" + entityClass.getName() + "] has no @Id field defined, restoreById requires at least one @Id field"), idValue);
        // 恢复本质是 UPDATE，统一应用 UPDATE 场景审计列 SET 注入（无实体上下文）
        AuditFillSupport.applyUpdateFill(mc, sqlBuilder, entityClass, null);
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

        // UPDATE 场景审计列 SET 注入：实体字段非空的列已由 columnHandler 进入 SET 且不覆盖，
        // 字段为 null 的审计列在此补入（与 Filler 的“仅为 null 填充”规则一致）
        AuditFillSupport.applyUpdateFill(mc, sqlBuilder, entityClass, entity);

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

        // 在执行插入前，对声明 @Column(fillScene = INSERT) 的字段进行回写填充
        AuditFillSupport.applyEntityFill(mc, entity, FillScene.INSERT);

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
     * <p>与单条插入语义对齐：列集合为集合中至少有一条记录取值为非空的字段，
     * 某条记录该字段为空时以 null 占位。</p>
     * <p><b>注意：</b>该方法会为每个字段生成占位符"?"，并使用批量参数方式执行</p>
     *
     * @param mc 方法上下文对象，包含实体集合参数信息
     * @return SQL执行器，用于执行批量插入的SQL语句
     * @throws IllegalArgumentException 如果实体集合为null或空、或没有任何可插入字段时抛出此异常
     */
    public static SQLExecutor batchInsertSql(MethodContext mc) {
        Class<?> entityClass = mc.getParameterContexts()[0].getType().getGeneric(0).toClass();
        Collection<?> entityList = (Collection<?>) mc.getArguments()[0];

        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("Batch insert entity collection must not be null or empty");
        }

        // 在执行批量插入前，对列表中每个实体的 @Column(fillScene = INSERT) 字段进行回写填充
        for (Object entity : entityList) {
            AuditFillSupport.applyEntityFill(mc, entity, FillScene.INSERT);
        }

        // 收集所有参与映射的字段（过滤静态字段与@Column(exist=false)字段）
        List<ColumnMetadata> allColumns = new ArrayList<>();
        for (ColumnMetadata column : EntityMetadataFactory.getMetadata(entityClass).getColumns()) {
            if (column.isStaticField() || !column.isExist()) {
                continue;
            }
            allColumns.add(column);
        }

        // 与单条 insert 对齐：只插入集合中至少有一条记录取值为非空的字段
        List<String> columnNames = new ArrayList<>();
        List<ColumnMetadata> insertColumns = new ArrayList<>();
        for (ColumnMetadata column : allColumns) {
            for (Object entity : entityList) {
                if (FieldUtils.getValue(entity, column.getField()) != null) {
                    insertColumns.add(column);
                    columnNames.add(column.getColumnName());
                    break;
                }
            }
        }
        if (insertColumns.isEmpty()) {
            throw new IllegalArgumentException("Batch insert entity collection has no non-null field to insert");
        }

        // 按列顺序收集每条记录的值，缺失字段以null占位
        List<Object[]> batchParams = new ArrayList<>();
        for (Object entity : entityList) {
            Object[] lineParams = new Object[insertColumns.size()];
            for (int i = 0; i < insertColumns.size(); i++) {
                lineParams[i] = FieldUtils.getValue(entity, insertColumns.get(i).getField());
            }
            batchParams.add(lineParams);
        }

        String sqlTemp = "INSERT INTO %s (%s) VALUES (%s)";
        String iColumn = String.join(",", columnNames);
        String iValue = columnNames.stream().map(n -> "?").collect(Collectors.joining(","));

        String sql = String.format(sqlTemp, EntityUtils.getTableName(entityClass), iColumn, iValue);

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

        // 批量 SQL 的参数从实体字段取值，保留回写式填充（@Column(fillScene = UPDATE)）
        for (Object entity : entityList) {
            AuditFillSupport.applyEntityFill(mc, entity, FillScene.UPDATE);
        }

        // 找出所有带有 @Column 且 exist() 为 true 的字段，并区分 ID 字段和普通字段
        List<ColumnMetadata> idColumns = new ArrayList<>();
        List<ColumnMetadata> normalColumns = new ArrayList<>();

        for (ColumnMetadata column : EntityMetadataFactory.getMetadata(entityClass).getColumns()) {
            if (column.isStaticField() || !column.isExist()) {
                continue;
            }
            // 判断是否为 ID 字段
            if (column.isId()) {
                idColumns.add(column);
            } else {
                normalColumns.add(column);
            }
        }

        if (idColumns.isEmpty()) {
            throw new IllegalArgumentException("Entity [" + entityClass.getName() + "] has no @Id field defined, batchUpdateById requires at least one @Id field");
        }

        // 构建列名（用于 SET 和 WHERE）
        String tableName = EntityUtils.getTableName(entityClass);
        List<String> setColumns = new ArrayList<>();
        List<String> whereColumns = new ArrayList<>();

        for (ColumnMetadata column : normalColumns) {
            setColumns.add(column.getColumnName() + " = ?");
        }
        for (ColumnMetadata column : idColumns) {
            whereColumns.add(column.getColumnName() + " = ?");
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
            for (ColumnMetadata column : normalColumns) {
                paramList.add(FieldUtils.getValue(entity, column.getField()));
            }
            // WHERE 部分的值
            for (ColumnMetadata column : idColumns) {
                paramList.add(FieldUtils.getValue(entity, column.getField()));
            }
            batchParams.add(paramList.toArray());
        }

        return new SQLWrapperExecutor(mc, SimpleSqlBuilder.ofBatch(finalSql, batchParams));
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
        columnHandler(null, entity, consumer);
    }

    /**
     * 处理实体对象的所有字段，提取列信息
     * <p>遍历实体的所有非静态字段，过滤掉@Column(exist=false)的字段，
     * 并将每个字段封装为ColumnInfo对象后传递给消费者处理</p>
     *
     * @param mc       方法上下文，为null时使用默认方式创建Condition实例
     * @param entity   实体对象
     * @param consumer 列信息消费者，用于处理每个字段的列信息
     */
    private static void columnHandler(MethodContext mc, Object entity, Consumer<ColumnInfo> consumer) {
        for (ColumnMetadata column : EntityMetadataFactory.getMetadata(entity.getClass()).getColumns()) {
            if (column.isStaticField() || !column.isExist()) {
                continue;
            }

            Object fieldValue = FieldUtils.getValue(entity, column.getField());
            Class<? extends Condition> conditionClass = column.getConditionClass();
            Condition condition = mc == null
                    ? ClassUtils.newObject(conditionClass)
                    : mc.generateObject(conditionClass, Scope.SINGLETON);

            consumer.accept(new ColumnInfo(column.getColumnName(), fieldValue, column.isId(), condition));
        }
    }
}