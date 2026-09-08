package org.openefms.frauddetection.decision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.fraud.ModelSignal;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.fraud.RiskScore;
import org.openefms.common.models.fraud.RuleHit;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedFraudDecisionEngine {
    private static final BigDecimal ONE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);

    public FraudDecisionResponse decide(
            TransactionIngestionRequest transaction,
            FraudDecisionProperties.Rules rules,
            FraudDecisionProperties.Ml ml,
            Clock clock) {
        List<RuleHit> ruleHits = new ArrayList<>();
        evaluateAmount(transaction, rules, ruleHits);
        evaluateVelocity(transaction, rules, ruleHits);
        evaluateCrossBorderParties(transaction, ruleHits);
        evaluateHighRiskHint(transaction, ruleHits);
        evaluateUnknownChannel(transaction, ruleHits);

        BigDecimal score = score(ruleHits);
        RiskBand riskBand = riskBand(score, rules);
        DecisionOutcome outcome = outcome(score, rules);
        Map<String, String> attributes = decisionAttributes(ruleHits, ml);

        return new FraudDecisionResponse(
                UUID.randomUUID(),
                transaction.transactionId(),
                transaction.tenantId(),
                DecisionMode.RULES,
                outcome,
                new RiskScore(score, riskBand, decisionReason(ruleHits)),
                ruleHits,
                List.<ModelSignal>of(),
                OffsetDateTime.now(clock),
                attributes);
    }

    private void evaluateAmount(
            TransactionIngestionRequest transaction,
            FraudDecisionProperties.Rules rules,
            List<RuleHit> ruleHits) {
        BigDecimal amount = transaction.amount().value();
        if (amount.compareTo(rules.criticalValueAmountThreshold()) >= 0) {
            ruleHits.add(ruleHit(
                    "CRITICAL_VALUE_AMOUNT",
                    "Critical value amount",
                    RiskBand.CRITICAL,
                    "0.65",
                    "Transaction amount is at or above the configured critical-value threshold.",
                    Map.of("threshold", rules.criticalValueAmountThreshold().toPlainString())));
            return;
        }

        if (amount.compareTo(rules.highValueAmountThreshold()) >= 0) {
            ruleHits.add(ruleHit(
                    "HIGH_VALUE_AMOUNT",
                    "High value amount",
                    RiskBand.HIGH,
                    "0.35",
                    "Transaction amount is at or above the configured high-value threshold.",
                    Map.of("threshold", rules.highValueAmountThreshold().toPlainString())));
        }
    }

    private void evaluateVelocity(
            TransactionIngestionRequest transaction,
            FraudDecisionProperties.Rules rules,
            List<RuleHit> ruleHits) {
        OptionalInt velocityCount = firstIntegerAttribute(
                transaction.attributes(),
                "velocityCount",
                "recentTransactionCount",
                "transactionsInLastHour");

        if (velocityCount.isEmpty()) {
            return;
        }

        if (velocityCount.getAsInt() >= rules.velocityBlockCount()) {
            ruleHits.add(ruleHit(
                    "VELOCITY_BLOCK_THRESHOLD",
                    "Velocity block threshold",
                    RiskBand.HIGH,
                    "0.35",
                    "Recent transaction count is at or above the configured block threshold.",
                    Map.of("count", Integer.toString(velocityCount.getAsInt()))));
            return;
        }

        if (velocityCount.getAsInt() >= rules.velocityReviewCount()) {
            ruleHits.add(ruleHit(
                    "VELOCITY_REVIEW_THRESHOLD",
                    "Velocity review threshold",
                    RiskBand.MEDIUM,
                    "0.20",
                    "Recent transaction count is at or above the configured review threshold.",
                    Map.of("count", Integer.toString(velocityCount.getAsInt()))));
        }
    }

    private void evaluateCrossBorderParties(TransactionIngestionRequest transaction, List<RuleHit> ruleHits) {
        String originatorCountry = countryCode(transaction.originator());
        String beneficiaryCountry = countryCode(transaction.beneficiary());
        if (originatorCountry.isBlank()
                || beneficiaryCountry.isBlank()
                || originatorCountry.equalsIgnoreCase(beneficiaryCountry)) {
            return;
        }

        ruleHits.add(ruleHit(
                "CROSS_BORDER_PARTIES",
                "Cross-border parties",
                RiskBand.MEDIUM,
                "0.15",
                "Originator and beneficiary country codes differ.",
                Map.of("originatorCountry", originatorCountry, "beneficiaryCountry", beneficiaryCountry)));
    }

    private void evaluateHighRiskHint(TransactionIngestionRequest transaction, List<RuleHit> ruleHits) {
        Map<String, String> attributes = transaction.attributes();
        String riskHint = textAttribute(attributes, "riskHint", "risk_hint", "externalRiskHint");
        String highRisk = textAttribute(attributes, "highRisk", "high_risk", "isHighRisk");
        if (!"high".equalsIgnoreCase(riskHint) && !isTruthy(highRisk)) {
            return;
        }

        ruleHits.add(ruleHit(
                "HIGH_RISK_HINT",
                "High risk hint",
                RiskBand.HIGH,
                "0.25",
                "Input attributes contain an upstream high-risk hint.",
                Map.of()));
    }

    private void evaluateUnknownChannel(TransactionIngestionRequest transaction, List<RuleHit> ruleHits) {
        if (transaction.channel() != TransactionChannel.OTHER) {
            return;
        }

        ruleHits.add(ruleHit(
                "UNKNOWN_TRANSACTION_CHANNEL",
                "Unknown transaction channel",
                RiskBand.LOW,
                "0.10",
                "Transaction arrived through the generic OTHER channel.",
                Map.of()));
    }

    private BigDecimal score(List<RuleHit> ruleHits) {
        BigDecimal total = ruleHits.stream()
                .map(RuleHit::contribution)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.min(ONE).setScale(2, RoundingMode.HALF_UP);
    }

    private RiskBand riskBand(BigDecimal score, FraudDecisionProperties.Rules rules) {
        if (score.compareTo(rules.escalateScoreThreshold()) >= 0) {
            return RiskBand.CRITICAL;
        }
        if (score.compareTo(rules.blockScoreThreshold()) >= 0) {
            return RiskBand.HIGH;
        }
        if (score.compareTo(rules.reviewScoreThreshold()) >= 0) {
            return RiskBand.MEDIUM;
        }
        return RiskBand.LOW;
    }

    private DecisionOutcome outcome(BigDecimal score, FraudDecisionProperties.Rules rules) {
        if (score.compareTo(rules.escalateScoreThreshold()) >= 0) {
            return DecisionOutcome.ESCALATE;
        }
        if (score.compareTo(rules.blockScoreThreshold()) >= 0) {
            return DecisionOutcome.BLOCK;
        }
        if (score.compareTo(rules.reviewScoreThreshold()) >= 0) {
            return DecisionOutcome.REVIEW;
        }
        return DecisionOutcome.APPROVE;
    }

    private Map<String, String> decisionAttributes(List<RuleHit> ruleHits, FraudDecisionProperties.Ml ml) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("ruleSet", "baseline-phase-1");
        attributes.put("ruleHitCount", Integer.toString(ruleHits.size()));
        attributes.put("mlProvider", ml.provider());
        attributes.put("modelSignalsIncluded", "false");
        return attributes;
    }

    private String decisionReason(List<RuleHit> ruleHits) {
        if (ruleHits.isEmpty()) {
            return "No baseline rule indicators matched.";
        }
        return ruleHits.size() + " baseline rule indicator(s) matched.";
    }

    private RuleHit ruleHit(
            String ruleCode,
            String ruleName,
            RiskBand severity,
            String contribution,
            String explanation,
            Map<String, String> attributes) {
        return new RuleHit(
                ruleCode,
                ruleName,
                severity,
                new BigDecimal(contribution),
                explanation,
                attributes);
    }

    private OptionalInt firstIntegerAttribute(Map<String, String> attributes, String... keys) {
        for (String key : keys) {
            String value = attributes.get(key);
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                return OptionalInt.of(Integer.parseInt(value.trim()));
            } catch (NumberFormatException ignored) {
                continue;
            }
        }
        return OptionalInt.empty();
    }

    private String textAttribute(Map<String, String> attributes, String... keys) {
        for (String key : keys) {
            String value = attributes.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isTruthy(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true", "yes", "y", "1" -> true;
            default -> false;
        };
    }

    private String countryCode(TransactionParty party) {
        if (party == null || party.countryCode() == null || party.countryCode().isBlank()) {
            return "";
        }
        return party.countryCode().trim().toUpperCase(Locale.ROOT);
    }
}
