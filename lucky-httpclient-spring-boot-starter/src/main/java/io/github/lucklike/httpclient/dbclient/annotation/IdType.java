package io.github.lucklike.httpclient.dbclient.annotation;

/**
 * ID 类型
 */
public enum IdType {

    /**
     * 自增 ID
     */
    AUTO_INCREMENT,

    /**
     * UUID
     */
    UUID,

    /**
     * nanoID
     */
    NANOID,

    /**
     * 雪花算法 ID 生成器（分布式生产级 · 时间可读前缀）
     * <p>
     * 生成的 ID 为 25 位纯数字字符串，结构如下：
     * <pre>
     * | yyyyMMddHHmmss | SSS  | WWWW     | SSSS     |
     * |  14位秒级时间    | 3位毫秒 | 4位机器ID | 4位序列号 |
     * </pre>
     * 示例：2026-10-12 16:34:55.123，机器ID=77，序列号=42 生成：
     * <pre>
     * 2026101216345512300770042
     * </pre>
     */
    SNOWFLAKE_ID,

    /**
     * 需要手动设置
     */
    MANUAL_SETTINGS


}
