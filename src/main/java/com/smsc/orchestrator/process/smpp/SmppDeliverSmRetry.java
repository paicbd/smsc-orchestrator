package com.smsc.orchestrator.process.smpp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.Converter;
import com.smsc.orchestrator.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmppDeliverSmRetry {
    private final AppProperties properties;
    private final JedisCluster jedisCluster;
    private final SmppDeliverPreProcess deliverPreProcess;

    @Scheduled(fixedRateString = "${smpp.retries.every}") // 10 minutes
    public void startScheduler() {
        log.info("Processing SMPP items to retry");
        int listSize = (int) jedisCluster.llen(properties.getSmppRetryList());
        if (listSize == 0) {
            log.info("No items to retry");
            return;
        }

        List<String> toRetry = jedisCluster.lpop(properties.getSmppRetryList(), listSize);
        log.info("Processing items to retry. Size: {}", toRetry.size());
        toRetry.parallelStream().forEach(value -> {
            MessageEvent deliverSmEvent = Converter.stringToObject(value, new TypeReference<>() {
            });
            deliverPreProcess.processDeliverSm(deliverSmEvent);
        });
    }
}
