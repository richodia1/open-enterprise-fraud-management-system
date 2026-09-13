package org.openefms.rulemanagement.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.openefms.common.models.rule.FraudRuleCategory;
import org.openefms.common.models.rule.FraudRuleDefinition;
import org.openefms.common.models.rule.FraudRuleStatus;
import org.springframework.stereotype.Service;

@Service
class RuleManagementService {
    private final RuleDefinitionRepository repository;
    private final RuleConfigurationRepository configurationRepository;
    private final RuleDefinitionValidator validator = new RuleDefinitionValidator();
    private final Clock clock;

    RuleManagementService(
            RuleDefinitionRepository repository,
            RuleConfigurationRepository configurationRepository,
            Clock clock) {
        this.repository = repository;
        this.configurationRepository = configurationRepository;
        this.clock = clock;
    }

    List<FraudRuleDefinition> list(String tenantId, String status, String category) {
        validator.validateFilter(tenantId, status, category);
        String resolvedTenantId = normaliseTenantId(tenantId);
        FraudRuleStatus resolvedStatus = parseStatus(status);
        FraudRuleCategory resolvedCategory = parseCategory(category);
        return repository.findAll().stream()
                .filter(rule -> rule.tenantId().equals(resolvedTenantId))
                .filter(rule -> resolvedStatus == null || rule.status() == resolvedStatus)
                .filter(rule -> resolvedCategory == null || rule.category() == resolvedCategory)
                .toList();
    }

    FraudRuleDefinition get(String tenantId, String ruleCode) {
        validator.validateLookup(tenantId, ruleCode);
        return repository.findByTenantIdAndRuleCode(normaliseTenantId(tenantId), normaliseRuleCode(ruleCode))
                .orElseThrow(() -> new RuleDefinitionNotFoundException(normaliseTenantId(tenantId), normaliseRuleCode(ruleCode)));
    }

    FraudRuleDefinition create(RuleDefinitionRequest request) {
        validator.validateCreateOrReplace(request);
        FraudRuleDefinition definition = definitionFrom(request, UUID.randomUUID(), 1);
        repository.findByTenantIdAndRuleCode(definition.tenantId(), definition.ruleCode())
                .ifPresent(existing -> {
                    throw new RuleDefinitionConflictException(existing.tenantId(), existing.ruleCode());
                });
        FraudRuleDefinition created = repository.save(definition);
        appendRuleAuditTrail(created, RuleAuditAction.CREATED, null, created.status(), "Rule created.", null, created.updatedBy());
        return created;
    }

    FraudRuleDefinition replace(String ruleCode, RuleDefinitionRequest request) {
        validator.validateCreateOrReplace(request);
        ensurePathMatchesRequest(ruleCode, request.ruleCode());
        FraudRuleDefinition existing = get(request.tenantId(), ruleCode);
        FraudRuleDefinition replaced = repository.save(definitionFrom(request, existing.ruleId(), existing.version() + 1));
        appendRuleAuditTrail(replaced, RuleAuditAction.UPDATED, existing.status(), replaced.status(), "Rule updated.", null, replaced.updatedBy());
        return replaced;
    }

    FraudRuleDefinition updateStatus(String ruleCode, RuleStatusUpdateRequest request) {
        validator.validateStatusUpdate(request);
        FraudRuleDefinition existing = get(request.tenantId(), ruleCode);
        FraudRuleDefinition updated = repository.save(new FraudRuleDefinition(
                existing.ruleId(),
                existing.tenantId(),
                existing.ruleCode(),
                existing.ruleName(),
                existing.category(),
                request.status(),
                existing.severity(),
                existing.contribution(),
                existing.explanation(),
                existing.parameters(),
                existing.version() + 1,
                OffsetDateTime.now(clock),
                textOrNull(request.updatedBy())));
        appendRuleAuditTrail(
                updated,
                RuleAuditAction.STATUS_CHANGED,
                existing.status(),
                updated.status(),
                "Rule status changed.",
                null,
                updated.updatedBy());
        return updated;
    }

    void delete(String tenantId, String ruleCode, String updatedBy) {
        validator.validateLookup(tenantId, ruleCode);
        validateRequiredText(updatedBy, "updatedBy");
        FraudRuleDefinition existing = get(tenantId, ruleCode);
        repository.delete(existing.tenantId(), existing.ruleCode());
        appendRuleAuditTrail(existing, RuleAuditAction.DELETED, existing.status(), null, "Rule deleted.", null, textOrNull(updatedBy));
    }

    RuleApproval submitForApproval(String ruleCode, RuleApprovalRequest request) {
        validateApprovalRequest(request);
        FraudRuleDefinition existing = get(request.tenantId(), ruleCode);
        RuleApprovalAction requestedAction = request.requestedAction() == null ? RuleApprovalAction.UPDATE : request.requestedAction();
        RuleApproval approval = new RuleApproval(
                newId(),
                existing.tenantId(),
                existing.ruleId(),
                existing.ruleCode(),
                existing.ruleName(),
                requestedAction,
                RuleApprovalStatus.PENDING,
                textOrNull(request.requestedBy()),
                OffsetDateTime.now(clock),
                textOrNull(request.note()),
                null,
                null,
                null);
        RuleApproval saved = configurationRepository.saveApproval(approval);
        appendRuleAuditTrail(
                existing,
                RuleAuditAction.SUBMITTED_FOR_APPROVAL,
                existing.status(),
                existing.status(),
                "Rule submitted for approval.",
                null,
                saved.requestedBy());
        return saved;
    }

    List<RuleApproval> listPendingApprovals(String tenantId) {
        validateRequiredText(tenantId, "tenantId");
        String resolvedTenantId = normaliseTenantId(tenantId);
        return configurationRepository.findApprovals().stream()
                .filter(approval -> approval.tenantId().equals(resolvedTenantId))
                .filter(approval -> approval.status() == RuleApprovalStatus.PENDING)
                .toList();
    }

    RuleApproval approve(String approvalId, RuleApprovalDecisionRequest request) {
        validateDecisionRequest(request);
        RuleApproval approval = pendingApproval(approvalId);
        RuleApproval approved = new RuleApproval(
                approval.approvalId(),
                approval.tenantId(),
                approval.ruleId(),
                approval.ruleCode(),
                approval.ruleName(),
                approval.requestedAction(),
                RuleApprovalStatus.APPROVED,
                approval.requestedBy(),
                approval.requestedAt(),
                approval.requestNote(),
                textOrNull(request.decidedBy()),
                OffsetDateTime.now(clock),
                textOrNull(request.decisionNote()));
        applyApprovedAction(approved, textOrNull(request.decidedBy()));
        RuleApproval saved = configurationRepository.saveApproval(approved);
        appendApprovalAuditTrail(saved, RuleAuditAction.APPROVED, saved.decisionNote(), saved.decidedBy());
        return saved;
    }

    RuleApproval reject(String approvalId, RuleApprovalDecisionRequest request) {
        validateDecisionRequest(request);
        RuleApproval approval = pendingApproval(approvalId);
        RuleApproval rejected = new RuleApproval(
                approval.approvalId(),
                approval.tenantId(),
                approval.ruleId(),
                approval.ruleCode(),
                approval.ruleName(),
                approval.requestedAction(),
                RuleApprovalStatus.REJECTED,
                approval.requestedBy(),
                approval.requestedAt(),
                approval.requestNote(),
                textOrNull(request.decidedBy()),
                OffsetDateTime.now(clock),
                textOrNull(request.decisionNote()));
        RuleApproval saved = configurationRepository.saveApproval(rejected);
        appendApprovalAuditTrail(saved, RuleAuditAction.REJECTED, saved.decisionNote(), saved.decidedBy());
        return saved;
    }

    List<RuleCategory> listCategories(String tenantId, String filterText, String name, String description) {
        validateRequiredText(tenantId, "tenantId");
        String resolvedTenantId = normaliseTenantId(tenantId);
        return configurationRepository.findCategories().stream()
                .filter(category -> category.tenantId().equals(resolvedTenantId))
                .filter(category -> matches(category.name(), name))
                .filter(category -> matches(category.description(), description))
                .filter(category -> matchesAny(filterText, category.name(), category.description()))
                .toList();
    }

    RuleCategory getCategory(String id) {
        validateRequiredText(id, "id");
        return configurationRepository.findCategory(id)
                .orElseThrow(() -> new RuleManagementResourceNotFoundException("Rule category", textOrNull(id)));
    }

    RuleCategory createCategory(RuleCategoryRequest request) {
        validateCategoryRequest(request);
        String tenantId = normaliseTenantId(request.tenantId());
        String name = textOrNull(request.name());
        ensureCategoryNameAvailable(null, tenantId, name);
        OffsetDateTime now = OffsetDateTime.now(clock);
        return configurationRepository.saveCategory(new RuleCategory(
                newId(),
                tenantId,
                name,
                textOrNull(request.description()),
                now,
                now,
                textOrNull(request.updatedBy())));
    }

    RuleCategory replaceCategory(String id, RuleCategoryRequest request) {
        validateRequiredText(id, "id");
        validateCategoryRequest(request);
        RuleCategory existing = getCategory(id);
        String tenantId = normaliseTenantId(request.tenantId());
        String name = textOrNull(request.name());
        ensureCategoryNameAvailable(existing.id(), tenantId, name);
        return configurationRepository.saveCategory(new RuleCategory(
                existing.id(),
                tenantId,
                name,
                textOrNull(request.description()),
                existing.createdAt(),
                OffsetDateTime.now(clock),
                textOrNull(request.updatedBy())));
    }

    void deleteCategory(String id) {
        RuleCategory existing = getCategory(id);
        configurationRepository.deleteCategory(existing.id());
    }

    List<RuleEscalationUser> listEscalationUsers(
            String tenantId,
            String filterText,
            String email,
            String userId,
            String ruleId,
            String ruleCode,
            String name) {
        validateRequiredText(tenantId, "tenantId");
        String resolvedTenantId = normaliseTenantId(tenantId);
        return configurationRepository.findEscalationUsers().stream()
                .filter(user -> user.tenantId().equals(resolvedTenantId))
                .filter(user -> matches(user.email(), email))
                .filter(user -> matches(user.userId(), userId))
                .filter(user -> matches(user.ruleId() == null ? null : user.ruleId().toString(), ruleId))
                .filter(user -> matches(user.ruleCode(), ruleCode))
                .filter(user -> matches(user.name(), name))
                .filter(user -> matchesAny(filterText, user.email(), user.userId(), user.ruleCode(), user.name()))
                .toList();
    }

    RuleEscalationUser getEscalationUser(String id) {
        validateRequiredText(id, "id");
        return configurationRepository.findEscalationUser(id)
                .orElseThrow(() -> new RuleManagementResourceNotFoundException("Rule escalation user", textOrNull(id)));
    }

    RuleEscalationUser createEscalationUser(RuleEscalationUserRequest request) {
        validateEscalationUserRequest(request);
        RuleReference reference = resolveRuleReference(request.tenantId(), request.ruleId(), request.ruleCode());
        String email = normaliseEmail(request.email());
        ensureEscalationUserAvailable(null, reference.tenantId(), reference.ruleId(), reference.ruleCode(), email);
        OffsetDateTime now = OffsetDateTime.now(clock);
        return configurationRepository.saveEscalationUser(new RuleEscalationUser(
                newId(),
                reference.tenantId(),
                reference.ruleId(),
                reference.ruleCode(),
                email,
                textOrNull(request.userId()),
                textOrNull(request.name()),
                now,
                now,
                textOrNull(request.updatedBy())));
    }

    RuleEscalationUser replaceEscalationUser(String id, RuleEscalationUserRequest request) {
        validateRequiredText(id, "id");
        validateEscalationUserRequest(request);
        RuleEscalationUser existing = getEscalationUser(id);
        RuleReference reference = resolveRuleReference(request.tenantId(), request.ruleId(), request.ruleCode());
        String email = normaliseEmail(request.email());
        ensureEscalationUserAvailable(existing.id(), reference.tenantId(), reference.ruleId(), reference.ruleCode(), email);
        return configurationRepository.saveEscalationUser(new RuleEscalationUser(
                existing.id(),
                reference.tenantId(),
                reference.ruleId(),
                reference.ruleCode(),
                email,
                textOrNull(request.userId()),
                textOrNull(request.name()),
                existing.createdAt(),
                OffsetDateTime.now(clock),
                textOrNull(request.updatedBy())));
    }

    void deleteEscalationUser(String id) {
        RuleEscalationUser existing = getEscalationUser(id);
        configurationRepository.deleteEscalationUser(existing.id());
    }

    List<RuleConditionDefinition> listConditionDefinitions(
            String tenantId,
            String filterText,
            String ruleId,
            String ruleCode,
            String sourceDataPointCode,
            String destinationDataPointCode,
            String operator) {
        validateRequiredText(tenantId, "tenantId");
        String resolvedTenantId = normaliseTenantId(tenantId);
        return configurationRepository.findConditionDefinitions().stream()
                .filter(definition -> definition.tenantId().equals(resolvedTenantId))
                .filter(definition -> matches(definition.ruleId() == null ? null : definition.ruleId().toString(), ruleId))
                .filter(definition -> matches(definition.ruleCode(), ruleCode))
                .filter(definition -> matches(definition.sourceDataPointCode(), sourceDataPointCode))
                .filter(definition -> matches(definition.destinationDataPointCode(), destinationDataPointCode))
                .filter(definition -> matches(definition.operator(), operator))
                .filter(definition -> matchesAny(
                        filterText,
                        definition.ruleCode(),
                        definition.operator(),
                        definition.datapointValue(),
                        definition.sourceDataPointCode(),
                        definition.destinationDataPointCode(),
                        definition.datapointDataType(),
                        definition.destinationDataType()))
                .toList();
    }

    RuleConditionDefinition getConditionDefinition(String id) {
        validateRequiredText(id, "id");
        return configurationRepository.findConditionDefinition(id)
                .orElseThrow(() -> new RuleManagementResourceNotFoundException("Rule definition", textOrNull(id)));
    }

    RuleConditionDefinition createConditionDefinition(RuleConditionDefinitionRequest request) {
        validateConditionDefinitionRequest(request);
        RuleReference reference = resolveRuleReference(request.tenantId(), request.ruleId(), request.ruleCode());
        OffsetDateTime now = OffsetDateTime.now(clock);
        return configurationRepository.saveConditionDefinition(new RuleConditionDefinition(
                newId(),
                reference.tenantId(),
                reference.ruleId(),
                reference.ruleCode(),
                textOrNull(request.operator()),
                textOrNull(request.datapointValue()),
                textOrNull(request.groupId()),
                textOrNull(request.groupConnector()),
                textOrNull(request.sourceDataPointId()),
                textOrNull(request.listTypeDataId()),
                codeOrNull(request.sourceDataPointCode()),
                codeOrNull(request.destinationDataPointCode()),
                textOrNull(request.datapointDataType()),
                textOrNull(request.destinationDataType()),
                now,
                now,
                textOrNull(request.updatedBy())));
    }

    RuleConditionDefinition replaceConditionDefinition(String id, RuleConditionDefinitionRequest request) {
        validateRequiredText(id, "id");
        validateConditionDefinitionRequest(request);
        RuleConditionDefinition existing = getConditionDefinition(id);
        RuleReference reference = resolveRuleReference(request.tenantId(), request.ruleId(), request.ruleCode());
        return configurationRepository.saveConditionDefinition(new RuleConditionDefinition(
                existing.id(),
                reference.tenantId(),
                reference.ruleId(),
                reference.ruleCode(),
                textOrNull(request.operator()),
                textOrNull(request.datapointValue()),
                textOrNull(request.groupId()),
                textOrNull(request.groupConnector()),
                textOrNull(request.sourceDataPointId()),
                textOrNull(request.listTypeDataId()),
                codeOrNull(request.sourceDataPointCode()),
                codeOrNull(request.destinationDataPointCode()),
                textOrNull(request.datapointDataType()),
                textOrNull(request.destinationDataType()),
                existing.createdAt(),
                OffsetDateTime.now(clock),
                textOrNull(request.updatedBy())));
    }

    void deleteConditionDefinition(String id) {
        RuleConditionDefinition existing = getConditionDefinition(id);
        configurationRepository.deleteConditionDefinition(existing.id());
    }

    List<RuleAuditTrail> listAuditTrails(String tenantId, String ruleId, String ruleCode, RuleAuditAction action) {
        validateRequiredText(tenantId, "tenantId");
        String resolvedTenantId = normaliseTenantId(tenantId);
        return configurationRepository.findAuditTrails().stream()
                .filter(audit -> audit.tenantId().equals(resolvedTenantId))
                .filter(audit -> matches(audit.ruleId() == null ? null : audit.ruleId().toString(), ruleId))
                .filter(audit -> matches(audit.ruleCode(), ruleCode))
                .filter(audit -> action == null || audit.action() == action)
                .toList();
    }

    RuleAuditTrail getAuditTrail(String id) {
        validateRequiredText(id, "id");
        return configurationRepository.findAuditTrail(id)
                .orElseThrow(() -> new RuleManagementResourceNotFoundException("Rule audit trail", textOrNull(id)));
    }

    RuleAuditTrail createAuditTrail(RuleAuditTrailRequest request) {
        validateAuditTrailRequest(request);
        OffsetDateTime now = OffsetDateTime.now(clock);
        String createdBy = textOrNull(request.createdBy());
        return configurationRepository.saveAuditTrail(new RuleAuditTrail(
                newId(),
                normaliseTenantId(request.tenantId()),
                request.ruleId(),
                codeOrNull(request.ruleCode()),
                request.action(),
                request.fromStatus(),
                request.toStatus(),
                textOrNull(request.note()),
                textOrNull(request.correlationId()),
                now,
                createdBy,
                now,
                textOrNull(request.updatedBy()) == null ? createdBy : textOrNull(request.updatedBy())));
    }

    RuleAuditTrail replaceAuditTrail(String id, RuleAuditTrailRequest request) {
        validateRequiredText(id, "id");
        validateAuditTrailRequest(request);
        RuleAuditTrail existing = getAuditTrail(id);
        return configurationRepository.saveAuditTrail(new RuleAuditTrail(
                existing.id(),
                normaliseTenantId(request.tenantId()),
                request.ruleId(),
                codeOrNull(request.ruleCode()),
                request.action(),
                request.fromStatus(),
                request.toStatus(),
                textOrNull(request.note()),
                textOrNull(request.correlationId()),
                existing.createdAt(),
                existing.createdBy(),
                OffsetDateTime.now(clock),
                textOrNull(request.updatedBy()) == null ? existing.createdBy() : textOrNull(request.updatedBy())));
    }

    void deleteAuditTrail(String id) {
        RuleAuditTrail existing = getAuditTrail(id);
        configurationRepository.deleteAuditTrail(existing.id());
    }

    private FraudRuleDefinition definitionFrom(RuleDefinitionRequest request, UUID ruleId, int version) {
        return new FraudRuleDefinition(
                ruleId,
                normaliseTenantId(request.tenantId()),
                normaliseRuleCode(request.ruleCode()),
                textOrNull(request.ruleName()),
                request.category(),
                request.status(),
                request.severity(),
                normaliseContribution(request.contribution()),
                textOrNull(request.explanation()),
                normaliseParameters(request.parameters()),
                version,
                OffsetDateTime.now(clock),
                textOrNull(request.updatedBy()));
    }

    private void ensurePathMatchesRequest(String pathRuleCode, String requestRuleCode) {
        if (!normaliseRuleCode(pathRuleCode).equals(normaliseRuleCode(requestRuleCode))) {
            throw new InvalidRuleDefinitionException(List.of("ruleCode in the path must match ruleCode in the request body"));
        }
    }

    private FraudRuleStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return FraudRuleStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidRuleDefinitionException(List.of("status must be one of DRAFT, ACTIVE, or INACTIVE"));
        }
    }

    private FraudRuleCategory parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return FraudRuleCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidRuleDefinitionException(List.of("category must be one of AMOUNT, VELOCITY, GEOGRAPHY, SIGNAL, or CHANNEL"));
        }
    }

    private String normaliseTenantId(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normaliseRuleCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String codeOrNull(String value) {
        String text = textOrNull(value);
        return text == null ? null : text.toUpperCase(Locale.ROOT);
    }

    private String normaliseEmail(String value) {
        return textOrNull(value).toLowerCase(Locale.ROOT);
    }

    private String textOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal normaliseContribution(BigDecimal contribution) {
        return contribution.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, String> normaliseParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Map.of();
        }
        Map<String, String> normalised = new LinkedHashMap<>();
        parameters.forEach((key, value) -> normalised.put(key.trim(), value.trim()));
        return normalised;
    }

    private void appendRuleAuditTrail(
            FraudRuleDefinition rule,
            RuleAuditAction action,
            FraudRuleStatus fromStatus,
            FraudRuleStatus toStatus,
            String note,
            String correlationId,
            String actor) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        configurationRepository.saveAuditTrail(new RuleAuditTrail(
                newId(),
                rule.tenantId(),
                rule.ruleId(),
                rule.ruleCode(),
                action,
                fromStatus,
                toStatus,
                note,
                correlationId,
                now,
                actor,
                now,
                actor));
    }

    private void appendApprovalAuditTrail(
            RuleApproval approval,
            RuleAuditAction action,
            String note,
            String actor) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        configurationRepository.saveAuditTrail(new RuleAuditTrail(
                newId(),
                approval.tenantId(),
                approval.ruleId(),
                approval.ruleCode(),
                action,
                null,
                null,
                note,
                null,
                now,
                actor,
                now,
                actor));
    }

    private RuleApproval pendingApproval(String approvalId) {
        validateRequiredText(approvalId, "approvalId");
        RuleApproval approval = configurationRepository.findApproval(approvalId)
                .orElseThrow(() -> new RuleManagementResourceNotFoundException("Rule approval", textOrNull(approvalId)));
        if (approval.status() != RuleApprovalStatus.PENDING) {
            throw new InvalidRuleDefinitionException(List.of("approval must be pending before it can be decided"));
        }
        return approval;
    }

    private void applyApprovedAction(RuleApproval approval, String actor) {
        if (approval.requestedAction() == RuleApprovalAction.DELETE) {
            repository.delete(approval.tenantId(), approval.ruleCode());
            return;
        }

        FraudRuleDefinition existing = get(approval.tenantId(), approval.ruleCode());
        FraudRuleStatus targetStatus = switch (approval.requestedAction()) {
            case ACTIVATE, CREATE -> FraudRuleStatus.ACTIVE;
            case DEACTIVATE -> FraudRuleStatus.INACTIVE;
            case UPDATE -> existing.status();
            case DELETE -> existing.status();
        };
        if (existing.status() == targetStatus) {
            return;
        }
        repository.save(new FraudRuleDefinition(
                existing.ruleId(),
                existing.tenantId(),
                existing.ruleCode(),
                existing.ruleName(),
                existing.category(),
                targetStatus,
                existing.severity(),
                existing.contribution(),
                existing.explanation(),
                existing.parameters(),
                existing.version() + 1,
                OffsetDateTime.now(clock),
                actor));
    }

    private RuleReference resolveRuleReference(String tenantId, UUID ruleId, String ruleCode) {
        validateRequiredText(tenantId, "tenantId");
        if (ruleCode == null || ruleCode.isBlank()) {
            if (ruleId == null) {
                throw new InvalidRuleDefinitionException(List.of("ruleCode or ruleId must be supplied"));
            }
            return new RuleReference(normaliseTenantId(tenantId), ruleId, null);
        }

        FraudRuleDefinition rule = get(tenantId, ruleCode);
        if (ruleId != null && !rule.ruleId().equals(ruleId)) {
            throw new InvalidRuleDefinitionException(List.of("ruleId must match the supplied ruleCode"));
        }
        return new RuleReference(rule.tenantId(), rule.ruleId(), rule.ruleCode());
    }

    private void ensureCategoryNameAvailable(String currentId, String tenantId, String name) {
        configurationRepository.findCategories().stream()
                .filter(category -> currentId == null || !category.id().equals(currentId))
                .filter(category -> category.tenantId().equals(tenantId))
                .filter(category -> category.name().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(category -> {
                    throw new RuleManagementResourceConflictException("Rule category " + name + " already exists");
                });
    }

    private void ensureEscalationUserAvailable(
            String currentId,
            String tenantId,
            UUID ruleId,
            String ruleCode,
            String email) {
        configurationRepository.findEscalationUsers().stream()
                .filter(user -> currentId == null || !user.id().equals(currentId))
                .filter(user -> user.tenantId().equals(tenantId))
                .filter(user -> ruleId == null || ruleId.equals(user.ruleId()))
                .filter(user -> ruleCode == null || ruleCode.equals(user.ruleCode()))
                .filter(user -> user.email().equalsIgnoreCase(email))
                .findFirst()
                .ifPresent(user -> {
                    throw new RuleManagementResourceConflictException("Rule escalation user " + email + " already exists");
                });
    }

    private boolean matches(String actual, String expected) {
        String expectedText = textOrNull(expected);
        if (expectedText == null) {
            return true;
        }
        String actualText = textOrNull(actual);
        return actualText != null && actualText.toLowerCase(Locale.ROOT).contains(expectedText.toLowerCase(Locale.ROOT));
    }

    private boolean matchesAny(String filterText, String... values) {
        String filter = textOrNull(filterText);
        if (filter == null) {
            return true;
        }
        for (String value : values) {
            if (matches(value, filter)) {
                return true;
            }
        }
        return false;
    }

    private void validateCategoryRequest(RuleCategoryRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            addRequiredTextError(request.tenantId(), "tenantId", errors);
            addRequiredTextError(request.name(), "name", errors);
            addRequiredTextError(request.updatedBy(), "updatedBy", errors);
        }
        throwIfInvalid(errors);
    }

    private void validateEscalationUserRequest(RuleEscalationUserRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            addRequiredTextError(request.tenantId(), "tenantId", errors);
            if ((request.ruleCode() == null || request.ruleCode().isBlank()) && request.ruleId() == null) {
                errors.add("ruleCode or ruleId must be supplied");
            }
            addRequiredTextError(request.email(), "email", errors);
            addRequiredTextError(request.userId(), "userId", errors);
            addRequiredTextError(request.name(), "name", errors);
            addRequiredTextError(request.updatedBy(), "updatedBy", errors);
            if (request.email() != null && !request.email().isBlank() && !request.email().contains("@")) {
                errors.add("email must contain @");
            }
        }
        throwIfInvalid(errors);
    }

    private void validateConditionDefinitionRequest(RuleConditionDefinitionRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            addRequiredTextError(request.tenantId(), "tenantId", errors);
            if ((request.ruleCode() == null || request.ruleCode().isBlank()) && request.ruleId() == null) {
                errors.add("ruleCode or ruleId must be supplied");
            }
            addRequiredTextError(request.operator(), "operator", errors);
            addRequiredTextError(request.sourceDataPointCode(), "sourceDataPointCode", errors);
            addRequiredTextError(request.datapointDataType(), "datapointDataType", errors);
            addRequiredTextError(request.updatedBy(), "updatedBy", errors);
        }
        throwIfInvalid(errors);
    }

    private void validateAuditTrailRequest(RuleAuditTrailRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            addRequiredTextError(request.tenantId(), "tenantId", errors);
            if ((request.ruleCode() == null || request.ruleCode().isBlank()) && request.ruleId() == null) {
                errors.add("ruleCode or ruleId must be supplied");
            }
            if (request.action() == null) {
                errors.add("action must not be null");
            }
            addRequiredTextError(request.createdBy(), "createdBy", errors);
        }
        throwIfInvalid(errors);
    }

    private void validateApprovalRequest(RuleApprovalRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            addRequiredTextError(request.tenantId(), "tenantId", errors);
            addRequiredTextError(request.requestedBy(), "requestedBy", errors);
        }
        throwIfInvalid(errors);
    }

    private void validateDecisionRequest(RuleApprovalDecisionRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            addRequiredTextError(request.decidedBy(), "decidedBy", errors);
        }
        throwIfInvalid(errors);
    }

    private void validateRequiredText(String value, String fieldName) {
        List<String> errors = new ArrayList<>();
        addRequiredTextError(value, fieldName, errors);
        throwIfInvalid(errors);
    }

    private void addRequiredTextError(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " must not be blank");
        }
    }

    private void throwIfInvalid(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new InvalidRuleDefinitionException(errors);
        }
    }

    private String newId() {
        return UUID.randomUUID().toString();
    }

    private record RuleReference(String tenantId, UUID ruleId, String ruleCode) {
    }
}
