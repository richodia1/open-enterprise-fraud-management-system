package org.openefms.rulemanagement.repository;

import java.util.List;
import java.util.Optional;
import org.openefms.rulemanagement.entity.RuleApproval;
import org.openefms.rulemanagement.entity.RuleAuditTrail;
import org.openefms.rulemanagement.entity.RuleCategory;
import org.openefms.rulemanagement.entity.RuleConditionDefinition;
import org.openefms.rulemanagement.entity.RuleEscalationUser;

public interface RuleConfigurationRepository {
    public List<RuleCategory> findCategories();

    public Optional<RuleCategory> findCategory(String id);

    public RuleCategory saveCategory(RuleCategory category);

    public void deleteCategory(String id);

    public List<RuleEscalationUser> findEscalationUsers();

    public Optional<RuleEscalationUser> findEscalationUser(String id);

    public RuleEscalationUser saveEscalationUser(RuleEscalationUser user);

    public void deleteEscalationUser(String id);

    public List<RuleConditionDefinition> findConditionDefinitions();

    public Optional<RuleConditionDefinition> findConditionDefinition(String id);

    public RuleConditionDefinition saveConditionDefinition(RuleConditionDefinition definition);

    public void deleteConditionDefinition(String id);

    public List<RuleAuditTrail> findAuditTrails();

    public Optional<RuleAuditTrail> findAuditTrail(String id);

    public RuleAuditTrail saveAuditTrail(RuleAuditTrail auditTrail);

    public void deleteAuditTrail(String id);

    public List<RuleApproval> findApprovals();

    public Optional<RuleApproval> findApproval(String id);

    public RuleApproval saveApproval(RuleApproval approval);
}
