package io.github.lucklike.httpclient.dbclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 逻辑删除字段标记注解。
 * <p>
 * 用于将实体中的某个字段声明为逻辑删除字段，用于记录删除状态。
 * 标注后的实体：
 * <ul>
 *     <li>常规查询将自动追加"不等于已删除值"的条件，已删除记录不会被返回；</li>
 *     <li>可通过逻辑删除 API 将字段更新为"已删除值"替代物理删除；</li>
 *     <li>可通过恢复 API 将字段写回"未删除值"；</li>
 *     <li>可通过 {@code includeDeleted()} 显式查询包含已删除记录。</li>
 * </ul>
 * <p>
 * 列名由既有列映射规则决定（{@link Column#value()} 或字段名），
 * 需要自定义列名时叠加 {@link Column} 即可。
 * <p>
 * 取值说明：
 * <ul>
 *     <li>默认已删除值为 {@code "1"}、未删除值为 {@code "0"}；</li>
 *     <li>字段为数值类型时按数字解析；</li>
 *     <li>字段为 {@code boolean/Boolean} 时接受 {@code true/false/1/0}。</li>
 * </ul>
 * <p>
 * 注意：同一实体最多只允许标注一个逻辑删除字段，多字段标注在执行相关操作时会抛出异常。
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/9
 * @see Column
 * @see Id
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LogicDelete {

    /**
     * 已删除值（字段被逻辑删除时写入的取值）。
     * <p>默认值为 {@code "1"}。</p>
     *
     * @return 已删除值的字符串表示
     */
    String deletedValue() default "1";

    /**
     * 未删除值（字段处于正常状态时的取值）。
     * <p>默认值为 {@code "0"}。</p>
     *
     * @return 未删除值的字符串表示
     */
    String notDeletedValue() default "0";

}
