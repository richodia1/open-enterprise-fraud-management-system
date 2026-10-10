import { Box, Stack, Typography } from '@mui/material'
import { PageHeader } from '../../app/AppShell'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'

type DecisionMappingRow = {
  action: string
  coreStatus: string
  id: string
  outcome: string
}

type PayloadFieldRow = {
  field: string
  id: string
  purpose: string
}

const decisionMappings: DecisionMappingRow[] = [
  {
    action: 'PROCEED_TO_COMPLETION',
    coreStatus: 'COMPLETED or PENDING_PAYOUT',
    id: 'approve',
    outcome: 'APPROVE',
  },
  {
    action: 'HOLD_FOR_MANUAL_REVIEW',
    coreStatus: 'HELD_FOR_REVIEW',
    id: 'review',
    outcome: 'REVIEW',
  },
  {
    action: 'REJECT_TRANSACTION',
    coreStatus: 'BLOCKED, REJECTED, or CANCELLED',
    id: 'block',
    outcome: 'BLOCK',
  },
  {
    action: 'ESCALATE_FOR_INVESTIGATION',
    coreStatus: 'ESCALATED or PENDING_INVESTIGATION',
    id: 'escalate',
    outcome: 'ESCALATE',
  },
]

const payloadFields: PayloadFieldRow[] = [
  { field: 'transactionId', id: 'transaction-id', purpose: 'Matches the fraud result to the bank transaction.' },
  { field: 'decisionId', id: 'decision-id', purpose: 'Links the feedback to the fraud decision record.' },
  { field: 'outcome', id: 'outcome', purpose: 'Fraud outcome produced by OEFMS.' },
  { field: 'recommendedAction', id: 'recommended-action', purpose: 'Instruction the transaction core maps to its own status model.' },
  { field: 'riskScore', id: 'risk-score', purpose: 'Numeric score, risk band, and reason.' },
  { field: 'explanationSummary', id: 'explanation-summary', purpose: 'Human-readable reason for the decision.' },
  { field: 'evidence', id: 'evidence', purpose: 'Decision evidence shown to investigators and auditors.' },
]

const flowSteps = [
  'Transaction core creates a transaction as PROCESSING or PENDING_FRAUD_DECISION.',
  'OEFMS evaluates the transaction and produces a fraud decision.',
  'Explainable AI prepares the human-readable decision support summary.',
  'OEFMS publishes fraud.feedback.ready for the transaction core adapter.',
  'The transaction core applies its own final status transition.',
]

export function TransactionFeedbackPage() {
  return (
    <>
      <PageHeader
        eyebrow="Operations"
        summary="Integration view for the fraud decision feedback consumed by core banking and transaction systems."
        title="Transaction Feedback"
      />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0, 1fr))', xl: 'repeat(4, minmax(0, 1fr))' },
        }}
      >
        <ContractCard label="Event" value="fraud.feedback.ready" />
        <ContractCard label="RabbitMQ Queue" value="oefms.transaction-core.fraud-feedback-ready" />
        <ContractCard label="Kafka Topic" value="oefms.fraud.feedback.ready" />
        <ContractCard label="Payload" value="EventEnvelope<FraudFeedbackReadyEvent>" />
      </Box>

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', xl: '1fr 1fr' },
          mt: 2,
        }}
      >
        <SectionPanel eyebrow="Feedback Flow" title="Core Banking Consumption">
          <Stack spacing={1.25}>
            {flowSteps.map((step, index) => (
              <Stack
                direction="row"
                key={step}
                spacing={1.25}
                sx={{ alignItems: 'flex-start', borderBottom: '1px solid', borderColor: 'divider', pb: 1 }}
              >
                <Box
                  sx={{
                    alignItems: 'center',
                    bgcolor: 'primary.light',
                    borderRadius: '50%',
                    color: 'primary.dark',
                    display: 'inline-flex',
                    flex: '0 0 auto',
                    fontSize: 12,
                    fontWeight: 800,
                    height: 26,
                    justifyContent: 'center',
                    width: 26,
                  }}
                >
                  {index + 1}
                </Box>
                <Typography variant="body2">{step}</Typography>
              </Stack>
            ))}
          </Stack>
        </SectionPanel>

        <SectionPanel eyebrow="Payload" title="TransactionFraudFeedbackResponse">
          <RecordTable
            columns={[
              { header: 'Field', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.field}</Typography> },
              { header: 'Purpose', render: (row) => row.purpose },
            ]}
            emptyLabel="No payload fields defined."
            rows={payloadFields}
          />
        </SectionPanel>
      </Box>

      <SectionPanel eyebrow="Decision Mapping" sx={{ mt: 2 }} title="Recommended Core Status">
        <RecordTable
          columns={[
            { header: 'Outcome', render: (row) => <StatusChip value={row.outcome} />, width: 150 },
            {
              header: 'Recommended Action',
              render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.action}</Typography>,
            },
            { header: 'Typical Core Status', render: (row) => row.coreStatus },
          ]}
          emptyLabel="No decision mappings defined."
          rows={decisionMappings}
        />
      </SectionPanel>
    </>
  )
}

function ContractCard({ label, value }: { label: string; value: string }) {
  return (
    <SectionPanel title={label}>
      <Typography sx={{ fontSize: 14, fontWeight: 800, overflowWrap: 'anywhere' }}>{value}</Typography>
    </SectionPanel>
  )
}
