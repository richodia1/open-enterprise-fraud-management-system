import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Alert, Box, Button, Stack, TextField, Typography } from '@mui/material'
import { MessagesSquare } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime, formatEnum } from '../../lib/formatters'
import type { ManagementQuestionAnswerResponse, ManagementQuestionRequest } from '../../types/api'

type QuestionForm = {
  contextAnswer: string
  question: string
  tenantId: string
}

const initialForm: QuestionForm = {
  contextAnswer: '',
  question: 'How many fraud case result from ATM this year?',
  tenantId: 'mvp-smoke-tenant',
}

export function ManagementQuestionsPage() {
  const [form, setForm] = useState<QuestionForm>(initialForm)
  const questionMutation = useMutation({
    mutationFn: () =>
      apiRequest<ManagementQuestionAnswerResponse>('/api/v1/explanations/management/questions', {
        body: toRequest(form),
        method: 'POST',
      }),
  })

  const updateField = <K extends keyof QuestionForm>(field: K, value: QuestionForm[K]) => {
    setForm((current) => ({ ...current, [field]: value }))
  }
  const answer = questionMutation.data

  return (
    <>
      <PageHeader
        eyebrow="Operations"
        summary="Ask management-style fraud questions and inspect the structured data plan needed for grounded answers."
        title="Management Q&A"
      />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', xl: '410px minmax(0, 1fr)' },
        }}
      >
        <SectionPanel eyebrow="Question" title="Ask Fraud Intelligence">
          {questionMutation.isError ? <ErrorState error={questionMutation.error} /> : null}
          {questionMutation.isSuccess && answer ? (
            <Alert severity={answer.liveDataRequired ? 'warning' : 'success'}>
              {answer.liveDataRequired ? 'Structured data is required before final answer.' : 'Answered from supplied context.'}
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
              label="Question"
              minRows={4}
              multiline
              onChange={(event) => updateField('question', event.target.value)}
              value={form.question}
            />
            <TextField
              helperText="Optional: supply a known answer or context to test grounded summarisation."
              label="Context Answer"
              minRows={3}
              multiline
              onChange={(event) => updateField('contextAnswer', event.target.value)}
              value={form.contextAnswer}
            />
            <Button
              disabled={questionMutation.isPending || !form.tenantId || !form.question}
              onClick={() => questionMutation.mutate()}
              startIcon={<MessagesSquare size={18} />}
              variant="contained"
            >
              Ask Question
            </Button>
          </Stack>
        </SectionPanel>

        <Stack spacing={2}>
          <SectionPanel eyebrow="Answer" title="Management Response">
            {answer ? (
              <Stack spacing={1.25}>
                <Box
                  sx={{
                    display: 'grid',
                    gap: 1.5,
                    gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0, 1fr))' },
                  }}
                >
                  <Fact label="Intent" value={formatEnum(answer.intent)} />
                  <Fact label="Status" value={<StatusChip value={answer.status} />} />
                  <Fact label="Live Data Required" value={<StatusChip value={answer.liveDataRequired} />} />
                  <Fact label="Answered At" value={formatDateTime(answer.answeredAt)} />
                </Box>
                <Typography color="text.secondary" variant="body2">
                  {answer.answer}
                </Typography>
              </Stack>
            ) : (
              <Typography color="text.secondary" variant="body2">
                Ask a question to classify it and see whether OEFMS can answer from context or needs structured data.
              </Typography>
            )}
          </SectionPanel>

          <SectionPanel eyebrow="Grounding" title="Structured Query Plan">
            <RecordTable
              columns={[
                { header: 'Service', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.sourceService}</Typography> },
                { header: 'Endpoint', render: (row) => row.endpoint },
                { header: 'Purpose', render: (row) => row.purpose },
                {
                  header: 'Parameters',
                  render: (row) =>
                    Object.entries(row.parameters)
                      .map(([key, value]) => `${key}: ${value}`)
                      .join(', ') || 'None',
                },
              ]}
              emptyLabel="No query plan produced yet."
              rows={(answer?.queryPlan ?? []).map((row, index) => ({ ...row, id: `${row.sourceService}-${index}` }))}
            />
          </SectionPanel>
        </Stack>
      </Box>
    </>
  )
}

function toRequest(form: QuestionForm): ManagementQuestionRequest {
  return {
    audience: 'MANAGEMENT',
    context: form.contextAnswer
      ? {
          answer: form.contextAnswer,
          evidenceDescription: form.contextAnswer,
          evidenceTitle: 'User supplied context',
        }
      : {},
    filters: {},
    question: form.question,
    tenantId: form.tenantId,
  }
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
