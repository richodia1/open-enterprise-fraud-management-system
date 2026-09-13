package org.openefms.frauddetection.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openefms.frauddetection.entity.FraudDecisionRecord;

public interface FraudDecisionRepository {
    public FraudDecisionRecord save(FraudDecisionRecord record);

    public Optional<FraudDecisionRecord> findByDecisionId(UUID decisionId);

    public List<FraudDecisionRecord> findByTransactionId(UUID transactionId);

    public List<FraudDecisionRecord> findByTenantId(String tenantId);
}
