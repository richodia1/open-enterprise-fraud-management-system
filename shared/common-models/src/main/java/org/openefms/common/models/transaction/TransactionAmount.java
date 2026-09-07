package org.openefms.common.models.transaction;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Monetary amount carried by a transaction contract.
 */
public record TransactionAmount(
        BigDecimal value,
        String currency
) {
    public TransactionAmount {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
