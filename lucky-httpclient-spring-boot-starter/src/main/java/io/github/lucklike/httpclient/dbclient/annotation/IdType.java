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
     * 基于雪花算法生成的 ID
     */
    SNOWFLAKE_ID,

    /**
     * 需要手动设置
     */
    MANUAL_SETTINGS


}
