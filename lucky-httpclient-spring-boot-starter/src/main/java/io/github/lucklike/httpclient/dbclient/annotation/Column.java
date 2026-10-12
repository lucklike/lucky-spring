package io.github.lucklike.httpclient.dbclient.annotation;

import io.github.lucklike.httpclient.dbclient.function.Condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列描述注解
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 02:31
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Column {

    /**
     * 列名
     */
    String value() default "";

    /**
     * 是否是数据库字段
     */
    boolean exist() default true;

    /**
     * 条件拼接策略
     */
    Class<? extends Condition> condition() default Condition.Eq.class;

    /**
     * 自动填充场景，默认 {@link FillScene#NOT_FILL} 不填充。
     * <p>
     * 统一填充规则：实体字段为 null 时才填充，已有值时保留用户显式赋值；
     * Lambda 路径下用户已显式 set 的列不覆盖，未 set 的审计列自动注入。
     * </p>
     * <p>
     * INSERT 场景在 insert/batchInsert 时回写实体字段；
     * UPDATE 场景在 updateById、Lambda 更新（lambdaUpdate/lambdaCondition.toUpdate）、
     * 逻辑删除与恢复等所有 UPDATE 路径直接注入 SET 子句；
     * 批量更新（batchUpdateById）因参数取自实体，采用回写方式。
     * </p>
     *
     * @see #fillValue()
     */
    FillScene fillScene() default FillScene.NOT_FILL;

    /**
     * 自动填充表达式（SpEL），与 {@link #fillScene()} 配合使用，
     * fillScene 非 NOT_FILL 且本值非空时生效。
     * <p>
     * 求值通过 {@code MethodContext#parseExpression} 完成，复用框架的表达式解析管线，
     * 支持 SpEL 函数、Bean 引用（{@code @beanName.method()}）等能力；
     * 不含表达式标记的普通字符串即为常量值。
     * </p>
     * <p>
     * 求值上下文变量（注册到表达式 rootObject，以属性方式访问）：
     * <ul>
     *     <li>{@code entity}  - 当前操作的实体对象；
     *     Lambda 更新等无实体路径为主键探针实体（仅主键字段有值，
     *     从 WHERE 等值条件提取，其余字段为 null），使 {@code #{entity.id}} 类表达式仍可用</li>
     *     <li>{@code entityClass} - 实体类型 Class</li>
     *     <li>{@code scene} - 当前填充场景（INSERT 或 UPDATE）</li>
     *     <li>{@code methodName} - 当前执行的 dbclient 方法签名</li>
     * </ul>
     * </p>
     * <p>
     * 使用示例：
     * <pre>{@code
     * @Column(fillScene = FillScene.INSERT, fillValue = "T(java.time.LocalDateTime).now()")
     * private LocalDateTime createTime;
     *
     * @Column(fillScene = FillScene.INSERT_AND_UPDATE, fillValue = "#{entity.id + '_Test'}")
     * private String test;
     * }</pre>
     * </p>
     *
     * @see #fillScene()
     */
    String fillValue() default "";

}
