package org.openefms.explainableai.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openefms.common.models.explanation.EvidenceItem;
import org.openefms.common.models.explanation.ExplanationRequest;
import org.openefms.common.models.explanation.ExplanationResponse;
import org.openefms.common.models.fraud.ModelSignal;
import org.openefms.common.models.fraud.RuleHit;
import org.springframework.stereotype.Service;

@Service
public class DecisionSupportExplanationService {
    public ExplanationResponse explain(ExplanationRequest request, OffsetDateTime explainedAt) {
        List<EvidenceItem> evidence = evidence(request);
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("decisionMode", request.context().getOrDefault("decisionMode", "UNKNOWN"));
        attributes.put("decisionOutcome", request.context().getOrDefault("decisionOutcome", "UNKNOWN"));
        attributes.put("riskBand", request.context().getOrDefault("riskBand", "UNKNOWN"));
        attributes.put("riskScore", request.context().getOrDefault("riskScore", "0.00"));
        attributes.put("evidenceCount", Integer.toString(evidence.size()));

        return new ExplanationResponse(
                request.explanationId(),
                request.decisionId(),
                request.transactionId(),
                request.tenantId(),
                summary(request, evidence),
                evidence,
                explainedAt,
                attributes);
    }

    private List<EvidenceItem> evidence(ExplanationRequest request) {
        List<EvidenceItem> evidence = new ArrayList<>();
        request.ruleHits().forEach(ruleHit -> evidence.add(ruleEvidence(ruleHit)));
        request.modelSignals().forEach(modelSignal -> evidence.add(modelSignalEvidence(modelSignal)));

        if (evidence.isEmpty()) {
            evidence.add(new EvidenceItem(
                    "BASELINE_DECISION",
                    "No elevated indicators",
                    "The fraud decision did not include rule hits or model signals requiring investigator attention.",
                    BigDecimal.ZERO,
                    Map.of("riskBand", request.context().getOrDefault("riskBand", "UNKNOWN"))));
        }

        return evidence;
    }

    private EvidenceItem ruleEvidence(RuleHit ruleHit) {
        Map<String, String> attributes = new LinkedHashMap<>(ruleHit.attributes());
        attributes.put("ruleCode", ruleHit.ruleCode());
        attributes.put("severity", ruleHit.severity().name());

        return new EvidenceItem(
                "RULE",
                textOrDefault(ruleHit.ruleName(), ruleHit.ruleCode()),
                textOrDefault(ruleHit.explanation(), "Rule " + ruleHit.ruleCode() + " contributed to the decision."),
                contributionOrZero(ruleHit.contribution()),
                attributes);
    }

    private EvidenceItem modelSignalEvidence(ModelSignal modelSignal) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("modelName", modelSignal.modelName());
        if (hasText(modelSignal.modelVersion())) {
            attributes.put("modelVersion", modelSignal.modelVersion());
        }
        attributes.put("score", modelSignal.score().toPlainString());
        modelSignal.featureValues().forEach((key, value) -> attributes.put("feature." + key, value));

        return new EvidenceItem(
                "MODEL_SIGNAL",
                textOrDefault(modelSignal.modelName(), "Model signal"),
                "Model score " + modelSignal.score().toPlainString() + " contributed to the fraud decision.",
                contributionOrZero(modelSignal.contribution()),
                attributes);
    }

    private String summary(ExplanationRequest request, List<EvidenceItem> evidence) {
        String outcome = request.context().getOrDefault("decisionOutcome", "UNKNOWN");
        String riskBand = request.context().getOrDefault("riskBand", "UNKNOWN");
        String riskScore = request.context().getOrDefault("riskScore", "0.00");

        if (evidence.size() == 1 && "BASELINE_DECISION".equals(evidence.getFirst().evidenceType())) {
            return "Decision " + outcome + " with " + riskBand + " risk score " + riskScore
                    + ". No elevated rule or model evidence was supplied.";
        }

        String topEvidence = evidence.stream()
                .limit(3)
                .map(EvidenceItem::title)
                .reduce((left, right) -> left + ", " + right)
                .orElse("available evidence");
        return "Decision " + outcome + " with " + riskBand + " risk score " + riskScore
                + ". Key evidence: " + topEvidence + ".";
    }

    private BigDecimal contributionOrZero(BigDecimal contribution) {
        return contribution == null ? BigDecimal.ZERO : contribution;
    }

    private String textOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
