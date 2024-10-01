package com.smsc.orchestrator.process.smpp;

import com.paicbd.smsc.exception.RTException;
import com.smsc.orchestrator.utils.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.JedisCluster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmppDeliverSmRetryTest {
    @Mock(strictness = Mock.Strictness.LENIENT)
    JedisCluster jedisCluster;
    @Mock
    AppProperties properties;
    SmppDeliverSmRetry smppDeliverSmRetry;
    @Mock
    SmppDeliverPreProcess deliverPreProcess;

    @BeforeEach
    void setUp() {
        this.smppDeliverSmRetry = new SmppDeliverSmRetry(properties, jedisCluster, deliverPreProcess);
    }

    @Test
    void startSchedulerEmptyList() {
        when(properties.getSmppRetryList()).thenReturn("smpp_dlr_retry_list");
        when(jedisCluster.llen(properties.getSmppRetryList())).thenReturn(0L);
        assertDoesNotThrow(() -> smppDeliverSmRetry.startScheduler());
    }

    @Test
    void startScheduler() {
        when(properties.getSmppRetryList()).thenReturn("smpp_dlr_retry_list");
        when(jedisCluster.llen(properties.getSmppRetryList())).thenReturn(1L);
        when(jedisCluster.lpop(properties.getSmppRetryList(), 1)).thenReturn(List.of(
                "{\"system_id\":\"1\",\"deliver_sm_id\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null,\"check_submit_sm_response\":true}"
        ));
        assertDoesNotThrow(() -> smppDeliverSmRetry.startScheduler());
    }

    @Test
    void startSchedulerMalformedJSON() {
        when(properties.getSmppRetryList()).thenReturn("smpp_dlr_retry_list");
        when(jedisCluster.llen(properties.getSmppRetryList())).thenReturn(1L);
        when(jedisCluster.lpop(properties.getSmppRetryList(), 1)).thenReturn(List.of(
                "{\"system_id\":\"1\"\"deliver_sm_id\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":null,\"error_code\":null,\"optional_parameters\":null,\"check_submit_sm_response\":true}"
        ));
        assertThrows(RTException.class, () -> smppDeliverSmRetry.startScheduler());
    }
}