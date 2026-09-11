package org.openefms.frauddetection.decision;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataFraudDecisionRepository extends JpaRepository<FraudDecisionEntity, UUID> {
    List<FraudDecisionEntity> findByTransactionIdOrderByDecidedAtDesc(UUID transactionId);

    List<FraudDecisionEntity> findByTenantIdOrderByDecidedAtDesc(String tenantId);
}
