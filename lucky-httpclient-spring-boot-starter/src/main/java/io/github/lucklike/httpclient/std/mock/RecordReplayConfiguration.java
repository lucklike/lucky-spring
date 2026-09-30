package io.github.lucklike.httpclient.std.mock;


import com.luckyframework.httpclient.proxy.mock.ConfigurableRecordReplay;

/**
 * 录制回放相关配置
 *
 * @author fk7075
 * @version 3.0.3
 * @since 2026-07-30 00:18:22
 */
public class RecordReplayConfiguration {

    /**
     * 回放时不匹配时的策略
     */
    private ConfigurableRecordReplay.MismatchStrategy replayMismatchStrategy = ConfigurableRecordReplay.MismatchStrategy.USE_TARGET;

    /**
     * 录制条件
     */
    private String recordConditions = "#{$contentLength$ < 1048576}";

    /**
     * 记录ID生成器
     */
    private String idGenerator = "#{__args_to_string__($mc$)}";

    /**
     * 录制文件存放位置
     */
    private String recordDir = "#{T(System).getProperty('user.dir')}/@RecordReplay";

    /**
     * 方法ID
     */
    private String methodId = "#{$method$.getName()}";

    /**
     * 录制的最大数量
     */
    private Integer recordMaxCount = 10;

    /**
     * 指定异步任务的执行器（支持SpEL表达式）
     */
    private String recordExecutor = "";

    /**
     * 回放时是否模拟延时
     */
    private boolean replayDelayMock = false;


    /**
     * 获取回放时不匹配时的策略
     *
     * @return 回放时不匹配时的策略
     */
    public ConfigurableRecordReplay.MismatchStrategy getReplayMismatchStrategy() {
        return replayMismatchStrategy;
    }

    /**
     * 回放时不匹配时的策略
     *
     * @param replayMismatchStrategy 回放时不匹配时的策略
     */
    public void setReplayMismatchStrategy(ConfigurableRecordReplay.MismatchStrategy replayMismatchStrategy) {
        this.replayMismatchStrategy = replayMismatchStrategy;
    }

    /**
     * 获取录制条件
     *
     * @return 录制条件
     */
    public String getRecordConditions() {
        return recordConditions;
    }

    /**
     * 设置录制条件
     *
     * @param recordConditions 录制条件
     */
    public void setRecordConditions(String recordConditions) {
        this.recordConditions = recordConditions;
    }

    /**
     * 获取记录ID生成器
     *
     * @return 记录ID生成器
     */
    public String getIdGenerator() {
        return idGenerator;
    }

    /**
     * 设置记录ID生成器
     *
     * @param idGenerator 记录ID生成器
     */
    public void setIdGenerator(String idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * 获取录制文件存放位置
     *
     * @return 录制文件存放位置
     */
    public String getRecordDir() {
        return recordDir;
    }

    /**
     * 设置录制文件存放位置
     *
     * @param recordDir 录制文件存放位置
     */
    public void setRecordDir(String recordDir) {
        this.recordDir = recordDir;
    }

    /**
     * 获取方法ID
     *
     * @return 方法ID
     */
    public String getMethodId() {
        return methodId;
    }

    /**
     * 设置方法ID
     *
     * @param methodId 方法ID
     */
    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    /**
     * 获取录制的最大数量
     *
     * @return 录制的最大数量
     */
    public Integer getRecordMaxCount() {
        return recordMaxCount;
    }

    /**
     * 设置录制的最大数量
     *
     * @param recordMaxCount 录制的最大数量
     */
    public void setRecordMaxCount(Integer recordMaxCount) {
        this.recordMaxCount = recordMaxCount;
    }

    /**
     * 获取指定异步任务的执行器
     *
     * @return 指定异步任务的执行器
     */
    public String getRecordExecutor() {
        return recordExecutor;
    }

    /**
     * 设置指定异步任务的执行器
     *
     * @param recordExecutor 指定异步任务的执行器
     */
    public void setRecordExecutor(String recordExecutor) {
        this.recordExecutor = recordExecutor;
    }

    /**
     * 获取回放时是否模拟延时
     *
     * @return 回放时是否模拟延时
     */
    public boolean isReplayDelayMock() {
        return replayDelayMock;
    }

    /**
     * 设置回放时是否模拟延时
     *
     * @param replayDelayMock 回放时是否模拟延时
     */
    public void setReplayDelayMock(boolean replayDelayMock) {
        this.replayDelayMock = replayDelayMock;
    }
}
