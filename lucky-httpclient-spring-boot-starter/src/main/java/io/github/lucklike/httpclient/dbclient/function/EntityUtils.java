package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.metadata.ColumnMetadata;
import io.github.lucklike.httpclient.dbclient.metadata.EntityMetadataFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityUtils {

    private static final Map<Class<?>, List<IdField>> idFieldListMap = new ConcurrentHashMap<>();

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
}
