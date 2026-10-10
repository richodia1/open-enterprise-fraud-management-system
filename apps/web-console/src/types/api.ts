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

export type DecisionMode = 'RULES' | 'ML' | 'HYBRID'

export type DecisionOutcome = 'APPROVE' | 'REVIEW' | 'BLOCK' | 'ESCALATE'

export type RecommendedTransactionAction =
  | 'PROCEED_TO_COMPLETION'
  | 'HOLD_FOR_MANUAL_REVIEW'
  | 'REJECT_TRANSACTION'
  | 'ESCALATE_FOR_INVESTIGATION'

export type RiskBand = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type RiskScore = {
  band: RiskBand
  reason?: string
  value: number | string
}

export type RuleHit = {
  attributes: Record<string, string>
  contribution: number | string
  explanation?: string
  ruleCode: string
  ruleName?: string
  severity: RiskBand
}

export type ModelSignal = {
  contribution?: number | string
  featureValues: Record<string, string>
  modelName: string
  modelVersion?: string
  score: number | string
}

export type FraudDecisionResponse = {
  attributes: Record<string, string>
  decidedAt: string
  decisionId: string
  mode: DecisionMode
  modelSignals: ModelSignal[]
  outcome: DecisionOutcome
  recommendedAction: RecommendedTransactionAction
  riskScore: RiskScore
  ruleHits: RuleHit[]
  tenantId: string
  transactionId: string
}

export type FraudDecisionRecord = {
  attributes: Record<string, string>
  causationId?: string
  correlationId?: string
  decidedAt: string
  decisionId: string
  eventId?: string
  eventType?: string
  mode: DecisionMode
  modelSignalCount: number
  modelSignalSummary: string
  outcome: DecisionOutcome
  recommendedAction: RecommendedTransactionAction
  riskBand: RiskBand
  riskReason: string
  riskScoreValue: number | string
  ruleEvidenceSummary: string
  ruleHitCount: number
  tenantId: string
  traceId?: string
  transactionId: string
}

export type EvidenceItem = {
  attributes: Record<string, string>
  contribution?: number | string
  description?: string
  evidenceType: string
  title: string
}

export type ExplanationResponse = {
  attributes: Record<string, string>
  decisionId: string
  evidence: EvidenceItem[]
  explainedAt: string
  explanationId: string
  summary: string
  tenantId: string
  transactionId: string
}

export type TransactionFraudFeedbackResponse = {
  attributes: Record<string, string>
  decidedAt: string
  decisionId: string
  evidence: EvidenceItem[]
  explainedAt: string
  explanationId: string
  explanationSummary: string
  mode: DecisionMode
  outcome: DecisionOutcome
  recommendedAction: RecommendedTransactionAction
  riskScore: RiskScore
  tenantId: string
  transactionId: string
}

export type ManagementQuestionIntent =
  | 'LATEST_FRAUD_CASE'
  | 'FRAUD_CASE_COUNT_BY_CHANNEL'
  | 'FRAUD_CASE_COUNT_BY_CAUSE'
  | 'FRAUD_CASE_EXPLANATION'
  | 'FRAUD_TREND_SUMMARY'
  | 'UNKNOWN'

export type ManagementQuestionAnswerStatus =
  | 'ANSWERED_FROM_CONTEXT'
  | 'NEEDS_STRUCTURED_DATA'
  | 'NEEDS_CASE_CONTEXT'
  | 'UNKNOWN'

export type ManagementQuestionRequest = {
  audience?: string
  context?: Record<string, string>
  filters?: Record<string, string>
  periodEnd?: string
  periodStart?: string
  question: string
  tenantId: string
}

export type ManagementQuestionQueryPlan = {
  endpoint: string
  parameters: Record<string, string>
  purpose: string
  sourceService: string
}

export type ManagementQuestionAnswerResponse = {
  answer: string
  answeredAt: string
  answerId: string
  attributes: Record<string, string>
  intent: ManagementQuestionIntent
  liveDataRequired: boolean
  queryPlan: ManagementQuestionQueryPlan[]
  question: string
  status: ManagementQuestionAnswerStatus
  supportingEvidence: EvidenceItem[]
  tenantId: string
}

export type MlRuntimeState = 'DISABLED' | 'CONTRACT_ONLY' | 'READY'

export type MlRuntimeStatus = {
  attributes: Record<string, string>
  checkedAt: string
  modelName: string
  provider: string
  reason: string
  state: MlRuntimeState
  supportedModes: DecisionMode[]
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
