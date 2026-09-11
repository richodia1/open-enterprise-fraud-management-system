package org.openefms.frauddetection.decision;

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
class FraudDecisionEntity {
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

    protected FraudDecisionEntity() {
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

    DecisionMode mode() {
        return mode;
    }

    void mode(DecisionMode mode) {
        this.mode = mode;
    }

    DecisionOutcome outcome() {
        return outcome;
    }

    void outcome(DecisionOutcome outcome) {
        this.outcome = outcome;
    }

    BigDecimal riskScoreValue() {
        return riskScoreValue;
    }

    void riskScoreValue(BigDecimal riskScoreValue) {
        this.riskScoreValue = riskScoreValue;
    }

    RiskBand riskBand() {
        return riskBand;
    }

    void riskBand(RiskBand riskBand) {
        this.riskBand = riskBand;
    }

    String riskReason() {
        return riskReason;
    }

    void riskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    int ruleHitCount() {
        return ruleHitCount;
    }

    void ruleHitCount(int ruleHitCount) {
        this.ruleHitCount = ruleHitCount;
    }

    int modelSignalCount() {
        return modelSignalCount;
    }

    void modelSignalCount(int modelSignalCount) {
        this.modelSignalCount = modelSignalCount;
    }

    String ruleEvidenceSummary() {
        return ruleEvidenceSummary;
    }

    void ruleEvidenceSummary(String ruleEvidenceSummary) {
        this.ruleEvidenceSummary = ruleEvidenceSummary;
    }

    String modelSignalSummary() {
        return modelSignalSummary;
    }

    void modelSignalSummary(String modelSignalSummary) {
        this.modelSignalSummary = modelSignalSummary;
    }

    OffsetDateTime decidedAt() {
        return decidedAt;
    }

    void decidedAt(OffsetDateTime decidedAt) {
        this.decidedAt = decidedAt;
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
