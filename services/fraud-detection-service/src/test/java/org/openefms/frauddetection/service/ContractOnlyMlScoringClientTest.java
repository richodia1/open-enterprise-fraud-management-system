package org.openefms.frauddetection.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.ml.MlRuntimeState;
import org.openefms.common.models.ml.MlScoreRequest;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.common.models.transaction.TransactionType;

class ContractOnlyMlScoringClientTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T09:00:00Z"), ZoneOffset.UTC);
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");

    @Test
    void reportsDisabledRuntimeWhenProviderIsDisabled() {
        ContractOnlyMlScoringClient client = new ContractOnlyMlScoringClient(
                new FraudDecisionProperties("rules", null, null),
                FIXED_CLOCK);

        var status = client.status();

        assertEquals("disabled", status.provider());
        assertEquals("xgboost-baseline", status.modelName());
        assertEquals(MlRuntimeState.DISABLED, status.state());
        assertTrue(status.supportedModes().contains(DecisionMode.ML));
        assertTrue(status.supportedModes().contains(DecisionMode.HYBRID));
        assertEquals("0.80", status.attributes().get("scoreThreshold"));
        assertEquals(OffsetDateTime.parse("2026-09-11T09:00:00Z"), status.checkedAt());
    }

    @Test
    void reportsContractOnlyRuntimeWhenProviderIsConfiguredWithoutAdapter() {
        ContractOnlyMlScoringClient client = new ContractOnlyMlScoringClient(
                new FraudDecisionProperties(
                        "ml",
                        null,
                        new FraudDecisionProperties.Ml("http", "xgboost-baseline", null)),
                FIXED_CLOCK);

        var status = client.status();

        assertEquals("http", status.provider());
        assertEquals(MlRuntimeState.CONTRACT_ONLY, status.state());
        assertTrue(status.reason().contains("no production scorer adapter"));
    }

    @Test
    void refusesLiveScoringUntilAnAdapterIsRegistered() {
        ContractOnlyMlScoringClient client = new ContractOnlyMlScoringClient(
                new FraudDecisionProperties("ml", null, null),
                FIXED_CLOCK);

        MlScoringUnavailableException exception = assertThrows(
                MlScoringUnavailableException.class,
                () -> client.score(new MlScoreRequest(
                        TRANSACTION_ID,
                        "tenant-a",
                        "xgboost-baseline",
                        transaction(),
                        Map.of())));

        assertEquals("disabled", exception.provider());
        assertEquals("xgboost-baseline", exception.modelName());
    }

    private TransactionIngestionRequest transaction() {
        return new TransactionIngestionRequest(
                TRANSACTION_ID,
                "EXT-001",
                "tenant-a",
                TransactionType.TRANSFER,
                TransactionChannel.MOBILE,
                TransactionDirection.OUTBOUND,
                new TransactionAmount(java.math.BigDecimal.TEN, "GBP"),
                party("originator"),
                party("beneficiary"),
                OffsetDateTime.parse("2026-09-11T08:58:00Z"),
                Map.of());
    }

    private TransactionParty party(String reference) {
        return new TransactionParty(reference, reference, reference, reference + "-hash", "BANK", "GB", Map.of());
    }
}
