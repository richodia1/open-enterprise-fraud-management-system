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
import org.openefms.common.persistence.entity.TenantScopedCausalEventAttributesEntity;

@Entity
@Table(name = "fraud_cases")
public class FraudCaseEntity extends TenantScopedCausalEventAttributesEntity {
    @Id
    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "alert_id", nullable = false)
    private UUID alertId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

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

}
