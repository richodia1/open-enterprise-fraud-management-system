package org.openefms.casemanagement.repository;

import java.util.List;
import java.util.UUID;
import org.openefms.casemanagement.entity.FraudAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFraudAlertRepository extends JpaRepository<FraudAlertEntity, UUID> {
    public List<FraudAlertEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
