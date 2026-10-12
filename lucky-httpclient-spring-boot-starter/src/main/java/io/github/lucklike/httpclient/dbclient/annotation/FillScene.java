package io.github.lucklike.httpclient.dbclient.annotation;

/**
 * 自动填充场景枚举。
 * <p>
 * 用于指定 {@link Column#fillScene()} 声明的填充规则触发时机：
 * <ul>
 *     <li>{@code INSERT} - 仅在插入时填充</li>
 *     <li>{@code UPDATE} - 仅在更新时填充</li>
 *     <li>{@code INSERT_AND_UPDATE} - 插入和更新时都填充</li>
 *     <li>{@code NOT_FILL} - 默认值，不填充</li>
 * </ul>
 * 表达式内容见 {@link Column#fillValue()}。
 * </p>
 *
 * @author fukang
 * @version 1.1.0
 * @date 2026-10-10
 * @see Column
 */
public enum FillScene {

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
    INSERT_AND_UPDATE,

    /**
     * 不填充
     */
    NOT_FILL
}
