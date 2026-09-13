package org.openefms.rulemanagement.rule;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.openefms.common.models.rule.FraudRuleDefinition;
import org.springframework.stereotype.Repository;

@Repository
class InMemoryRuleDefinitionRepository implements RuleDefinitionRepository {
    private final ConcurrentMap<RuleKey, FraudRuleDefinition> definitions = new ConcurrentHashMap<>();

    InMemoryRuleDefinitionRepository(BaselineRuleDefinitions baselineRuleDefinitions, Clock clock) {
        baselineRuleDefinitions.load(clock).forEach(this::save);
    }

    @Override
    public List<FraudRuleDefinition> findAll() {
        return definitions.values().stream()
                .sorted(Comparator
                        .comparing(FraudRuleDefinition::tenantId)
                        .thenComparing(FraudRuleDefinition::ruleCode))
                .toList();
    }

    @Override
    public Optional<FraudRuleDefinition> findByTenantIdAndRuleCode(String tenantId, String ruleCode) {
        return Optional.ofNullable(definitions.get(new RuleKey(tenantId, ruleCode)));
    }

    @Override
    public FraudRuleDefinition save(FraudRuleDefinition definition) {
        definitions.put(new RuleKey(definition.tenantId(), definition.ruleCode()), definition);
        return definition;
    }

    @Override
    public void delete(String tenantId, String ruleCode) {
        definitions.remove(new RuleKey(tenantId, ruleCode));
    }

    private record RuleKey(String tenantId, String ruleCode) {
        RuleKey {
            tenantId = normaliseTenantId(tenantId);
            ruleCode = normaliseRuleCode(ruleCode);
        }

        private static String normaliseTenantId(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        }

        private static String normaliseRuleCode(String value) {
            return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        }
    }
}
