package io.github.lucklike.httpclient.dbclient.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体元数据
 * <p>
 * 描述实体类与数据库表的映射关系，聚合该类所有字段的 {@link ColumnMetadata}，
 * 并提供表名、主键列列表、字段名索引等查询能力。
 * </p>
 * <p>
 * 列顺序与 {@code ClassUtils.getAllFields} 的既有语义保持一致：父类字段在前、子类字段在后；
 * 字段名索引在构建时按列顺序写入，同名字段（父子类隐藏场景）由子类字段覆盖。
 * </p>
 * <p>
 * 实例由 {@link EntityMetadataFactory} 统一创建并缓存，不可变。
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/9
 */
public class EntityMetadata {

    /**
     * 实体类类型
     */
    private final Class<?> entityClass;

    /**
     * 数据库表名
     */
    private final String tableName;

    /**
     * 全部字段的列元数据（父类字段在前，含静态与 transient 字段）
     */
    private final List<ColumnMetadata> columns;

    /**
     * 主键字段的列元数据（顺序与 {@link #columns} 一致）
     */
    private final List<ColumnMetadata> idColumns;

    /**
     * 字段名索引（同名字段由子类字段覆盖）
     */
    private final Map<String, ColumnMetadata> fieldIndex;

    /**
     * 是否存在带有 @Column 注解（含 @Id 等派生注解）的字段
     */
    private final boolean columnAnnotated;

    EntityMetadata(Class<?> entityClass, String tableName, List<ColumnMetadata> columns) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));

        List<ColumnMetadata> idColumnList = new ArrayList<>();
        Map<String, ColumnMetadata> index = new HashMap<>(columns.size() * 2);
        boolean annotated = false;
        for (ColumnMetadata column : columns) {
            // 父类字段在前、子类字段在后，同名字段由子类字段覆盖
            index.put(column.getFieldName(), column);
            if (column.isId()) {
                idColumnList.add(column);
            }
            if (column.isColumnAnnotated()) {
                annotated = true;
            }
        }
        this.idColumns = Collections.unmodifiableList(idColumnList);
        this.fieldIndex = Collections.unmodifiableMap(index);
        this.columnAnnotated = annotated;
    }

    /**
     * 获取实体类类型
     *
     * @return 实体类类型
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * 获取数据库表名
     *
     * @return 数据库表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 获取全部字段的列元数据
     *
     * @return 不可变的列元数据列表（父类字段在前）
     */
    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    /**
     * 获取主键字段的列元数据
     *
     * @return 不可变的主键列元数据列表，没有 @Id 字段时返回空列表
     */
    public List<ColumnMetadata> getIdColumns() {
        return idColumns;
    }

    /**
     * 判断是否存在带有 @Column 注解（含 @Id 等派生注解）的字段
     *
     * @return true 表示存在
     */
    public boolean isColumnAnnotated() {
        return columnAnnotated;
    }

    /**
     * 根据字段名获取列元数据
     *
     * @param fieldName 字段名
     * @return 列元数据，字段不存在时返回 null
     */
    public ColumnMetadata getColumnByFieldName(String fieldName) {
        return fieldIndex.get(fieldName);
    }
}
