package org.openefms.casemanagement.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.common.models.alert.AlertPriority;
import org.openefms.common.models.fraud.RiskBand;

@Entity
@Table(name = "fraud_alerts")
class FraudAlertEntity {
    @Id
    @Column(name = "alert_id", nullable = false)
    private UUID alertId;

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "tenant_id", nullable = false, length = 128)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    private AlertPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_band", nullable = false, length = 32)
    private RiskBand riskBand;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AlertStatus status;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "summary", nullable = false, columnDefinition = "text")
    private String summary;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "event_type", length = 128)
    private String eventType;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "causation_id", length = 128)
    private String causationId;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "attributes_text", nullable = false, columnDefinition = "text")
    private String attributesText;

    protected FraudAlertEntity() {
    }

    UUID alertId() {
        return alertId;
    }

    void alertId(UUID alertId) {
        this.alertId = alertId;
    }

    UUID decisionId() {
        return decisionId;
    }

    void decisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }

    UUID transactionId() {
        return transactionId;
    }

    void transactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    String tenantId() {
        return tenantId;
    }

    void tenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    AlertPriority priority() {
        return priority;
    }

    void priority(AlertPriority priority) {
        this.priority = priority;
    }

    RiskBand riskBand() {
        return riskBand;
    }

    void riskBand(RiskBand riskBand) {
        this.riskBand = riskBand;
    }

    AlertStatus status() {
        return status;
    }

    void status(AlertStatus status) {
        this.status = status;
    }

    String title() {
        return title;
    }

    void title(String title) {
        this.title = title;
    }

    String summary() {
        return summary;
    }

    void summary(String summary) {
        this.summary = summary;
    }

    OffsetDateTime createdAt() {
        return createdAt;
    }

    void createdAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    String eventType() {
        return eventType;
    }

    void eventType(String eventType) {
        this.eventType = eventType;
    }

    UUID eventId() {
        return eventId;
    }

    void eventId(UUID eventId) {
        this.eventId = eventId;
    }

    String correlationId() {
        return correlationId;
    }

    void correlationId(String correlationId) {
        this.correlationId = correlationId;
    }

    String causationId() {
        return causationId;
    }

    void causationId(String causationId) {
        this.causationId = causationId;
    }

    String traceId() {
        return traceId;
    }

    void traceId(String traceId) {
        this.traceId = traceId;
    }

    String attributesText() {
        return attributesText;
    }

    void attributesText(String attributesText) {
        this.attributesText = attributesText;
    }
}
