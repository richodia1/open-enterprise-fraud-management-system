package org.openefms.rulemanagement.rule;

import java.util.List;
import java.util.Optional;
import org.openefms.common.models.rule.FraudRuleDefinition;

interface RuleDefinitionRepository {
    List<FraudRuleDefinition> findAll();

    Optional<FraudRuleDefinition> findByTenantIdAndRuleCode(String tenantId, String ruleCode);

    FraudRuleDefinition save(FraudRuleDefinition definition);

    void delete(String tenantId, String ruleCode);
}
