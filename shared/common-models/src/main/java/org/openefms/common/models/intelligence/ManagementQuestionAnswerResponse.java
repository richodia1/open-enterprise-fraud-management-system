package org.openefms.common.models.intelligence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.explanation.EvidenceItem;

/**
 * Management-facing answer contract for fraud intelligence questions.
 */
public record ManagementQuestionAnswerResponse(
        UUID answerId,
        String tenantId,
        String question,
        ManagementQuestionIntent intent,
        ManagementQuestionAnswerStatus status,
        String answer,
        boolean liveDataRequired,
        List<ManagementQuestionQueryPlan> queryPlan,
        List<EvidenceItem> supportingEvidence,
        OffsetDateTime answeredAt,
        Map<String, String> attributes
) {
    public ManagementQuestionAnswerResponse {
        Objects.requireNonNull(answerId, "answerId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(question, "question must not be null");
        Objects.requireNonNull(intent, "intent must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(answer, "answer must not be null");
        Objects.requireNonNull(answeredAt, "answeredAt must not be null");
        queryPlan = queryPlan == null ? List.of() : List.copyOf(queryPlan);
        supportingEvidence = supportingEvidence == null ? List.of() : List.copyOf(supportingEvidence);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
