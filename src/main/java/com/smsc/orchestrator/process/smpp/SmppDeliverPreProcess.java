package com.smsc.orchestrator.process.smpp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.Watcher;
import com.smsc.orchestrator.utils.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Component
public class SmppDeliverPreProcess {
    private final AppProperties properties;
    private final JedisCluster jedisCluster;
    private final AtomicInteger requestPerSecond = new AtomicInteger(0);

    public SmppDeliverPreProcess(AppProperties properties, JedisCluster jedisCluster) {
        this.properties = properties;
        this.jedisCluster = jedisCluster;
        Thread.startVirtualThread(() -> new Watcher("SMPP-Pre-Deliver", requestPerSecond, 1));
    }

    @Scheduled(fixedRateString = "${smpp.queue.deliverySm.consumer.scheduler}")
    public void startScheduler() {
        try {
            IntStream.range(0, properties.getSmppWorkers()).parallel().forEach(i -> {
                Collection<String> submitSmItems = jedisCluster.lpop(properties.getSmppDeliverySmQueue(), properties.getSmppBatchSizePerWorker());

                if (Objects.isNull(submitSmItems)) {
                    return;
                }

                Collection<MessageEvent> deliverSmEvents = submitSmItems.parallelStream()
                        .map(x -> Converter.<MessageEvent>stringToObject(x, new TypeReference<>() {
                        }))
                        .filter(Objects::nonNull)
                        .toList();

                Flux.fromIterable(deliverSmEvents)
                        .parallel()
                        .runOn(Schedulers.boundedElastic())
                        .doOnNext(this::processDeliverSm)
                        .subscribe();
            });
        } catch (Exception ex) {
            log.error("An error occurred in the scheduler", ex);
        }
    }

    @Async
    public void processDeliverSm(MessageEvent deliverSmEvent) {
        try {
            if (Boolean.TRUE.equals(deliverSmEvent.getCheckSubmitSmResponse())) {
                String key = deliverSmEvent.getSystemId() + "-" + deliverSmEvent.getDeliverSmId();
                String submitResponseRaw = jedisCluster.hget("submit_sm_result", key);
                if (submitResponseRaw != null) {
                    UtilsRecords.SubmitSmResponseEvent submitSmResponseEvent = Converter.stringToObject(submitResponseRaw, new TypeReference<>() {
                    });
                    deliverSmEvent.setDeliverSmServerId(submitSmResponseEvent.submitSmServerId());
                    deliverSmEvent.setSystemId(submitSmResponseEvent.systemId());
                    deliverSmEvent.setParentId(submitSmResponseEvent.parentId());
                    String protocol = submitSmResponseEvent.originProtocol();
                    switch (protocol) {
                        case "SMPP" -> jedisCluster.rpush(properties.getSmppQueue(), deliverSmEvent.toString());
                        case "HTTP" -> jedisCluster.rpush(properties.getHttpQueue(), deliverSmEvent.toString());
                        default -> log.error("Protocol not found for key {}", key);
                    }
                    requestPerSecond.incrementAndGet();
                    jedisCluster.hdel("submit_sm_result", key);
                } else {
                    log.error("Getting null for send deliverSm for key {}. This DLR will be retry", key);
                    jedisCluster.rpush(properties.getSmppRetryList(), deliverSmEvent.toString());
                }
            }
        } catch (Exception ex) {
            log.error("Error on process deliver_sm with exception {}", ex.getMessage());
        }
    }
}
