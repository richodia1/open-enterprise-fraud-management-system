package org.openefms.ingestionmanagement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.ingestionmanagement.entity.TransactionReceiptRecord;

public interface TransactionReceiptRepository {
    public TransactionReceiptRecord saveReceived(TransactionReceiptRecord record, TransactionIngestionRequest transaction);

    public TransactionReceiptRecord markPublished(UUID transactionId);

    public Optional<TransactionReceiptRecord> findByTransactionId(UUID transactionId);

    public List<TransactionReceiptRecord> findByTenantId(String tenantId);
}
