package org.openefms.ingestionmanagement.transaction;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaTransactionReceiptRepository implements TransactionReceiptRepository {
    private final SpringDataReceivedTransactionRepository repository;
    private final TransactionReceiptMapper mapper;
    private final Clock clock;

    JpaTransactionReceiptRepository(
            SpringDataReceivedTransactionRepository repository,
            TransactionReceiptMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public TransactionReceiptRecord saveReceived(TransactionReceiptRecord record, TransactionIngestionRequest transaction) {
        ReceivedTransactionEntity entity = mapper.toEntity(record, transaction);
        return mapper.toRecord(repository.save(entity));
    }

    @Override
    @Transactional
    public TransactionReceiptRecord markPublished(UUID transactionId) {
        ReceivedTransactionEntity entity = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionReceiptNotFoundException(transactionId));
        entity.status(TransactionReceiptStatus.PUBLISHED);
        entity.publishedAt(OffsetDateTime.now(clock));
        return mapper.toRecord(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionReceiptRecord> findByTransactionId(UUID transactionId) {
        return repository.findById(transactionId).map(mapper::toRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionReceiptRecord> findByTenantId(String tenantId) {
        return repository.findByTenantIdOrderByAcceptedAtDesc(tenantId).stream()
                .map(mapper::toRecord)
                .toList();
    }
}
