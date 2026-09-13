export type ManagedResourceTypeDescriptor = {
  displayName: string
  legacyResource: string
  notes: string
  resourceType: string
}

export type ManagedResourceRecord = {
  active: boolean
  attributes: Record<string, string>
  code: string
  createdAt: string
  description: string
  id: string
  name: string
  resourceType: string
  tenantId: string
  updatedAt: string
  updatedBy: string
}

export type ManagedResourceRequest = {
  active: boolean
  attributes: Record<string, string>
  code: string
  description: string
  name: string
  tenantId: string
  updatedBy: string
}

export type AlertRecord = {
  alertId: string
  caseId?: string
  createdAt: string
  decisionId: string
  priority: string
  riskBand: string
  status: string
  summary: string
  tenantId: string
  title: string
  transactionId: string
}

export type CaseRecord = {
  alertId: string
  caseId: string
  createdAt: string
  decisionId: string
  priority: string
  status: string
  summary: string
  tenantId: string
  title: string
  transactionId: string
}

export type AuditLogRecord = {
  action: string
  actorId: string
  auditEventId: string
  correlationId?: string
  occurredAt: string
  resourceId: string
  resourceType: string
  summary: string
  tenantId: string
}

export type RuleDefinition = {
  active?: boolean
  category?: string
  description?: string
  name?: string
  riskScore?: number
  ruleCode: string
  status?: string
  tenantId: string
}

export type TenantIdentityProvider = {
  displayName: string
  enabled: boolean
  id: string
  providerCode: string
  providerType: string
  status: string
  tenantId: string
  updatedAt: string
  updatedBy: string
}

export type HealthResponse = {
  components?: Record<string, unknown>
  status: string
}
