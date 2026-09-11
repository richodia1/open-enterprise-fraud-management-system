package org.openefms.frauddetection.decision;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudDecisionRepository {
    FraudDecisionRecord save(FraudDecisionRecord record);

    Optional<FraudDecisionRecord> findByDecisionId(UUID decisionId);

    List<FraudDecisionRecord> findByTransactionId(UUID transactionId);

    List<FraudDecisionRecord> findByTenantId(String tenantId);
}
