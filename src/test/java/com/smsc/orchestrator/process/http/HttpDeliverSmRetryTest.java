package com.smsc.orchestrator.process.http;

import com.smsc.orchestrator.utils.AppProperties;
import com.smsc.orchestrator.utils.Records;
import org.junit.jupiter.api.BeforeEach;
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
class HttpDeliverSmRetryTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    JedisCluster jedisCluster;
    @Mock(strictness = Mock.Strictness.LENIENT)
    AppProperties properties;
    HttpDeliverSmRetry httpDeliverSmRetry;

    @BeforeEach
    void setUp() {
        this.httpDeliverSmRetry = new HttpDeliverSmRetry(properties, jedisCluster);
    }

    @Test
    void startHttpDlrScheduler() {
        when(properties.getHttpRetryList()).thenReturn("http_dlr_retry_list");
        when(jedisCluster.llen(properties.getHttpRetryList())).thenReturn(1L);
        when(jedisCluster.lpop(properties.getHttpRetryList(), 1)).thenReturn(List.of(
                "{\"message_id\":\"1719421854353-11028072268459\",\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"status\":\"DELIVRD\",\"error_code\":null,\"optional_parameters\":null}"
        ));
        when(jedisCluster.hget("http_submit_sm_result", "1719421854353-11028072268459")).thenReturn(
                "{\"hash_id\":\"1\",\"id\":\"1719421854353-11028072268459\",\"system_id\":\"1\",\"submit_sm_id\":\"1\",\"submit_sm_server_id\":\"1\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":1,\"origin_network_type\":\"1\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}"
        );
        when(properties.getSmppQueue()).thenReturn("smpp_dlr");

        assertDoesNotThrow(() -> this.httpDeliverSmRetry.startHttpDlrScheduler());
    }

    @Test
    void startHttpDlrSchedulerThrowMapper() {
        when(properties.getHttpRetryList()).thenReturn("http_dlr_retry_list");
        when(jedisCluster.llen(properties.getHttpRetryList())).thenReturn(1L);
        when(jedisCluster.lpop(properties.getHttpRetryList(), 1)).thenReturn(List.of(
                "{\"msisdn\":null,\"check_submit_sm_response\":null\"origin_network_type\":\"SP\"}"
        ));

        assertDoesNotThrow(() -> this.httpDeliverSmRetry.startHttpDlrScheduler());
    }

    @Test
    void startHttpDlrSchedulerListSize0() {
        when(properties.getHttpRetryList()).thenReturn("http_dlr_retry_list");
        when(jedisCluster.llen(properties.getHttpRetryList())).thenReturn(0L);

        assertDoesNotThrow(() -> this.httpDeliverSmRetry.startHttpDlrScheduler());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SMPP", "HTTP", "SS7", ""})
    void createDlrPreProcess(String type) {
        Records.DlrRequest dlrRequest = new Records.DlrRequest("1719421854353-11028072268459", 1, 1, "50510201020", 1, 1, "50582368999", "DELIVRD", null, null);
        when(jedisCluster.hget("http_submit_sm_result", dlrRequest.messageId())).thenReturn(
                messageResult(type)
        );
        when(properties.getSmppQueue()).thenReturn("smpp_dlr");
        assertDoesNotThrow(() -> this.httpDeliverSmRetry.createDlrPreProcess(dlrRequest));
    }

    @Test
    void createDlrPreProcessThrowMapper() {
        Records.DlrRequest dlrRequest = new Records.DlrRequest("1719421854353-11028072268459", 1, 1, "50510201020", 1, 1, "50582368999", "DELIVRD", null, null);
        when(jedisCluster.hget("http_submit_sm_result", dlrRequest.messageId())).thenReturn(
                "{\"hash_id\":\"1\"\"id\":\"1719421854353-11028072268459\",\"system_id\":\"1\",\"submit_sm_id\":\"1\",\"submit_sm_server_id\":\"1\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":1,\"origin_network_type\":\"1\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}"
        );
        when(properties.getSmppQueue()).thenReturn("smpp_dlr");
        assertDoesNotThrow(() -> this.httpDeliverSmRetry.createDlrPreProcess(dlrRequest));
    }

    String messageResult(String protocol) {
        return "{\"hash_id\":\"1\",\"id\":\"1719421854353-11028072268459\",\"system_id\":\"1\",\"submit_sm_id\":\"1\",\"submit_sm_server_id\":\"1\",\"origin_protocol\":\"" + protocol +"\",\"origin_network_id\":1,\"origin_network_type\":\"1\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}";
    }
}