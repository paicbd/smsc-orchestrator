# Orchestrator Module

This service, `orchestrator-module`, is responsible for managing the orchestration of SMS messages across different protocols (SMPP, HTTP, SS7) and ensuring proper handling of Delivery Receipts (DLRs). The orchestrator is designed to split message lists by protocol and Network ID, as well as manage DLRs effectively.

## Main Responsibilities

### Message Splitting
- Splits general lists of messages by protocol and further divides them into specific lists by Network ID + protocol.

### DLR Management
- Fills in DLR information, verifying that DLRs correspond to the message IDs sent to the gateways.

By isolating these functions, the orchestrator allows other core modules to focus on their own responsibilities without consuming additional threads and resources for unrelated tasks.

## Key Environment Variables

### JVM Settings
- **JVM_XMS**: Minimum heap size for JVM (-Xms4g).
- **JVM_XMX**: Maximum heap size for JVM (-Xmx4g).

### Server Configuration
- **SERVER_PORT**: Port for the orchestrator module.
- **APPLICATION_NAME**: Name of the application (smsc-orchestrator).

### Queues

#### SMPP
- **SMPP_SUBMIT_SM_QUEUE**: Queue for SMPP messages (smpp_message).
- **SMPP_SUBMIT_SM_CONSUMER_WORKERS**: Number of workers for SMPP messages.
- **SMPP_SUBMIT_SM_CONSUMER_BATCH_SIZE**: Batch size for SMPP messages.
- **SMPP_SUBMIT_SM_CONSUMER_SCHEDULER**: Scheduler interval for SMPP messages.

#### HTTP
- **HTTP_SUBMIT_SM_QUEUE**: Queue for HTTP messages (http_message).
- **HTTP_SUBMIT_SM_CONSUMER_WORKERS**: Number of workers for HTTP messages.
- **HTTP_SUBMIT_SM_CONSUMER_BATCH_SIZE**: Batch size for HTTP messages.
- **HTTP_SUBMIT_SM_CONSUMER_SCHEDULER**: Scheduler interval for HTTP messages.

#### SS7
- **SS7_SUBMIT_SM_QUEUE**: Queue for SS7 messages (ss7_message).
- **SS7_SUBMIT_SM_CONSUMER_WORKERS**: Number of workers for SS7 messages.
- **SS7_SUBMIT_SM_CONSUMER_BATCH_SIZE**: Batch size for SS7 messages.
- **SS7_SUBMIT_SM_CONSUMER_SCHEDULER**: Scheduler interval for SS7 messages.

### DLRs
- **DELIVER_SM_SMPP_QUEUE**: Queue for SMPP DLRs (smpp_dlr).
- **DELIVER_SM_HTTP_QUEUE**: Queue for HTTP DLRs (http_dlr).

### Retry Settings
- **SMPP_DELIVERY_SM_RETRY_LIST**: List for SMPP DLR retries (smpp_dlr_retry_list).
- **HTTP_DELIVERY_SM_RETRY_LIST**: List for HTTP DLR retries (http_dlr_retry_list).

### Redis Configuration
- **CLUSTER_NODES**: List of Redis cluster nodes.
- **THREAD_POOL_MAX_TOTAL**: Maximum total threads in the pool.
- **THREAD_POOL_MAX_IDLE**: Maximum idle threads in the pool.
- **THREAD_POOL_MIN_IDLE**: Minimum idle threads in the pool.
- **THREAD_POOL_BLOCK_WHEN_EXHAUSTED**: Whether to block when exhausted.

### JMX Configuration
- **ENABLE_JMX**: Enables JMX monitoring (true).
- **IP_JMX**: IP address for JMX.
- **JMX_PORT**: Port for JMX .

---

### Docker Compose Example

```yaml
services:
  orchestrator-module:
    image: paicbusinessdev/orchestrator-module:latest
    ulimits:
      nofile:
        soft: 1000000
        hard: 1000000
    container_name: orchestrator
    depends_on:
      smsc-management-be:
        condition: service_healthy
    environment:
      JVM_XMS: "-Xms4g"
      JVM_XMX: "-Xms4g"
      SERVER_PORT: 9080
      APPLICATION_NAME: "smsc-orchestrator"
      # About SMPP SubmitSm
      SMPP_SUBMIT_SM_QUEUE: "smpp_message"
      SMPP_SUBMIT_SM_CONSUMER_WORKERS: 10
      SMPP_SUBMIT_SM_CONSUMER_BATCH_SIZE: 5000
      SMPP_SUBMIT_SM_CONSUMER_SCHEDULER: 1000
      # About HTTP SubmitSm
      HTTP_SUBMIT_SM_QUEUE: "http_message"
      HTTP_SUBMIT_SM_CONSUMER_WORKERS: 10
      HTTP_SUBMIT_SM_CONSUMER_BATCH_SIZE: 5000
      HTTP_SUBMIT_SM_CONSUMER_SCHEDULER: 1000
      # About SS7 SubmitSm
      SS7_SUBMIT_SM_QUEUE: "ss7_message"
      SS7_SUBMIT_SM_CONSUMER_WORKERS: 10
      SS7_SUBMIT_SM_CONSUMER_BATCH_SIZE: 5000
      SS7_SUBMIT_SM_CONSUMER_SCHEDULER: 1000
      # Global DLRs Queue
      DELIVER_SM_SMPP_QUEUE: "smpp_dlr"
      DELIVER_SM_HTTP_QUEUE: "http_dlr"
      # About DeliverySmSmpp
      SMPP_DELIVERY_SM_QUEUE: "deliver_sm_pre_process"
      SMPP_DELIVERY_SM_CONSUMER_WORKERS: 10
      SMPP_DELIVERY_SM_CONSUMER_BATCH_SIZE: 5000
      SMPP_DELIVERY_SM_CONSUMER_SCHEDULER: 1000
      SMPP_DELIVERY_SM_RETRY_LIST: "smpp_dlr_retry_list"
      SMPP_DELIVERY_SM_RETRY_EVERY: 300000
      # About DeliverySmHttp
      HTTP_DELIVERY_SM_QUEUE: "http_dlr_request"
      HTTP_DELIVERY_SM_CONSUMER_WORKERS: 10
      HTTP_DELIVERY_SM_CONSUMER_BATCH_SIZE: 5000
      HTTP_DELIVERY_SM_CONSUMER_SCHEDULER: 1000
      HTTP_DELIVERY_SM_RETRY_LIST: "http_dlr_retry_list"
      HTTP_DELIVERY_SM_RETRY_EVERY: 300000
      # About Redis
      CLUSTER_NODES: "localhost:7000,localhost:7001,localhost:7002,localhost:7003,localhost:7004,localhost:7005,localhost:7006,localhost:7007,localhost:7008,localhost:7009"
      THREAD_POOL_MAX_TOTAL: 20
      THREAD_POOL_MAX_IDLE: 20
      THREAD_POOL_MIN_IDLE: 1
      THREAD_POOL_BLOCK_WHEN_EXHAUSTED: true
      # JMX Configuration
      ENABLE_JMX: "true"
      IP_JMX: "{HOST_IP_ADDRESS}"
      JMX_PORT: "9016"
    volumes:
      - /opt/paic/smsc/docker/conf/logs/orchestrator/logback.xml:/opt/paic/ORCHESTRATOR_MODULE/conf/logback.xml
      - /var/log/paic/smsc/orchestrator:/opt/paic/ORCHESTRATOR_MODULE/logs
    network_mode: host
     
