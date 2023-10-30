package io.github.lucklike.httpclient;

import com.luckyframework.spel.AnnotationAccessor;
import com.luckyframework.spel.ClassFieldAccessor;
import com.luckyframework.spel.EvaluationContextFactory;
import com.luckyframework.spel.NotExistReturnNullMapAccessor;
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
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.util.ClassUtils;

/**
 * {@link SpELRuntime} 对象工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:13
 */
public class SpELRuntimeFactory implements EvaluationContextFactory {

    private final BeanFactory beanFactory;

    public SpELRuntimeFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public SpELRuntime createSpELRuntime() {
        return new SpELRuntime(this);
    }

    @Override
    public EvaluationContext getEvaluationContext() {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.addPropertyAccessor(new NotExistReturnNullMapAccessor());
        evaluationContext.addPropertyAccessor(new EnvironmentAccessor());
        evaluationContext.addPropertyAccessor(new BeanFactoryAccessor());
        evaluationContext.addPropertyAccessor(new AnnotationAccessor());
        evaluationContext.addPropertyAccessor(new BeanExpressionContextAccessor());
        evaluationContext.addPropertyAccessor(DataBindingPropertyAccessor.forReadWriteAccess());
        evaluationContext.addPropertyAccessor(new ClassFieldAccessor());
        evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        evaluationContext.setTypeLocator(new StandardTypeLocator(ClassUtils.getDefaultClassLoader()));
        evaluationContext.setTypeConverter(new StandardTypeConverter(new ApplicationConversionService()));
        return evaluationContext;
    }
}
