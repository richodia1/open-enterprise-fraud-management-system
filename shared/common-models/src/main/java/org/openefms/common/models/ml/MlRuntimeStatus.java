package org.openefms.common.models.ml;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openefms.common.models.fraud.DecisionMode;

/**
 * Runtime status exposed by a fraud ML scoring adapter.
 */
public record MlRuntimeStatus(
        String provider,
        String modelName,
        MlRuntimeState state,
        List<DecisionMode> supportedModes,
        String reason,
        OffsetDateTime checkedAt,
        Map<String, String> attributes
) {
    public MlRuntimeStatus {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(checkedAt, "checkedAt must not be null");
        supportedModes = supportedModes == null ? List.of() : List.copyOf(supportedModes);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
