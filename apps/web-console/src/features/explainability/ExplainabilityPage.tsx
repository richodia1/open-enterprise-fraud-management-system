import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Alert, Box, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { BrainCircuit } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime, formatEnum } from '../../lib/formatters'
import type {
  DecisionMode,
  DecisionOutcome,
  FraudDecisionResponse,
  RecommendedTransactionAction,
  RiskBand,
  TransactionFraudFeedbackResponse,
} from '../../types/api'

type ExplanationForm = {
  decisionMode: DecisionMode
  outcome: DecisionOutcome
  riskBand: RiskBand
  riskReason: string
  riskScore: string
  ruleCode: string
  ruleName: string
  tenantId: string
}

const decisionModes: DecisionMode[] = ['RULES', 'ML', 'HYBRID']
const decisionOutcomes: DecisionOutcome[] = ['APPROVE', 'REVIEW', 'BLOCK', 'ESCALATE']
const riskBands: RiskBand[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

const initialForm: ExplanationForm = {
  decisionMode: 'RULES',
  outcome: 'BLOCK',
  riskBand: 'HIGH',
  riskReason: 'High-value cross-border transfer matched risk indicators.',
  riskScore: '0.82',
  ruleCode: 'HIGH_VALUE_CROSS_BORDER',
  ruleName: 'High value cross-border transfer',
  tenantId: 'mvp-smoke-tenant',
}

export function ExplainabilityPage() {
  const [form, setForm] = useState<ExplanationForm>(initialForm)
  const feedbackMutation = useMutation({
    mutationFn: () =>
      apiRequest<TransactionFraudFeedbackResponse>('/api/v1/explanations/fraud-decisions/feedback', {
        body: toDecision(form),
        method: 'POST',
      }),
  })

  const updateField = <K extends keyof ExplanationForm>(field: K, value: ExplanationForm[K]) => {
    setForm((current) => ({ ...current, [field]: value }))
  }
  const feedback = feedbackMutation.data

  return (
    <>
      <PageHeader
        eyebrow="Operations"
        summary="Generate investigator-facing explanations and the combined fraud feedback payload used by transaction core adapters."
        title="Explainability"
      />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', xl: '410px minmax(0, 1fr)' },
        }}
      >
        <SectionPanel eyebrow="Decision Support" title="Generate Feedback">
          {feedbackMutation.isError ? <ErrorState error={feedbackMutation.error} /> : null}
          {feedbackMutation.isSuccess && feedback ? (
            <Alert severity="success">
              Feedback ready for transaction {feedback.transactionId}.
            </Alert>
          ) : null}
          <Stack spacing={1.5}>
            <TextField
              label="Tenant"
              onChange={(event) => updateField('tenantId', event.target.value)}
              size="small"
              value={form.tenantId}
            />
            <TextField
              label="Decision Mode"
              onChange={(event) => updateField('decisionMode', event.target.value as DecisionMode)}
              select
              size="small"
              value={form.decisionMode}
            >
              {decisionModes.map((option) => (
                <MenuItem key={option} value={option}>
                  {formatEnum(option)}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Outcome"
              onChange={(event) => updateField('outcome', event.target.value as DecisionOutcome)}
              select
              size="small"
              value={form.outcome}
            >
              {decisionOutcomes.map((option) => (
                <MenuItem key={option} value={option}>
                  {formatEnum(option)}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Risk Band"
              onChange={(event) => updateField('riskBand', event.target.value as RiskBand)}
              select
              size="small"
              value={form.riskBand}
            >
              {riskBands.map((option) => (
                <MenuItem key={option} value={option}>
                  {formatEnum(option)}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Risk Score"
              onChange={(event) => updateField('riskScore', event.target.value)}
              slotProps={{ htmlInput: { max: 1, min: 0, step: '0.01' } }}
              size="small"
              type="number"
              value={form.riskScore}
            />
            <TextField
              label="Risk Reason"
              minRows={2}
              multiline
              onChange={(event) => updateField('riskReason', event.target.value)}
              value={form.riskReason}
            />
            <TextField
              label="Rule Code"
              onChange={(event) => updateField('ruleCode', event.target.value)}
              size="small"
              value={form.ruleCode}
            />
            <TextField
              label="Rule Name"
              onChange={(event) => updateField('ruleName', event.target.value)}
              size="small"
              value={form.ruleName}
            />
            <Button
              disabled={feedbackMutation.isPending || !form.tenantId || !form.riskScore}
              onClick={() => feedbackMutation.mutate()}
              startIcon={<BrainCircuit size={18} />}
              variant="contained"
            >
              Generate Feedback
            </Button>
          </Stack>
        </SectionPanel>

        <Stack spacing={2}>
          <SectionPanel eyebrow="Feedback" title="Core Banking Response">
            {feedback ? (
              <Stack spacing={1.25}>
                <Box
                  sx={{
                    display: 'grid',
                    gap: 1.5,
                    gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0, 1fr))' },
                  }}
                >
                  <Fact label="Outcome" value={<StatusChip value={feedback.outcome} />} />
                  <Fact label="Recommended Action" value={formatEnum(feedback.recommendedAction)} />
                  <Fact label="Risk Band" value={<StatusChip value={feedback.riskScore.band} />} />
                  <Fact label="Risk Score" value={String(feedback.riskScore.value)} />
                  <Fact label="Decision" value={feedback.decisionId} />
                  <Fact label="Explanation" value={feedback.explanationId} />
                  <Fact label="Decided" value={formatDateTime(feedback.decidedAt)} />
                  <Fact label="Explained" value={formatDateTime(feedback.explainedAt)} />
                </Box>
                <Typography color="text.secondary" variant="body2">
                  {feedback.explanationSummary}
                </Typography>
              </Stack>
            ) : (
              <Typography color="text.secondary" variant="body2">
                Generate feedback to preview the explanation sent with the fraud decision.
              </Typography>
            )}
          </SectionPanel>

          <SectionPanel eyebrow="Evidence" title="Decision Evidence">
            <RecordTable
              columns={[
                { header: 'Type', render: (row) => <StatusChip value={row.evidenceType} />, width: 150 },
                { header: 'Title', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.title}</Typography> },
                { header: 'Contribution', render: (row) => row.contribution ?? '0', width: 130 },
                { header: 'Description', render: (row) => row.description || 'No description' },
              ]}
              emptyLabel="No evidence generated yet."
              rows={(feedback?.evidence ?? []).map((row, index) => ({ ...row, id: `${row.evidenceType}-${index}` }))}
            />
          </SectionPanel>
        </Stack>
      </Box>
    </>
  )
}

function toDecision(form: ExplanationForm): FraudDecisionResponse {
  const outcome = form.outcome
  const riskScore = Number.parseFloat(form.riskScore)
  return {
    attributes: {
      explanationRequestedBy: 'web-console',
      sourceScreen: 'explainability',
    },
    decidedAt: new Date().toISOString(),
    decisionId: randomUuid(),
    mode: form.decisionMode,
    modelSignals: form.decisionMode === 'RULES'
      ? []
      : [
          {
            contribution: Number.isFinite(riskScore) ? riskScore : 0,
            featureValues: {
              amountBand: 'HIGH',
              countryMismatch: 'true',
            },
            modelName: 'oefms-xgboost-baseline',
            modelVersion: 'v0.1',
            score: Number.isFinite(riskScore) ? riskScore : 0,
          },
        ],
    outcome,
    recommendedAction: recommendedActionFor(outcome),
    riskScore: {
      band: form.riskBand,
      reason: form.riskReason,
      value: Number.isFinite(riskScore) ? riskScore : 0,
    },
    ruleHits: form.ruleCode
      ? [
          {
            attributes: {
              evidenceSource: 'web-console',
            },
            contribution: Number.isFinite(riskScore) ? riskScore : 0,
            explanation: form.riskReason,
            ruleCode: form.ruleCode,
            ruleName: form.ruleName,
            severity: form.riskBand,
          },
        ]
      : [],
    tenantId: form.tenantId,
    transactionId: randomUuid(),
  }
}

function recommendedActionFor(outcome: DecisionOutcome): RecommendedTransactionAction {
  if (outcome === 'APPROVE') {
    return 'PROCEED_TO_COMPLETION'
  }
  if (outcome === 'REVIEW') {
    return 'HOLD_FOR_MANUAL_REVIEW'
  }
  if (outcome === 'BLOCK') {
    return 'REJECT_TRANSACTION'
  }
  return 'ESCALATE_FOR_INVESTIGATION'
}

function randomUuid() {
  if (globalThis.crypto && 'randomUUID' in globalThis.crypto) {
    return globalThis.crypto.randomUUID()
  }
  return `00000000-0000-4000-8000-${Math.random().toString(16).slice(2, 14).padEnd(12, '0')}`
}

function Fact({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <Box sx={{ minWidth: 0 }}>
      <Typography color="text.secondary" sx={{ fontSize: 12, fontWeight: 800 }} variant="body2">
        {label}
      </Typography>
      {typeof value === 'string' ? (
        <Typography sx={{ overflowWrap: 'anywhere' }} variant="body2">
          {value}
        </Typography>
      ) : (
        value
      )}
    </Box>
  )
}
