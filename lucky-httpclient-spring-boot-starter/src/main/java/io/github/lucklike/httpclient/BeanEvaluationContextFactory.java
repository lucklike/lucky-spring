package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.spel.LuckyHttpClientEvaluationContextFactory;
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
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;

import java.util.List;

/**
 * {@link SpELRuntime} 对象工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:13
 */
public class BeanEvaluationContextFactory implements EvaluationContextFactory {

    private final EvaluationContextFactory delegate = new LuckyHttpClientEvaluationContextFactory();
    private final BeanFactory beanFactory;

    public BeanEvaluationContextFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public EvaluationContext getEvaluationContext(ParamWrapper paramWrapper) {
        StandardEvaluationContext evaluationContext = (StandardEvaluationContext) delegate.getEvaluationContext(paramWrapper);

        List<PropertyAccessor> propertyAccessors = evaluationContext.getPropertyAccessors();

        propertyAccessors.add(propertyAccessors.size() - 1, new EnvironmentAccessor());
        propertyAccessors.add(propertyAccessors.size() - 1, new BeanFactoryAccessor());
        propertyAccessors.add(propertyAccessors.size() - 1, new BeanExpressionContextAccessor());
        propertyAccessors.add(propertyAccessors.size() - 1, DataBindingPropertyAccessor.forReadWriteAccess());
        evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        evaluationContext.setTypeConverter(new StandardTypeConverter(new ApplicationConversionService()));
        return evaluationContext;
    }
}
