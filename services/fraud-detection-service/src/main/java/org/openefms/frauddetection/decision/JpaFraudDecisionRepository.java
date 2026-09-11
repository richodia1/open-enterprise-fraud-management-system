package org.openefms.frauddetection.decision;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaFraudDecisionRepository implements FraudDecisionRepository {
    private final SpringDataFraudDecisionRepository repository;
    private final FraudDecisionRecordMapper mapper;

    JpaFraudDecisionRepository(
            SpringDataFraudDecisionRepository repository,
            FraudDecisionRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public FraudDecisionRecord save(FraudDecisionRecord record) {
        return mapper.toRecord(repository.save(mapper.toEntity(record)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FraudDecisionRecord> findByDecisionId(UUID decisionId) {
        return repository.findById(decisionId).map(mapper::toRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudDecisionRecord> findByTransactionId(UUID transactionId) {
        return repository.findByTransactionIdOrderByDecidedAtDesc(transactionId).stream()
                .map(mapper::toRecord)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudDecisionRecord> findByTenantId(String tenantId) {
        return repository.findByTenantIdOrderByDecidedAtDesc(tenantId).stream()
                .map(mapper::toRecord)
                .toList();
    }
}
