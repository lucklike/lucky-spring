package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.spel.SpELConvert;
import com.luckyframework.spel.ParamWrapper;
import com.luckyframework.spel.SpELRuntime;
import org.springframework.core.env.Environment;

/**
 * 提供持使用'${}'占位符从Spring环境变量中取值的功能，
 * ‘${}’表达式的优先级高于'#{}'表达式
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/24 04:52
 */
public class SpringSpELConvert extends SpELConvert {

    private final Environment environment;

    public SpringSpELConvert(Environment environment) {
        this.environment = environment;
    }

    public SpringSpELConvert(SpELRuntime spELRuntime, Environment environment) {
        super(spELRuntime);
        this.environment = environment;
    }

    @Override
    protected void paramWrapperPostProcess(ParamWrapper paramWrapper) {
        paramWrapper.setExpression(environment.resolveRequiredPlaceholders(paramWrapper.getExpression()));
        super.paramWrapperPostProcess(paramWrapper);
    }

    @Override
    protected boolean needParse(Object value) {
        if (value instanceof String) {
            String text = (String) value;
            return isSpELExpression(text) || isEnvValueLExpression(text);
        }
        return false;
    }

    /**
     * 是否为环境变量取值表达式
     *
     * @param text 待判断的文本
     * @return 否为环境变量取值表达式
     */
    protected boolean isEnvValueLExpression(String text) {
        return isExpression(text, "${", "}");
    }
}
