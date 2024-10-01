package com.smsc.orchestrator.process.jss7;

import com.smsc.orchestrator.utils.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.JedisCluster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Ss7SubmitPreProcessTest {
    @Mock(strictness = Mock.Strictness.LENIENT)
    JedisCluster jedisCluster;
    @Mock
    AppProperties properties;
    Ss7SubmitPreProcess ss7SubmitPreProcess;
    @BeforeEach
    void setUp() {
        this.ss7SubmitPreProcess = new Ss7SubmitPreProcess(properties, jedisCluster);
    }

    @Test
    void startScheduler() {
        when(properties.getSs7Workers()).thenReturn(10);
        when(properties.getSs7SubmitSmQueue()).thenReturn("ss7_message");
        when(properties.getSs7BatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(properties.getSs7SubmitSmQueue(), 1)).thenReturn(List.of(
                "{\"system_id\":\"1\",\"deliver_sm_id\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null,\"check_submit_sm_response\":true}"
        ));
        assertDoesNotThrow(() -> ss7SubmitPreProcess.startScheduler());
    }

    @Test
    void startSchedulerMalformedJSON() {
        when(properties.getSs7Workers()).thenReturn(10);
        when(properties.getSs7SubmitSmQueue()).thenReturn("ss7_message");
        when(properties.getSs7BatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(properties.getSs7SubmitSmQueue(), 1)).thenReturn(List.of(
                "{\"system_id\":\"1\"\"deliver_sm_id\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null,\"check_submit_sm_response\":true}"
        ));
        assertDoesNotThrow(() -> ss7SubmitPreProcess.startScheduler());
    }
}