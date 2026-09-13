package org.openefms.frauddetection.repository;

import java.util.List;
import java.util.UUID;
import org.openefms.frauddetection.entity.FraudDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFraudDecisionRepository extends JpaRepository<FraudDecisionEntity, UUID> {
    public List<FraudDecisionEntity> findByTransactionIdOrderByDecidedAtDesc(UUID transactionId);

    public List<FraudDecisionEntity> findByTenantIdOrderByDecidedAtDesc(String tenantId);
}
