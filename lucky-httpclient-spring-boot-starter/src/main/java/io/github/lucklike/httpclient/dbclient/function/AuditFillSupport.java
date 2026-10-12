package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.annotation.FillScene;
import io.github.lucklike.httpclient.dbclient.metadata.ColumnMetadata;
import io.github.lucklike.httpclient.dbclient.metadata.EntityMetadataFactory;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 审计字段自动填充支撑工具（SQL 构建层统一注入引擎）。
 * <p>
 * 本类是 @Column 自动填充（fillScene/fillValue）功能的执行核心，提供两种采集方式：
 * </p>
 * <ul>
 *     <li><b>回写式</b>（{@link #applyEntityFill}）：用于 INSERT 场景与批量更新，
 *     求值结果回写实体字段，由后续列采集逻辑从实体取值进入 SQL；</li>
 *     <li><b>SET 注入式</b>（{@link #applyUpdateFill}）：用于所有 UPDATE 类构建路径
 *     （updateById、Lambda 更新、逻辑删除/恢复等），求值结果直接注入
 *     {@link SqlBuilder} 的 SET 子句，不依赖实体对象——这是 Lambda 链路
 *     （lambdaUpdate().set(...).update() 等无实体操作）自动填充生效的关键。</li>
 * </ul>
 * <p>
 * 统一填充规则（与 {@link DefaultSpELFiller} 共同保证）：
 * <ul>
 *     <li>实体模式：字段为 null 才填充，已有值时保留用户显式赋值，不覆盖；</li>
 *     <li>Lambda 模式：已显式 set 的列保持用户值，未 set 的审计列才自动注入。</li>
 * </ul>
 * 注入去重补充规则：非表字段（@Column(exist = false)）与 @Id 字段不参与注入。
 * </p>
 * <p>
 * 所有方法均吞掉填充异常（仅记录日志），保证填充失败不阻断 SQL 执行。
 * </p>
 *
 * @author fukang
 * @version 2.0.0
 * @date 2026/10/12
 * @see FillRegistry
 * @see Filler
 */
public final class AuditFillSupport {

    private static final Logger log = LoggerFactory.getLogger(AuditFillSupport.class);

    private AuditFillSupport() {
        // 工具类，禁止实例化
    }

    /**
     * 回写式填充：执行已注册的 Filler 并将结果回写到实体字段。
     * <p>
     * 用于 INSERT 场景（insertSql/batchInsertSql）与批量更新（batchUpdateById），
     * 这些路径的列清单或参数从实体字段取值，必须先行回写。
     * </p>
     *
     * @param mc     方法上下文，供 Filler 解析 SpEL 表达式
     * @param entity 实体对象
     * @param scene  填充场景（INSERT 或 UPDATE）
     */
    public static void applyEntityFill(MethodContext mc, Object entity, FillScene scene) {
        if (entity == null || !FillRegistry.hasHandlers()) {
            return;
        }

        Class<?> entityClass = entity.getClass();
        try {
            List<Filler> fillers = FillRegistry.getFillers(entityClass, scene);
            // 依次执行每个 Filler（回写由 Filler 实现内部完成）
            for (Filler filler : fillers) {
                try {
                    filler.fill(mc, entity, entityClass, scene);
                } catch (Exception e) {
                    // 单个 Filler 失败不中断整体流程，记录错误日志后继续
                    log.error("[AuditFill] Filler '{}' execution failed for entity '{}' in scene '{}'",
                            filler.getClass().getName(), entityClass.getName(), scene, e);
                }
            }
        } catch (Exception e) {
            // 全局异常捕获，确保填充逻辑不会干扰正常 SQL 生成
            log.error("[AuditFill] Failed to fill entity '{}' in scene '{}'",
                    entityClass.getName(), scene, e);
        }
    }

    /**
     * SET 注入式填充：执行已注册的 Filler 并将结果直接注入 SqlBuilder 的 SET 子句。
     * <p>
     * 用于所有 UPDATE 类构建路径的统一收口：
     * <ul>
     *     <li>updateById：传入实体，表达式可用 {@code entity} 变量，
     *     字段已有值的列由 Filler 直接跳过（不覆盖用户赋值，也不回写实体）；</li>
     *     <li>Lambda 更新（lambdaUpdate/lambdaCondition.toUpdate）、逻辑删除、恢复：
     *     无实体对象（entity 传 null），表达式仅可用 entityClass/scene/methodName 变量。</li>
     * </ul>
     * </p>
     *
     * @param mc          方法上下文，供 Filler 解析 SpEL 表达式
     * @param sqlBuilder  已完成用户侧构建的 SQL 构建器（UPDATE 语句）
     * @param entityClass 实体类型（Lambda 构建器携带）
     * @param entity      实体对象，可为 null（无实体路径将自动构造主键探针实体）
     */
    public static void applyUpdateFill(MethodContext mc, SqlBuilder sqlBuilder,
                                       Class<?> entityClass, Object entity) {
        if (mc == null || sqlBuilder == null || entityClass == null || !FillRegistry.hasHandlers()) {
            return;
        }

        // 该实体类在 UPDATE 场景下的审计列（无标注时快速返回）
        List<ColumnMetadata> fillColumns =
                EntityMetadataFactory.getMetadata(entityClass).getFillColumns(FillScene.UPDATE);
        if (fillColumns.isEmpty()) {
            return;
        }

        // 无实体路径（Lambda 更新、逻辑删除等）：构造探针实体，
        // 从 WHERE 等值条件提取主键值，使表达式中的 entity.id 仍可用（其余字段为 null）
        if (entity == null) {
            entity = buildProbeEntity(entityClass, sqlBuilder);
        }

        // 依次执行 Filler，合并填充结果（fieldName -> value），后注册者覆盖同名值
        Map<String, Object> filledValues = collectFillValues(mc, entity, entityClass, fillersOf(entityClass));
        if (filledValues.isEmpty()) {
            return;
        }

        Set<String> alreadySet = sqlBuilder.getSetColumns();
        for (ColumnMetadata column : fillColumns) {
            // 非表字段与主键列不参与 SET 注入
            if (!column.isFill() || !column.isExist() || column.isId()) {
                continue;
            }
            Object value = filledValues.get(column.getFieldName());
            if (value == null) {
                continue;
            }
            String columnName = column.getColumnName();
            // 用户已显式 set 的列保持原值，不重复注入（Lambda 模式下即“set 优先”）
            if (alreadySet.contains(columnName)) {
                continue;
            }
            sqlBuilder.set(columnName, value);
        }
    }

    private static List<Filler> fillersOf(Class<?> entityClass) {
        return FillRegistry.getFillers(entityClass, FillScene.UPDATE);
    }

    /**
     * 构造主键探针实体（无实体路径专用）。
     * <p>
     * 新建一个空实体实例，并从 WHERE 子句中提取各主键列的等值条件参数回填，
     * 使 {@code #{entity.id}} 类表达式在 Lambda 更新/逻辑删除等无实体场景下依然可用。
     * 非主键字段均为 null；主键提取失败（无等值条件）时对应字段保持 null。
     * </p>
     *
     * @param entityClass 实体类型
     * @param sqlBuilder  已构建的 SQL 构建器（含 WHERE 条件）
     * @return 探针实体，创建失败时返回 null
     */
    private static Object buildProbeEntity(Class<?> entityClass, SqlBuilder sqlBuilder) {
        try {
            Object probe = ClassUtils.newObject(entityClass);
            List<ColumnMetadata> idColumns =
                    EntityMetadataFactory.getMetadata(entityClass).getIdColumns();
            for (ColumnMetadata idColumn : idColumns) {
                Object value = sqlBuilder.findEqValue(idColumn.getColumnName());
                if (value == null) {
                    continue;
                }
                Field field = idColumn.getField();
                if (field.getType().isInstance(value)) {
                    FieldUtils.setValue(probe, field, value);
                }
                // 类型不匹配时不回填（主键值类型通常由实体定义决定，不做激进转换）
            }
            return probe;
        } catch (Exception e) {
            log.error("[AuditFill] Failed to build probe entity for '{}'", entityClass.getName(), e);
            return null;
        }
    }

    /**
     * 遍历所有 Filler 并合并其返回的字段映射。
     *
     * @param mc          方法上下文
     * @param entity      实体对象，可为 null
     * @param entityClass 实体类型
     * @param fillers     填充器列表
     * @return 合并后的字段名 -> 填充值映射
     */
    private static Map<String, Object> collectFillValues(MethodContext mc, Object entity,
                                                         Class<?> entityClass, List<Filler> fillers) {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (Filler filler : fillers) {
            try {
                Map<String, Object> result = filler.fill(mc, entity, entityClass, FillScene.UPDATE);
                if (result != null) {
                    merged.putAll(result);
                }
            } catch (Exception e) {
                // 单个 Filler 失败不中断整体流程
                log.error("[AuditFill] Filler '{}' execution failed for entity '{}' in scene 'UPDATE'",
                        filler.getClass().getName(), entityClass.getName(), e);
            }
        }
        return merged;
    }
}
