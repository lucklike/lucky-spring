package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.interceptor.PriorityConstant;

import java.util.HashSet;
import java.util.Set;

/**
 * 日志相关的配置
 */
public class LoggerConfiguration {

    /**
     * 指定需要打印日志的包
     */
    private Set<String> packages = new HashSet<>();

    /**
     * 是否开启请求日志，默认开启（只有在{@link #packages}不为{@code null}时才生效）
     */
    private boolean enableReqLog = true;

    /**
     * 是否开启响应日志，默认开启（只有在{@link #packages}不为{@code null}时才生效）
     */
    private boolean enableRespLog = true;

    /**
     * 是否开启打印注解信息功能，默认关闭
     */
    private boolean enableAnnotationLog = false;

    /**
     * 是否开启打印参数信息功能，默认关闭
     */
    private boolean enableArgsLog = false;

    /**
     * 是否强制打印响应体信息
     */
    private boolean forcePrintBody = false;

    /**
     * 是否打印响应头信息
     */
    private boolean enableRespHeaderLog = true;

    /**
     * 日志打印拦截器的优先级，默认{@value PriorityConstant#OVERALL_LOGGER_PRIORITY}
     */
    private Integer priority = PriorityConstant.OVERALL_LOGGER_PRIORITY;

    /**
     * MimeType为这些类型时，将打印响应体日志（覆盖默认值）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/xml</li>
     * <li>application/x-java-serialized-object</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
     * </ui>
     */
    private Set<String> setAllowMimeTypes;

    /**
     * MimeType为这些类型时，将打印响应体日志（在默认值的基础上新增）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/xml</li>
     * <li>application/x-java-serialized-object</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
     * </ui>
     */
    private Set<String> addAllowMimeTypes;

    /**
     * 响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    private long bodyMaxLength = -1L;

    /**
     * 打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    private String reqLogCondition;

    /**
     * 打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
     */
    private String respLogCondition;


    /**
     * 指定需要打印日志的包
     *
     * @param packages 指定需要打印日志的包
     */
    public void setPackages(Set<String> packages) {
        this.packages = packages;
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
     * MimeType为这些类型时，将打印响应体日志（覆盖默认值）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/xml</li>
     * <li>application/x-java-serialized-object</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
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
     * <li>application/xml</li>
     * <li>application/x-java-serialized-object</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
     * </ui>
     *
     * @param addAllowMimeTypes 追加的打印响应体内容的MimeType集合
     */
    public void setAddAllowMimeTypes(Set<String> addAllowMimeTypes) {
        this.addAllowMimeTypes = addAllowMimeTypes;
    }

    /**
     * 设置打印响应日志的阈值，响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public void setBodyMaxLength(long bodyMaxLength) {
        this.bodyMaxLength = bodyMaxLength;
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
     * 设置是否开启打印注解信息功能
     *
     * @param enableAnnotationLog 是否开启打印注解信息功能
     */
    public void setEnableAnnotationLog(boolean enableAnnotationLog) {
        this.enableAnnotationLog = enableAnnotationLog;
    }

    /**
     * 设置是否开启打印参数信息功能
     *
     * @param enableArgsLog 是否开启打印参数信息功能
     */
    public void setEnableArgsLog(boolean enableArgsLog) {
        this.enableArgsLog = enableArgsLog;
    }

    /**
     * 设置日志打印拦截器的优先级
     *
     * @param priority 日志打印拦截器的优先级
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * 设置是否打印响应头信息
     *
     * @param enableRespHeaderLog 是否打印响应头信息
     */
    public void setEnableRespHeaderLog(boolean enableRespHeaderLog) {
        this.enableRespHeaderLog = enableRespHeaderLog;
    }

    /**
     * 设置是否强制打印响应体信息
     *
     * @param forcePrintBody 是否强制打印响应体信息
     */
    public void setForcePrintBody(boolean forcePrintBody) {
        this.forcePrintBody = forcePrintBody;
    }

    /**
     * 获取需要打印日志的包集合
     *
     * @return 需要打印日志的包集合
     */
    public Set<String> getPackages() {
        return packages;
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
     * 是否开启了打印注解信息的功能
     *
     * @return 是否开启了打印注解信息的功能
     */
    public boolean isEnableAnnotationLog() {
        return enableAnnotationLog;
    }

    /**
     * 是否开启了打印参数信息的功能
     *
     * @return 是否开启了打印参数信息的功能
     */
    public boolean isEnableArgsLog() {
        return enableArgsLog;
    }

    /**
     * 获取日志打印拦截器的优先级
     *
     * @return 日志打印拦截器的优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * MimeType为这些类型时，将打印响应体日志（覆盖默认值）<br/>
     * (注： *&frasl;* : 表示所有类型)<br/>
     * 默认值：
     * <ui>
     * <li>application/json</li>
     * <li>application/xml</li>
     * <li>application/x-java-serialized-object</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
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
     * <li>application/xml</li>
     * <li>application/x-java-serialized-object</li>
     * <li>text/xml</li>
     * <li>text/plain</li>
     * <li>text/html</li>
     * </ui>
     * </ui>
     */
    public Set<String> getAddAllowMimeTypes() {
        return addAllowMimeTypes;
    }

    /**
     * 获取打印响应日志的阈值，响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制<br/>
     * 单位：字节<br/>
     * 默认值：-1
     */
    public long getBodyMaxLength() {
        return bodyMaxLength;
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
     * 是否强制打印响应体信息
     *
     * @return 是否强制打印响应体信息
     */
    public boolean isForcePrintBody() {
        return forcePrintBody;
    }

    /**
     * 是否打印响应头信息
     *
     * @return 是否打印响应头信息
     */
    public boolean isEnableRespHeaderLog() {
        return enableRespHeaderLog;
    }
}
