package org.openefms.casemanagement.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.common.models.casefile.CasePriority;

@Entity
@Table(name = "fraud_cases")
class FraudCaseEntity {
    @Id
    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "alert_id", nullable = false)
    private UUID alertId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "tenant_id", nullable = false, length = 128)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    private CasePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CaseStatus status;

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

    protected FraudCaseEntity() {
    }

    UUID caseId() {
        return caseId;
    }

    void caseId(UUID caseId) {
        this.caseId = caseId;
    }

    UUID alertId() {
        return alertId;
    }

    void alertId(UUID alertId) {
        this.alertId = alertId;
    }

    UUID transactionId() {
        return transactionId;
    }

    void transactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    UUID decisionId() {
        return decisionId;
    }

    void decisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }

    String tenantId() {
        return tenantId;
    }

    void tenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    CasePriority priority() {
        return priority;
    }

    void priority(CasePriority priority) {
        this.priority = priority;
    }

    CaseStatus status() {
        return status;
    }

    void status(CaseStatus status) {
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
