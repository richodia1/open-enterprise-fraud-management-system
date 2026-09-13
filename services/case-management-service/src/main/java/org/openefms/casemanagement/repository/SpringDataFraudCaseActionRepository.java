package org.openefms.casemanagement.repository;

import java.util.List;
import java.util.UUID;
import org.openefms.casemanagement.entity.FraudCaseActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFraudCaseActionRepository extends JpaRepository<FraudCaseActionEntity, UUID> {
    public List<FraudCaseActionEntity> findByCaseIdOrderByOccurredAtAsc(UUID caseId);
}
