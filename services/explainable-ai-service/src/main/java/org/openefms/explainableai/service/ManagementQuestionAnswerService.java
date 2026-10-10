package org.openefms.explainableai.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.openefms.common.models.explanation.EvidenceItem;
import org.openefms.common.models.intelligence.ManagementQuestionAnswerResponse;
import org.openefms.common.models.intelligence.ManagementQuestionAnswerStatus;
import org.openefms.common.models.intelligence.ManagementQuestionIntent;
import org.openefms.common.models.intelligence.ManagementQuestionQueryPlan;
import org.openefms.common.models.intelligence.ManagementQuestionRequest;
import org.springframework.stereotype.Service;

@Service
public class ManagementQuestionAnswerService {
    private final Clock clock;

    public ManagementQuestionAnswerService(Clock clock) {
        this.clock = clock;
    }

    public ManagementQuestionAnswerResponse answer(ManagementQuestionRequest request) {
        ManagementQuestionRequest normalised = normalise(request);
        OffsetDateTime answeredAt = OffsetDateTime.now(clock);
        ManagementQuestionIntent intent = classify(normalised);
        Map<String, String> attributes = attributes(normalised, intent, answeredAt);

        if (hasText(normalised.context().get("answer"))) {
            return response(
                    normalised,
                    intent,
                    ManagementQuestionAnswerStatus.ANSWERED_FROM_CONTEXT,
                    normalised.context().get("answer"),
                    false,
                    List.of(),
                    contextEvidence(normalised),
                    answeredAt,
                    attributes);
        }

        return switch (intent) {
            case LATEST_FRAUD_CASE -> latestCaseResponse(normalised, answeredAt, attributes);
            case FRAUD_CASE_COUNT_BY_CHANNEL -> caseCountByChannelResponse(normalised, answeredAt, attributes);
            case FRAUD_CASE_COUNT_BY_CAUSE -> caseCountByCauseResponse(normalised, answeredAt, attributes);
            case FRAUD_CASE_EXPLANATION -> caseExplanationResponse(normalised, answeredAt, attributes);
            case FRAUD_TREND_SUMMARY -> trendSummaryResponse(normalised, answeredAt, attributes);
            case UNKNOWN -> unknownResponse(normalised, answeredAt, attributes);
        };
    }

    private ManagementQuestionAnswerResponse latestCaseResponse(
            ManagementQuestionRequest request,
            OffsetDateTime answeredAt,
            Map<String, String> attributes) {
        String latestCaseId = request.context().get("latestCaseId");
        String latestCaseTitle = request.context().get("latestCaseTitle");
        if (hasText(latestCaseId) || hasText(latestCaseTitle)) {
            String answer = "The latest fraud case is "
                    + textOrDefault(latestCaseTitle, "case " + latestCaseId)
                    + ". Use the case detail view for full investigation context.";
            return response(
                    request,
                    ManagementQuestionIntent.LATEST_FRAUD_CASE,
                    ManagementQuestionAnswerStatus.ANSWERED_FROM_CONTEXT,
                    answer,
                    false,
                    List.of(),
                    contextEvidence(request),
                    answeredAt,
                    attributes);
        }

        return response(
                request,
                ManagementQuestionIntent.LATEST_FRAUD_CASE,
                ManagementQuestionAnswerStatus.NEEDS_STRUCTURED_DATA,
                "I identified this as a latest fraud case question. The answer should be grounded by reading the newest case for the tenant, ordered by creation time.",
                true,
                List.of(caseQueryPlan(
                        request,
                        "Find the latest fraud case for the tenant.",
                        Map.of("sort", "createdAt,desc", "limit", "1"))),
                List.of(),
                answeredAt,
                attributes);
    }

    private ManagementQuestionAnswerResponse caseCountByChannelResponse(
            ManagementQuestionRequest request,
            OffsetDateTime answeredAt,
            Map<String, String> attributes) {
        String channel = channel(request);
        String suppliedCount = request.context().get("caseCount");
        if (hasText(suppliedCount)) {
            return response(
                    request,
                    ManagementQuestionIntent.FRAUD_CASE_COUNT_BY_CHANNEL,
                    ManagementQuestionAnswerStatus.ANSWERED_FROM_CONTEXT,
                    "There are " + suppliedCount + " fraud case(s) for channel " + channel
                            + " in the selected period.",
                    false,
                    List.of(),
                    contextEvidence(request),
                    answeredAt,
                    attributes);
        }

        return response(
                request,
                ManagementQuestionIntent.FRAUD_CASE_COUNT_BY_CHANNEL,
                ManagementQuestionAnswerStatus.NEEDS_STRUCTURED_DATA,
                "I identified this as a case-count-by-channel question. The count must come from structured case or alert data, filtered by channel and period.",
                true,
                List.of(caseQueryPlan(
                        request,
                        "Count fraud cases for the requested channel and period.",
                        Map.of(
                                "channel", channel,
                                "periodStart", attributes.get("periodStart"),
                                "periodEnd", attributes.get("periodEnd")))),
                List.of(),
                answeredAt,
                attributes);
    }

    private ManagementQuestionAnswerResponse caseCountByCauseResponse(
            ManagementQuestionRequest request,
            OffsetDateTime answeredAt,
            Map<String, String> attributes) {
        String cause = cause(request);
        return response(
                request,
                ManagementQuestionIntent.FRAUD_CASE_COUNT_BY_CAUSE,
                ManagementQuestionAnswerStatus.NEEDS_STRUCTURED_DATA,
                "I identified this as a case-count-by-cause question. The count should be computed from case attributes, decision rule evidence, or fraud type classifications.",
                true,
                List.of(
                        caseQueryPlan(
                                request,
                                "Find cases matching the requested cause or classification.",
                                Map.of("cause", cause, "periodStart", attributes.get("periodStart"), "periodEnd", attributes.get("periodEnd"))),
                        fraudDecisionQueryPlan(
                                request,
                                "Inspect decision evidence for the requested rule, model signal, or fraud reason.",
                                Map.of("cause", cause))),
                List.of(),
                answeredAt,
                attributes);
    }

    private ManagementQuestionAnswerResponse caseExplanationResponse(
            ManagementQuestionRequest request,
            OffsetDateTime answeredAt,
            Map<String, String> attributes) {
        String explanation = firstText(
                request.context().get("explanationSummary"),
                request.context().get("caseSummary"),
                request.context().get("managementSummary"));
        if (hasText(explanation)) {
            return response(
                    request,
                    ManagementQuestionIntent.FRAUD_CASE_EXPLANATION,
                    ManagementQuestionAnswerStatus.ANSWERED_FROM_CONTEXT,
                    explanation,
                    false,
                    List.of(),
                    contextEvidence(request),
                    answeredAt,
                    attributes);
        }

        return response(
                request,
                ManagementQuestionIntent.FRAUD_CASE_EXPLANATION,
                ManagementQuestionAnswerStatus.NEEDS_CASE_CONTEXT,
                "I identified this as a case explanation question. The answer should combine the case record, fraud decision, explanation evidence, and audit trail before preparing a management summary.",
                true,
                List.of(
                        caseQueryPlan(request, "Load the target case and lifecycle state.", Map.of()),
                        fraudDecisionQueryPlan(request, "Load the related decision, risk score, and evidence.", Map.of()),
                        auditQueryPlan(request, "Load audit activity for the case or correlation id.", Map.of())),
                List.of(),
                answeredAt,
                attributes);
    }

    private ManagementQuestionAnswerResponse trendSummaryResponse(
            ManagementQuestionRequest request,
            OffsetDateTime answeredAt,
            Map<String, String> attributes) {
        return response(
                request,
                ManagementQuestionIntent.FRAUD_TREND_SUMMARY,
                ManagementQuestionAnswerStatus.NEEDS_STRUCTURED_DATA,
                "I identified this as a fraud trend question. The answer should aggregate cases, alerts, decisions, channels, fraud types, and rule evidence over the selected period.",
                true,
                List.of(
                        caseQueryPlan(request, "Aggregate case volume by status, priority, channel, and fraud type.", Map.of()),
                        fraudDecisionQueryPlan(request, "Aggregate decision outcomes, risk bands, rule hits, and model signals.", Map.of())),
                List.of(),
                answeredAt,
                attributes);
    }

    private ManagementQuestionAnswerResponse unknownResponse(
            ManagementQuestionRequest request,
            OffsetDateTime answeredAt,
            Map<String, String> attributes) {
        return response(
                request,
                ManagementQuestionIntent.UNKNOWN,
                ManagementQuestionAnswerStatus.UNKNOWN,
                "I could not confidently map this question to a supported fraud intelligence intent yet. Try asking about latest cases, case counts by channel, case counts by cause, case explanation, or fraud trends.",
                false,
                List.of(),
                List.of(),
                answeredAt,
                attributes);
    }

    private ManagementQuestionRequest normalise(ManagementQuestionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        String tenantId = textOrNull(request.tenantId());
        String question = textOrNull(request.question());
        if (!hasText(tenantId)) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        if (!hasText(question)) {
            throw new IllegalArgumentException("question must not be blank");
        }
        return new ManagementQuestionRequest(
                tenantId,
                question,
                request.audience(),
                request.periodStart(),
                request.periodEnd(),
                request.filters(),
                request.context());
    }

    private ManagementQuestionIntent classify(ManagementQuestionRequest request) {
        String question = request.question().toLowerCase(Locale.ROOT);
        boolean asksCount = question.contains("how many")
                || question.contains("count")
                || question.contains("number of");
        if ((question.contains("latest") || question.contains("recent")) && question.contains("case")) {
            return ManagementQuestionIntent.LATEST_FRAUD_CASE;
        }
        if (asksCount && hasText(channel(request))) {
            return ManagementQuestionIntent.FRAUD_CASE_COUNT_BY_CHANNEL;
        }
        if (asksCount && (question.contains("result from")
                || question.contains("caused by")
                || question.contains("because of")
                || question.contains("rule")
                || question.contains("fraud type"))) {
            return ManagementQuestionIntent.FRAUD_CASE_COUNT_BY_CAUSE;
        }
        if (question.contains("explain") || question.contains("why") || question.contains("management")) {
            return ManagementQuestionIntent.FRAUD_CASE_EXPLANATION;
        }
        if (question.contains("trend") || question.contains("pattern") || question.contains("summary")) {
            return ManagementQuestionIntent.FRAUD_TREND_SUMMARY;
        }
        return ManagementQuestionIntent.UNKNOWN;
    }

    private Map<String, String> attributes(
            ManagementQuestionRequest request,
            ManagementQuestionIntent intent,
            OffsetDateTime answeredAt) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("audience", request.audience());
        attributes.put("intent", intent.name());
        attributes.put("periodStart", periodStart(request, answeredAt).toString());
        attributes.put("periodEnd", periodEnd(request, answeredAt).toString());
        putIfPresent(attributes, "channel", channel(request));
        putIfPresent(attributes, "cause", cause(request));
        return attributes;
    }

    private OffsetDateTime periodStart(ManagementQuestionRequest request, OffsetDateTime answeredAt) {
        if (request.periodStart() != null) {
            return request.periodStart();
        }
        String question = request.question().toLowerCase(Locale.ROOT);
        if (question.contains("today")) {
            return answeredAt.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        if (question.contains("this month")) {
            return YearMonth.from(answeredAt).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        if (question.contains("this year")) {
            return Year.from(answeredAt).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        return answeredAt.minusDays(30);
    }

    private OffsetDateTime periodEnd(ManagementQuestionRequest request, OffsetDateTime answeredAt) {
        return request.periodEnd() == null ? answeredAt : request.periodEnd();
    }

    private String channel(ManagementQuestionRequest request) {
        String filterChannel = request.filters().get("channel");
        if (hasText(filterChannel)) {
            return filterChannel.trim().toUpperCase(Locale.ROOT);
        }
        String question = request.question().toLowerCase(Locale.ROOT);
        if (question.contains("atm")) {
            return "ATM";
        }
        if (question.contains("mobile")) {
            return "MOBILE";
        }
        if (question.contains("pos")) {
            return "POS";
        }
        if (question.contains("branch")) {
            return "BRANCH";
        }
        if (question.contains("card")) {
            return "CARD";
        }
        if (question.contains("web") || question.contains("internet")) {
            return "INTERNET_BANKING";
        }
        if (question.contains("api")) {
            return "API";
        }
        if (question.contains("payment switch")) {
            return "PAYMENT_SWITCH";
        }
        return "";
    }

    private String cause(ManagementQuestionRequest request) {
        String cause = firstText(
                request.filters().get("cause"),
                request.filters().get("ruleCode"),
                request.filters().get("fraudType"));
        if (hasText(cause)) {
            return cause;
        }
        String question = request.question();
        String lower = question.toLowerCase(Locale.ROOT);
        for (String marker : List.of("result from", "caused by", "because of")) {
            int index = lower.indexOf(marker);
            if (index >= 0) {
                return question.substring(index + marker.length()).trim();
            }
        }
        return "specified fraud cause";
    }

    private ManagementQuestionQueryPlan caseQueryPlan(
            ManagementQuestionRequest request,
            String purpose,
            Map<String, String> parameters) {
        return queryPlan("case-management-service", "/api/v1/cases", purpose, request, parameters);
    }

    private ManagementQuestionQueryPlan fraudDecisionQueryPlan(
            ManagementQuestionRequest request,
            String purpose,
            Map<String, String> parameters) {
        return queryPlan("fraud-detection-service", "/api/v1/fraud/decisions", purpose, request, parameters);
    }

    private ManagementQuestionQueryPlan auditQueryPlan(
            ManagementQuestionRequest request,
            String purpose,
            Map<String, String> parameters) {
        return queryPlan("audit-logging-service", "/api/v1/audit-events", purpose, request, parameters);
    }

    private ManagementQuestionQueryPlan queryPlan(
            String sourceService,
            String endpoint,
            String purpose,
            ManagementQuestionRequest request,
            Map<String, String> parameters) {
        Map<String, String> merged = new LinkedHashMap<>();
        merged.put("tenantId", request.tenantId());
        merged.putAll(parameters);
        return new ManagementQuestionQueryPlan(sourceService, endpoint, purpose, merged);
    }

    private List<EvidenceItem> contextEvidence(ManagementQuestionRequest request) {
        if (!hasText(request.context().get("evidenceTitle"))) {
            return List.of();
        }
        return List.of(new EvidenceItem(
                "CONTEXT",
                request.context().get("evidenceTitle"),
                request.context().getOrDefault("evidenceDescription", "User-supplied management question context."),
                null,
                request.context()));
    }

    private ManagementQuestionAnswerResponse response(
            ManagementQuestionRequest request,
            ManagementQuestionIntent intent,
            ManagementQuestionAnswerStatus status,
            String answer,
            boolean liveDataRequired,
            List<ManagementQuestionQueryPlan> queryPlan,
            List<EvidenceItem> evidence,
            OffsetDateTime answeredAt,
            Map<String, String> attributes) {
        return new ManagementQuestionAnswerResponse(
                UUID.randomUUID(),
                request.tenantId(),
                request.question(),
                intent,
                status,
                answer,
                liveDataRequired,
                queryPlan,
                evidence,
                answeredAt,
                attributes);
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String textOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private String textOrNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void putIfPresent(Map<String, String> values, String key, String value) {
        if (hasText(value)) {
            values.put(key, value);
        }
    }
}
