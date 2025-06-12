package io.github.lucklike.httpclient.parameter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ParameterInstanceFactoryAutoConfiguration {

    @Order(Integer.MIN_VALUE)
    @Bean("__httpReferenceParameterInstanceFactory__")
    public ParameterInstanceFactory httpReferenceParameterInstanceFactory() {
        return new HttpReferenceParameterInstanceFactory();
    }

    @Order(Integer.MIN_VALUE + 1)
    @Bean("__objectProviderParameterInstanceFactory__")
    public ParameterInstanceFactory objectProviderParameterInstanceFactory() {
        return new ObjectProviderParameterInstanceFactory();
    }

    @Order(Integer.MIN_VALUE + 2)
    @Bean("__qualifierParameterInstanceFactory__")
    public ParameterInstanceFactory qualifierParameterInstanceFactory() {
        return new QualifierParameterInstanceFactory();
    }

    @Order(Integer.MIN_VALUE + 3)
    @Bean("__valueParameterInstanceFactory__")
    public ParameterInstanceFactory valueParameterInstanceFactory() {
        return new ValueParameterInstanceFactory();
    }
}
