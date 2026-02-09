package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.logging.LoggerHandler;
import io.github.lucklike.httpclient.config.impl.LoggerImpl;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * 日志相关的配置
 */
public class LoggerConfiguration {

    /**
     * 是否启用日志处理功能
     */
    private boolean enable = true;

    /**
     * 是否开启请求日志，默认开启（只有在{@link #packages}不为{@code null}时才生效）
     */
    private boolean enableReqLog = true;

    /**
     * 是否开启响应日志，默认开启（只有在{@link #packages}不为{@code null}时才生效）
     */
    private boolean enableRespLog = true;

    /**
     * 日志组件异常时是否打印详细的错误信息
     */
    private boolean logErrorWithDetails = false;

    /**
     * 日志打印类型
     */
    private LoggerImpl type = LoggerImpl.BEAUTIFUL;

    /**
     * 处理类
     */
    @NestedConfigurationProperty
    private SimpleGenerateEntry<LoggerHandler> handlerClass;

    /**
     * 指定需要打印日志的包<br/>
     * *.*表示所有包
     */
    private Set<String> packages = new HashSet<>();

    /**
     * 是否打印响应头信息
     */
    private String enableRespHeaderLog;

    /**
     * MimeType为这些类型时，将打印响应体日志（覆盖默认值）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/x-ndjson</li>
     * <li>application/*+json</li>
     *
     * <li>application/xml</li>
     * <li>application/*+xml</li>
     *
     * <li>application/x-protobuf</li>
     *
     * <li>application/x-java-serialized-object</li>
     *
     * <li>application/x-www-form-urlencoded</li>
     *
     * <li>application/x-yaml</li>
     *
     * <li>text/plain</li>
     * <li>text/html</li>
     * <li>text/css</li>
     * <li>text/javascript</li>
     * <li>text/markdown</li>
     * <li>text/csv</li>
     * <li>text/xml</li>
     *
     * <li>application/javascript</li>
     * <li>application/x-javascript</li>
     * </ui>
     */
    private Set<String> setAllowMimeTypes;

    /**
     * MimeType为这些类型时，将打印响应体日志（在默认值的基础上新增）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/x-ndjson</li>
     * <li>application/*+json</li>
     *
     * <li>application/xml</li>
     * <li>application/*+xml</li>
     *
     * <li>application/x-protobuf</li>
     *
     * <li>application/x-java-serialized-object</li>
     *
     * <li>application/x-www-form-urlencoded</li>
     *
     * <li>application/x-yaml</li>
     *
     * <li>text/plain</li>
     * <li>text/html</li>
     * <li>text/css</li>
     * <li>text/javascript</li>
     * <li>text/markdown</li>
     * <li>text/csv</li>
     * <li>text/xml</li>
     *
     * <li>application/javascript</li>
     * <li>application/x-javascript</li>
     * </ui>
     */
    private Set<String> addAllowMimeTypes;

    /**
     * 请求体超过该值时，将不会打印请求体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    private long reqBodyMaxLength = -1L;

    /**
     * 响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    private long respBodyMaxLength = -1L;

    /**
     * 触发警告标志的最小耗时（单位：毫秒）
     */
    private long warnTime = -1;

    /**
     * 触发错误标志的最小耗时（单位：毫秒）
     */
    private long slowTime = -1;

    /**
     * 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    private String reqLogCondition;

    /**
     * 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    private String respLogCondition;

    /**
     * 日志脱敏配置
     */
    @NestedConfigurationProperty
    private LoggerMaskerConfig maskers = new LoggerMaskerConfig();

    /**
     * 获取用于日志处理的处理类
     *
     * @return 用于日志处理的处理类
     */

    public SimpleGenerateEntry<LoggerHandler> getHandlerClass() {
        return handlerClass;
    }


    /**
     * 设置用于日志处理的处理类
     *
     * @param handlerClass 用于日志处理的处理类
     */
    public void setHandlerClass(SimpleGenerateEntry<LoggerHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     * 是否开启日志处理功能
     *
     * @return 是否开启日志处理功能
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * 设置是否开启日志处理功能
     *
     * @param enable 是否开启日志处理功能
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 获取日志打印类型
     *
     * @return 日志打印类型
     */
    public LoggerImpl getType() {
        return type;
    }

    /**
     * 设置日志打印类型
     *
     * @param type 日志打印类型
     */
    public void setType(LoggerImpl type) {
        this.type = type;
    }

    /**
     * 指定需要打印日志的包<br/>
     * *.*表示所有包
     *
     * @param packages 指定需要打印日志的包
     */
    public void setPackages(Set<String> packages) {
        this.packages = packages;
    }


    /**
     * 获取需要打印日志的包集合<br/>
     * *.*表示所有包
     *
     * @return 需要打印日志的包集合
     */
    public Set<String> getPackages() {
        return packages;
    }

    /**
     * 日志组件异常时是否打印详细的错误信息
     *
     * @return 日志组件异常时是否打印详细的错误信息
     */
    public boolean isLogErrorWithDetails() {
        return logErrorWithDetails;
    }

    /**
     * 设置日志组件异常时是否打印详细的错误信息
     *
     * @param logErrorWithDetails 日志组件异常时是否打印详细的错误信息
     */
    public void setLogErrorWithDetails(boolean logErrorWithDetails) {
        this.logErrorWithDetails = logErrorWithDetails;
    }

    /**
     * 设置是否开启请求日志的打印，默认开启
     *
     * @param enableReqLog 是否开启请求日志的打印
     */
    public void setEnableReqLog(boolean enableReqLog) {
        this.enableReqLog = enableReqLog;
    }

    /**
     * 设置是否开启响应日志的打印，默认开启
     *
     * @param enableRespLog 否开启响应日志的打印
     */
    public void setEnableRespLog(boolean enableRespLog) {
        this.enableRespLog = enableRespLog;
    }

    /**
     * 是否开启了请求日志打印功能
     *
     * @return 是否开启了请求日志打印功能
     */
    public boolean isEnableReqLog() {
        return enableReqLog;
    }

    /**
     * 是否开启了响应日志打印功能
     *
     * @return 是否开启了响应日志打印功能
     */
    public boolean isEnableRespLog() {
        return enableRespLog;
    }

    /**
     * MimeType为这些类型时，将打印响应体日志（覆盖默认值）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/x-ndjson</li>
     * <li>application/*+json</li>
     *
     * <li>application/xml</li>
     * <li>application/*+xml</li>
     *
     * <li>application/x-protobuf</li>
     *
     * <li>application/x-java-serialized-object</li>
     *
     * <li>application/x-www-form-urlencoded</li>
     *
     * <li>application/x-yaml</li>
     *
     * <li>text/plain</li>
     * <li>text/html</li>
     * <li>text/css</li>
     * <li>text/javascript</li>
     * <li>text/markdown</li>
     * <li>text/csv</li>
     * <li>text/xml</li>
     *
     * <li>application/javascript</li>
     * <li>application/x-javascript</li>
     * </ui>
     *
     * @param setAllowMimeTypes 打印响应体内容的MimeType集合
     */
    public void setSetAllowMimeTypes(Set<String> setAllowMimeTypes) {
        this.setAllowMimeTypes = setAllowMimeTypes;
    }

    /**
     * MimeType为这些类型时，将打印响应体日志（在默认值的基础上新增）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/x-ndjson</li>
     * <li>application/*+json</li>
     *
     * <li>application/xml</li>
     * <li>application/*+xml</li>
     *
     * <li>application/x-protobuf</li>
     *
     * <li>application/x-java-serialized-object</li>
     *
     * <li>application/x-www-form-urlencoded</li>
     *
     * <li>application/x-yaml</li>
     *
     * <li>text/plain</li>
     * <li>text/html</li>
     * <li>text/css</li>
     * <li>text/javascript</li>
     * <li>text/markdown</li>
     * <li>text/csv</li>
     * <li>text/xml</li>
     *
     * <li>application/javascript</li>
     * <li>application/x-javascript</li>
     * </ui>
     *
     * @param addAllowMimeTypes 追加的打印响应体内容的MimeType集合
     */
    public void setAddAllowMimeTypes(Set<String> addAllowMimeTypes) {
        this.addAllowMimeTypes = addAllowMimeTypes;
    }

    /**
     * 设置打印请求日志的阈值，请求体超过该值时，将不会打印请求体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public void setReqBodyMaxLength(long reqBodyMaxLength) {
        this.reqBodyMaxLength = reqBodyMaxLength;
    }

    /**
     * 设置打印响应日志的阈值，响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public void setRespBodyMaxLength(long respBodyMaxLength) {
        this.respBodyMaxLength = respBodyMaxLength;
    }

    /**
     * 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @param reqLogCondition 打印请求日志的条件
     */
    public void setReqLogCondition(String reqLogCondition) {
        this.reqLogCondition = reqLogCondition;
    }

    /**
     * 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @param respLogCondition 打印请求日志的条件
     */
    public void setRespLogCondition(String respLogCondition) {
        this.respLogCondition = respLogCondition;
    }

    /**
     * 设置是否打印响应头信息
     *
     * @param enableRespHeaderLog 是否打印响应头信息
     */
    public void setEnableRespHeaderLog(String enableRespHeaderLog) {
        this.enableRespHeaderLog = enableRespHeaderLog;
    }


    /**
     * MimeType为这些类型时，将打印响应体日志（覆盖默认值）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/x-ndjson</li>
     * <li>application/*+json</li>
     *
     * <li>application/xml</li>
     * <li>application/*+xml</li>
     *
     * <li>application/x-protobuf</li>
     *
     * <li>application/x-java-serialized-object</li>
     *
     * <li>application/x-www-form-urlencoded</li>
     *
     * <li>application/x-yaml</li>
     *
     * <li>text/plain</li>
     * <li>text/html</li>
     * <li>text/css</li>
     * <li>text/javascript</li>
     * <li>text/markdown</li>
     * <li>text/csv</li>
     * <li>text/xml</li>
     *
     * <li>application/javascript</li>
     * <li>application/x-javascript</li>
     * </ui>
     */
    public Set<String> getSetAllowMimeTypes() {
        return setAllowMimeTypes;
    }

    /**
     * MimeType为这些类型时，将打印响应体日志（在默认值的基础上新增）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/x-ndjson</li>
     * <li>application/*+json</li>
     *
     * <li>application/xml</li>
     * <li>application/*+xml</li>
     *
     * <li>application/x-protobuf</li>
     *
     * <li>application/x-java-serialized-object</li>
     *
     * <li>application/x-www-form-urlencoded</li>
     *
     * <li>application/x-yaml</li>
     *
     * <li>text/plain</li>
     * <li>text/html</li>
     * <li>text/css</li>
     * <li>text/javascript</li>
     * <li>text/markdown</li>
     * <li>text/csv</li>
     * <li>text/xml</li>
     *
     * <li>application/javascript</li>
     * <li>application/x-javascript</li>
     * </ui>
     */
    public Set<String> getAddAllowMimeTypes() {
        return addAllowMimeTypes;
    }


    /**
     * 获取打印请求日志的阈值，请求体超过该值时，将不会打印请求体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public long getReqBodyMaxLength() {
        return reqBodyMaxLength;
    }

    /**
     * 获取打印响应日志的阈值，响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public long getRespBodyMaxLength() {
        return respBodyMaxLength;
    }

    /**
     * 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @return 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    public String getReqLogCondition() {
        return reqLogCondition;
    }

    /**
     * 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     *
     * @return 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    public String getRespLogCondition() {
        return respLogCondition;
    }

    /**
     * 是否打印响应头信息
     *
     * @return 是否打印响应头信息
     */
    public String getEnableRespHeaderLog() {
        return enableRespHeaderLog;
    }

    /**
     * 获取触发警告标志的最小耗时（单位：毫秒）
     *
     * @return 触发警告标志的最小耗时
     */
    public long getWarnTime() {
        return warnTime;
    }

    /**
     * 设置触发警告标志的最小耗时（单位：毫秒）
     *
     * @param warnTime 触发警告标志的最小耗时
     */
    public void setWarnTime(long warnTime) {
        this.warnTime = warnTime;
    }

    /**
     * 获取触发错误标志的最小耗时（单位：毫秒）
     *
     * @return 触发错误标志的最小耗时
     */
    public long getSlowTime() {
        return slowTime;
    }

    /**
     * 设置触发错误标志的最小耗时（单位：毫秒）
     *
     * @param slowTime 触发错误标志的最小耗时
     */
    public void setSlowTime(long slowTime) {
        this.slowTime = slowTime;
    }

    /**
     * 获取日志脱敏配置
     *
     * @return 日志脱敏配置
     */
    public LoggerMaskerConfig getMaskers() {
        return maskers;
    }

    /**
     * 设置日志脱敏配置
     *
     * @param maskers 日志脱敏配置
     */
    public void setMaskers(LoggerMaskerConfig maskers) {
        this.maskers = maskers;
    }
}
