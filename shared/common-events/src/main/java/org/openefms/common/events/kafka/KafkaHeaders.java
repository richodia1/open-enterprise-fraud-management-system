package org.openefms.common.events.kafka;

/**
 * Broker header names used to duplicate searchable event metadata on Kafka records.
 */
public final class KafkaHeaders {
    public static final String EVENT_ID = "X-OEFMS-Event-Id";
    public static final String EVENT_TYPE = "X-OEFMS-Event-Type";
    public static final String EVENT_VERSION = "X-OEFMS-Event-Version";
    public static final String SOURCE_SERVICE = "X-OEFMS-Source-Service";
    public static final String TENANT_ID = "X-OEFMS-Tenant-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String CAUSATION_ID = "X-OEFMS-Causation-Id";
    public static final String TRACE_ID = "X-OEFMS-Trace-Id";

    private KafkaHeaders() {
    }
}
