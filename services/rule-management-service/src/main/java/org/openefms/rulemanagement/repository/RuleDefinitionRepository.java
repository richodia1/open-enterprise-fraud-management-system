package org.openefms.rulemanagement.repository;

import java.util.List;
import java.util.Optional;
import org.openefms.common.models.rule.FraudRuleDefinition;

public interface RuleDefinitionRepository {
    public List<FraudRuleDefinition> findAll();

    public Optional<FraudRuleDefinition> findByTenantIdAndRuleCode(String tenantId, String ruleCode);

    public FraudRuleDefinition save(FraudRuleDefinition definition);

    public void delete(String tenantId, String ruleCode);
}
