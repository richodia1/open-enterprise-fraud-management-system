package org.openefms.ingestionmanagement.repository;

import java.util.List;
import java.util.UUID;
import org.openefms.ingestionmanagement.entity.ReceivedTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReceivedTransactionRepository extends JpaRepository<ReceivedTransactionEntity, UUID> {
    public List<ReceivedTransactionEntity> findByTenantIdOrderByAcceptedAtDesc(String tenantId);
}
