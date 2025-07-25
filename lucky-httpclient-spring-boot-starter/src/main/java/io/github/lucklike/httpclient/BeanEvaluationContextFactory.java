package io.github.lucklike.httpclient;

import com.luckyframework.spel.EvaluationContextFactory;
import com.luckyframework.spel.ParamWrapper;
import com.luckyframework.spel.SpELRuntime;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;

/**
 * {@link SpELRuntime} 对象工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:13
 */
public class BeanEvaluationContextFactory implements EvaluationContextFactory {

    private final BeanFactory beanFactory;

    public BeanEvaluationContextFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public EvaluationContext getEvaluationContext(ParamWrapper paramWrapper) {
        StandardEvaluationContext evaluationContext = (StandardEvaluationContext) EvaluationContextFactory.DEFAULT_FACTORY.getEvaluationContext(paramWrapper);
        evaluationContext.addPropertyAccessor(new EnvironmentAccessor());
        evaluationContext.addPropertyAccessor(new BeanFactoryAccessor());
        evaluationContext.addPropertyAccessor(new BeanExpressionContextAccessor());
        evaluationContext.addPropertyAccessor(DataBindingPropertyAccessor.forReadWriteAccess());
        evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        evaluationContext.setTypeConverter(new StandardTypeConverter(new ApplicationConversionService()));
        return evaluationContext;
    }
}
