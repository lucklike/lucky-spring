package io.github.lucklike.httpclient.parameter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParameterInstanceFactoryAutoConfiguration {

    @Bean("__httpReferenceParameterInstanceFactory__")
    public ParameterInstanceFactory httpReferenceParameterInstanceFactory() {
        return new HttpReferenceParameterInstanceFactory();
    }

    @Bean("__qualifierParameterInstanceFactory__")
    public ParameterInstanceFactory qualifierParameterInstanceFactory() {
        return new QualifierParameterInstanceFactory();
    }

    @Bean("__valueParameterInstanceFactory__")
    public ParameterInstanceFactory valueParameterInstanceFactory() {
        return new ValueParameterInstanceFactory();
    }
}
