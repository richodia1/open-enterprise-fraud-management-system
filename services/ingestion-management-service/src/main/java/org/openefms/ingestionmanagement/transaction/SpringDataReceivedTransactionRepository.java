package org.openefms.ingestionmanagement.transaction;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataReceivedTransactionRepository extends JpaRepository<ReceivedTransactionEntity, UUID> {
    List<ReceivedTransactionEntity> findByTenantIdOrderByAcceptedAtDesc(String tenantId);
}
