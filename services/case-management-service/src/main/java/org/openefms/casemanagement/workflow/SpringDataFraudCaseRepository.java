package org.openefms.casemanagement.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataFraudCaseRepository extends JpaRepository<FraudCaseEntity, UUID> {
    List<FraudCaseEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
