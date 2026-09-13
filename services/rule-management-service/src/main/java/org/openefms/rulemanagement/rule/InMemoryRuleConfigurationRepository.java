package org.openefms.rulemanagement.rule;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
class InMemoryRuleConfigurationRepository implements RuleConfigurationRepository {
    private final ConcurrentMap<String, RuleCategory> categories = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RuleEscalationUser> escalationUsers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RuleConditionDefinition> conditionDefinitions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RuleAuditTrail> auditTrails = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RuleApproval> approvals = new ConcurrentHashMap<>();

    InMemoryRuleConfigurationRepository(Clock clock) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        seedCategory("Amount", "Amount and value threshold rules.", now);
        seedCategory("Velocity", "Transaction frequency and repeated activity rules.", now);
        seedCategory("Geography", "Country, corridor, and location anomaly rules.", now);
        seedCategory("Signal", "Risk hints, watchlist signals, and model features.", now);
        seedCategory("Channel", "Channel and source-system behaviour rules.", now);
    }

    @Override
    public List<RuleCategory> findCategories() {
        return categories.values().stream()
                .sorted(Comparator
                        .comparing(RuleCategory::tenantId)
                        .thenComparing(RuleCategory::name)
                        .thenComparing(RuleCategory::id))
                .toList();
    }

    @Override
    public Optional<RuleCategory> findCategory(String id) {
        return Optional.ofNullable(categories.get(normaliseId(id)));
    }

    @Override
    public RuleCategory saveCategory(RuleCategory category) {
        categories.put(normaliseId(category.id()), category);
        return category;
    }

    @Override
    public void deleteCategory(String id) {
        categories.remove(normaliseId(id));
    }

    @Override
    public List<RuleEscalationUser> findEscalationUsers() {
        return escalationUsers.values().stream()
                .sorted(Comparator
                        .comparing(RuleEscalationUser::tenantId)
                        .thenComparing(RuleEscalationUser::ruleCode, Comparator.nullsLast(String::compareTo))
                        .thenComparing(RuleEscalationUser::email)
                        .thenComparing(RuleEscalationUser::id))
                .toList();
    }

    @Override
    public Optional<RuleEscalationUser> findEscalationUser(String id) {
        return Optional.ofNullable(escalationUsers.get(normaliseId(id)));
    }

    @Override
    public RuleEscalationUser saveEscalationUser(RuleEscalationUser user) {
        escalationUsers.put(normaliseId(user.id()), user);
        return user;
    }

    @Override
    public void deleteEscalationUser(String id) {
        escalationUsers.remove(normaliseId(id));
    }

    @Override
    public List<RuleConditionDefinition> findConditionDefinitions() {
        return conditionDefinitions.values().stream()
                .sorted(Comparator
                        .comparing(RuleConditionDefinition::tenantId)
                        .thenComparing(RuleConditionDefinition::ruleCode, Comparator.nullsLast(String::compareTo))
                        .thenComparing(RuleConditionDefinition::sourceDataPointCode, Comparator.nullsLast(String::compareTo))
                        .thenComparing(RuleConditionDefinition::id))
                .toList();
    }

    @Override
    public Optional<RuleConditionDefinition> findConditionDefinition(String id) {
        return Optional.ofNullable(conditionDefinitions.get(normaliseId(id)));
    }

    @Override
    public RuleConditionDefinition saveConditionDefinition(RuleConditionDefinition definition) {
        conditionDefinitions.put(normaliseId(definition.id()), definition);
        return definition;
    }

    @Override
    public void deleteConditionDefinition(String id) {
        conditionDefinitions.remove(normaliseId(id));
    }

    @Override
    public List<RuleAuditTrail> findAuditTrails() {
        return auditTrails.values().stream()
                .sorted(Comparator
                        .comparing(RuleAuditTrail::createdAt).reversed()
                        .thenComparing(RuleAuditTrail::id))
                .toList();
    }

    @Override
    public Optional<RuleAuditTrail> findAuditTrail(String id) {
        return Optional.ofNullable(auditTrails.get(normaliseId(id)));
    }

    @Override
    public RuleAuditTrail saveAuditTrail(RuleAuditTrail auditTrail) {
        auditTrails.put(normaliseId(auditTrail.id()), auditTrail);
        return auditTrail;
    }

    @Override
    public void deleteAuditTrail(String id) {
        auditTrails.remove(normaliseId(id));
    }

    @Override
    public List<RuleApproval> findApprovals() {
        return approvals.values().stream()
                .sorted(Comparator
                        .comparing(RuleApproval::requestedAt).reversed()
                        .thenComparing(RuleApproval::approvalId))
                .toList();
    }

    @Override
    public Optional<RuleApproval> findApproval(String id) {
        return Optional.ofNullable(approvals.get(normaliseId(id)));
    }

    @Override
    public RuleApproval saveApproval(RuleApproval approval) {
        approvals.put(normaliseId(approval.approvalId()), approval);
        return approval;
    }

    private void seedCategory(String name, String description, OffsetDateTime now) {
        String id = UUID.nameUUIDFromBytes(
                ("global:rule-category:" + name.toLowerCase(Locale.ROOT)).getBytes(StandardCharsets.UTF_8))
                .toString();
        saveCategory(new RuleCategory(
                id,
                BaselineRuleDefinitions.GLOBAL_TENANT_ID,
                name,
                description,
                now,
                now,
                "system"));
    }

    private String normaliseId(String id) {
        return id == null ? "" : id.trim();
    }
}
