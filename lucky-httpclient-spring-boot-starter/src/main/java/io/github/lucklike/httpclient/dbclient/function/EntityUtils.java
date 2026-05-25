package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.StringUtils;
import com.luckyframework.reflect.AnnotationUtils;
import com.luckyframework.reflect.ClassUtils;
import io.github.lucklike.httpclient.dbclient.annotation.Id;
import io.github.lucklike.httpclient.dbclient.annotation.Table;

import java.lang.reflect.Field;

public class EntityUtils {

    public static String getIdColumn(Class<?> clazz) {
        for (Field field : ClassUtils.getAllFields(clazz)) {
            Id idAnn = AnnotationUtils.findMergedAnnotation(field, Id.class);
            if (idAnn != null) {
                return StringUtils.hasText(idAnn.value()) ? idAnn.value() : field.getName();
            }
        }
        throw new IllegalArgumentException("The ID attribute was not found in class '" + ClassUtils.getClassName(clazz) + "'");
    }

    public static String getTableName(Class<?> clazz) {
        Table tableAnn = AnnotationUtils.findMergedAnnotation(clazz, Table.class);
        if (tableAnn != null && StringUtils.hasText(tableAnn.value())) {
            return tableAnn.value();
        }
        return clazz.getSimpleName();
    }
}
