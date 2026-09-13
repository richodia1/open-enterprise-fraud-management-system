package org.openefms.casemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.casemanagement.enums.CaseActionType;
import org.openefms.casemanagement.enums.CaseStatus;
import org.openefms.common.persistence.entity.TenantScopedAttributesEntity;

@Entity
@Table(name = "fraud_case_actions")
public class FraudCaseActionEntity extends TenantScopedAttributesEntity {
    @Id
    @Column(name = "case_action_id", nullable = false)
    private UUID caseActionId;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private CaseActionType actionType;

    @Column(name = "actor_id", nullable = false, length = 128)
    private String actorId;

    @Column(name = "note", nullable = false, columnDefinition = "text")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private CaseStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 32)
    private CaseStatus toStatus;

    @Column(name = "assignee_id", length = 128)
    private String assigneeId;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    public FraudCaseActionEntity() {
    }

    public UUID caseActionId() {
        return caseActionId;
    }

    public void caseActionId(UUID caseActionId) {
        this.caseActionId = caseActionId;
    }

    public UUID caseId() {
        return caseId;
    }

    public void caseId(UUID caseId) {
        this.caseId = caseId;
    }

    public CaseActionType actionType() {
        return actionType;
    }

    public void actionType(CaseActionType actionType) {
        this.actionType = actionType;
    }

    public String actorId() {
        return actorId;
    }

    public void actorId(String actorId) {
        this.actorId = actorId;
    }

    public String note() {
        return note;
    }

    public void note(String note) {
        this.note = note;
    }

    public CaseStatus fromStatus() {
        return fromStatus;
    }

    public void fromStatus(CaseStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public CaseStatus toStatus() {
        return toStatus;
    }

    public void toStatus(CaseStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String assigneeId() {
        return assigneeId;
    }

    public void assigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public OffsetDateTime occurredAt() {
        return occurredAt;
    }

    public void occurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
