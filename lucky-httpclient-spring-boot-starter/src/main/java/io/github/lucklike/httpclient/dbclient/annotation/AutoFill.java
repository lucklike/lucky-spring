package io.github.lucklike.httpclient.dbclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体字段自动填充注解。
 * <p>
 * 用于在实体字段上声明自动填充规则，系统会在 INSERT/UPDATE 操作时按场景评估 SpEL 表达式并赋值。
 * 使用示例：
 * <pre>{@code
 * // 仅插入时填充创建时间
 * @AutoFill(expression = "T(java.time.LocalDateTime).now()", scene = INSERT)
 * private LocalDateTime createTime;
 *
 * // 插入和更新时都填充更新时间
 * @AutoFill(expression = "T(java.time.LocalDateTime).now()", scene = INSERT_AND_UPDATE)
 * private LocalDateTime updateTime;
 *
 * // 从 Spring Bean 获取当前用户 ID
 * @AutoFill(expression = "@securityContext.currentUserId()")
 * private Long createBy;
 * }</pre>
 * </p>
 * <p>
 * <b>注意：</b>同一字段只能应用一个填充规则，重复标注会抛出异常。
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/10
 * @see AuditFillScene
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AutoFill {

    /**
     * SpEL 表达式：值将在执行写入前由框架评估。
     * <p>
     * 求值通过 {@code MethodContext#parseExpression} 完成，复用框架的表达式解析管线，
     * 因此嵌套解析、SpEL 函数等框架能力均可直接使用。
     * </p>
     * <p>
     * 支持的表达式语法：
     * <ul>
     *     <li>静态方法调用：{@code T(java.time.LocalDateTime).now()}</li>
     *     <li>Spring Bean 访问：{@code @securityContext.currentUserId()}</li>
     *     <li>算术运算、条件表达式等标准 SpEL 语法</li>
     * </ul>
     * </p>
     * <p>
     * 求值上下文变量（同时注册到 rootObject 与 variables，支持两种访问方式）：
     * <ul>
     *     <li>{@code entity} / {@code #entity} - 当前操作的实体对象</li>
     *     <li>{@code entityClass} / {@code #entityClass} - 实体类型 Class</li>
     *     <li>{@code scene} / {@code #scene} - 当前填充场景（INSERT 或 UPDATE）</li>
     *     <li>{@code methodName} / {@code #methodName} - 当前执行的 dbclient 方法签名</li>
     * </ul>
     * </p>
     *
     * @return SpEL 表达式字符串
     */
    String expression();

    /**
     * 填充场景，决定何时触发自动填充。
     * <p>
     * 默认为 {@code INSERT_AND_UPDATE}，即插入和更新时都会尝试填充。
     * 如果表达式结果为 null，则跳过该字段（保留实体原始值）。
     * </p>
     *
     * @return 填充场景枚举
     */
    AuditFillScene scene() default AuditFillScene.INSERT_AND_UPDATE;
}
