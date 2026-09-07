package org.openefms.common.models.fraud;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Normalised risk score and coarse risk band.
 */
public record RiskScore(
        BigDecimal value,
        RiskBand band,
        String reason
) {
    public RiskScore {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(band, "band must not be null");
    }
}
