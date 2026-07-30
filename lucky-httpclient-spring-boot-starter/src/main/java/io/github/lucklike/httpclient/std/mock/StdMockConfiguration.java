package io.github.lucklike.httpclient.std.mock;


import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 标准 Mock 相关配置
 *
 * @author fk7075
 * @version 3.0.3
 * @since 2026-07-29 11:54:03
 */
public class StdMockConfiguration {

    /**
     * 是否开启 Mock 功能
     */
    private Boolean enable;

    /**
     * Mock 类型
     */
    private MockType mockType =  MockType.CONFIG;

    /**
     * Mock相关配置, mockType为CONFIG时生效
     */
    @NestedConfigurationProperty
    private MockResult configuration;

    /**
     * 录制与回放相关配置，mockType为RECORD、REPLAY时生效
     */
    @NestedConfigurationProperty
    public RecordReplayConfiguration recordReplayConfig;


}
