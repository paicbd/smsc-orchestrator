package com.smsc.orchestrator.process.smpp;

import com.smsc.orchestrator.utils.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.JedisCluster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmppDeliverPreProcessTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    JedisCluster jedisCluster;
    @Mock
    AppProperties properties;
    SmppDeliverPreProcess smppDeliverPreProcess;

    @ParameterizedTest
    @ValueSource(strings = {"SMPP", "HTTP", "SS7", ""})
    void startScheduler(String type) {
        this.smppDeliverPreProcess = new SmppDeliverPreProcess(properties, jedisCluster);
        when(properties.getSmppWorkers()).thenReturn(10);
        when(properties.getSmppDeliverySmQueue()).thenReturn("deliver_sm_pre_process");
        when(properties.getSmppBatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(properties.getSmppDeliverySmQueue(), 1)).thenReturn(List.of(
                "{\"system_id\":\"1\",\"deliver_sm_id\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null,\"check_submit_sm_response\":true}"
        ));
        when(jedisCluster.hget("submit_sm_result", "1-1")).thenReturn(
                "{\"hash_id\":\"1\",\"id\":\"1719421854353-11028072268459\",\"system_id\":\"1\",\"submit_sm_id\":\"1\",\"submit_sm_server_id\":\"1\",\"origin_protocol\":\"" + type + "\",\"origin_network_id\":1,\"origin_network_type\":\"1\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}"
        );
        assertDoesNotThrow(() -> smppDeliverPreProcess.startScheduler());
    }

    @Test
    void startSchedulerMalformedSubmitSmResult() {
        this.smppDeliverPreProcess = new SmppDeliverPreProcess(properties, jedisCluster);
        when(properties.getSmppWorkers()).thenReturn(10);
        when(properties.getSmppDeliverySmQueue()).thenReturn("deliver_sm_pre_process");
        when(properties.getSmppBatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(properties.getSmppDeliverySmQueue(), 1)).thenReturn(List.of(
                "{\"system_id\":\"1\",\"deliver_sm_id\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null,\"check_submit_sm_response\":true}"
        ));
        when(jedisCluster.hget("submit_sm_result", "1-1")).thenReturn(
                "{\"hash_id\":\"1\"\"id\":\"1719421854353-11028072268459\",\"system_id\":\"1\",\"submit_sm_id\":\"1\",\"submit_sm_server_id\":\"1\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":1,\"origin_network_type\":\"1\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}"
        );
        assertDoesNotThrow(() -> smppDeliverPreProcess.startScheduler());
    }

    @Test
    void startSchedulerMalformedDeliverSm() {
        this.smppDeliverPreProcess = new SmppDeliverPreProcess(properties, jedisCluster);
        when(properties.getSmppWorkers()).thenReturn(10);
        when(properties.getSmppDeliverySmQueue()).thenReturn("deliver_sm_pre_process");
        when(properties.getSmppBatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(properties.getSmppDeliverySmQueue(), 1)).thenReturn(List.of(
                "{\"system_id\":\"1\"\"deliver_sm_id\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null,\"check_submit_sm_response\":true}"
        ));
        assertDoesNotThrow(() -> smppDeliverPreProcess.startScheduler());
    }

    @Test
    void startSchedulerThrowsException() {
        this.smppDeliverPreProcess = new SmppDeliverPreProcess(null, jedisCluster);
        assertDoesNotThrow(() -> smppDeliverPreProcess.startScheduler());
    }
}