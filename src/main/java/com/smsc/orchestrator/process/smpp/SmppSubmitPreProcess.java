package com.smsc.orchestrator.process.smpp;

import com.paicbd.smsc.utils.Watcher;
import com.smsc.orchestrator.utils.OrchestratorUtils;
import com.smsc.orchestrator.utils.AppProperties;
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
public class SmppSubmitPreProcess {
    private final AppProperties properties;
    private final JedisCluster jedisCluster;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicInteger requestPerSecond = new AtomicInteger(0);

    public SmppSubmitPreProcess(AppProperties properties, JedisCluster jedisCluster) {
        this.properties = properties;
        this.jedisCluster = jedisCluster;
        Thread.startVirtualThread(() -> new Watcher("SMPP-Pre-Submit", requestPerSecond, 1));
    }

    @Scheduled(fixedRateString = "${smpp.queue.submitSm.consumer.scheduler}")
    public void startScheduler() {
        try {
            IntStream.range(0, properties.getSmppWorkers()).parallel().forEach(i -> {
                Collection<String> messageItems = jedisCluster.lpop(properties.getSmppSubmitSmQueue(), properties.getSmppBatchSizePerWorker());
                OrchestratorUtils.processItems(requestPerSecond, executorService, jedisCluster, messageItems, "_smpp_message");
            });
        } catch (Exception ex) {
            log.error("An error occurred in the scheduler", ex);
        }
    }
}
