package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.spel.SpELConvert;
import com.luckyframework.spel.ParamWrapper;
import com.luckyframework.spel.SpELRuntime;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

/**
 * 提供持使用'${}'占位符从Spring环境变量中取值的功能，
 * <pre>
 *     1.如果表达式是一个单一的环境变量表达式，即以'${'开头，以'}'结尾的表达式，则会先进行一次 SpEL 计算
 *     2.其他情况会优先执行‘${}’，再执行'#{}'
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/24 04:52
 */
public class SpringSpELConvert extends SpELConvert {

    /**
     * 环境变量表达式前嘴
     */
    private final String ENV_EXPRESSION_PREFIX = "${";

    /**
     * 环境变量表达式前嘴
     */
    private final String ENV_EXPRESSION_SUFFIX = "}";

    /**
     * 环境变量
     */
    private final Environment environment;

    public SpringSpELConvert(Environment environment) {
        this.environment = environment;
    }

    public SpringSpELConvert(SpELRuntime spELRuntime, Environment environment, String nestExpressionPrefix, String nestExpressionSuffix) {
        super(spELRuntime, nestExpressionPrefix, nestExpressionSuffix);
        this.environment = environment;
    }

    public SpringSpELConvert(SpELRuntime spELRuntime, Environment environment) {
        super(spELRuntime);
        this.environment = environment;
    }

    @Override
    protected void paramWrapperPostProcess(ParamWrapper paramWrapper) {
        super.paramWrapperPostProcess(paramWrapper);
        String expression = paramWrapper.getExpression();
        if (isSingleEnvExpression(expression)) {
            ResolvableType sourceResult = paramWrapper.getExpectedResultType();
            expression = getSpELRuntime().getValueForType(paramWrapper.setExpectedResultType(String.class));
            paramWrapper.setExpectedResultType(sourceResult);
        }
        paramWrapper.setExpression(environment.resolveRequiredPlaceholders(expression));
    }

    @Override
    protected boolean needParse(Object value) {
        if (value instanceof String) {
            String text = (String) value;
            return isSpELExpression(text) || isEnvExpression(text);
        }
        return false;
    }

    /**
     * 是否为环境变量取值表达式
     *
     * @param text 待判断的文本
     * @return 否为环境变量取值表达式
     */
    protected boolean isEnvExpression(String text) {
        return isExpression(text, ENV_EXPRESSION_PREFIX, ENV_EXPRESSION_SUFFIX);
    }

    /**
     * 判断是否为单一环境变量取值表达式，即以'${'开头，以'}'结尾的表达式？
     *
     * @param text 待判断的文本
     * @return 否为单一环境变量取值表达式
     */
    private boolean isSingleEnvExpression(String text) {
        String expression = text.trim();
        return expression.startsWith(ENV_EXPRESSION_PREFIX) && expression.endsWith(ENV_EXPRESSION_SUFFIX);
    }
}
