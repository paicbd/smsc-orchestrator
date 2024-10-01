package com.smsc.orchestrator.process.http;

import com.smsc.orchestrator.utils.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.JedisCluster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpDeliverPreProcessTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    JedisCluster jedisCluster;
    @Mock
    AppProperties appProperties;
    @Mock(strictness = Mock.Strictness.LENIENT)
    HttpDeliverSmRetry httpDeliverSmRetry;
    HttpDeliverPreProcess httpDeliverPreProcess;

    @Test
    void startSchedulerTest() {
        this.httpDeliverPreProcess = new HttpDeliverPreProcess(appProperties, jedisCluster, httpDeliverSmRetry);
        when(appProperties.getHttpWorkers()).thenReturn(10);
        when(appProperties.getHttpDeliverySmQueue()).thenReturn("http_dlr_request");
        when(appProperties.getHttpBatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(appProperties.getHttpDeliverySmQueue(), 1)).thenReturn(List.of(
                "{\"message_id\":\"1719421854353-11028072268459\",\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null}"
        ));
        assertDoesNotThrow(() -> httpDeliverPreProcess.startScheduler());
    }

    @Test
    void startThrowParseDlrRequest() {
        this.httpDeliverPreProcess = new HttpDeliverPreProcess(appProperties, jedisCluster, httpDeliverSmRetry);
        when(appProperties.getHttpWorkers()).thenReturn(10);
        when(appProperties.getHttpDeliverySmQueue()).thenReturn("http_dlr_request");
        when(appProperties.getHttpBatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(appProperties.getHttpDeliverySmQueue(), 1)).thenReturn(List.of(
                "{\"msisdn\":null,\"check_submit_sm_response\":null\"origin_network_type\":\"SP\"}"
        ));
        assertDoesNotThrow(() -> httpDeliverPreProcess.startScheduler());
    }

    @Test
    void startSchedulerThrowsException() {
        this.httpDeliverPreProcess = new HttpDeliverPreProcess(null, jedisCluster, httpDeliverSmRetry);
        assertDoesNotThrow(() -> httpDeliverPreProcess.startScheduler());
    }
}