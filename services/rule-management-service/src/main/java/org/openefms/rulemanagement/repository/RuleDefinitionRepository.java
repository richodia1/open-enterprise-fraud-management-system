package org.openefms.rulemanagement.repository;

import java.util.List;
import java.util.Optional;
import org.openefms.common.models.rule.FraudRuleDefinition;

public interface RuleDefinitionRepository {
    List<FraudRuleDefinition> findAll();

    Optional<FraudRuleDefinition> findByTenantIdAndRuleCode(String tenantId, String ruleCode);

    FraudRuleDefinition save(FraudRuleDefinition definition);

    void delete(String tenantId, String ruleCode);
}
