package io.github.lucklike.httpclient.dbclient.metadata;

import com.luckyframework.common.StringUtils;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.dbclient.annotation.Column;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.annotation.LogicDelete;
import io.github.lucklike.httpclient.dbclient.annotation.Table;
import io.github.lucklike.httpclient.dbclient.function.Condition;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据工厂
 * <p>
 * 统一反射构建实体类的 {@link EntityMetadata} 并进行缓存，是元数据层的唯一入口。
 * 原分散在 EntityUtils、LambdaUtils、SQLFunctions、CachedAnnotationRowMapper 中的
 * 反射逻辑统一由此提供，避免重复反射并保证元数据语义一致。
 * </p>
 * <p>
 * 列名解析规则：@Column.value 非空时取注解值（@Id.value 通过 @AliasFor 合并到 @Column.value），否则取字段名。
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/9
 */
public final class EntityMetadataFactory {

    /**
     * 实体元数据缓存
     */
    private static final Map<Class<?>, EntityMetadata> METADATA_CACHE = new ConcurrentHashMap<>();

    private EntityMetadataFactory() {
    }

    /**
     * 获取实体类的元数据（带缓存）
     *
     * @param entityClass 实体类类型
     * @return 实体元数据
     */
    public static EntityMetadata getMetadata(Class<?> entityClass) {
        return METADATA_CACHE.computeIfAbsent(entityClass, EntityMetadataFactory::buildMetadata);
    }

    /**
     * 清除全部缓存
     */
    public static void clearCache() {
        METADATA_CACHE.clear();
    }

    /**
     * 清除指定实体类的缓存
     *
     * @param entityClass 实体类类型
     */
    public static void clearCache(Class<?> entityClass) {
        METADATA_CACHE.remove(entityClass);
    }

    /**
     * 反射构建实体元数据
     * <p>
     * 使用 {@link ClassUtils#getAllFields(Class)} 保持既有顺序语义：父类字段在前、子类字段在后，
     * 包含静态与 transient 字段，同名字段不做去重（由 {@link EntityMetadata} 索引时子类覆盖）。
     * </p>
     */
    private static EntityMetadata buildMetadata(Class<?> entityClass) {
        List<ColumnMetadata> columns = new ArrayList<>();
        for (Field field : ClassUtils.getAllFields(entityClass)) {
            columns.add(buildColumnMetadata(field));
        }
        return new EntityMetadata(entityClass, resolveTableName(entityClass), columns);
    }

    /**
     * 构建单个字段的列元数据
     */
    private static ColumnMetadata buildColumnMetadata(Field field) {
        Column columnAnn = AnnotationUtils.findMergedAnnotation(field, Column.class);
        Id idAnn = AnnotationUtils.findMergedAnnotation(field, Id.class);
        LogicDelete logicDeleteAnn = AnnotationUtils.findMergedAnnotation(field, LogicDelete.class);

        String columnName = (columnAnn != null && StringUtils.hasText(columnAnn.value()))
                ? columnAnn.value()
                : field.getName();
        boolean exist = columnAnn == null || columnAnn.exist();
        Class<? extends Condition> conditionClass = columnAnn == null ? Condition.Eq.class : columnAnn.condition();

        return new ColumnMetadata(field, columnName, exist, columnAnn != null, idAnn != null,
                idAnn == null ? null : idAnn.type(), conditionClass,
                logicDeleteAnn != null,
                logicDeleteAnn == null ? null : logicDeleteAnn.deletedValue(),
                logicDeleteAnn == null ? null : logicDeleteAnn.notDeletedValue());
    }

    /**
     * 解析实体类对应的数据库表名（@Table.value 优先，否则类名小写）
     */
    private static String resolveTableName(Class<?> entityClass) {
        Table tableAnn = AnnotationUtils.findMergedAnnotation(entityClass, Table.class);
        if (tableAnn != null && StringUtils.hasText(tableAnn.value())) {
            return tableAnn.value();
        }
        return entityClass.getSimpleName().toLowerCase();
    }
}
