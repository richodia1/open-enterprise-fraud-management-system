package org.openefms.casemanagement.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaCaseWorkflowRepository implements CaseWorkflowRepository {
    private final SpringDataFraudAlertRepository alertRepository;
    private final SpringDataFraudCaseRepository caseRepository;
    private final CaseWorkflowRecordMapper mapper;

    JpaCaseWorkflowRepository(
            SpringDataFraudAlertRepository alertRepository,
            SpringDataFraudCaseRepository caseRepository,
            CaseWorkflowRecordMapper mapper) {
        this.alertRepository = alertRepository;
        this.caseRepository = caseRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CaseWorkflowRecord save(AlertRecord alert, CaseRecord caseFile) {
        AlertRecord persistedAlert = mapper.toRecord(alertRepository.save(mapper.toEntity(alert)));
        CaseRecord persistedCase = mapper.toRecord(caseRepository.save(mapper.toEntity(caseFile)));
        return new CaseWorkflowRecord(persistedAlert, persistedCase);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlertRecord> findAlertById(UUID alertId) {
        return alertRepository.findById(alertId).map(mapper::toRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertRecord> findAlertsByTenantId(String tenantId) {
        return alertRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(mapper::toRecord)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CaseRecord> findCaseById(UUID caseId) {
        return caseRepository.findById(caseId).map(mapper::toRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaseRecord> findCasesByTenantId(String tenantId) {
        return caseRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(mapper::toRecord)
                .toList();
    }
}
