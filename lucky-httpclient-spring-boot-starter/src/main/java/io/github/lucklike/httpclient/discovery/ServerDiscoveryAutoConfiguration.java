package io.github.lucklike.httpclient.discovery;

import com.netflix.discovery.EurekaClient;
import io.github.lucklike.httpclient.discovery.eureka.EurekaDomainGetter;
import io.github.lucklike.httpclient.discovery.nacos.NacosDomainGetter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static io.github.lucklike.httpclient.discovery.Constant.EUREKA_DOMAIN_GETTER_BEAN_NAME;
import static io.github.lucklike.httpclient.discovery.Constant.NACOS_DOMAIN_GETTER_BEAN_NAME;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

@Configuration
public class ServerDiscoveryAutoConfiguration {

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {
            "com.alibaba.nacos.api.naming.NamingService",
            "com.alibaba.nacos.api.annotation.NacosInjected",
            "com.alibaba.boot.nacos.discovery.autoconfigure.NacosDiscoveryAutoConfiguration"
    })
    static class NacosAutoConfiguration {

        @Bean(NACOS_DOMAIN_GETTER_BEAN_NAME)
        public NacosDomainGetter nacosDomainGetter() {
            return new NacosDomainGetter();
        }
    }


    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {
            "com.netflix.discovery.EurekaClient",
            "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration"
    })
    static class EurekaAutoConfiguration {

        @Bean(EUREKA_DOMAIN_GETTER_BEAN_NAME)
        public EurekaDomainGetter eurekaDomainGetter() {
            return new EurekaDomainGetter();
        }
    }


}
