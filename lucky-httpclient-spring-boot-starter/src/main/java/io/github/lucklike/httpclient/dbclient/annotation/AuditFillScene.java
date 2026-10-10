package io.github.lucklike.httpclient.dbclient.annotation;

/**
 * 自动填充场景枚举。
 * <p>
 * 用于指定 {@link AutoFill @AutoFill} 注解触发的时机：
 * <ul>
 *     <li>{@code INSERT} - 仅在插入时填充</li>
 *     <li>{@code UPDATE} - 仅在更新时填充</li>
 *     <li>{@code INSERT_AND_UPDATE} - 插入和更新时都填充</li>
 * </ul>
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/10
 * @see AutoFill
 */
public enum AuditFillScene {

    /**
     * 插入时填充
     */
    INSERT,

    /**
     * 更新时填充
     */
    UPDATE,

    /**
     * 插入和更新时都填充
     */
    INSERT_AND_UPDATE
}
