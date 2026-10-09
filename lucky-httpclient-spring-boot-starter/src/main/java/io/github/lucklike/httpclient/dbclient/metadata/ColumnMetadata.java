package io.github.lucklike.httpclient.dbclient.metadata;

import io.github.lucklike.httpclient.dbclient.annotation.IdType;
import io.github.lucklike.httpclient.dbclient.function.Condition;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 列元数据
 * <p>
 * 描述实体类中单个字段与数据库列的映射关系，统一承载各消费方所需的全部标志位：
 * <ul>
 *     <li>{@link #isStaticField()}/{@link #isTransitory()}：字段修饰符标志（静态、transient）</li>
 *     <li>{@link #isExist()}：@Column(exist=false) 标记为非数据库字段</li>
 *     <li>{@link #isColumnAnnotated()}：字段是否带有 @Column 注解（含 @Id 等派生注解）</li>
 *     <li>{@link #isId()}/{@link #getIdType()}：主键信息</li>
 *     <li>{@link #isLogicDelete()}：@LogicDelete 逻辑删除字段标志与取值配置</li>
 *     <li>{@link #getConditionClass()}：条件拼接策略</li>
 * </ul>
 * 消费方应根据自身语义选择过滤条件，以保持各自原有行为。
 * </p>
 * <p>
 * 实例由 {@link EntityMetadataFactory} 统一创建并缓存，不可变。
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/9
 */
public class ColumnMetadata {

    /**
     * 字段对象
     */
    private final Field field;

    /**
     * 字段名
     */
    private final String fieldName;

    /**
     * 数据库列名（@Column.value 非空时取注解值，@Id.value 会通过 @AliasFor 合并到 @Column.value）
     */
    private final String columnName;

    /**
     * 是否为数据库字段（@Column.exist()，无注解时默认为 true）
     */
    private final boolean exist;

    /**
     * 字段是否带有 @Column 注解（含 @Id 等派生注解）
     */
    private final boolean columnAnnotated;

    /**
     * 是否为 @Id 主键字段
     */
    private final boolean id;

    /**
     * 主键生成策略，非主键字段为 null
     */
    private final IdType idType;

    /**
     * 条件拼接策略（@Column.condition()，无注解时默认为 Condition.Eq）
     */
    private final Class<? extends Condition> conditionClass;

    /**
     * 是否为 @LogicDelete 逻辑删除字段
     */
    private final boolean logicDelete;

    /**
     * @LogicDelete 配置的已删除值原始字符串（非逻辑删除字段为 null）
     */
    private final String logicDeleteDeletedValue;

    /**
     * @LogicDelete 配置的未删除值原始字符串（非逻辑删除字段为 null）
     */
    private final String logicDeleteNotDeletedValue;

    /**
     * 是否为静态字段
     */
    private final boolean staticField;

    /**
     * 是否为 transient 字段
     */
    private final boolean transitory;

    ColumnMetadata(Field field, String columnName, boolean exist, boolean columnAnnotated,
                   boolean id, IdType idType, Class<? extends Condition> conditionClass,
                   boolean logicDelete, String logicDeleteDeletedValue, String logicDeleteNotDeletedValue) {
        this.field = field;
        this.fieldName = field.getName();
        this.columnName = columnName;
        this.exist = exist;
        this.columnAnnotated = columnAnnotated;
        this.id = id;
        this.idType = idType;
        this.conditionClass = conditionClass;
        this.logicDelete = logicDelete;
        this.logicDeleteDeletedValue = logicDeleteDeletedValue;
        this.logicDeleteNotDeletedValue = logicDeleteNotDeletedValue;
        this.staticField = Modifier.isStatic(field.getModifiers());
        this.transitory = Modifier.isTransient(field.getModifiers());
    }

    /**
     * 获取字段对象
     *
     * @return 字段对象
     */
    public Field getField() {
        return field;
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 获取数据库列名
     *
     * @return 数据库列名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 判断是否为数据库字段
     *
     * @return true 表示是数据库字段，false 表示 @Column(exist=false)
     */
    public boolean isExist() {
        return exist;
    }

    /**
     * 判断字段是否带有 @Column 注解（含 @Id 等派生注解）
     *
     * @return true 表示带有注解
     */
    public boolean isColumnAnnotated() {
        return columnAnnotated;
    }

    /**
     * 判断是否为主键字段
     *
     * @return true 表示是 @Id 主键字段
     */
    public boolean isId() {
        return id;
    }

    /**
     * 获取主键生成策略
     *
     * @return 主键生成策略，非主键字段为 null
     */
    public IdType getIdType() {
        return idType;
    }

    /**
     * 判断是否为逻辑删除字段（带有 @LogicDelete 注解）
     *
     * @return true 表示是逻辑删除字段
     */
    public boolean isLogicDelete() {
        return logicDelete;
    }

    /**
     * 获取 @LogicDelete 配置的已删除值原始字符串
     *
     * @return 已删除值原始字符串，非逻辑删除字段时返回 null
     */
    public String getLogicDeleteDeletedValue() {
        return logicDeleteDeletedValue;
    }

    /**
     * 获取 @LogicDelete 配置的未删除值原始字符串
     *
     * @return 未删除值原始字符串，非逻辑删除字段时返回 null
     */
    public String getLogicDeleteNotDeletedValue() {
        return logicDeleteNotDeletedValue;
    }

    /**
     * 条件拼接策略
     *
     * @return 条件拼接策略类型
     */
    public Class<? extends Condition> getConditionClass() {
        return conditionClass;
    }

    /**
     * 判断是否为静态字段
     *
     * @return true 表示是静态字段
     */
    public boolean isStaticField() {
        return staticField;
    }

    /**
     * 判断是否为 transient 字段
     *
     * @return true 表示是 transient 字段
     */
    public boolean isTransitory() {
        return transitory;
    }
}
