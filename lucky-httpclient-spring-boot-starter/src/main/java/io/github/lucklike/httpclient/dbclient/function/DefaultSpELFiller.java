package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.spel.MutableMapParamWrapper;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.annotation.FillScene;
import io.github.lucklike.httpclient.dbclient.metadata.ColumnMetadata;
import io.github.lucklike.httpclient.dbclient.metadata.EntityMetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认 SpEL 填充器实现。
 * <p>
 * 负责评估 @Column 的 fillValue 表达式，并按场景产出填充结果。
 * 统一填充规则：实体字段为 null 时才填充，已有值时保留用户显式赋值：
 * 有实体时回写字段（INSERT/批量路径），无实体时仅返回字段映射（由
 * {@link AuditFillSupport#applyUpdateFill} 注入 SET 子句）。
 * 表达式求值不直接依赖 {@code SpELRuntime}，而是通过
 * {@link MethodContext#parseExpression(String, Class, com.luckyframework.httpclient.proxy.spel.ParamWrapperSetter)}
 * 完成，从而复用框架的表达式解析管线（嵌套解析、上下文变量、SpEL 函数等能力全部可用）。
 * </p>
 * <p>
 * 表达式中可通过以下扩展变量访问填充上下文（注册到表达式 rootObject，以属性方式访问）：
 * <ul>
 *     <li>{@code entity} - 当前操作的实体对象</li>
 *     <li>{@code entityClass} - 实体类型 Class</li>
 *     <li>{@code scene} - 当前填充场景（INSERT 或 UPDATE）</li>
 *     <li>{@code methodName} - 当前执行的 dbclient 方法签名</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 本填充器由 FillRegistry 默认内置注册，无需手动配置
 *
 * // 实体字段声明（仅插入时填充创建时间）
 * @Column(fillScene = FillScene.INSERT, fillValue = "T(java.time.LocalDateTime).now()")
 * private LocalDateTime createTime;
 *
 * // 访问填充上下文变量（无实体路径 entity 为主键探针实体）
 * @Column(fillScene = FillScene.UPDATE, fillValue = "#{entity.id + '_suffix'}")
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
    public Map<String, Object> fill(MethodContext mc, Object entity, Class<?> entityClass, FillScene scene) {
        // 表达式求值依赖 MethodContext，上下文/实体类型缺失时静默跳过
        if (mc == null || entityClass == null) {
            return null;
        }

        // 获取该实体类的自动填充元数据
        List<ColumnMetadata> fillColumns = EntityMetadataFactory.getMetadata(entityClass).getFillColumns(scene);
        if (fillColumns == null || fillColumns.isEmpty()) {
            return null;
        }

        String methodName = resolveMethodName(mc);
        Map<String, Object> result = new HashMap<>();

        for (ColumnMetadata column : fillColumns) {
            String fillValue = column.getFillValue();
            Field field = column.getField();
            if (fillValue == null || fillValue.isEmpty()) {
                continue;
            }

            // 统一填充规则：实体字段已有值时保留用户显式赋值，不覆盖、不求值
            // （Lambda 等无实体路径的探针实体除主键外均为 null，不受此判断影响）
            if (entity != null && FieldUtils.getValue(entity, field) != null) {
                continue;
            }

            try {
                // 通过 MethodContext 解析 SpEL 表达式，并注入填充上下文扩展变量
                Object value = mc.parseExpression(fillValue, ResolvableType.forField(field),
                        wrapper -> addFillContextVars(wrapper, entity, entityClass, scene, methodName));

                // 处理 null 值：跳过填充
                if (value == null) {
                    continue;
                }

                // 有实体时回写字段（INSERT/批量路径依赖实体取值）；无实体路径仅返回映射
                if (entity != null) {
                    FieldUtils.setValue(entity, field, value);
                }

                // 记录到结果中（供后续 SQL 生成采集）
                result.put(column.getFieldName(), value);

            } catch (Exception e) {
                // 填充失败时不中断整体流程，记录错误日志并跳过该字段
                log.error("[AuditFill] Failed to fill field '{}' in entity '{}' with expression '{}'",
                        column.getFieldName(), entityClass.getName(), fillValue, e);
            }
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * 向 SpEL 参数包装器中注入填充上下文扩展变量。
     * <p>
     * 向 SpEL 参数包装器的 rootObject 中注入填充上下文扩展变量，
     * 表达式以属性访问方式（如 {@code entity.id}）获取上下文。
     * 无实体路径（Lambda 更新等）传入的是主键探针实体；探针创建失败时
     * entity 为 null，此时不注入 entity 变量，表达式引用 {@code entity}
     * 将求值失败并按字段跳过处理。
     * </p>
     *
     * @param wrapper     SpEL 参数包装器
     * @param entity      当前实体对象，可为 null
     * @param entityClass 实体类型
     * @param scene       填充场景
     * @param methodName  方法签名
     */
    private void addFillContextVars(MutableMapParamWrapper wrapper, Object entity,
                                    Class<?> entityClass, FillScene scene, String methodName) {
        Map<String, Object> vars = new HashMap<>(4);
        if (entity != null) {
            vars.put("entity", entity);
        }
        vars.put("entityClass", entityClass);
        vars.put("scene", scene);
        vars.put("methodName", methodName);

        // addFirst 保证扩展变量优先于框架默认变量参与解析
        wrapper.getRootObject().addFirst(Collections.unmodifiableMap(vars));
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
}
