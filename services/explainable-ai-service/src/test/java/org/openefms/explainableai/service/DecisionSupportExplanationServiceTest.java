package org.openefms.explainableai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.explanation.EvidenceItem;
import org.openefms.common.models.explanation.ExplanationRequest;
import org.openefms.common.models.fraud.ModelSignal;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.fraud.RuleHit;

class DecisionSupportExplanationServiceTest {
    private static final UUID EXPLANATION_ID = UUID.fromString("d88154d9-5246-43ec-bb65-fcdfd38f64a3");
    private static final UUID DECISION_ID = UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0");
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");
    private static final OffsetDateTime EXPLAINED_AT = OffsetDateTime.parse("2026-09-11T10:00:00Z");

    @Test
    void turnsRuleHitsAndModelSignalsIntoEvidenceItems() {
        DecisionSupportExplanationService service = new DecisionSupportExplanationService();

        var response = service.explain(request(
                List.of(new RuleHit(
                        "HIGH_VALUE_AMOUNT",
                        "High value amount",
                        RiskBand.MEDIUM,
                        new BigDecimal("0.25"),
                        "The amount exceeded the high-value review threshold.",
                        Map.of("threshold", "10000.00"))),
                List.of(new ModelSignal(
                        "xgboost-baseline",
                        "v0.1",
                        new BigDecimal("0.82"),
                        new BigDecimal("0.35"),
                        Map.of("amount", "25000.00")))),
                EXPLAINED_AT);

        assertEquals(EXPLANATION_ID, response.explanationId());
        assertEquals(DECISION_ID, response.decisionId());
        assertEquals(TRANSACTION_ID, response.transactionId());
        assertEquals("Decision REVIEW with MEDIUM risk score 0.60. Key evidence: High value amount, xgboost-baseline.",
                response.summary());
        assertEquals(2, response.evidence().size());
        assertEquals("RULE", response.evidence().getFirst().evidenceType());
        assertEquals("HIGH_VALUE_AMOUNT", response.evidence().getFirst().attributes().get("ruleCode"));
        assertEquals("MODEL_SIGNAL", response.evidence().get(1).evidenceType());
        assertEquals("0.82", response.evidence().get(1).attributes().get("score"));
        assertEquals("2", response.attributes().get("evidenceCount"));
        assertEquals("HOLD_FOR_MANUAL_REVIEW", response.attributes().get("recommendedAction"));
        assertEquals(EXPLAINED_AT, response.explainedAt());
    }

    @Test
    void createsBaselineEvidenceWhenDecisionHasNoSignals() {
        DecisionSupportExplanationService service = new DecisionSupportExplanationService();

        var response = service.explain(request(List.of(), List.of()), EXPLAINED_AT);

        assertEquals(1, response.evidence().size());
        EvidenceItem evidence = response.evidence().getFirst();
        assertEquals("BASELINE_DECISION", evidence.evidenceType());
        assertTrue(response.summary().contains("No elevated rule or model evidence was supplied."));
    }

    private ExplanationRequest request(List<RuleHit> ruleHits, List<ModelSignal> modelSignals) {
        return new ExplanationRequest(
                EXPLANATION_ID,
                DECISION_ID,
                TRANSACTION_ID,
                "tenant-a",
                ruleHits,
                modelSignals,
                Map.of(
                        "decisionMode", "RULES",
                        "decisionOutcome", "REVIEW",
                        "recommendedAction", "HOLD_FOR_MANUAL_REVIEW",
                        "riskBand", "MEDIUM",
                        "riskScore", "0.60"));
    }
}
