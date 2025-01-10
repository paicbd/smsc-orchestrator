package com.smsc.orchestrator.config;

import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import com.smsc.orchestrator.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;


@Configuration
@RequiredArgsConstructor
public class BeansDefinition {
    private final AppProperties properties;

    @Bean
    public JedisCluster jedisCluster() {
        return Converter.paramsToJedisCluster(
                new UtilsRecords.JedisConfigParams(properties.getRedisNodes(), properties.getRedisMaxTotal(),
                        properties.getRedisMaxIdle(), properties.getRedisMinIdle(),
                        properties.isRedisBlockWhenExhausted(), properties.getRedisConnectionTimeout(),
                        properties.getRedisSoTimeout(), properties.getRedisMaxAttempts(),
                        properties.getRedisUser(), properties.getRedisPassword())
        );
    }
}
