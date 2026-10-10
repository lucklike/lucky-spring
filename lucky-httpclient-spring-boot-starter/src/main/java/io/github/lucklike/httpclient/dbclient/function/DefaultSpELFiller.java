package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.spel.MutableMapParamWrapper;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.annotation.AuditFillScene;
import io.github.lucklike.httpclient.dbclient.annotation.AutoFill;
import io.github.lucklike.httpclient.dbclient.metadata.ColumnMetadata;
import io.github.lucklike.httpclient.dbclient.metadata.EntityMetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认 SpEL 填充器实现。
 * <p>
 * 负责评估 @AutoFill 注解中的 SpEL 表达式，并将结果赋值到实体字段。
 * 表达式求值不直接依赖 {@code SpELRuntime}，而是通过
 * {@link MethodContext#parseExpression(String, Class, com.luckyframework.httpclient.proxy.spel.ParamWrapperSetter)}
 * 完成，从而复用框架的表达式解析管线（嵌套解析、上下文变量、SpEL 函数等能力全部可用）。
 * </p>
 * <p>
 * 表达式中可通过以下扩展变量访问填充上下文（同时注册到 rootObject 与 variables）：
 * <ul>
 *     <li>{@code entity} / {@code #entity} - 当前操作的实体对象</li>
 *     <li>{@code entityClass} / {@code #entityClass} - 实体类型 Class</li>
 *     <li>{@code scene} / {@code #scene} - 当前填充场景（INSERT 或 UPDATE）</li>
 *     <li>{@code methodName} / {@code #methodName} - 当前执行的 dbclient 方法签名</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 注册为默认填充处理器
 * FillRegistry.register(new DefaultSpELFillHandler());
 *
 * // 实体字段标注
 * @AutoFill(expression = "T(java.time.LocalDateTime).now()", scene = AuditFillScene.INSERT)
 * private LocalDateTime createTime;
 *
 * // 访问填充上下文变量
 * @AutoFill(expression = "#entity.name + '-suffix'")
 * private String remark;
 * }</pre>
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/10
 * @see FillHandler
 * @see FillRegistry
 */
public class DefaultSpELFiller implements Filler {

    private static final Logger log = LoggerFactory.getLogger(DefaultSpELFiller.class);

    @Override
    public Map<String, Object> fill(MethodContext mc, Object entity, AuditFillScene scene) {
        // 表达式求值依赖 MethodContext，上下文缺失时静默跳过
        if (mc == null || entity == null) {
            return null;
        }

        Class<?> entityClass = entity.getClass();

        // 获取该实体类的自动填充元数据
        List<ColumnMetadata> fillColumns = EntityMetadataFactory.getMetadata(entityClass).getFillColumns(scene);
        if (fillColumns == null || fillColumns.isEmpty()) {
            return null;
        }

        String methodName = resolveMethodName(mc);
        Map<String, Object> result = new HashMap<>();

        for (ColumnMetadata column : fillColumns) {
            AutoFill autoFill = column.getAutoFill();
            if (autoFill == null) {
                continue;
            }

            try {
                // 通过 MethodContext 解析 SpEL 表达式，并注入填充上下文扩展变量
                Object value = mc.parseExpression(autoFill.expression(), Object.class,
                        wrapper -> addFillContextVars(wrapper, entity, entityClass, scene, methodName));

                // 处理 null 值：跳过填充
                if (value == null) {
                    continue;
                }

                // 类型转换
                Field field = column.getField();
                value = convertType(value, field.getType());

                // 回写到实体字段
                FieldUtils.setValue(entity, field, value);

                // 记录到结果中（供后续 SQL 生成采集）
                result.put(column.getFieldName(), value);

            } catch (Exception e) {
                // 填充失败时不中断整体流程，记录错误日志并跳过该字段
                log.error("[AuditFill] Failed to fill field '{}' in entity '{}' with expression '{}'",
                        column.getFieldName(), entityClass.getName(), autoFill.expression(), e);
            }
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * 向 SpEL 参数包装器中注入填充上下文扩展变量。
     * <p>
     * 同时写入 rootObject 与 variables，使表达式既可以用 {@code entity}
     * 属性访问的方式，也可以用 {@code #entity} 变量访问的方式获取上下文。
     * </p>
     *
     * @param wrapper     SpEL 参数包装器
     * @param entity      当前实体对象
     * @param entityClass 实体类型
     * @param scene       填充场景
     * @param methodName  方法签名
     */
    private void addFillContextVars(MutableMapParamWrapper wrapper, Object entity,
                                    Class<?> entityClass, AuditFillScene scene, String methodName) {
        Map<String, Object> vars = new HashMap<>(4);
        vars.put("entity", entity);
        vars.put("entityClass", entityClass);
        vars.put("scene", scene);
        vars.put("methodName", methodName);

        // addFirst 保证扩展变量优先于框架默认变量参与解析
        wrapper.getRootObject().addFirst(Collections.unmodifiableMap(vars));
        wrapper.getVariables().addFirst(Collections.unmodifiableMap(vars));
    }

    /**
     * 解析当前方法签名，供表达式中的 {@code methodName} 变量使用。
     *
     * @param mc 方法上下文
     * @return 方法签名字符串
     */
    private String resolveMethodName(MethodContext mc) {
        try {
            return mc.getSimpleSignature();
        } catch (Exception e) {
            return "default";
        }
    }

    /**
     * 将 SpEL 返回值转换为目标字段类型。
     * <p>
     * 支持的转换：
     * <ul>
     *     <li>String → 基本类型包装类（Integer、Long 等）</li>
     *     <li>Number → 兼容的基本类型包装类（long → Long）</li>
     *     <li>基本类型与包装类的自动装箱由反射赋值完成</li>
     * </ul>
     * </p>
     *
     * @param value      SpEL 表达式返回值
     * @param targetType 目标字段类型
     * @return 转换后的值
     */
    private Object convertType(Object value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return value;
        }

        // 如果类型已匹配，直接返回
        if (targetType.isInstance(value)) {
            return value;
        }

        // String → 数值类型
        if (value instanceof String) {
            String str = (String) value;
            try {
                if (targetType == Integer.class || targetType == int.class) {
                    return Integer.valueOf(str);
                }
                if (targetType == Long.class || targetType == long.class) {
                    return Long.valueOf(str);
                }
                if (targetType == Double.class || targetType == double.class) {
                    return Double.valueOf(str);
                }
                if (targetType == Float.class || targetType == float.class) {
                    return Float.valueOf(str);
                }
                if (targetType == Short.class || targetType == short.class) {
                    return Short.valueOf(str);
                }
                if (targetType == Byte.class || targetType == byte.class) {
                    return Byte.valueOf(str);
                }
                if (targetType == Boolean.class || targetType == boolean.class) {
                    return Boolean.valueOf(str);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Audit fill expression value '" + str
                        + "' cannot be converted to target type '" + targetType.getName() + "'", e);
            }
        }

        // Number → 兼容的包装类型（如表达式返回 int 但字段是 Long）
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == Long.class || targetType == long.class) {
                return num.longValue();
            }
            if (targetType == Integer.class || targetType == int.class) {
                return num.intValue();
            }
            if (targetType == Double.class || targetType == double.class) {
                return num.doubleValue();
            }
            if (targetType == Float.class || targetType == float.class) {
                return num.floatValue();
            }
            if (targetType == Short.class || targetType == short.class) {
                return num.shortValue();
            }
            if (targetType == Byte.class || targetType == byte.class) {
                return num.byteValue();
            }
        }

        // 类型不匹配且无转换规则时原样返回，交由反射赋值校验
        return value;
    }
}
