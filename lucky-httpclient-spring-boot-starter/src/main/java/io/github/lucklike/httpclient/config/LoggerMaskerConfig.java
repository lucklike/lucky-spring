package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.logging.MaskType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 日志脱敏配置
 */
public class LoggerMaskerConfig {

    /**
     * 启用请求日志脱敏
     */
    private boolean maskRequest = false;

    /**
     * 启用响应日志脱敏
     */
    private boolean maskResponse = false;

    /**
     * 是否开启常用字段无关的裸值脱敏（手机号、身份证、邮箱、Basic认证、JWT），默认关闭<br/>
     * (注：该配置为全局生效，开启后所有日志脱敏都会应用裸值规则，存在误伤可能，请按需开启)
     */
    private boolean enableCommonValueMaskers = false;

    /**
     * 预定义的脱敏配置
     */
    private Map<MaskType, Set<String>> predefined;

    /**
     * 自定义脱敏配置
     */
    private List<CustomMaskerConfig> extended;


    /**
     * 获取通预定义的脱敏配置
     *
     * @return 通预定义的脱敏配置
     */
    public Map<MaskType, Set<String>> getPredefined() {
        return predefined;
    }

    /**
     * 设置通预定义的脱敏配置
     *
     * @param predefined 通预定义的脱敏配置
     */
    public void setPredefined(Map<MaskType, Set<String>> predefined) {
        this.predefined = predefined;
    }

    /**
     * 获取自定义脱敏配置
     *
     * @return 自定义脱敏配置
     */
    public List<CustomMaskerConfig> getExtended() {
        return extended;
    }

    /**
     * 设置自定义脱敏配置
     *
     * @param extended 自定义脱敏配置
     */
    public void setExtended(List<CustomMaskerConfig> extended) {
        this.extended = extended;
    }

    /**
     * 是否启用请求日志脱敏
     *
     * @return 启用请求日志脱敏
     */
    public boolean isMaskRequest() {
        return maskRequest;
    }

    /**
     * 设置是否启用请求日志脱敏
     *
     * @param maskRequest 是否启用请求日志脱敏
     */
    public void setMaskRequest(boolean maskRequest) {
        this.maskRequest = maskRequest;
    }

    /**
     * 是否启用响应日志脱敏
     *
     * @return 是否启用响应日志脱敏
     */
    public boolean isMaskResponse() {
        return maskResponse;
    }

    /**
     * 设置是否启用响应日志脱敏
     *
     * @param maskResponse 是否启用响应日志脱敏
     */
    public void setMaskResponse(boolean maskResponse) {
        this.maskResponse = maskResponse;
    }

    /**
     * 是否开启常用字段无关的裸值脱敏（手机号、身份证、邮箱、Basic认证、JWT），默认关闭
     *
     * @return 是否开启常用裸值脱敏
     */
    public boolean isEnableCommonValueMaskers() {
        return enableCommonValueMaskers;
    }

    /**
     * 设置是否开启常用字段无关的裸值脱敏（手机号、身份证、邮箱、Basic认证、JWT），默认关闭
     *
     * @param enableCommonValueMaskers 是否开启常用裸值脱敏
     */
    public void setEnableCommonValueMaskers(boolean enableCommonValueMaskers) {
        this.enableCommonValueMaskers = enableCommonValueMaskers;
    }
}
