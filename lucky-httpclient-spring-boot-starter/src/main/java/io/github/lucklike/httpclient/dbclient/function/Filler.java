package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.httpclient.proxy.context.MethodContext;
import io.github.lucklike.httpclient.dbclient.annotation.AuditFillScene;

import java.util.Map;

/**
 * 自动填充器接口。
 * <p>
 * 每个 Filler 负责为特定场景下的实体字段填充值。
 * 默认实现 {@link DefaultSpELFiller} 通过 SpEL 表达式求值提供通用填充能力；
 * 业务模块可实现此接口自定义填充逻辑（如从安全上下文获取用户 ID、生成序列号等）。
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/10
 * @see FillHandler
 * @see DefaultSpELFiller
 */
public interface Filler {

    /**
     * 为实体的指定场景字段填充值。
     * <p>
     * 调用方会遍历已注册的 Filler 列表依次执行此方法。
     * 如果返回 null，表示跳过该字段的填充，保留实体原始值。
     * </p>
     * <p>
     * 实现注意：
     * <ul>
     *     <li>不应抛出未捕获的异常，如需失败应由外层统一处理</li>
     *     <li>返回的 Map 中 key 为字段名，value 为填充后的值</li>
     *     <li>多个 Filler 可同时填充不同字段，互不干扰</li>
     *     <li>SpEL 表达式求值应通过 {@code mc.parseExpression(...)} 完成，复用框架的表达式上下文</li>
     * </ul>
     * </p>
     *
     * @param mc     方法上下文，用于解析 SpEL 表达式（可能为 null，为 null 时应跳过求值类填充）
     * @param entity 实体对象
     * @param scene  填充场景
     * @return 填充后的字段映射（fieldName -> fieldValue），null 表示跳过填充
     */
    Map<String, Object> fill(MethodContext mc, Object entity, AuditFillScene scene);
}
