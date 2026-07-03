package io.github.lucklike.httpclient.discovery;

import com.luckyframework.reflect.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static io.github.lucklike.httpclient.discovery.Constant.LOAD_BALANCER_CLIENT_URL_GETTER_BEAN_NAME;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

@Configuration
public class ServerDiscoveryAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ServerDiscoveryAutoConfiguration.class);

    @Role(ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(name = {"org.springframework.cloud.client.loadbalancer.LoadBalancerClient"})
    static class SpringCloudAutoConfiguration {

        @Bean(LOAD_BALANCER_CLIENT_URL_GETTER_BEAN_NAME)
        public LoadBalancerClientUrlGetter springCloudDomainNameGetter(LoadBalancerClient loadBalancerClient) {
            logger.info("[☁️] LoadBalancerClientUrlGetter bean initialized successfully, loadBalancerClient implementation: {}", ClassUtils.getClassSimpleName(loadBalancerClient));
            return new LoadBalancerClientUrlGetter(loadBalancerClient);
        }
    }


}
