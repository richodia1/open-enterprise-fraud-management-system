package org.openefms.explainableai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.intelligence.ManagementQuestionAnswerStatus;
import org.openefms.common.models.intelligence.ManagementQuestionIntent;
import org.openefms.common.models.intelligence.ManagementQuestionRequest;

class ManagementQuestionAnswerServiceTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneOffset.UTC);

    private final ManagementQuestionAnswerService service = new ManagementQuestionAnswerService(FIXED_CLOCK);

    @Test
    void plansLatestFraudCaseQueryWhenLiveDataIsNeeded() {
        var response = service.answer(new ManagementQuestionRequest(
                "tenant-a",
                "What is the latest fraud case?",
                "MANAGEMENT",
                null,
                null,
                Map.of(),
                Map.of()));

        assertEquals(ManagementQuestionIntent.LATEST_FRAUD_CASE, response.intent());
        assertEquals(ManagementQuestionAnswerStatus.NEEDS_STRUCTURED_DATA, response.status());
        assertTrue(response.liveDataRequired());
        assertEquals("case-management-service", response.queryPlan().getFirst().sourceService());
        assertEquals("1", response.queryPlan().getFirst().parameters().get("limit"));
    }

    @Test
    void classifiesAtmCaseCountForCurrentYear() {
        var response = service.answer(new ManagementQuestionRequest(
                "tenant-a",
                "How many fraud case result from ATM this year?",
                "MANAGEMENT",
                null,
                null,
                Map.of(),
                Map.of()));

        assertEquals(ManagementQuestionIntent.FRAUD_CASE_COUNT_BY_CHANNEL, response.intent());
        assertEquals("ATM", response.attributes().get("channel"));
        assertEquals("2026-01-01T00:00Z", response.attributes().get("periodStart"));
        assertEquals("2026-10-10T12:00Z", response.attributes().get("periodEnd"));
        assertEquals("ATM", response.queryPlan().getFirst().parameters().get("channel"));
    }

    @Test
    void answersFromSuppliedContextWithoutLiveData() {
        var response = service.answer(new ManagementQuestionRequest(
                "tenant-a",
                "Explain this case to management",
                "MANAGEMENT",
                null,
                null,
                Map.of(),
                Map.of(
                        "answer", "The case was blocked because the transaction matched high-risk rules.",
                        "evidenceTitle", "High-risk rule match")));

        assertEquals(ManagementQuestionAnswerStatus.ANSWERED_FROM_CONTEXT, response.status());
        assertFalse(response.liveDataRequired());
        assertEquals("The case was blocked because the transaction matched high-risk rules.", response.answer());
        assertEquals("High-risk rule match", response.supportingEvidence().getFirst().title());
    }
}
