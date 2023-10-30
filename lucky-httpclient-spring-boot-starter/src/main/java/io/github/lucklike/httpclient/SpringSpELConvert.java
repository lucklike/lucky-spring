package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.SpELConvert;
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
        paramWrapper.setExpression(environment.resolvePlaceholders(paramWrapper.getExpression()));
        super.paramWrapperPostProcess(paramWrapper);
    }
}
