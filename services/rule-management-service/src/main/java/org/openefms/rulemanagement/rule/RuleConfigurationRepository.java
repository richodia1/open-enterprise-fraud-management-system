package org.openefms.rulemanagement.rule;

import java.util.List;
import java.util.Optional;

interface RuleConfigurationRepository {
    List<RuleCategory> findCategories();

    Optional<RuleCategory> findCategory(String id);

    RuleCategory saveCategory(RuleCategory category);

    void deleteCategory(String id);

    List<RuleEscalationUser> findEscalationUsers();

    Optional<RuleEscalationUser> findEscalationUser(String id);

    RuleEscalationUser saveEscalationUser(RuleEscalationUser user);

    void deleteEscalationUser(String id);

    List<RuleConditionDefinition> findConditionDefinitions();

    Optional<RuleConditionDefinition> findConditionDefinition(String id);

    RuleConditionDefinition saveConditionDefinition(RuleConditionDefinition definition);

    void deleteConditionDefinition(String id);

    List<RuleAuditTrail> findAuditTrails();

    Optional<RuleAuditTrail> findAuditTrail(String id);

    RuleAuditTrail saveAuditTrail(RuleAuditTrail auditTrail);

    void deleteAuditTrail(String id);

    List<RuleApproval> findApprovals();

    Optional<RuleApproval> findApproval(String id);

    RuleApproval saveApproval(RuleApproval approval);
}
