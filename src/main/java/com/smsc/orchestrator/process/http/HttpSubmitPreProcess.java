package com.smsc.orchestrator.process.http;

import com.smsc.orchestrator.utils.OrchestratorUtils;
import com.smsc.orchestrator.utils.AppProperties;
import com.paicbd.smsc.utils.Watcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Component
public class HttpSubmitPreProcess {
    private final AppProperties properties;
    private final JedisCluster jedisCluster;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicInteger requestPerSecond = new AtomicInteger(0);

    public HttpSubmitPreProcess(AppProperties properties, JedisCluster jedisCluster) {
        this.properties = properties;
        this.jedisCluster = jedisCluster;
        Thread.startVirtualThread(() -> new Watcher("HTTP-Pre-Submit", requestPerSecond, 1));
    }

    @Scheduled(fixedRateString = "${http.queue.submitSm.consumer.scheduler}")
    public void startScheduler() {
        try {
            IntStream.range(0, properties.getHttpWorkers()).parallel().forEach(i -> {
                Collection<String> messageItems = jedisCluster.lpop(properties.getHttpSubmitSmQueue(), properties.getHttpBatchSizePerWorker());
                OrchestratorUtils.processItems(requestPerSecond, executorService, jedisCluster, messageItems, "_http_message");
            });
        } catch (Exception ex) {
            log.error("An error occurred in the scheduler", ex);
        }
    }
}
