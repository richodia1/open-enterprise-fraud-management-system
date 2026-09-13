package org.openefms.casemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.casemanagement.enums.CaseStatus;
import org.openefms.common.models.casefile.CasePriority;

@Entity
@Table(name = "fraud_cases")
public class FraudCaseEntity {
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

    public FraudCaseEntity() {
    }

    public UUID caseId() {
        return caseId;
    }

    public void caseId(UUID caseId) {
        this.caseId = caseId;
    }

    public UUID alertId() {
        return alertId;
    }

    public void alertId(UUID alertId) {
        this.alertId = alertId;
    }

    public UUID transactionId() {
        return transactionId;
    }

    public void transactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID decisionId() {
        return decisionId;
    }

    public void decisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }

    public String tenantId() {
        return tenantId;
    }

    public void tenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public CasePriority priority() {
        return priority;
    }

    public void priority(CasePriority priority) {
        this.priority = priority;
    }

    public CaseStatus status() {
        return status;
    }

    public void status(CaseStatus status) {
        this.status = status;
    }

    public String title() {
        return title;
    }

    public void title(String title) {
        this.title = title;
    }

    public String summary() {
        return summary;
    }

    public void summary(String summary) {
        this.summary = summary;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public void createdAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String eventType() {
        return eventType;
    }

    public void eventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID eventId() {
        return eventId;
    }

    public void eventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String correlationId() {
        return correlationId;
    }

    public void correlationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String causationId() {
        return causationId;
    }

    public void causationId(String causationId) {
        this.causationId = causationId;
    }

    public String traceId() {
        return traceId;
    }

    public void traceId(String traceId) {
        this.traceId = traceId;
    }

    public String attributesText() {
        return attributesText;
    }

    public void attributesText(String attributesText) {
        this.attributesText = attributesText;
    }
}
