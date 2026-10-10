package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.annotation.AuditFillScene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局填充器注册表。
 * <p>
 * 持有所有已注册的 {@link FillHandler}，并提供线程安全的增删查操作。
 * 注册表默认已内置 {@link DefaultSpELFillHandler}（负责 @AutoFill 的 SpEL 表达式求值），
 * 开箱即用；业务可额外调用 {@link #register(FillHandler)} 追加自定义填充处理器，
 * 执行自动填充时按注册顺序依次路由到对应的 Filler 列表。
 * </p>
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 追加注册自定义填充器（默认 SpEL 处理器仍生效）
 * FillRegistry.register(new MyCustomFillHandler());
 *
 * // 全局关闭填充功能（灰度开关，会连同默认处理器一起移除）
 * FillRegistry.clearAll();
 *
 * // 重新启用默认 SpEL 填充处理器
 * FillRegistry.registerDefaultHandler();
 * }</pre>
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026-10-10
 * @see FillHandler
 */
public final class FillRegistry {

    /**
     * 默认 SpEL 填充处理器（静态初始化，保证 @AutoFill 开箱即用）
     */
    private static final FillHandler DEFAULT_HANDLER = new DefaultSpELFillHandler();

    /**
     * 全局注册表：handler 实例 -> handler 对象
     */
    private static final List<FillHandler> HANDLERS = new CopyOnWriteArrayList<>();

    /**
     * 缓存映射：(entityClass, scene) -> filler 列表
     */
    private static final Map<String, List<Filler>> FILLER_CACHE = new ConcurrentHashMap<>();

    static {
        HANDLERS.add(DEFAULT_HANDLER);
    }

    private FillRegistry() {
        // 工具类，禁止实例化
    }

    /**
     * 注册填充处理器。
     * <p>
     * 支持多次注册多个 Handler，执行时按注册顺序依次调用（默认处理器始终在首）。
     * </p>
     *
     * @param handler 填充处理器
     */
    public static void register(FillHandler handler) {
        if (handler != null && handler != DEFAULT_HANDLER && !HANDLERS.contains(handler)) {
            HANDLERS.add(handler);
            clearCacheByEntityClass(null); // 清空缓存，重新计算
        }
    }

    /**
     * 重新注册默认 SpEL 填充处理器。
     * <p>
     * 在 {@link #clearAll()} 之后可通过此方法恢复默认的 @AutoFill SpEL 求值能力。
     * </p>
     */
    public static void registerDefaultHandler() {
        if (!HANDLERS.contains(DEFAULT_HANDLER)) {
            HANDLERS.add(0, DEFAULT_HANDLER);
            clearCacheByEntityClass(null);
        }
    }

    /**
     * 注销填充处理器。
     *
     * @param handler 填充处理器
     */
    public static void unregister(FillHandler handler) {
        if (handler == DEFAULT_HANDLER) {
            return; // 默认处理器请通过 clearAll() 移除
        }
        HANDLERS.remove(handler);
        clearCacheByEntityClass(null);
    }

    /**
     * 清空所有已注册的填充处理器（包含默认的 SpEL 处理器，即关闭整个自动填充能力）。
     * <p>
     * 用于灰度开关或测试环境；可通过 {@link #registerDefaultHandler()} 重新启用默认能力。
     * </p>
     */
    public static void clearAll() {
        HANDLERS.clear();
        clearCacheByEntityClass(null);
    }

    /**
     * 获取指定实体类和场景下的所有填充器。
     * <p>
     * 结果会被缓存以提升性能。
     * </p>
     *
     * @param entityClass 实体类类型
     * @param scene       填充场景
     * @return 填充器列表，无可用填充器时返回空列表
     */
    public static List<Filler> getFillers(Class<?> entityClass, AuditFillScene scene) {
        String cacheKey = entityClass.getName() + ":" + scene.name();
        
        // 先检查缓存
        List<Filler> cached = FILLER_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从所有 Handler 中收集 Filler
        List<Filler> fillers = new ArrayList<>();
        for (FillHandler handler : HANDLERS) {
            List<Filler> handlerFillers = handler.getFillers(entityClass, scene);
            if (handlerFillers != null && !handlerFillers.isEmpty()) {
                fillers.addAll(handlerFillers);
            }
        }

        // 如果无填充器但存在 Handler，缓存空列表避免重复计算
        if (fillers.isEmpty()) {
            fillers = Collections.emptyList();
        } else {
            fillers = Collections.unmodifiableList(fillers);
        }

        // 放入缓存
        FILLER_CACHE.putIfAbsent(cacheKey, fillers);
        return FILLER_CACHE.get(cacheKey);
    }

    /**
     * 判断当前是否有已注册的填充处理器。
     *
     * @return true 表示至少有一个 Handler
     */
    public static boolean hasHandlers() {
        return !HANDLERS.isEmpty();
    }

    /**
     * 清除缓存（可指定实体类，null 表示清空全部）。
     *
     * @param entityClass 实体类，为 null 时清空全部缓存
     */
    private static void clearCacheByEntityClass(Class<?> entityClass) {
        if (entityClass == null) {
            FILLER_CACHE.clear();
        } else {
            // 只清空该实体类的缓存
            for (String key : FILLER_CACHE.keySet()) {
                if (key.startsWith(entityClass.getName() + ":")) {
                    FILLER_CACHE.remove(key);
                }
            }
        }
    }
}
