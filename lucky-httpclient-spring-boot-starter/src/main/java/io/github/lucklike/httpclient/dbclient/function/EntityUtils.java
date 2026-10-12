package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.annotation.LogicDelete;
import io.github.lucklike.httpclient.dbclient.metadata.ColumnMetadata;
import io.github.lucklike.httpclient.dbclient.metadata.EntityMetadataFactory;
import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class EntityUtils {

    private static final Map<Class<?>, List<IdField>> idFieldListMap = new ConcurrentHashMap<>();

    /**
     * 逻辑删除规则缓存（未标注实体的缓存值采用哨兵对象，因 ConcurrentHashMap 不允许 null 值）
     */
    private static final Map<Class<?>, Object> logicDeleteRuleMap = new ConcurrentHashMap<>();

    /**
     * 未标注逻辑删除字段的缓存哨兵
     */
    private static final Object NO_LOGIC_DELETE_RULE = new Object();

    public static String getIdColumn(Class<?> clazz, String notIdErrorMsg) {
        List<ColumnMetadata> idColumns = EntityMetadataFactory.getMetadata(clazz).getIdColumns();
        if (idColumns.isEmpty()) {
            throw new IllegalArgumentException(notIdErrorMsg);
        }
        return idColumns.get(0).getColumnName();
    }

    public static String getTableName(Class<?> clazz) {
        return EntityMetadataFactory.getMetadata(clazz).getTableName();
    }

    /**
     * 获取实体类中所有被 {@link Id @Id} 标注的字段（支持复合主键），结果会被缓存
     *
     * @param clazz 实体类类型
     * @return ID 字段列表，没有 @Id 字段时返回空列表
     */
    public static List<IdField> getIdFields(Class<?> clazz) {
        return idFieldListMap.computeIfAbsent(clazz, _c -> {
            List<IdField> idFields = new ArrayList<>();
            for (ColumnMetadata idColumn : EntityMetadataFactory.getMetadata(clazz).getIdColumns()) {
                idFields.add(IdField.of(idColumn.getField(), idColumn.getIdType()));
            }
            return Collections.unmodifiableList(idFields);
        });
    }

    /**
     * 获取实体对象的第一个非空 ID 值
     *
     * @param entity 实体对象
     * @return 第一个非空 ID 值，没有非空 ID 字段时返回 null
     */
    public static Object getIdValue(Object entity) {
        for (IdField idField : getIdFields(entity.getClass())) {
            Object value = FieldUtils.getValue(entity, idField.getField());
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获取实体类中第一个 {@link Id @Id} 字段
     *
     * @param clazz 实体类类型
     * @return 第一个 ID 字段，没有 @Id 字段时返回 {@link IdField#NULL}
     */
    public static IdField getAutoIncrementIdField(Class<?> clazz) {
        List<IdField> idFields = getIdFields(clazz);
        return idFields.isEmpty() ? IdField.NULL : idFields.get(0);
    }

    /**
     * 获取实体类的逻辑删除规则（带缓存）
     * <p>
     * 规则来源：实体中标注了 {@link LogicDelete @LogicDelete} 的字段。
     * 列名由既有列映射规则决定；已删除值/未删除值交由框架
     * {@link ConversionUtils} 按字段类型通用转换，不限定字段类型。
     * </p>
     *
     * @param clazz 实体类类型
     * @return 逻辑删除规则；实体未标注逻辑删除字段时返回 null（调用方走原逻辑）
     * @throws IllegalArgumentException 存在多个逻辑删除字段、取值无法转换为字段类型，或两值转换后相同（逻辑删除无效）时
     */
    public static LogicDeleteRule getLogicDeleteRule(Class<?> clazz) {
        Object cached = logicDeleteRuleMap.computeIfAbsent(clazz, _c -> {
            LogicDeleteRule rule = resolveLogicDeleteRule(_c);
            return rule == null ? NO_LOGIC_DELETE_RULE : rule;
        });
        return cached == NO_LOGIC_DELETE_RULE ? null : (LogicDeleteRule) cached;
    }

    /**
     * 将实体类的逻辑删除过滤规则应用到 SQL 构建器（公共启用钩子）
     * <p>
     * 标注了 {@link LogicDelete @LogicDelete} 的实体：将规则列名与已删除值置入构建器，
     * 使其渲染的 SELECT 语句自动追加“不等于已删除值”的条件；未标注实体为 no-op，行为与变更前一致。
     * </p>
     *
     * @param sqlBuilder 目标 SQL 构建器
     * @param clazz      实体类类型
     */
    public static void applyLogicDeleteFilter(SqlBuilder sqlBuilder, Class<?> clazz) {
        LogicDeleteRule rule = getLogicDeleteRule(clazz);
        if (rule != null) {
            sqlBuilder.applyLogicDeleteFilter(rule.getColumnName(), rule.getDeletedValue());
        }
    }

    /**
     * 解析实体类的逻辑删除规则（不做缓存，配置错误时每次调用均抛出）
     */
    private static LogicDeleteRule resolveLogicDeleteRule(Class<?> clazz) {
        List<ColumnMetadata> logicDeleteColumns = EntityMetadataFactory.getMetadata(clazz).getLogicDeleteColumns();
        if (logicDeleteColumns.isEmpty()) {
            return null;
        }
        if (logicDeleteColumns.size() > 1) {
            List<String> names = new ArrayList<>(logicDeleteColumns.size());
            for (ColumnMetadata column : logicDeleteColumns) {
                names.add(column.getColumnName());
            }
            throw new IllegalArgumentException(String.format(
                    "Entity [%s] has multiple @LogicDelete fields defined, at most one is allowed: %s",
                    clazz.getName(), names));
        }

        ColumnMetadata column = logicDeleteColumns.get(0);
        Object deletedValue = convertLogicDeleteValue(clazz, column, column.getLogicDeleteDeletedValue(), "deletedValue");
        Object notDeletedValue = convertLogicDeleteValue(clazz, column, column.getLogicDeleteNotDeletedValue(), "notDeletedValue");
        if (Objects.equals(deletedValue, notDeletedValue)) {
            throw new IllegalArgumentException(String.format(
                    "Entity [%s] @LogicDelete field [%s] deletedValue [%s] and notDeletedValue [%s] are the same after type conversion, logical delete will not work",
                    clazz.getName(), column.getFieldName(), column.getLogicDeleteDeletedValue(), column.getLogicDeleteNotDeletedValue()));
        }
        return new LogicDeleteRule(column.getColumnName(), deletedValue, notDeletedValue);
    }

    /**
     * 将 @LogicDelete 配置的原始字符串转换为字段类型
     * <p>
     * 委托框架 {@link ConversionUtils#conversion(Object, ResolvableType)} 完成，
     * 不对字段类型做白名单限制（String、数值、boolean 等均可），
     * 转换失败时包装为带实体/字段/属性上下文的 IllegalArgumentException。
     * </p>
     *
     * @param clazz     实体类（用于错误提示）
     * @param column    逻辑删除列元数据
     * @param rawValue  注解配置的原始字符串
     * @param attribute 属性名（deletedValue / notDeletedValue，用于错误提示）
     * @return 转换后的字段类型值
     */
    private static Object convertLogicDeleteValue(Class<?> clazz, ColumnMetadata column, String rawValue, String attribute) {
        if (rawValue == null) {
            throw new IllegalArgumentException(String.format(
                    "Entity [%s] @LogicDelete field [%s] attribute [%s] is null",
                    clazz.getName(), column.getFieldName(), attribute));
        }
        try {
            return ConversionUtils.conversion(rawValue, ResolvableType.forField(column.getField()));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Entity [%s] @LogicDelete field [%s] attribute [%s] value [%s] cannot be converted to type [%s]",
                    clazz.getName(), column.getFieldName(), attribute, rawValue, column.getField().getGenericType().getTypeName()), e);
        }
    }

    /**
     * 逻辑删除规则
     * <p>
     * 由 {@link #getLogicDeleteRule(Class)} 解析得到，包含逻辑删除列名与
     * 按字段类型转换后的已删除值、未删除值。
     * </p>
     */
    public static final class LogicDeleteRule {

        /**
         * 逻辑删除列名
         */
        private final String columnName;

        /**
         * 已删除值（已按字段类型转换）
         */
        private final Object deletedValue;

        /**
         * 未删除值（已按字段类型转换）
         */
        private final Object notDeletedValue;

        LogicDeleteRule(String columnName, Object deletedValue, Object notDeletedValue) {
            this.columnName = columnName;
            this.deletedValue = deletedValue;
            this.notDeletedValue = notDeletedValue;
        }

        /**
         * 获取逻辑删除列名
         *
         * @return 逻辑删除列名
         */
        public String getColumnName() {
            return columnName;
        }

        /**
         * 获取已删除值（已按字段类型转换）
         *
         * @return 已删除值
         */
        public Object getDeletedValue() {
            return deletedValue;
        }

        /**
         * 获取未删除值（已按字段类型转换）
         *
         * @return 未删除值
         */
        public Object getNotDeletedValue() {
            return notDeletedValue;
        }
    }
}
