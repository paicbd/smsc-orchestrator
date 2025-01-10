package com.smsc.orchestrator.utils;

import com.paicbd.smsc.utils.Generated;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@Generated
public class AppProperties {
    // Redis
    @Value("#{'${redis.cluster.nodes}'.split(',')}")
    private List<String> redisNodes;

    @Value("${redis.threadPool.maxTotal}")
    private int redisMaxTotal;

    @Value("${redis.threadPool.maxIdle}")
    private int redisMaxIdle;

    @Value("${redis.threadPool.minIdle}")
    private int redisMinIdle;

    @Value("${redis.threadPool.blockWhenExhausted}")
    private boolean redisBlockWhenExhausted;

    @Value("${redis.connection.timeout:0}")
    private int redisConnectionTimeout;

    @Value("${redis.so.timeout:0}")
    private int redisSoTimeout;

    @Value("${redis.maxAttempts:0}")
    private int redisMaxAttempts;

    @Value("${redis.connection.password:}")
    private String redisPassword;

    @Value("${redis.connection.user:}")
    private String redisUser;

    // Smpp Traffic
    @Value("${smpp.redis.submitSm.queue}")
    private String smppSubmitSmQueue;

    @Value("${smpp.queue.submitSm.consumer.workers}")
    private int smppWorkers;

    @Value("${smpp.queue.submitSm.consumer.batch.size}")
    private int smppBatchSizePerWorker;

    @Value("${smpp.queue.submitSm.consumer.scheduler}")
    private int smppConsumerScheduler; // milliseconds


    // Smpp Dlrs
    @Value("${smpp.redis.deliverySm.queue}")
    private String smppDeliverySmQueue;

    @Value("${smpp.queue.deliverySm.consumer.workers}")
    private int smppDeliveryWorkers;

    @Value("${smpp.queue.deliverySm.consumer.batch.size}")
    private int smppDeliveryBatchSizePerWorker;

    @Value("${smpp.queue.deliverySm.consumer.scheduler}")
    private int smppDeliveryConsumerScheduler; // milliseconds

    @Value("${smpp.redis.deliverySm.retryList}")
    private String smppRetryList;

    // Http Traffic
    @Value("${http.redis.submitSm.queue}")
    private String httpSubmitSmQueue;

    @Value("${http.queue.submitSm.consumer.workers}")
    private int httpWorkers;

    @Value("${http.queue.submitSm.consumer.batch.size}")
    private int httpBatchSizePerWorker;

    @Value("${http.queue.submitSm.consumer.scheduler}")
    private int httpConsumerScheduler; // milliseconds

    // Http Dlrs
    @Value("${http.redis.deliverySm.queue}")
    private String httpDeliverySmQueue;

    @Value("${http.queue.deliverySm.consumer.workers}")
    private int httpDeliveryWorkers;

    @Value("${http.queue.deliverySm.consumer.batch.size}")
    private int httpDeliveryBatchSizePerWorker;

    @Value("${http.queue.deliverySm.consumer.scheduler}")
    private int httpDeliveryConsumerScheduler; // milliseconds

    @Value("${http.redis.deliverySm.retryList}")
    private String httpRetryList;

    // SS7 Traffic
    @Value("${ss7.redis.submitSm.queue}")
    private String ss7SubmitSmQueue;

    @Value("${ss7.queue.submitSm.consumer.workers}")
    private int ss7Workers;

    @Value("${ss7.queue.submitSm.consumer.batch.size}")
    private int ss7BatchSizePerWorker;

    @Value("${ss7.queue.submitSm.consumer.scheduler}")
    private int ss7ConsumerScheduler; // milliseconds

    // Global DLR queues
    @Value("${redis.deliverySm.smppQueue}")
    private String smppQueue;

    @Value("${redis.deliverySm.httpQueue}")
    private String httpQueue;

}
