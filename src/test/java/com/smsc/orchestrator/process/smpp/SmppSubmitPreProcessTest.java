package com.smsc.orchestrator.process.smpp;

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
class SmppSubmitPreProcessTest {
    @Mock(strictness = Mock.Strictness.LENIENT)
    JedisCluster jedisCluster;
    @Mock(strictness = Mock.Strictness.LENIENT)
    AppProperties properties;
    SmppSubmitPreProcess smppSubmitPreProcess;

    @Test
    void startScheduler() {
        this.smppSubmitPreProcess = new SmppSubmitPreProcess(properties, jedisCluster);
        when(properties.getSmppWorkers()).thenReturn(10);
        when(properties.getSmppSubmitSmQueue()).thenReturn("smpp_message");
        when(properties.getSmppBatchSizePerWorker()).thenReturn(1);
        when(jedisCluster.lpop(properties.getSmppDeliverySmQueue(), 1)).thenReturn(List.of(
                "{\"msisdn\":null,\"id\":\"1719421854353-11028072268459\",\"message_id\":\"1719421854353-11028072268459\",\"system_id\":\"httpsp01\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":0,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"esm_class\":0,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Prueba\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":1,\"dest_network_type\":\"GW\",\"dest_protocol\":\"HTTP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}"
        ));
        assertDoesNotThrow(() -> smppSubmitPreProcess.startScheduler());
    }

    @Test
    void startSchedulerThrowsException() {
        this.smppSubmitPreProcess = new SmppSubmitPreProcess(null, jedisCluster);
        assertDoesNotThrow(() -> smppSubmitPreProcess.startScheduler());
    }
}