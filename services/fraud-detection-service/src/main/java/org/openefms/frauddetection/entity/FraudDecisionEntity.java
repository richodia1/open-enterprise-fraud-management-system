package org.openefms.frauddetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.RiskBand;

@Entity
@Table(name = "fraud_decisions")
public class FraudDecisionEntity {
    @Id
    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "tenant_id", nullable = false, length = 128)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 32)
    private DecisionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 32)
    private DecisionOutcome outcome;

    @Column(name = "risk_score_value", nullable = false, precision = 5, scale = 4)
    private BigDecimal riskScoreValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_band", nullable = false, length = 32)
    private RiskBand riskBand;

    @Column(name = "risk_reason", nullable = false, columnDefinition = "text")
    private String riskReason;

    @Column(name = "rule_hit_count", nullable = false)
    private int ruleHitCount;

    @Column(name = "model_signal_count", nullable = false)
    private int modelSignalCount;

    @Column(name = "rule_evidence_summary", nullable = false, columnDefinition = "text")
    private String ruleEvidenceSummary;

    @Column(name = "model_signal_summary", nullable = false, columnDefinition = "text")
    private String modelSignalSummary;

    @Column(name = "decided_at", nullable = false)
    private OffsetDateTime decidedAt;

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

    public FraudDecisionEntity() {
    }

    public UUID decisionId() {
        return decisionId;
    }

    public void decisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }

    public UUID transactionId() {
        return transactionId;
    }

    public void transactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String tenantId() {
        return tenantId;
    }

    public void tenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public DecisionMode mode() {
        return mode;
    }

    public void mode(DecisionMode mode) {
        this.mode = mode;
    }

    public DecisionOutcome outcome() {
        return outcome;
    }

    public void outcome(DecisionOutcome outcome) {
        this.outcome = outcome;
    }

    public BigDecimal riskScoreValue() {
        return riskScoreValue;
    }

    public void riskScoreValue(BigDecimal riskScoreValue) {
        this.riskScoreValue = riskScoreValue;
    }

    public RiskBand riskBand() {
        return riskBand;
    }

    public void riskBand(RiskBand riskBand) {
        this.riskBand = riskBand;
    }

    public String riskReason() {
        return riskReason;
    }

    public void riskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public int ruleHitCount() {
        return ruleHitCount;
    }

    public void ruleHitCount(int ruleHitCount) {
        this.ruleHitCount = ruleHitCount;
    }

    public int modelSignalCount() {
        return modelSignalCount;
    }

    public void modelSignalCount(int modelSignalCount) {
        this.modelSignalCount = modelSignalCount;
    }

    public String ruleEvidenceSummary() {
        return ruleEvidenceSummary;
    }

    public void ruleEvidenceSummary(String ruleEvidenceSummary) {
        this.ruleEvidenceSummary = ruleEvidenceSummary;
    }

    public String modelSignalSummary() {
        return modelSignalSummary;
    }

    public void modelSignalSummary(String modelSignalSummary) {
        this.modelSignalSummary = modelSignalSummary;
    }

    public OffsetDateTime decidedAt() {
        return decidedAt;
    }

    public void decidedAt(OffsetDateTime decidedAt) {
        this.decidedAt = decidedAt;
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
