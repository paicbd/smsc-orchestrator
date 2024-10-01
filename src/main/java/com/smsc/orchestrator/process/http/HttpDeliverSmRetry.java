package com.smsc.orchestrator.process.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.UtilsEnum;
import com.smsc.orchestrator.utils.AppProperties;
import com.smsc.orchestrator.utils.OrchestratorUtils;
import com.smsc.orchestrator.utils.Records;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.DeliveryReceipt;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpDeliverSmRetry {
    private final AppProperties properties;
    private final JedisCluster jedisCluster;

    @Scheduled(fixedRateString = "${http.retries.every}")
    public void startHttpDlrScheduler() {
        log.info("Processing HTTP DeliverSm retry");
        int listSize = (int) jedisCluster.llen(properties.getHttpRetryList());
        if (listSize == 0) {
            log.info("No items to retry");
            return;
        }

        List<String> toRetry = jedisCluster.lpop(properties.getHttpRetryList(), listSize);
        Flux.fromIterable(toRetry)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .doOnNext(value -> {
                    Records.DlrRequest request = OrchestratorUtils.parseDlrRequest(value);
                    createDlrPreProcess(request);
                })
                .doOnError(e -> log.error("An error occurred processing items", e))
                .subscribe();
    }

    public void createDlrPreProcess(Records.DlrRequest dlrRequest) {
        CompletableFuture.runAsync(() -> {
            String resultEvent = jedisCluster.hget("http_submit_sm_result", dlrRequest.messageId());
            if (resultEvent == null) {
                log.error("Getting null messageId -> {}", dlrRequest.messageId());
                jedisCluster.rpush(properties.getHttpRetryList(), dlrRequest.toString());
                return;
            }

            UtilsRecords.SubmitSmResponseEvent responseEvent = Converter.stringToObject(resultEvent, new TypeReference<>() {
            });
            MessageEvent preProcess = new MessageEvent();
            preProcess.setId(System.currentTimeMillis() + "-" + System.nanoTime());
            preProcess.setStatus(dlrRequest.status());
            preProcess.setDeliverSmServerId(responseEvent.submitSmServerId());
            preProcess.setSourceAddr(dlrRequest.sourceAddr());
            preProcess.setSourceAddrTon(dlrRequest.sourceAddrTon());
            preProcess.setSourceAddrNpi(dlrRequest.sourceAddrNpi());
            preProcess.setDestinationAddr(dlrRequest.destinationAddr());
            preProcess.setDestAddrTon(dlrRequest.destAddrTon());
            preProcess.setDestAddrNpi(dlrRequest.destAddrNpi());
            preProcess.setDeliverSmId(dlrRequest.messageId());
            preProcess.setMessageId(responseEvent.submitSmServerId());
            preProcess.setOptionalParameters(dlrRequest.optionalParameters());
            preProcess.setSystemId(responseEvent.systemId());
            preProcess.setParentId(responseEvent.parentId());

            if ("SMPP".equalsIgnoreCase(responseEvent.originProtocol())) {
                DeliveryReceipt receipt = new DeliveryReceipt(dlrRequest.messageId(), 1, 1,
                        new Date(), new Date(), UtilsEnum.getDeliverReceiptState(dlrRequest.status().toUpperCase()), dlrRequest.errorCode(), "");
                receipt.setId(responseEvent.submitSmServerId());
                preProcess.setShortMessage(receipt.toString());
                preProcess.setDelReceipt(receipt.toString());
            }

            String asJson = preProcess.toString();
            switch (responseEvent.originProtocol().toUpperCase()) {
                case "SMPP" -> jedisCluster.rpush(properties.getSmppQueue(), asJson);
                case "HTTP" -> jedisCluster.rpush(properties.getHttpQueue(), asJson);
                case "SS7" -> this.prepareAndSendSs7Dlr(preProcess, responseEvent);
                default -> log.error("Unknown origin protocol: {}", responseEvent.originProtocol());
            }
        });
    }

    private void prepareAndSendSs7Dlr(MessageEvent dlr, UtilsRecords.SubmitSmResponseEvent result) {
        dlr.setEsmClass(-1);
        dlr.setCheckSubmitSmResponse(true);
        dlr.setOriginNetworkId(result.originNetworkId());
        dlr.setOriginProtocol(result.originProtocol());
        dlr.setDestProtocol("SS7");
        jedisCluster.rpush("preDeliver", dlr.toString());
    }
}
