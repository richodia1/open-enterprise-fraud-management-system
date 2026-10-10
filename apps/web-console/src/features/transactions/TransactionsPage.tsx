import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Box, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { RefreshCw, Send } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime, formatEnum } from '../../lib/formatters'
import type {
  TransactionChannel,
  TransactionDirection,
  TransactionIngestionRequest,
  TransactionIngestionResponse,
  TransactionReceiptRecord,
  TransactionType,
} from '../../types/api'

type TransactionForm = {
  amount: string
  beneficiaryCustomerReference: string
  channel: TransactionChannel
  currency: string
  direction: TransactionDirection
  externalReference: string
  originatorCustomerReference: string
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

const initialForm: TransactionForm = {
  amount: '250000',
  beneficiaryCustomerReference: 'BENEFICIARY-001',
  channel: 'MOBILE',
  currency: 'USD',
  direction: 'OUTBOUND',
  externalReference: `WEB-${Date.now()}`,
  originatorCustomerReference: 'CUSTOMER-001',
  tenantId: 'mvp-smoke-tenant',
  transactionType: 'TRANSFER',
}

export function TransactionsPage() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState<TransactionForm>(initialForm)
  const receiptsQuery = useQuery({
    queryFn: () =>
      apiRequest<TransactionReceiptRecord[]>('/api/v1/ingestion/transactions', {
        searchParams: { tenantId: form.tenantId },
      }),
    queryKey: ['transaction-receipts', form.tenantId],
  })
  const submitMutation = useMutation({
    mutationFn: () => apiRequest<TransactionIngestionResponse>('/api/v1/ingestion/transactions', {
      body: toRequest(form),
      method: 'POST',
    }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['transaction-receipts', form.tenantId] })
      setForm((current) => ({ ...current, externalReference: `WEB-${Date.now()}` }))
    },
  })

  const updateField = <K extends keyof TransactionForm>(field: K, value: TransactionForm[K]) => {
    setForm((current) => ({ ...current, [field]: value }))
  }

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={() => receiptsQuery.refetch()} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Operations"
        summary="Submit sample transactions and inspect ingestion receipts published into the fraud event flow."
        title="Transactions"
      />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', xl: '410px minmax(0, 1fr)' },
        }}
      >
        <SectionPanel eyebrow="Ingestion" title="Submit Transaction">
          {submitMutation.isError ? <ErrorState error={submitMutation.error} /> : null}
          {submitMutation.isSuccess ? (
            <Alert severity="success">
              Transaction {submitMutation.data.transactionId} accepted as {submitMutation.data.status}.
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
              label="External Reference"
              onChange={(event) => updateField('externalReference', event.target.value)}
              size="small"
              value={form.externalReference}
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
            <TextField
              label="Originator Customer"
              onChange={(event) => updateField('originatorCustomerReference', event.target.value)}
              size="small"
              value={form.originatorCustomerReference}
            />
            <TextField
              label="Beneficiary Customer"
              onChange={(event) => updateField('beneficiaryCustomerReference', event.target.value)}
              size="small"
              value={form.beneficiaryCustomerReference}
            />
            <Button
              disabled={submitMutation.isPending || !form.tenantId || !form.amount || !form.currency}
              onClick={() => submitMutation.mutate()}
              startIcon={<Send size={18} />}
              variant="contained"
            >
              Submit Transaction
            </Button>
          </Stack>
        </SectionPanel>

        <SectionPanel eyebrow="Receipts" title="Accepted Transactions">
          {receiptsQuery.isLoading ? <LoadingState label="Loading transaction receipts" /> : null}
          {receiptsQuery.isError ? <ErrorState error={receiptsQuery.error} /> : null}
          {!receiptsQuery.isLoading && !receiptsQuery.isError ? (
            <RecordTable
              columns={[
                {
                  header: 'Transaction',
                  render: (row) => (
                    <Box sx={{ minWidth: 0 }}>
                      <Typography sx={{ fontWeight: 800, overflowWrap: 'anywhere' }}>{row.transactionId}</Typography>
                      <Typography color="text.secondary" variant="body2">
                        {row.externalReference || 'No external reference'}
                      </Typography>
                    </Box>
                  ),
                },
                { header: 'Channel', render: (row) => formatEnum(row.channel), width: 150 },
                { header: 'Type', render: (row) => formatEnum(row.transactionType), width: 160 },
                {
                  header: 'Amount',
                  render: (row) => `${row.amount} ${row.currency}`,
                  width: 150,
                },
                { header: 'Status', render: (row) => <StatusChip value={row.status} />, width: 130 },
                { header: 'Event', render: (row) => row.eventType, width: 180 },
                { header: 'Accepted', render: (row) => formatDateTime(row.acceptedAt), width: 190 },
              ]}
              emptyLabel="No transaction receipts found for this tenant."
              rows={(receiptsQuery.data ?? []).map((row) => ({ ...row, id: row.transactionId }))}
            />
          ) : null}
        </SectionPanel>
      </Box>
    </>
  )
}

function toRequest(form: TransactionForm): TransactionIngestionRequest {
  const amount = Number.parseFloat(form.amount)
  return {
    amount: {
      currency: form.currency,
      value: Number.isFinite(amount) ? amount : 0,
    },
    attributes: {
      submittedBy: 'web-console',
      submissionMode: 'demo',
    },
    beneficiary: {
      accountReference: `${form.beneficiaryCustomerReference}-ACCOUNT`,
      countryCode: 'GB',
      customerReference: form.beneficiaryCustomerReference,
      institutionCode: 'BENEFICIARY-BANK',
      partyReference: `${form.beneficiaryCustomerReference}-PARTY`,
    },
    channel: form.channel,
    direction: form.direction,
    externalReference: form.externalReference,
    initiatedAt: new Date().toISOString(),
    originator: {
      accountReference: `${form.originatorCustomerReference}-ACCOUNT`,
      countryCode: 'GB',
      customerReference: form.originatorCustomerReference,
      institutionCode: 'ORIGINATOR-BANK',
      partyReference: `${form.originatorCustomerReference}-PARTY`,
    },
    tenantId: form.tenantId,
    transactionId: randomUuid(),
    transactionType: form.transactionType,
  }
}

function randomUuid() {
  if (globalThis.crypto && 'randomUUID' in globalThis.crypto) {
    return globalThis.crypto.randomUUID()
  }
  return `00000000-0000-4000-8000-${Math.random().toString(16).slice(2, 14).padEnd(12, '0')}`
}
