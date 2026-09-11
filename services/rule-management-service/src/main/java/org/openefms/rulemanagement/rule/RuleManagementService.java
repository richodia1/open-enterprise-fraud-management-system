package org.openefms.rulemanagement.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final RuleDefinitionValidator validator = new RuleDefinitionValidator();
    private final Clock clock;

    RuleManagementService(RuleDefinitionRepository repository, Clock clock) {
        this.repository = repository;
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
        return repository.save(definition);
    }

    FraudRuleDefinition replace(String ruleCode, RuleDefinitionRequest request) {
        validator.validateCreateOrReplace(request);
        ensurePathMatchesRequest(ruleCode, request.ruleCode());
        FraudRuleDefinition existing = get(request.tenantId(), ruleCode);
        return repository.save(definitionFrom(request, existing.ruleId(), existing.version() + 1));
    }

    FraudRuleDefinition updateStatus(String ruleCode, RuleStatusUpdateRequest request) {
        validator.validateStatusUpdate(request);
        FraudRuleDefinition existing = get(request.tenantId(), ruleCode);
        return repository.save(new FraudRuleDefinition(
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
}
