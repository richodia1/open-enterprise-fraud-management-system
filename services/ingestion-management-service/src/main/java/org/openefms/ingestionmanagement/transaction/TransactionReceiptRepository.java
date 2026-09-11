package org.openefms.ingestionmanagement.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openefms.common.models.transaction.TransactionIngestionRequest;

interface TransactionReceiptRepository {
    TransactionReceiptRecord saveReceived(TransactionReceiptRecord record, TransactionIngestionRequest transaction);

    TransactionReceiptRecord markPublished(UUID transactionId);

    Optional<TransactionReceiptRecord> findByTransactionId(UUID transactionId);

    List<TransactionReceiptRecord> findByTenantId(String tenantId);
}
