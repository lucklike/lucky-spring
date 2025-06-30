package io.github.lucklike.httpclient;

import io.github.lucklike.httpclient.injection.parameter.BindParameterInstanceFactory;
import io.github.lucklike.httpclient.injection.parameter.HttpReferenceParameterInstanceFactory;
import io.github.lucklike.httpclient.injection.parameter.ParameterInstanceFactory;
import io.github.lucklike.httpclient.injection.parameter.QualifierParameterInstanceFactory;
import io.github.lucklike.httpclient.injection.parameter.ValueParameterInstanceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class LuckyAutoInjectionAutoConfiguration {

    //---------------------------------------------------------------------------------------------------------
    //                                  ParameterInstanceFactory
    //---------------------------------------------------------------------------------------------------------

    @Order(Integer.MIN_VALUE)
    @Bean("__httpReferenceParameterInstanceFactory__")
    public ParameterInstanceFactory httpReferenceParameterInstanceFactory() {
        return new HttpReferenceParameterInstanceFactory();
    }

    @Order(Integer.MIN_VALUE + 1)
    @Bean("__qualifierParameterInstanceFactory__")
    public ParameterInstanceFactory qualifierParameterInstanceFactory() {
        return new QualifierParameterInstanceFactory();
    }

    @Order(Integer.MIN_VALUE + 2)
    @Bean("__valueParameterInstanceFactory__")
    public ParameterInstanceFactory valueParameterInstanceFactory() {
        return new ValueParameterInstanceFactory();
    }

    @Order(Integer.MIN_VALUE + 3)
    @Bean("__bindParameterInstanceFactory__")
    public ParameterInstanceFactory bindParameterInstanceFactory() {
        return new BindParameterInstanceFactory();
    }

}
