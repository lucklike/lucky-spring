package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.reflect.AnnotationUtils;
import io.github.lucklike.httpclient.dbclient.annotation.Column;
import io.github.lucklike.httpclient.dbclient.executor.SFunction;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda 工具类，用于解析 SFunction 获取字段名和列名
 * 支持 @Column 注解
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25
 */
public class LambdaUtils {

    /**
     * 字段名缓存：key = 类名 + ":" + lambda方法名, value = 字段名
     */
    private static final Map<String, String> FIELD_NAME_CACHE = new ConcurrentHashMap<>();

    /**
     * 列名缓存：key = 类名, value = (字段名 -> 列名)
     */
    private static final Map<Class<?>, Map<String, String>> COLUMN_CACHE = new ConcurrentHashMap<>();

    /**
     * 实体类字段缓存：key = 类名, value = 字段列表
     */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * SerializedLambda 缓存
     */
    private static final Map<String, SerializedLambda> LAMBDA_CACHE = new ConcurrentHashMap<>();

    /**
     * 从 SFunction 中提取字段名
     */
    public static <T, R> String getFieldName(SFunction<T, R> function) {
        SerializedLambda lambda = resolveLambda(function);
        String implMethodName = lambda.getImplMethodName();
        String implClassName = lambda.getImplClass().replace("/", ".");

        String cacheKey = implClassName + ":" + implMethodName;

        return FIELD_NAME_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                String fieldName = resolveFieldName(implMethodName);
                // 验证字段是否存在
                Class<?> entityClass = Class.forName(implClassName);
                Map<String, Field> fieldMap = getFieldMap(entityClass);

                if (fieldMap.containsKey(fieldName)) {
                    return fieldName;
                }

                // 如果字段名本身不存在，可能是特殊命名，尝试匹配
                for (String name : fieldMap.keySet()) {
                    if (name.equalsIgnoreCase(fieldName)) {
                        return name;
                    }
                }

                return fieldName;
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract field name from SFunction: " + function, e);
            }
        });
    }

    /**
     * 获取 SFunction 对应的字段对象
     */
    public static <T, R> Field getField(SFunction<T, R> function) {
        String fieldName = getFieldName(function);
        SerializedLambda lambda = resolveLambda(function);
        try {
            Class<?> entityClass = Class.forName(lambda.getImplClass().replace("/", "."));
            Map<String, Field> fieldMap = getFieldMap(entityClass);
            Field field = fieldMap.get(fieldName);
            if (field == null) {
                throw new RuntimeException("Field not found: " + fieldName + " in class " + entityClass.getName());
            }
            return field;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + lambda.getImplClass(), e);
        }
    }

    /**
     * 获取字段对应的数据库列名
     * 优先使用 @Column 注解的值，没有则使用字段名
     */
    public static <T, R> String getColumnName(Class<T> entityClass, SFunction<T, R> function) {
        String fieldName = getFieldName(function);
        return getColumnName(entityClass, fieldName);
    }

    /**
     * 根据实体类和字段名获取列名
     */
    public static String getColumnName(Class<?> entityClass, String fieldName) {
        Map<String, String> columnMap = getColumnMap(entityClass);
        return columnMap.getOrDefault(fieldName, fieldName);
    }

    /**
     * 获取实体类的列名映射表
     */
    public static Map<String, String> getColumnMap(Class<?> entityClass) {
        return COLUMN_CACHE.computeIfAbsent(entityClass, clazz -> {
            Map<String, String> map = new ConcurrentHashMap<>();
            Map<String, Field> fieldMap = getFieldMap(clazz);

            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                String fieldName = entry.getKey();
                Field field = entry.getValue();
                Column column = AnnotationUtils.findMergedAnnotation(field, Column.class);

                if (column != null && !column.value().isEmpty()) {
                    // 使用 @Column 注解的值
                    map.put(fieldName, column.value());
                } else {
                    // 使用字段名
                    map.put(fieldName, fieldName);
                }
            }
            return map;
        });
    }

    /**
     * 获取实体类的字段映射表
     */
    public static Map<String, Field> getFieldMap(Class<?> entityClass) {
        return FIELD_CACHE.computeIfAbsent(entityClass, clazz -> {
            Map<String, Field> fieldMap = new ConcurrentHashMap<>();
            collectFields(clazz, fieldMap);
            return fieldMap;
        });
    }

    /**
     * 递归收集类及其父类的所有字段
     */
    private static void collectFields(Class<?> clazz, Map<String, Field> fieldMap) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        // 处理当前类的字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 忽略静态字段和 transient 字段
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            // 如果子类已经定义了同名字段，不覆盖
            fieldMap.putIfAbsent(field.getName(), field);
        }

        // 处理父类
        collectFields(clazz.getSuperclass(), fieldMap);
    }

    /**
     * 从方法名解析字段名
     */
    private static String resolveFieldName(String methodName) {
        String fieldName;
        if (methodName.startsWith("get")) {
            fieldName = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            fieldName = methodName.substring(2);
        } else if (methodName.startsWith("set")) {
            fieldName = methodName.substring(3);
        } else {
            fieldName = methodName;
        }
        return Introspector.decapitalize(fieldName);
    }

    /**
     * 解析 SerializedLambda
     */
    private static SerializedLambda resolveLambda(SFunction<?, ?> function) {
        String cacheKey = function.getClass().getName() + "@" + System.identityHashCode(function);

        return LAMBDA_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
                writeReplace.setAccessible(true);
                return (SerializedLambda) writeReplace.invoke(function);
            } catch (Exception e) {
                throw new RuntimeException("Failed to resolve SerializedLambda for SFunction: " + function, e);
            }
        });
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        FIELD_NAME_CACHE.clear();
        COLUMN_CACHE.clear();
        FIELD_CACHE.clear();
        LAMBDA_CACHE.clear();
    }

    /**
     * 清除指定类的缓存
     */
    public static void clearCache(Class<?> entityClass) {
        COLUMN_CACHE.remove(entityClass);
        FIELD_CACHE.remove(entityClass);

        // 清除相关字段名缓存
        String className = entityClass.getName();
        FIELD_NAME_CACHE.entrySet().removeIf(entry -> entry.getKey().startsWith(className + ":"));
    }
}