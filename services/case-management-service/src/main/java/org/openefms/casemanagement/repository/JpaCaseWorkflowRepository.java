package org.openefms.casemanagement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseActionRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.entity.CaseWorkflowRecord;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCaseWorkflowRepository implements CaseWorkflowRepository {
    private final SpringDataFraudAlertRepository alertRepository;
    private final SpringDataFraudCaseActionRepository actionRepository;
    private final SpringDataFraudCaseRepository caseRepository;
    private final CaseWorkflowRecordMapper mapper;

    public JpaCaseWorkflowRepository(
            SpringDataFraudAlertRepository alertRepository,
            SpringDataFraudCaseActionRepository actionRepository,
            SpringDataFraudCaseRepository caseRepository,
            CaseWorkflowRecordMapper mapper) {
        this.alertRepository = alertRepository;
        this.actionRepository = actionRepository;
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

    @Override
    @Transactional
    public CaseActionRecord saveCaseAction(CaseRecord caseFile, CaseActionRecord action) {
        caseRepository.save(mapper.toEntity(caseFile));
        return mapper.toRecord(actionRepository.save(mapper.toEntity(action)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaseActionRecord> findCaseActionsByCaseId(UUID caseId) {
        return actionRepository.findByCaseIdOrderByOccurredAtAsc(caseId).stream()
                .map(mapper::toRecord)
                .toList();
    }
}
