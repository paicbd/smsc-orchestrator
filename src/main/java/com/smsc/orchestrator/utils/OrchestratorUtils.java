package com.smsc.orchestrator.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.Converter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisCluster;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class OrchestratorUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private OrchestratorUtils() {
        throw new IllegalStateException("Utility Class");
    }

    public static void processItems(AtomicInteger requestPerSecond, ExecutorService executorService, JedisCluster jedisCluster, Collection<String> messageItems, String listName) {
        CompletableFuture.runAsync(() -> {
            if (messageItems != null) {
                int size = messageItems.size();
                Collection<MessageEvent> submitSmEvents = messageItems.parallelStream()
                        .map(x -> Converter.<MessageEvent>stringToObject(x, new TypeReference<>() {
                        }))
                        .filter(Objects::nonNull)
                        .toList();

                // Group by destNetworkId
                submitSmEvents.parallelStream()
                        .collect(Collectors.groupingBy(MessageEvent::getDestNetworkId))
                        .forEach((networkId, listMessage) -> {
                            String queueName = networkId + listName;
                            jedisCluster.rpush(queueName, listMessage.parallelStream()
                                    .map(MessageEvent::toString)
                                    .toList().toArray(String[]::new));
                        });
                requestPerSecond.addAndGet(size);
            }
        }, executorService);
    }

    public static Records.DlrRequest parseDlrRequest(String value) {
        try {
            return objectMapper.readValue(value, Records.DlrRequest.class);
        } catch (Exception e) {
            log.error("An error occurred parsing DeliverSmEvent", e);
            return null;
        }
    }
}
