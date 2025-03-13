package io.github.lucklike.httpclient.discovery;

import io.github.lucklike.httpclient.discovery.cloud.SpringCloudDomainNameGetter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static io.github.lucklike.httpclient.discovery.Constant.SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

@Configuration
public class ServerDiscoveryAutoConfiguration {

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {
            "org.springframework.cloud.client.loadbalancer.LoadBalancerClient"
    })
    static class SpringCloudAutoConfiguration {

        @Bean(SPRING_CLOUD_DOMAIN_GETTER_BEAN_NAME)
        public SpringCloudDomainNameGetter springCloudDomainNameGetter() {
            return new SpringCloudDomainNameGetter();
        }
    }


}
