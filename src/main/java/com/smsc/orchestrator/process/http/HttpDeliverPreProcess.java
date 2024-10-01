package com.smsc.orchestrator.process.http;

import com.paicbd.smsc.utils.Watcher;
import com.smsc.orchestrator.utils.AppProperties;
import com.smsc.orchestrator.utils.OrchestratorUtils;
import com.smsc.orchestrator.utils.Records;
import lombok.extern.slf4j.Slf4j;
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
public class HttpDeliverPreProcess {
    private final AppProperties properties;
    private final JedisCluster jedisCluster;
    private final AtomicInteger requestPerSecond = new AtomicInteger(0);
    private final HttpDeliverSmRetry httpDeliverSmRetry;

    public HttpDeliverPreProcess(AppProperties properties, JedisCluster jedisCluster, HttpDeliverSmRetry httpDeliverSmRetry) {
        this.properties = properties;
        this.jedisCluster = jedisCluster;
        this.httpDeliverSmRetry = httpDeliverSmRetry;
        Thread.startVirtualThread(() -> new Watcher("HTTP-Pre-Deliver", requestPerSecond, 1));
    }

    @Scheduled(fixedRateString = "${http.queue.deliverySm.consumer.scheduler}")
    public void startScheduler() {
        try {
            IntStream.range(0, properties.getHttpWorkers()).parallel().forEach(i -> {
                Collection<String> submitSmItems = jedisCluster.lpop(properties.getHttpDeliverySmQueue(), properties.getHttpBatchSizePerWorker());

                if (Objects.isNull(submitSmItems)) {
                    return;
                }

                Collection<Records.DlrRequest> deliverSmEvents = submitSmItems.parallelStream()
                        .map(OrchestratorUtils::parseDlrRequest)
                        .filter(Objects::nonNull)
                        .toList();

                Flux.fromIterable(deliverSmEvents)
                        .parallel()
                        .runOn(Schedulers.boundedElastic())
                        .doOnNext(httpDeliverSmRetry::createDlrPreProcess)
                        .doOnNext(request -> requestPerSecond.incrementAndGet())
                        .subscribe();
            });
        } catch (Exception ex) {
            log.error("An error occurred in the scheduler", ex);
        }
    }
}
