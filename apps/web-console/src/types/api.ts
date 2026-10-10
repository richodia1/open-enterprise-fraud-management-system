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
  assignedAt?: string
  assigneeId?: string
  attributes?: Record<string, string>
  caseId: string
  closedAt?: string
  createdAt: string
  decisionId: string
  priority: string
  status: string
  summary: string
  tenantId: string
  title: string
  transactionId: string
  updatedAt: string
}

export type CaseActionRecord = {
  actionType: string
  actorId: string
  assigneeId?: string
  attributes?: Record<string, string>
  caseActionId: string
  caseId: string
  fromStatus?: string
  note: string
  occurredAt: string
  tenantId: string
  toStatus?: string
}

export type CaseActionResponse = {
  action: CaseActionRecord
  caseFile: CaseRecord
}

export type CaseAssignmentPurpose = 'ASSIGNMENT' | 'ESCALATION'

export type CaseAssignableUser = {
  active: boolean
  attributes: Record<string, string>
  code: string
  displayName: string
  email?: string
  eligiblePurposes: CaseAssignmentPurpose[]
  id: string
  roles: string[]
  tenantId: string
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

export type TransactionType =
  | 'ACCOUNT_OPENING'
  | 'BILL_PAYMENT'
  | 'CARD_PAYMENT'
  | 'CASH_DEPOSIT'
  | 'CASH_WITHDRAWAL'
  | 'LOAN_DISBURSEMENT'
  | 'REVERSAL'
  | 'TRANSFER'
  | 'OTHER'

export type TransactionChannel =
  | 'API'
  | 'ATM'
  | 'BRANCH'
  | 'CARD'
  | 'CORE_BANKING'
  | 'INTERNET_BANKING'
  | 'MOBILE'
  | 'PAYMENT_SWITCH'
  | 'POS'
  | 'OTHER'

export type TransactionDirection = 'INBOUND' | 'OUTBOUND' | 'INTERNAL'

export type TransactionParty = {
  accountNumberHash?: string
  accountReference?: string
  attributes?: Record<string, string>
  countryCode?: string
  customerReference?: string
  institutionCode?: string
  partyReference?: string
}

export type TransactionIngestionRequest = {
  amount: {
    currency: string
    value: number
  }
  attributes?: Record<string, string>
  beneficiary?: TransactionParty
  channel: TransactionChannel
  direction: TransactionDirection
  externalReference?: string
  initiatedAt: string
  originator?: TransactionParty
  tenantId: string
  transactionId: string
  transactionType: TransactionType
}

export type TransactionIngestionResponse = {
  acceptedAt: string
  attributes: Record<string, string>
  correlationId: string
  eventType: string
  externalReference?: string
  status: string
  tenantId: string
  transactionId: string
}

export type TransactionReceiptRecord = {
  acceptedAt: string
  amount: number | string
  attributes: Record<string, string>
  channel: TransactionChannel
  correlationId: string
  currency: string
  direction: TransactionDirection
  eventId: string
  eventType: string
  externalReference?: string
  initiatedAt: string
  publishedAt?: string
  status: string
  tenantId: string
  traceId?: string
  transactionId: string
  transactionType: TransactionType
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
