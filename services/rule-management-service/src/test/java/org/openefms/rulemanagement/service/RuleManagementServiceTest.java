package org.openefms.rulemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.rule.FraudRuleCategory;
import org.openefms.common.models.rule.FraudRuleDefinition;
import org.openefms.common.models.rule.FraudRuleStatus;
import org.openefms.rulemanagement.dto.RuleApprovalDecisionRequest;
import org.openefms.rulemanagement.dto.RuleApprovalRequest;
import org.openefms.rulemanagement.dto.RuleAuditTrailRequest;
import org.openefms.rulemanagement.dto.RuleCategoryRequest;
import org.openefms.rulemanagement.dto.RuleConditionDefinitionRequest;
import org.openefms.rulemanagement.dto.RuleDefinitionRequest;
import org.openefms.rulemanagement.dto.RuleEscalationUserRequest;
import org.openefms.rulemanagement.dto.RuleStatusUpdateRequest;
import org.openefms.rulemanagement.entity.RuleApproval;
import org.openefms.rulemanagement.entity.RuleAuditTrail;
import org.openefms.rulemanagement.entity.RuleCategory;
import org.openefms.rulemanagement.entity.RuleConditionDefinition;
import org.openefms.rulemanagement.entity.RuleEscalationUser;
import org.openefms.rulemanagement.enums.RuleApprovalAction;
import org.openefms.rulemanagement.enums.RuleApprovalStatus;
import org.openefms.rulemanagement.enums.RuleAuditAction;
import org.openefms.rulemanagement.repository.BaselineRuleDefinitions;
import org.openefms.rulemanagement.repository.InMemoryRuleConfigurationRepository;
import org.openefms.rulemanagement.repository.InMemoryRuleDefinitionRepository;

class RuleManagementServiceTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T09:00:00Z"), ZoneOffset.UTC);

    @Test
    void listsActiveBaselineRulesForTheGlobalTenant() {
        RuleManagementService service = service();

        List<FraudRuleDefinition> rules = service.list("GLOBAL", "active", null);

        assertEquals(7, rules.size());
        assertTrue(ruleCodes(rules).contains("HIGH_VALUE_AMOUNT"));
        assertTrue(ruleCodes(rules).contains("CRITICAL_VALUE_AMOUNT"));
        assertTrue(ruleCodes(rules).contains("VELOCITY_REVIEW_THRESHOLD"));
        assertTrue(ruleCodes(rules).contains("VELOCITY_BLOCK_THRESHOLD"));
        assertTrue(ruleCodes(rules).contains("CROSS_BORDER_PARTIES"));
        assertTrue(ruleCodes(rules).contains("HIGH_RISK_HINT"));
        assertTrue(ruleCodes(rules).contains("UNKNOWN_TRANSACTION_CHANNEL"));
        assertEquals(OffsetDateTime.parse("2026-09-11T09:00:00Z"), rules.getFirst().updatedAt());
    }

    @Test
    void filtersRulesByCategory() {
        RuleManagementService service = service();

        List<FraudRuleDefinition> amountRules = service.list("global", null, "amount");

        assertEquals(2, amountRules.size());
        assertTrue(amountRules.stream().allMatch(rule -> rule.category() == FraudRuleCategory.AMOUNT));
    }

    @Test
    void createsCustomTenantRuleDefinitions() {
        RuleManagementService service = service();

        FraudRuleDefinition created = service.create(customRule(" tenant-a ", " custom_rule_1 "));

        assertEquals("tenant-a", created.tenantId());
        assertEquals("CUSTOM_RULE_1", created.ruleCode());
        assertEquals("Custom rule", created.ruleName());
        assertEquals(FraudRuleCategory.SIGNAL, created.category());
        assertEquals(FraudRuleStatus.ACTIVE, created.status());
        assertEquals(RiskBand.HIGH, created.severity());
        assertEquals(new BigDecimal("0.27"), created.contribution());
        assertEquals("manual", created.parameters().get("source"));
        assertEquals(1, created.version());
        assertEquals("analyst", created.updatedBy());
    }

    @Test
    void rejectsDuplicateRuleDefinitionsForTheSameTenant() {
        RuleManagementService service = service();
        service.create(customRule("tenant-a", "CUSTOM_RULE_1"));

        RuleDefinitionConflictException exception = assertThrows(
                RuleDefinitionConflictException.class,
                () -> service.create(customRule("tenant-a", "CUSTOM_RULE_1")));

        assertTrue(exception.getMessage().contains("CUSTOM_RULE_1 already exists"));
    }

    @Test
    void replacesExistingRuleDefinitionsAndIncrementsVersion() {
        RuleManagementService service = service();
        FraudRuleDefinition created = service.create(customRule("tenant-a", "CUSTOM_RULE_1"));

        FraudRuleDefinition replaced = service.replace(
                "CUSTOM_RULE_1",
                new RuleDefinitionRequest(
                        "tenant-a",
                        "CUSTOM_RULE_1",
                        "Custom rule replacement",
                        FraudRuleCategory.SIGNAL,
                        FraudRuleStatus.INACTIVE,
                        RiskBand.MEDIUM,
                        new BigDecimal("0.18"),
                        "Replacement explanation.",
                        Map.of("source", "governance"),
                        "approver"));

        assertEquals(created.ruleId(), replaced.ruleId());
        assertEquals(2, replaced.version());
        assertEquals("Custom rule replacement", replaced.ruleName());
        assertEquals(FraudRuleStatus.INACTIVE, replaced.status());
        assertEquals(new BigDecimal("0.18"), replaced.contribution());
        assertEquals("governance", replaced.parameters().get("source"));
        assertEquals("approver", replaced.updatedBy());
    }

    @Test
    void updatesExistingRuleStatus() {
        RuleManagementService service = service();

        FraudRuleDefinition updated = service.updateStatus(
                "high_value_amount",
                new RuleStatusUpdateRequest(" GLOBAL ", FraudRuleStatus.INACTIVE, " analyst "));

        assertEquals("global", updated.tenantId());
        assertEquals("HIGH_VALUE_AMOUNT", updated.ruleCode());
        assertEquals(FraudRuleStatus.INACTIVE, updated.status());
        assertEquals(2, updated.version());
        assertEquals("analyst", updated.updatedBy());
    }

    @Test
    void deletesExistingRuleDefinitionsAndRecordsAuditTrail() {
        RuleManagementService service = service();

        service.delete("global", "HIGH_VALUE_AMOUNT", "admin");

        assertThrows(
                RuleDefinitionNotFoundException.class,
                () -> service.get("global", "HIGH_VALUE_AMOUNT"));
        assertEquals(
                1,
                service.listAuditTrails("global", null, "HIGH_VALUE_AMOUNT", RuleAuditAction.DELETED).size());
    }

    @Test
    void managesRuleCategoriesForConfigurationScreens() {
        RuleManagementService service = service();

        List<RuleCategory> seeded = service.listCategories("GLOBAL", "velo", null, null);
        assertEquals(1, seeded.size());
        assertEquals("Velocity", seeded.getFirst().name());

        RuleCategory created = service.createCategory(new RuleCategoryRequest(
                "Tenant-A",
                "Sanctions",
                "Sanctions screening rules.",
                "analyst"));

        assertEquals("tenant-a", created.tenantId());
        assertEquals("Sanctions", created.name());

        RuleCategory replaced = service.replaceCategory(
                created.id(),
                new RuleCategoryRequest("tenant-a", "Sanctions Review", "Updated text.", "approver"));

        assertEquals(created.id(), replaced.id());
        assertEquals("Sanctions Review", replaced.name());
        assertEquals(created.createdAt(), replaced.createdAt());
        assertEquals("approver", replaced.updatedBy());

        service.deleteCategory(created.id());

        assertThrows(RuleManagementResourceNotFoundException.class, () -> service.getCategory(created.id()));
    }

    @Test
    void managesRuleEscalationUsersForWorkflowConfiguration() {
        RuleManagementService service = service();

        RuleEscalationUser created = service.createEscalationUser(new RuleEscalationUserRequest(
                "global",
                null,
                "high_value_amount",
                "Fraud.Team@Example.com",
                "user-001",
                "Fraud Team",
                "analyst"));

        assertEquals("global", created.tenantId());
        assertEquals("HIGH_VALUE_AMOUNT", created.ruleCode());
        assertEquals("fraud.team@example.com", created.email());
        assertEquals(service.get("global", "HIGH_VALUE_AMOUNT").ruleId(), created.ruleId());

        List<RuleEscalationUser> filtered = service.listEscalationUsers(
                "global",
                "fraud",
                null,
                null,
                null,
                "HIGH_VALUE_AMOUNT",
                null);

        assertEquals(1, filtered.size());

        RuleEscalationUser replaced = service.replaceEscalationUser(
                created.id(),
                new RuleEscalationUserRequest(
                        "global",
                        created.ruleId(),
                        "HIGH_VALUE_AMOUNT",
                        "Level2@Example.com",
                        "user-002",
                        "Level 2 Fraud Team",
                        "approver"));

        assertEquals("level2@example.com", replaced.email());
        assertEquals("Level 2 Fraud Team", replaced.name());

        service.deleteEscalationUser(created.id());

        assertThrows(RuleManagementResourceNotFoundException.class, () -> service.getEscalationUser(created.id()));
    }

    @Test
    void managesRuleConditionDefinitionsForRuleBuilders() {
        RuleManagementService service = service();

        RuleConditionDefinition created = service.createConditionDefinition(new RuleConditionDefinitionRequest(
                "global",
                null,
                "high_value_amount",
                "GREATER_THAN",
                "10000",
                "group-1",
                "AND",
                "source-amount",
                null,
                "transaction_amount",
                "configured_limit",
                "Number",
                "Number",
                "analyst"));

        assertEquals("global", created.tenantId());
        assertEquals("HIGH_VALUE_AMOUNT", created.ruleCode());
        assertEquals("TRANSACTION_AMOUNT", created.sourceDataPointCode());
        assertEquals("CONFIGURED_LIMIT", created.destinationDataPointCode());
        assertEquals(service.get("global", "HIGH_VALUE_AMOUNT").ruleId(), created.ruleId());

        List<RuleConditionDefinition> filtered = service.listConditionDefinitions(
                "global",
                "amount",
                null,
                "high_value_amount",
                null,
                null,
                null);

        assertEquals(1, filtered.size());

        RuleConditionDefinition replaced = service.replaceConditionDefinition(
                created.id(),
                new RuleConditionDefinitionRequest(
                        "global",
                        created.ruleId(),
                        "HIGH_VALUE_AMOUNT",
                        "GREATER_THAN_OR_EQUAL",
                        "12000",
                        "group-1",
                        "AND",
                        "source-amount",
                        null,
                        "transaction_amount",
                        "configured_limit",
                        "Number",
                        "Number",
                        "approver"));

        assertEquals("GREATER_THAN_OR_EQUAL", replaced.operator());
        assertEquals("12000", replaced.datapointValue());

        service.deleteConditionDefinition(created.id());

        assertThrows(RuleManagementResourceNotFoundException.class, () -> service.getConditionDefinition(created.id()));
    }

    @Test
    void submitsAndApprovesRulesForMakerCheckerWorkflow() {
        RuleManagementService service = service();
        service.updateStatus(
                "HIGH_VALUE_AMOUNT",
                new RuleStatusUpdateRequest("global", FraudRuleStatus.INACTIVE, "analyst"));

        RuleApproval approval = service.submitForApproval(
                "HIGH_VALUE_AMOUNT",
                new RuleApprovalRequest("global", RuleApprovalAction.ACTIVATE, "maker", "Reactivate threshold rule."));

        assertEquals(RuleApprovalStatus.PENDING, approval.status());
        assertEquals(1, service.listPendingApprovals("global").size());

        RuleApproval approved = service.approve(
                approval.approvalId(),
                new RuleApprovalDecisionRequest("checker", "Approved for monitoring."));

        assertEquals(RuleApprovalStatus.APPROVED, approved.status());
        assertEquals("checker", approved.decidedBy());
        assertEquals(FraudRuleStatus.ACTIVE, service.get("global", "HIGH_VALUE_AMOUNT").status());
        assertTrue(service.listPendingApprovals("global").isEmpty());

        InvalidRuleDefinitionException exception = assertThrows(
                InvalidRuleDefinitionException.class,
                () -> service.approve(approval.approvalId(), new RuleApprovalDecisionRequest("checker", null)));
        assertEquals(List.of("approval must be pending before it can be decided"), exception.errors());
    }

    @Test
    void createsUpdatesAndDeletesManualAuditTrailEntries() {
        RuleManagementService service = service();

        RuleAuditTrail created = service.createAuditTrail(new RuleAuditTrailRequest(
                "global",
                null,
                "HIGH_VALUE_AMOUNT",
                RuleAuditAction.UPDATED,
                FraudRuleStatus.DRAFT,
                FraudRuleStatus.ACTIVE,
                "Manual import from governance board.",
                "corr-001",
                "analyst",
                null));

        assertEquals("global", created.tenantId());
        assertEquals("HIGH_VALUE_AMOUNT", created.ruleCode());
        assertEquals("analyst", created.createdBy());
        assertEquals("analyst", created.updatedBy());

        RuleAuditTrail replaced = service.replaceAuditTrail(
                created.id(),
                new RuleAuditTrailRequest(
                        "global",
                        null,
                        "HIGH_VALUE_AMOUNT",
                        RuleAuditAction.REJECTED,
                        FraudRuleStatus.DRAFT,
                        FraudRuleStatus.DRAFT,
                        "Rejected during review.",
                        "corr-002",
                        "analyst",
                        "checker"));

        assertEquals(created.id(), replaced.id());
        assertEquals(RuleAuditAction.REJECTED, replaced.action());
        assertEquals("checker", replaced.updatedBy());
        assertFalse(service.listAuditTrails("global", null, "HIGH_VALUE_AMOUNT", RuleAuditAction.REJECTED).isEmpty());

        service.deleteAuditTrail(created.id());

        assertThrows(RuleManagementResourceNotFoundException.class, () -> service.getAuditTrail(created.id()));
    }

    @Test
    void rejectsInvalidRuleDefinitions() {
        RuleManagementService service = service();

        InvalidRuleDefinitionException exception = assertThrows(
                InvalidRuleDefinitionException.class,
                () -> service.create(new RuleDefinitionRequest(
                        " ",
                        "bad-rule",
                        " ",
                        null,
                        null,
                        null,
                        new BigDecimal("1.01"),
                        " ",
                        Map.of(),
                        " ")));

        assertTrue(exception.errors().contains("tenantId must not be blank"));
        assertTrue(exception.errors().contains("ruleName must not be blank"));
        assertTrue(exception.errors().contains("category must not be null"));
        assertTrue(exception.errors().contains("status must not be null"));
        assertTrue(exception.errors().contains("severity must not be null"));
        assertTrue(exception.errors().contains("contribution must be between 0.00 and 1.00"));
        assertTrue(exception.errors().contains("explanation must not be blank"));
        assertTrue(exception.errors().contains("updatedBy must not be blank"));
    }

    @Test
    void reportsMissingRuleDefinitions() {
        RuleManagementService service = service();

        assertThrows(
                RuleDefinitionNotFoundException.class,
                () -> service.get("tenant-a", "MISSING_RULE"));
    }

    @Test
    void rejectsUnknownFilters() {
        RuleManagementService service = service();

        InvalidRuleDefinitionException statusException = assertThrows(
                InvalidRuleDefinitionException.class,
                () -> service.list("global", "archived", null));
        assertEquals(List.of("status must be one of DRAFT, ACTIVE, or INACTIVE"), statusException.errors());

        InvalidRuleDefinitionException categoryException = assertThrows(
                InvalidRuleDefinitionException.class,
                () -> service.list("global", null, "pattern"));
        assertEquals(
                List.of("category must be one of AMOUNT, VELOCITY, GEOGRAPHY, SIGNAL, or CHANNEL"),
                categoryException.errors());
    }

    private RuleManagementService service() {
        return new RuleManagementService(
                new InMemoryRuleDefinitionRepository(new BaselineRuleDefinitions(), FIXED_CLOCK),
                new InMemoryRuleConfigurationRepository(FIXED_CLOCK),
                FIXED_CLOCK);
    }

    private RuleDefinitionRequest customRule(String tenantId, String ruleCode) {
        return new RuleDefinitionRequest(
                tenantId,
                ruleCode,
                " Custom rule ",
                FraudRuleCategory.SIGNAL,
                FraudRuleStatus.ACTIVE,
                RiskBand.HIGH,
                new BigDecimal("0.27"),
                " Custom rule explanation. ",
                Map.of(" source ", " manual "),
                " analyst ");
    }

    private List<String> ruleCodes(List<FraudRuleDefinition> rules) {
        return rules.stream().map(FraudRuleDefinition::ruleCode).toList();
    }
}
