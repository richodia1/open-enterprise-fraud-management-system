package org.openefms.casemanagement.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataFraudAlertRepository extends JpaRepository<FraudAlertEntity, UUID> {
    List<FraudAlertEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
