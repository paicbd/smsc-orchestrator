package com.smsc.orchestrator.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppPropertiesTest {
    @InjectMocks
    private AppProperties appProperties;

    @BeforeEach
    void setUp() throws Exception {
        injectField("redisNodes", Arrays.asList("node1", "node2", "node3"));
        injectField("maxTotal", 20);
        injectField("maxIdle", 20);
        injectField("minIdle", 1);
        injectField("blockWhenExhausted", true);
        injectField("smppSubmitSmQueue", "smpp_message");
        injectField("smppWorkers", 10);
        injectField("smppBatchSizePerWorker", 5);
        injectField("smppConsumerScheduler", 30);
        injectField("smppDeliverySmQueue", "deliver_sm_pre_process");
        injectField("smppDeliveryWorkers", 10);
        injectField("smppDeliveryBatchSizePerWorker", 5);
        injectField("smppDeliveryConsumerScheduler", 30);
        injectField("smppRetryList", "smpp_dlr_retry_list");
        injectField("httpSubmitSmQueue", "http_message");
        injectField("httpWorkers", 10);
        injectField("httpBatchSizePerWorker", 5);
        injectField("httpConsumerScheduler", 30);
        injectField("httpDeliverySmQueue", "http_dlr_request");
        injectField("httpDeliveryWorkers", 1);
        injectField("httpDeliveryBatchSizePerWorker", 1);
        injectField("httpDeliveryConsumerScheduler", 1000);
        injectField("httpRetryList", "http_dlr_retry_list");
        injectField("ss7SubmitSmQueue", "ss7_message");
        injectField("ss7Workers", 10);
        injectField("ss7BatchSizePerWorker", 5);
        injectField("ss7ConsumerScheduler", 30);
        injectField("smppQueue", "smpp_dlr");
        injectField("httpQueue", "http_dlr");
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = AppProperties.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(appProperties, value);
    }

    @Test
    void testProperties() {
        List<String> expectedRedisNodes = Arrays.asList("node1", "node2", "node3");
        assertEquals(expectedRedisNodes, appProperties.getRedisNodes());
        assertEquals(20, appProperties.getMaxTotal());
        assertEquals(20, appProperties.getMaxIdle());
        assertEquals(1, appProperties.getMinIdle());
        assertTrue(appProperties.isBlockWhenExhausted());
        assertEquals("smpp_message", appProperties.getSmppSubmitSmQueue());
        assertEquals(10, appProperties.getSmppWorkers());
        assertEquals(5, appProperties.getSmppBatchSizePerWorker());
        assertEquals(30, appProperties.getSmppConsumerScheduler());
        assertEquals("deliver_sm_pre_process", appProperties.getSmppDeliverySmQueue());
        assertEquals(10, appProperties.getSmppDeliveryWorkers());
        assertEquals(5, appProperties.getSmppDeliveryBatchSizePerWorker());
        assertEquals(30, appProperties.getSmppDeliveryConsumerScheduler());
    }

    @Test
    void testProperties2() {
        assertEquals("smpp_dlr_retry_list", appProperties.getSmppRetryList());
        assertEquals("http_message", appProperties.getHttpSubmitSmQueue());
        assertEquals(10, appProperties.getHttpWorkers());
        assertEquals(5, appProperties.getHttpBatchSizePerWorker());
        assertEquals(30, appProperties.getHttpConsumerScheduler());
        assertEquals("http_dlr_request", appProperties.getHttpDeliverySmQueue());
        assertEquals(1, appProperties.getHttpDeliveryWorkers());
        assertEquals(1, appProperties.getHttpDeliveryBatchSizePerWorker());
        assertEquals(1000, appProperties.getHttpDeliveryConsumerScheduler());
        assertEquals("http_dlr_retry_list", appProperties.getHttpRetryList());
        assertEquals("ss7_message", appProperties.getSs7SubmitSmQueue());
        assertEquals(10, appProperties.getSs7Workers());
        assertEquals(5, appProperties.getSs7BatchSizePerWorker());
        assertEquals(30, appProperties.getSs7ConsumerScheduler());
        assertEquals("smpp_dlr", appProperties.getSmppQueue());
        assertEquals("http_dlr", appProperties.getHttpQueue());
    }
}