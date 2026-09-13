package org.openefms.frauddetection.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.ml.MlRuntimeState;
import org.openefms.common.models.ml.MlRuntimeStatus;
import org.openefms.common.models.ml.MlScoreRequest;
import org.openefms.common.models.ml.MlScoreResponse;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MlScoringConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MlScoringConfiguration.class)
            .withBean(FraudDecisionProperties.class, () -> new FraudDecisionProperties("rules", null, null))
            .withBean(Clock.class, Clock::systemUTC);

    @Test
    void registersContractOnlyClientWhenNoAdapterExists() {
        contextRunner.run(context ->
                assertInstanceOf(ContractOnlyMlScoringClient.class, context.getBean(MlScoringClient.class)));
    }

    @Test
    void backsOffWhenAdapterExists() {
        MlScoringClient adapter = new TestMlScoringClient();

        contextRunner
                .withBean("adapterMlScoringClient", MlScoringClient.class, () -> adapter)
                .run(context -> assertSame(adapter, context.getBean(MlScoringClient.class)));
    }

    private static final class TestMlScoringClient implements MlScoringClient {
        @Override
        public MlRuntimeStatus status() {
            return new MlRuntimeStatus(
                    "test",
                    "test-model",
                    MlRuntimeState.READY,
                    List.of(),
                    "test adapter",
                    OffsetDateTime.parse("2026-09-12T09:00:00Z"),
                    Map.of());
        }

        @Override
        public MlScoreResponse score(MlScoreRequest request) {
            throw new UnsupportedOperationException("test adapter");
        }
    }
}
