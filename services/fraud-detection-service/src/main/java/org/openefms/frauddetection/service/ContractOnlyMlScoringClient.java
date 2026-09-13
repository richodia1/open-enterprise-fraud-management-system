package org.openefms.frauddetection.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.ml.MlRuntimeState;
import org.openefms.common.models.ml.MlRuntimeStatus;
import org.openefms.common.models.ml.MlScoreRequest;
import org.openefms.common.models.ml.MlScoreResponse;

public class ContractOnlyMlScoringClient implements MlScoringClient {
    private static final String DISABLED_PROVIDER = "disabled";

    private final FraudDecisionProperties properties;
    private final Clock clock;

    public ContractOnlyMlScoringClient(FraudDecisionProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public MlRuntimeStatus status() {
        FraudDecisionProperties.Ml ml = properties.ml();
        MlRuntimeState state = DISABLED_PROVIDER.equalsIgnoreCase(ml.provider())
                ? MlRuntimeState.DISABLED
                : MlRuntimeState.CONTRACT_ONLY;
        String reason = state == MlRuntimeState.DISABLED
                ? "ML scoring is disabled by configuration."
                : "ML scoring contract is configured, but no production scorer adapter is registered.";

        return new MlRuntimeStatus(
                ml.provider(),
                ml.modelName(),
                state,
                List.of(DecisionMode.ML, DecisionMode.HYBRID),
                reason,
                OffsetDateTime.now(clock),
                Map.of("scoreThreshold", ml.scoreThreshold().toPlainString()));
    }

    @Override
    public MlScoreResponse score(MlScoreRequest request) {
        FraudDecisionProperties.Ml ml = properties.ml();
        MlRuntimeStatus status = status();
        throw new MlScoringUnavailableException(
                ml.provider(),
                ml.modelName(),
                status.reason());
    }
}
