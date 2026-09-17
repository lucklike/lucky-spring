package io.github.lucklike.httpclient.token;

import com.luckyframework.common.FontUtil;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.generalapi.token.TokenCacheException;
import com.luckyframework.httpclient.generalapi.token.TokenManager;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.plugin.ProxyDecorator;
import com.luckyframework.httpclient.proxy.plugin.ProxyPlugin;
import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.luckyframework.httpclient.core.serialization.SerializationConstant.JSON_SCHEME;

/**
 * 用于实现Token缓存的Redis代理插件
 * <pre>
 *     1.注：使用该插件的方法返回值类型必须要实现{@link RedisToken}接口
 *     2.刷新Token后会将方法返回的Token对象序列化后作为值写入Redis，
 *       将{@link RedisToken#expires()}（过期时间，单位毫秒）直接作为过期时间(TTL)写入Redis
 *     3.缓存的Token未过期时将会一直使用Redis中缓存的Token，不会调用真实方法重新获取Token
 *     4.缓存的Token不存在或者已经过期时，将会调用真实方法重新获取Token
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/9/18 10:00
 */
public final class RedisTokenCacheProxyPlugin implements ProxyPlugin {

    /**
     * 默认的RedisTemplate Bean名称
     */
    private static final String DEFAULT_REDIS_TEMPLATE_BEAN_NAME = "redisTemplate";

    /**
     * Token管理器缓存
     */
    private final Map<Method, TokenManager<?>> tokenManagerCache = new ConcurrentHashMap<>();

    @Override
    public Object decorate(ProxyDecorator decorator) {
        MethodMetaContext mc = decorator.getMeta().getMethodMetaContext();
        ResolvableType returnResolvableType = mc.getMethodConvertReturnResolvableType();

        // 方法返回值类型检查
        Class<?> resolveClass = returnResolvableType.toClass();
        if (!RedisToken.class.isAssignableFrom(resolveClass)) {
            throw new TokenCacheException("RedisTokenCacheProxyPlugin decorate method ['{}'] return type is not 'io.github.lucklike.httpclient.token.RedisToken'", FontUtil.getYellowUnderline(MethodUtils.getLocation(mc.getCurrentAnnotatedElement())));
        }

        // 获取注解配置信息
        UseRedisTokenManager tokenManagerAnn = mc.getMergedAnnotation(UseRedisTokenManager.class);
        String key = mc.parseExpression(tokenManagerAnn.key(), String.class);
        String redisTemplateBeanName = mc.parseExpression(tokenManagerAnn.redisTemplate(), String.class);

        // 未配置Key时使用当前方法的位置信息作为Key
        if (!StringUtils.hasText(key)) {
            key = MethodUtils.getLocation(mc.getCurrentAnnotatedElement());
        }

        return createTokenManager(decorator, returnResolvableType, key, redisTemplateBeanName).getToken();
    }

    /**
     * 创建Token管理器
     * <pre>
     *     1.如果缓存中存在则使用缓存中的
     *     2.缓存中不存在时构建{@link RedisTokenCacheManager}并存入缓存
     * </pre>
     *
     * @param decorator            代理装饰器
     * @param returnResolvableType 方法返回值类型
     * @param key                  缓存Token的Redis Key
     * @param redisTemplateBeanName RedisTemplate Bean名称
     * @return Token管理器
     */
    private TokenManager<?> createTokenManager(ProxyDecorator decorator, ResolvableType returnResolvableType, String key, String redisTemplateBeanName) {
        Method method = decorator.getMeta().getMethod();
        TokenManager<?> tokenManager = tokenManagerCache.get(method);
        if (tokenManager == null) {
            tokenManager = new RedisTokenCacheManager(key, returnResolvableType.getType(), redisTemplateBeanName);
            tokenManagerCache.put(method, tokenManager);
        }

        if (tokenManager instanceof ProxyDecoratorAware) {
            ((ProxyDecoratorAware) tokenManager).setDecorator(decorator);
        }
        return tokenManager;
    }

    /**
     * 装饰器感知接口
     */
    interface ProxyDecoratorAware {

        void setDecorator(ProxyDecorator decorator);
    }

    /**
     * 将Token缓存到Redis中的管理器
     */
    static class RedisTokenCacheManager extends TokenManager<RedisToken> implements ProxyDecoratorAware {

        private static final Logger log = LoggerFactory.getLogger(RedisTokenCacheManager.class);

        private final String key;
        private final Type tokenType;
        private final String redisTemplateBeanName;

        private ProxyDecorator decorator;
        private RedisTemplate<String, Object> redisTemplate;

        RedisTokenCacheManager(String key, Type tokenType, String redisTemplateBeanName) {
            this.key = key;
            this.tokenType = tokenType;
            this.redisTemplateBeanName = redisTemplateBeanName;
        }

        @Override
        public void setDecorator(ProxyDecorator decorator) {
            this.decorator = decorator;
        }

        @Override
        protected RedisToken getCachedToken() {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            // 读取缓存的Token值
            Object cacheValue;
            try {
                cacheValue = redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                throw new TokenCacheException(e, "Failed to read token from redis! key: ['{}']", key);
            }

            // Redis中不存在缓存时视为未命中
            if (cacheValue == null) {
                return null;
            }

            // 将缓存的字符串反序列化为Token对象，反序列化失败时视为未命中
            String tokenValue = cacheValue instanceof String ? (String) cacheValue : cacheValue.toString();
            try {
                return (RedisToken) JSON_SCHEME.deserialization(tokenValue, tokenType);
            } catch (Exception e) {
                log.warn("Failed to deserialize the cached token, it will be treated as a cache miss! key: [{}], decorate method is ['{}']", key, FontUtil.getYellowUnderline(getMethodLocation()), e);
                return null;
            }
        }

        @Override
        protected RedisToken refreshToken(@Nullable RedisToken oldToken) {
            try {
                return (RedisToken) decorator.proceed();
            } catch (Throwable e) {
                throw new TokenCacheException(e, "RedisTokenCacheProxyPlugin refreshToken error! decorate method is ['{}']", FontUtil.getYellowUnderline(getMethodLocation()));
            }
        }

        @Override
        protected void saveToken(RedisToken token) {

            // expires()即为Token的过期时间（毫秒），直接作为Redis的过期时间(TTL)写入
            long expires = token.expires();
            if (expires <= 0) {
                log.warn("The token will not be cached because the 'expires' returned by RedisToken#expires() is not a positive number! key: [{}], expires: [{}], decorate method is ['{}']", key, expires, FontUtil.getYellowUnderline(getMethodLocation()));
                return;
            }

            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
            try {
                redisTemplate.opsForValue().set(key, JSON_SCHEME.serialization(token), expires, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new TokenCacheException(e, "Failed to save token to redis! key: ['{}']", key);
            }
        }

        /**
         * 判断Token是否已经过期
         * <pre>
         *     Token的实际过期时间由Redis控制，缓存的Token不存在或者已经过期时视为Token已经过期
         * </pre>
         *
         * @param token Token数据
         * @return Token是否已经过期
         */
        @Override
        protected boolean isExpires(RedisToken token) {
            // Token在Redis中的剩余存活时间（毫秒）
            long remainingTime;
            try {
                remainingTime = getRedisTemplate().getExpire(key, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new TokenCacheException(e, "Failed to get the remaining expiration time of the token from redis! key: ['{}']", key);
            }

            // 缓存不存在或者已经过期时视为Token已经过期
            return remainingTime <= 0;
        }

        /**
         * 获取当前被装饰方法的位置信息
         *
         * @return 方法的位置信息
         */
        private String getMethodLocation() {
            return MethodUtils.getLocation(decorator.getMeta().getMethod());
        }

        /**
         * 获取用于读写Token数据的RedisTemplate
         *
         * @return RedisTemplate
         */
        private RedisTemplate<String, Object> getRedisTemplate() {
            if (redisTemplate == null) {
                redisTemplate = StringUtils.hasText(redisTemplateBeanName)
                        ? getRedisTemplateByBeanName(redisTemplateBeanName)
                        : getDefaultRedisTemplate();
            }
            return redisTemplate;
        }

        /**
         * 根据Bean名称从Spring容器中获取RedisTemplate
         *
         * @param beanName Bean名称
         * @return RedisTemplate
         */
        @SuppressWarnings("unchecked")
        private static RedisTemplate<String, Object> getRedisTemplateByBeanName(String beanName) {
            if (!ApplicationContextUtils.containsBean(beanName)) {
                throw new TokenCacheException("The RedisTemplate bean named ['{}'] does not exist in the Spring container", beanName);
            }
            return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(beanName, RedisTemplate.class);
        }

        /**
         * 获取默认的RedisTemplate
         * <pre>
         *     1.优先使用名称为"redisTemplate"的Bean
         *     2.不存在时按照类型匹配Spring容器中唯一的RedisTemplate Bean
         * </pre>
         *
         * @return RedisTemplate
         */
        @SuppressWarnings("unchecked")
        private static RedisTemplate<String, Object> getDefaultRedisTemplate() {
            if (ApplicationContextUtils.containsBean(DEFAULT_REDIS_TEMPLATE_BEAN_NAME)) {
                return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(DEFAULT_REDIS_TEMPLATE_BEAN_NAME, RedisTemplate.class);
            }

            String[] beanNames = ApplicationContextUtils.getBeanNamesForType(RedisTemplate.class);
            if (beanNames.length == 0) {
                throw new TokenCacheException("No RedisTemplate bean found in the Spring container! Please define a RedisTemplate bean named '{}', or specify the Bean name to use through the 'redisTemplate' attribute of @RedisTokenManager", DEFAULT_REDIS_TEMPLATE_BEAN_NAME);
            }
            if (beanNames.length > 1) {
                throw new TokenCacheException("Multiple RedisTemplate beans found in the Spring container: {}, please specify the Bean name to use through the 'redisTemplate' attribute of @RedisTokenManager", Arrays.toString(beanNames));
            }
            return (RedisTemplate<String, Object>) ApplicationContextUtils.getBean(beanNames[0], RedisTemplate.class);
        }
    }
}
