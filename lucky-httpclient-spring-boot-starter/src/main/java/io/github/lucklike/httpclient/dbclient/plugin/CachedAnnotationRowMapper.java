package io.github.lucklike.httpclient.dbclient.plugin;

import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.dbclient.annotation.Column;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强版注解感知的BeanPropertyRowMapper
 * 支持@Column注解，并提供缓存机制优化性能
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25
 */
public class CachedAnnotationRowMapper<T> extends BeanPropertyRowMapper<T> {

    // 缓存类的映射元数据
    private static final Map<Class<?>, MappingMetadata> METADATA_CACHE = new ConcurrentHashMap<>();

    // 当前类的映射元数据
    private final MappingMetadata metadata;

    /**
     * 构造函数
     */
    public CachedAnnotationRowMapper(Class<T> mappedClass) {
        super(mappedClass);
        this.metadata = getOrCreateMetadata(mappedClass);
    }

    /**
     * 获取或创建映射元数据
     */
    private MappingMetadata getOrCreateMetadata(Class<T> mappedClass) {
        return METADATA_CACHE.computeIfAbsent(mappedClass, _clazz -> buildMetadata(mappedClass));
    }

    /**
     * 构建映射元数据
     */
    private MappingMetadata buildMetadata(Class<T> mappedClass) {
        MappingMetadata metadata = new MappingMetadata();
        collectColumnAnnotations(mappedClass, metadata);
        return metadata;
    }

    /**
     * 收集@Column注解信息
     */
    private void collectColumnAnnotations(Class<?> clazz, MappingMetadata metadata) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        // 处理当前类的字段
        Field[] fields = ClassUtils.getAllFields(clazz);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();

            Column column = AnnotationUtils.findMergedAnnotation(field, Column.class);
            if (column != null) {
                if (column.exist()) {
                    String columnName = StringUtils.hasText(column.value()) ? column.value() : fieldName;
                    metadata.addMapping(fieldName, columnName);
                }
            } else {
                metadata.addMapping(fieldName, fieldName);
            }
        }

    }

    /**
     * 重写映射方法
     */
    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        // 如果没有任何@Column注解，使用父类的默认映射
        if (!metadata.hasAnnotationMappings()) {
            return super.mapRow(rs, rowNumber);
        }

        // 使用自定义映射逻辑
        return customMapRow(rs, rowNumber);
    }

    /**
     * 自定义映射实现
     */
    private T customMapRow(ResultSet rs, int rowNumber) throws SQLException {
        T mappedObject = createInstance();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        PropertyDescriptor[] pds = getBeanWrapper(mappedObject).getPropertyDescriptors();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = JdbcUtils.lookupColumnName(rsmd, i);

            // 查找对应的字段
            String fieldName = metadata.findFieldByColumn(columnName);

            // 查找属性描述符
            PropertyDescriptor pd = findProperty(fieldName, pds);
            if (pd != null && pd.getWriteMethod() != null) {
                try {
                    Object value = getColumnValue(rs, i, pd);
                    setPropertyValue(mappedObject, fieldName, value);
                } catch (Exception e) {
                    logger.debug("Failed to set property '" + fieldName + "': " + e.getMessage());
                }
            }
        }

        return mappedObject;
    }

    // 辅助方法
    private T createInstance() {
        try {
            return getMappedClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + getMappedClass(), e);
        }
    }

    private BeanWrapper getBeanWrapper(T object) {
        return PropertyAccessorFactory.forBeanPropertyAccess(object);
    }

    private void setPropertyValue(T object, String propertyName, Object value) {
        getBeanWrapper(object).setPropertyValue(propertyName, value);
    }

    private PropertyDescriptor findProperty(String fieldName, PropertyDescriptor[] pds) {
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(fieldName)) {
                return pd;
            }
        }
        return null;
    }

    private static String underscoreToCamel(String underscore) {
        if (!underscore.contains("_")) {
            return underscore;
        }

        StringBuilder result = new StringBuilder();
        boolean toUpper = false;

        for (char c : underscore.toCharArray()) {
            if (c == '_') {
                toUpper = true;
            } else {
                result.append(toUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                toUpper = false;
            }
        }

        return result.toString();
    }

    /**
     * 映射元数据类
     */
    private static class MappingMetadata {
        // 字段名 -> 列名
        private final Map<String, String> fieldToColumn = new HashMap<>();
        // 列名 -> 字段名
        private final Map<String, String> columnToField = new LinkedCaseInsensitiveMap<>();

        public void addMapping(String fieldName, String columnName) {
            fieldToColumn.put(fieldName, columnName);
            columnToField.put(columnName, fieldName);
        }

        public String findFieldByColumn(String columnName) {
            // 精确匹配
            String field = columnToField.get(columnName);
            if (field != null) {
                return field;
            }

            // 转驼峰
            String humpColumn = underscoreToCamel(columnName);
            String field1 = columnToField.get(humpColumn);
            if (field1 != null) {
                return field1;
            }
            return columnName;
        }

        public boolean hasAnnotationMappings() {
            return !fieldToColumn.isEmpty();
        }

        public Map<String, String> getFieldToColumnMappings() {
            return new HashMap<>(fieldToColumn);
        }
    }
}