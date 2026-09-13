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
import org.openefms.common.models.fraud.RecommendedTransactionAction;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.persistence.entity.TenantScopedCausalEventAttributesEntity;

@Entity
@Table(name = "fraud_decisions")
public class FraudDecisionEntity extends TenantScopedCausalEventAttributesEntity {
    @Id
    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 32)
    private DecisionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 32)
    private DecisionOutcome outcome;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action", nullable = false, length = 48)
    private RecommendedTransactionAction recommendedAction;

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

    public RecommendedTransactionAction recommendedAction() {
        return recommendedAction;
    }

    public void recommendedAction(RecommendedTransactionAction recommendedAction) {
        this.recommendedAction = recommendedAction;
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

}
