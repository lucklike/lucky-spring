package io.github.lucklike.httpclient.discovery;

import io.github.lucklike.httpclient.discovery.nacos.NacosDomainGetter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static io.github.lucklike.httpclient.discovery.Constant.NACOS_DOMAIN_GETTER_BEAN_NAME;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

@Configuration
public class ServerDiscoveryAutoConfiguration {

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {
            "com.alibaba.nacos.api.annotation.NacosInjected",
            "com.alibaba.boot.nacos.discovery.properties.NacosDiscoveryProperties"
    })
    static class NacosAutoConfiguration {

        @Bean(NACOS_DOMAIN_GETTER_BEAN_NAME)
        public NacosDomainGetter nacosDomainGetter() {
            return new NacosDomainGetter();
        }
    }


}
