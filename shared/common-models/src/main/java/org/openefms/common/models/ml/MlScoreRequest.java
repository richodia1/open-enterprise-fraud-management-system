package org.openefms.common.models.ml;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.transaction.TransactionIngestionRequest;

/**
 * Stable request contract for a transaction ML scorer.
 */
public record MlScoreRequest(
        UUID transactionId,
        String tenantId,
        String modelName,
        TransactionIngestionRequest transaction,
        Map<String, String> features
) {
    public MlScoreRequest {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(transaction, "transaction must not be null");
        features = features == null ? Map.of() : Map.copyOf(features);
    }
}
