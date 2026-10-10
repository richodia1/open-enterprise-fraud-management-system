import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Box, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { RefreshCw, ScanSearch } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime, formatEnum } from '../../lib/formatters'
import type {
  FraudDecisionRecord,
  FraudDecisionResponse,
  MlRuntimeStatus,
  TransactionChannel,
  TransactionDirection,
  TransactionIngestionRequest,
  TransactionType,
} from '../../types/api'

type DecisionForm = {
  amount: string
  beneficiaryCountryCode: string
  channel: TransactionChannel
  currency: string
  direction: TransactionDirection
  originatorCountryCode: string
  tenantId: string
  transactionType: TransactionType
}

const transactionTypes: TransactionType[] = [
  'ACCOUNT_OPENING',
  'BILL_PAYMENT',
  'CARD_PAYMENT',
  'CASH_DEPOSIT',
  'CASH_WITHDRAWAL',
  'LOAN_DISBURSEMENT',
  'REVERSAL',
  'TRANSFER',
  'OTHER',
]
const transactionChannels: TransactionChannel[] = [
  'API',
  'ATM',
  'BRANCH',
  'CARD',
  'CORE_BANKING',
  'INTERNET_BANKING',
  'MOBILE',
  'PAYMENT_SWITCH',
  'POS',
  'OTHER',
]
const transactionDirections: TransactionDirection[] = ['INBOUND', 'OUTBOUND', 'INTERNAL']

const initialForm: DecisionForm = {
  amount: '250000',
  beneficiaryCountryCode: 'NG',
  channel: 'MOBILE',
  currency: 'USD',
  direction: 'OUTBOUND',
  originatorCountryCode: 'GB',
  tenantId: 'mvp-smoke-tenant',
  transactionType: 'TRANSFER',
}

export function FraudDecisionsPage() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState<DecisionForm>(initialForm)
  const runtimeQuery = useQuery({
    queryFn: () => apiRequest<MlRuntimeStatus>('/api/v1/fraud/ml/runtime'),
    queryKey: ['ml-runtime-status'],
  })
  const decisionsQuery = useQuery({
    queryFn: () =>
      apiRequest<FraudDecisionRecord[]>('/api/v1/fraud/decisions', {
        searchParams: { tenantId: form.tenantId },
      }),
    queryKey: ['fraud-decisions', form.tenantId],
  })
  const decideMutation = useMutation({
    mutationFn: () =>
      apiRequest<FraudDecisionResponse>('/api/v1/fraud/decisions', {
        body: toRequest(form),
        method: 'POST',
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['fraud-decisions', form.tenantId] })
    },
  })

  const updateField = <K extends keyof DecisionForm>(field: K, value: DecisionForm[K]) => {
    setForm((current) => ({ ...current, [field]: value }))
  }

  const refresh = () => {
    void runtimeQuery.refetch()
    void decisionsQuery.refetch()
  }

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={refresh} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Operations"
        summary="Run sample fraud decisions, inspect risk outcomes, and verify the active rules or ML runtime posture."
        title="Fraud Decisions"
      />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', xl: '410px minmax(0, 1fr)' },
        }}
      >
        <Stack spacing={2}>
          <SectionPanel eyebrow="Runtime" title="ML Decision Mode">
            {runtimeQuery.isLoading ? <LoadingState label="Loading ML runtime" /> : null}
            {runtimeQuery.isError ? <ErrorState error={runtimeQuery.error} /> : null}
            {runtimeQuery.data ? (
              <Stack spacing={1}>
                <Fact label="State" value={<StatusChip value={runtimeQuery.data.state} />} />
                <Fact label="Provider" value={runtimeQuery.data.provider} />
                <Fact label="Model" value={runtimeQuery.data.modelName} />
                <Fact label="Supported Modes" value={runtimeQuery.data.supportedModes.map(formatEnum).join(', ')} />
                <Fact label="Reason" value={runtimeQuery.data.reason} />
                <Fact label="Checked" value={formatDateTime(runtimeQuery.data.checkedAt)} />
              </Stack>
            ) : null}
          </SectionPanel>

          <SectionPanel eyebrow="Decisioning" title="Run Sample Decision">
            {decideMutation.isError ? <ErrorState error={decideMutation.error} /> : null}
            {decideMutation.isSuccess ? (
              <Alert severity={alertSeverity(decideMutation.data.outcome)}>
                Decision {decideMutation.data.outcome} with {decideMutation.data.riskScore.band} risk.
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
                label="Type"
                onChange={(event) => updateField('transactionType', event.target.value as TransactionType)}
                select
                size="small"
                value={form.transactionType}
              >
                {transactionTypes.map((option) => (
                  <MenuItem key={option} value={option}>
                    {formatEnum(option)}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Channel"
                onChange={(event) => updateField('channel', event.target.value as TransactionChannel)}
                select
                size="small"
                value={form.channel}
              >
                {transactionChannels.map((option) => (
                  <MenuItem key={option} value={option}>
                    {formatEnum(option)}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Direction"
                onChange={(event) => updateField('direction', event.target.value as TransactionDirection)}
                select
                size="small"
                value={form.direction}
              >
                {transactionDirections.map((option) => (
                  <MenuItem key={option} value={option}>
                    {formatEnum(option)}
                  </MenuItem>
                ))}
              </TextField>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
                <TextField
                  fullWidth
                  label="Amount"
                  onChange={(event) => updateField('amount', event.target.value)}
                  slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                  size="small"
                  type="number"
                  value={form.amount}
                />
                <TextField
                  label="Currency"
                  onChange={(event) => updateField('currency', event.target.value.toUpperCase())}
                  slotProps={{ htmlInput: { maxLength: 3 } }}
                  size="small"
                  value={form.currency}
                />
              </Stack>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
                <TextField
                  fullWidth
                  label="Originator Country"
                  onChange={(event) => updateField('originatorCountryCode', event.target.value.toUpperCase())}
                  slotProps={{ htmlInput: { maxLength: 2 } }}
                  size="small"
                  value={form.originatorCountryCode}
                />
                <TextField
                  fullWidth
                  label="Beneficiary Country"
                  onChange={(event) => updateField('beneficiaryCountryCode', event.target.value.toUpperCase())}
                  slotProps={{ htmlInput: { maxLength: 2 } }}
                  size="small"
                  value={form.beneficiaryCountryCode}
                />
              </Stack>
              <Button
                disabled={decideMutation.isPending || !form.tenantId || !form.amount || !form.currency}
                onClick={() => decideMutation.mutate()}
                startIcon={<ScanSearch size={18} />}
                variant="contained"
              >
                Run Decision
              </Button>
            </Stack>
          </SectionPanel>
        </Stack>

        <SectionPanel eyebrow="Decision History" title="Tenant Fraud Decisions">
          {decisionsQuery.isLoading ? <LoadingState label="Loading fraud decisions" /> : null}
          {decisionsQuery.isError ? <ErrorState error={decisionsQuery.error} /> : null}
          {!decisionsQuery.isLoading && !decisionsQuery.isError ? (
            <RecordTable
              columns={[
                {
                  header: 'Decision',
                  render: (row) => (
                    <Box sx={{ minWidth: 0 }}>
                      <Typography sx={{ fontWeight: 800, overflowWrap: 'anywhere' }}>{row.decisionId}</Typography>
                      <Typography color="text.secondary" variant="body2">
                        {row.transactionId}
                      </Typography>
                    </Box>
                  ),
                },
                { header: 'Mode', render: (row) => <StatusChip value={row.mode} />, width: 120 },
                { header: 'Outcome', render: (row) => <StatusChip value={row.outcome} />, width: 130 },
                { header: 'Action', render: (row) => formatEnum(row.recommendedAction), width: 210 },
                {
                  header: 'Risk',
                  render: (row) => (
                    <Stack spacing={0.5}>
                      <StatusChip value={row.riskBand} />
                      <Typography color="text.secondary" variant="body2">
                        {row.riskScoreValue}
                      </Typography>
                    </Stack>
                  ),
                  width: 120,
                },
                { header: 'Rules', render: (row) => row.ruleHitCount, width: 90 },
                { header: 'Model', render: (row) => row.modelSignalCount, width: 90 },
                { header: 'Decided', render: (row) => formatDateTime(row.decidedAt), width: 190 },
              ]}
              emptyLabel="No fraud decisions found for this tenant."
              rows={(decisionsQuery.data ?? []).map((row) => ({ ...row, id: row.decisionId }))}
            />
          ) : null}
        </SectionPanel>
      </Box>
    </>
  )
}

function toRequest(form: DecisionForm): TransactionIngestionRequest {
  const amount = Number.parseFloat(form.amount)
  return {
    amount: {
      currency: form.currency,
      value: Number.isFinite(amount) ? amount : 0,
    },
    attributes: {
      decisionSubmittedBy: 'web-console',
      decisionSubmissionMode: 'demo',
    },
    beneficiary: {
      accountReference: 'BENEFICIARY-DEMO-ACCOUNT',
      countryCode: form.beneficiaryCountryCode,
      customerReference: 'BENEFICIARY-DEMO',
      institutionCode: 'BENEFICIARY-BANK',
      partyReference: 'BENEFICIARY-DEMO-PARTY',
    },
    channel: form.channel,
    direction: form.direction,
    externalReference: `DECISION-${Date.now()}`,
    initiatedAt: new Date().toISOString(),
    originator: {
      accountReference: 'ORIGINATOR-DEMO-ACCOUNT',
      countryCode: form.originatorCountryCode,
      customerReference: 'ORIGINATOR-DEMO',
      institutionCode: 'ORIGINATOR-BANK',
      partyReference: 'ORIGINATOR-DEMO-PARTY',
    },
    tenantId: form.tenantId,
    transactionId: randomUuid(),
    transactionType: form.transactionType,
  }
}

function alertSeverity(outcome: string) {
  if (outcome === 'BLOCK') {
    return 'error'
  }
  if (outcome === 'ESCALATE' || outcome === 'REVIEW') {
    return 'warning'
  }
  return 'success'
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
