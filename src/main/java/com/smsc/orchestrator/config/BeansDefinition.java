package com.smsc.orchestrator.config;

import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import com.smsc.orchestrator.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BeansDefinition {
    private final AppProperties properties;

    @Bean
    public synchronized JedisCluster jedisCluster() {
        return Converter.paramsToJedisCluster(getJedisClusterParams(properties.getRedisNodes(), properties.getMaxTotal(),
                properties.getMinIdle(), properties.getMaxIdle(), properties.isBlockWhenExhausted()));
    }

    private UtilsRecords.JedisConfigParams getJedisClusterParams(List<String> nodes, int maxTotal, int minIdle, int maxIdle, boolean blockWhenExhausted) {
        return new UtilsRecords.JedisConfigParams(nodes, maxTotal, minIdle, maxIdle, blockWhenExhausted);
    }
}
