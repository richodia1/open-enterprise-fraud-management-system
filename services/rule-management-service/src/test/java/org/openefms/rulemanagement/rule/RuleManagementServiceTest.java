package org.openefms.rulemanagement.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
