package org.openefms.casemanagement.repository;

import java.util.List;
import java.util.UUID;
import org.openefms.casemanagement.entity.FraudCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFraudCaseRepository extends JpaRepository<FraudCaseEntity, UUID> {
    public List<FraudCaseEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
