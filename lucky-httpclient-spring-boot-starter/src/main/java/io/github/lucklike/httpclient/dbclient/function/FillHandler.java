package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.annotation.FillScene;

import java.util.List;

/**
 * 自动填充处理器接口。
 * <p>
 * 负责按实体类型和场景路由可用的 {@link Filler} 列表。
 * 业务模块可实现此接口并注册到 {@link FillRegistry}，以提供自定义填充逻辑（如从安全上下文获取用户 ID）。
 * </p>
 * <p>
 * 系统默认实现由 {@link DefaultSpELFiller} 提供，负责 SpEL 表达式求值。
 * 多个 FillHandler 可同时注册，依次执行。
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/10
 * @see Filler
 * @see FillRegistry
 */
public interface FillHandler {

    /**
     * 获取指定实体类和场景下的所有填充器。
     * <p>
     * 调用方会按返回值顺序依次执行每个 Filler.fill() 方法。
     * 如果某个 Filler 返回 null，表示跳过该字段的填充。
     * </p>
     *
     * @param entityClass 实体类类型
     * @param scene       填充场景（INSERT 或 UPDATE）
     * @return 填充器列表，无可用填充器时返回空列表（非 null）
     */
    List<Filler> getFillers(Class<?> entityClass, FillScene scene);
}
