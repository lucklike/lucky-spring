package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.StringUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.annotation.Table;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityUtils {

    private static final Map<Class<?>, AutoIncrementField> autoIncrementIdFieldMap = new ConcurrentHashMap<>();

    public static String getIdColumn(Class<?> clazz, String notIdErrorMsg) {
        for (Field field : ClassUtils.getAllFields(clazz)) {
            Id idAnn = AnnotationUtils.findMergedAnnotation(field, Id.class);
            if (idAnn != null) {
                return StringUtils.hasText(idAnn.value()) ? idAnn.value() : field.getName();
            }
        }
        throw new IllegalArgumentException(notIdErrorMsg);
    }

    public static String getTableName(Class<?> clazz) {
        Table tableAnn = AnnotationUtils.findMergedAnnotation(clazz, Table.class);
        if (tableAnn != null && StringUtils.hasText(tableAnn.value())) {
            return tableAnn.value();
        }
        return clazz.getSimpleName().toLowerCase();
    }

    public static Id.Type getIdType(Class<?> clazz) {
        for (Field field : ClassUtils.getAllFields(clazz)) {
            Id idAnn = AnnotationUtils.findMergedAnnotation(field, Id.class);
            if (idAnn != null) {
                return idAnn.type();
            }
        }
        return null;
    }

    public static boolean isAutoIncrementId(Class<?> clazz) {
        return Id.Type.AUTO_INCREMENT.equals(getIdType(clazz));
    }

    public static AutoIncrementField getAutoIncrementIdField(Class<?> clazz) {
        return autoIncrementIdFieldMap.computeIfAbsent(clazz, _c -> {
            for (Field field : ClassUtils.getAllFields(clazz)) {
                Id idAnn = AnnotationUtils.findMergedAnnotation(field, Id.class);
                if (idAnn != null && Id.Type.AUTO_INCREMENT.equals(getIdType(clazz))) {
                    return new  AutoIncrementField(field);
                }
            }
            return AutoIncrementField.NULL;
        });
    }


    public static class AutoIncrementField {

        public static final AutoIncrementField NULL = new AutoIncrementField(null);

        private final Field field;

        public AutoIncrementField(Field field) {
            this.field = field;
        }

        public Field getField() {
            return field;
        }

        public boolean hasAutoIncrementId() {
            return field != null;
        }

        public void setId(Object entity, KeyHolder keyHolder) {
            FieldUtils.setValue(entity, field, ConversionUtils.conversion(keyHolder.getKey(), field.getType()));
        }
    }

}
