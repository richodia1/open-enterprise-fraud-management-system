import { useState, type ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Box, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { ArrowLeft, RefreshCw, Save, UserCheck } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { PageHeader } from '../../app/AppShell'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime, formatEnum } from '../../lib/formatters'
import type {
  CaseActionRecord,
  CaseActionResponse,
  CaseAssignableUser,
  CaseRecord,
} from '../../types/api'

const terminalStatuses = new Set(['CLOSED', 'FALSE_POSITIVE', 'FRAUD_CONFIRMED'])
const statusOptions = ['IN_REVIEW', 'ESCALATED', 'FRAUD_CONFIRMED', 'FALSE_POSITIVE', 'CLOSED']

export function CaseDetailPage() {
  const { caseId = '' } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [assigneeId, setAssigneeId] = useState('')
  const [assignmentActorId, setAssignmentActorId] = useState('web-console')
  const [assignmentNote, setAssignmentNote] = useState('')
  const [status, setStatus] = useState('IN_REVIEW')
  const [statusActorId, setStatusActorId] = useState('web-console')
  const [statusNote, setStatusNote] = useState('')

  const caseQuery = useQuery({
    enabled: Boolean(caseId),
    queryFn: () => apiRequest<CaseRecord>(`/api/v1/cases/${caseId}`),
    queryKey: ['case', caseId],
  })
  const actionsQuery = useQuery({
    enabled: Boolean(caseId),
    queryFn: () => apiRequest<CaseActionRecord[]>(`/api/v1/cases/${caseId}/actions`),
    queryKey: ['case-actions', caseId],
  })
  const caseFile = caseQuery.data
  const isTerminal = terminalStatuses.has(caseFile?.status ?? '')
  const assigneesQuery = useQuery({
    enabled: Boolean(caseFile?.tenantId) && !isTerminal,
    queryFn: () =>
      apiRequest<CaseAssignableUser[]>('/api/v1/cases/assignees', {
        searchParams: { tenantId: caseFile?.tenantId },
      }),
    queryKey: ['case-assignees', caseFile?.tenantId],
  })

  const refresh = () => {
    void caseQuery.refetch()
    void actionsQuery.refetch()
  }

  const onLifecycleSuccess = async (response: CaseActionResponse) => {
    queryClient.setQueryData(['case', caseId], response.caseFile)
    await queryClient.invalidateQueries({ queryKey: ['case-actions', caseId] })
    await queryClient.invalidateQueries({ queryKey: ['cases'] })
  }

  const assignMutation = useMutation({
    mutationFn: () =>
      apiRequest<CaseActionResponse>(`/api/v1/cases/${caseId}/assignment`, {
        body: {
          actorId: assignmentActorId,
          assigneeId,
          note: assignmentNote,
        },
        method: 'PATCH',
      }),
    onSuccess: async (response) => {
      setAssignmentNote('')
      await onLifecycleSuccess(response)
    },
  })

  const statusMutation = useMutation({
    mutationFn: () =>
      apiRequest<CaseActionResponse>(`/api/v1/cases/${caseId}/status`, {
        body: {
          actorId: statusActorId,
          note: statusNote,
          status,
        },
        method: 'PATCH',
      }),
    onSuccess: async (response) => {
      setStatusNote('')
      await onLifecycleSuccess(response)
    },
  })

  const assignees = assigneesQuery.data ?? []
  const selectedAssigneeKnown = assignees.some((assignee) => assignee.id === assigneeId)
  const canAssign = !isTerminal && !assignMutation.isPending && !assigneesQuery.isLoading && Boolean(assigneeId)

  return (
    <>
      <PageHeader
        actions={
          <Stack direction="row" spacing={1}>
            <Button onClick={() => navigate('/cases')} startIcon={<ArrowLeft size={18} />}>
              Back
            </Button>
            <Button onClick={refresh} startIcon={<RefreshCw size={18} />} variant="contained">
              Refresh
            </Button>
          </Stack>
        }
        eyebrow="Investigation"
        summary="Review case context, assignment, lifecycle actions, and final fraud disposition."
        title={caseFile?.title ?? 'Case Detail'}
      />

      {caseQuery.isLoading ? <LoadingState label="Loading case" /> : null}
      {caseQuery.isError ? <ErrorState error={caseQuery.error} /> : null}

      {caseFile ? (
        <Stack spacing={2}>
          <Box
            sx={{
              display: 'grid',
              gap: 2,
              gridTemplateColumns: { xs: '1fr', lg: '2fr 1fr' },
            }}
          >
            <SectionPanel eyebrow="Case" title="Overview">
              <Box
                sx={{
                  display: 'grid',
                  gap: 1.5,
                  gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0, 1fr))' },
                }}
              >
                <Fact label="Status" value={<StatusChip value={caseFile.status} />} />
                <Fact label="Priority" value={<StatusChip value={caseFile.priority} />} />
                <Fact label="Assignee" value={caseFile.assigneeId || 'Unassigned'} />
                <Fact label="Tenant" value={caseFile.tenantId} />
                <Fact label="Transaction" value={caseFile.transactionId} />
                <Fact label="Decision" value={caseFile.decisionId} />
                <Fact label="Created" value={formatDateTime(caseFile.createdAt)} />
                <Fact label="Updated" value={formatDateTime(caseFile.updatedAt)} />
              </Box>
              <Typography color="text.secondary" variant="body2">
                {caseFile.summary}
              </Typography>
            </SectionPanel>

            <SectionPanel eyebrow="Decision Support" title="Evidence Attributes">
              <Stack spacing={1}>
                {Object.entries(caseFile.attributes ?? {}).map(([key, value]) => (
                  <Fact key={key} label={formatEnum(key)} value={value} />
                ))}
                {Object.keys(caseFile.attributes ?? {}).length === 0 ? (
                  <Typography color="text.secondary" variant="body2">
                    No attributes captured for this case.
                  </Typography>
                ) : null}
              </Stack>
            </SectionPanel>
          </Box>

          <Box
            sx={{
              display: 'grid',
              gap: 2,
              gridTemplateColumns: { xs: '1fr', xl: 'repeat(2, minmax(0, 1fr))' },
            }}
          >
            <SectionPanel eyebrow="Workflow" title="Assignment">
              {assignMutation.isError ? <ErrorState error={assignMutation.error} /> : null}
              {assigneesQuery.isError ? <ErrorState error={assigneesQuery.error} /> : null}
              {isTerminal ? <Alert severity="info">Terminal cases cannot be reassigned.</Alert> : null}
              {!isTerminal && !assigneesQuery.isLoading && !assigneesQuery.isError && assignees.length === 0 ? (
                <Alert severity="warning">No active Identity users found for this tenant.</Alert>
              ) : null}
              <Stack spacing={1.5}>
                <TextField
                  disabled={isTerminal || assigneesQuery.isLoading || assigneesQuery.isError}
                  label="Assignee"
                  onChange={(event) => setAssigneeId(event.target.value)}
                  select
                  size="small"
                  value={assigneeId}
                >
                  <MenuItem value="">Select user</MenuItem>
                  {assigneeId && !selectedAssigneeKnown ? (
                    <MenuItem value={assigneeId}>{assigneeId}</MenuItem>
                  ) : null}
                  {assignees.map((assignee) => (
                    <MenuItem key={assignee.id} value={assignee.id}>
                      {assignee.email
                        ? `${assignee.displayName} - ${assignee.email}`
                        : assignee.displayName}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  disabled={isTerminal}
                  label="Actor"
                  onChange={(event) => setAssignmentActorId(event.target.value)}
                  size="small"
                  value={assignmentActorId}
                />
                <TextField
                  disabled={isTerminal}
                  label="Note"
                  minRows={3}
                  multiline
                  onChange={(event) => setAssignmentNote(event.target.value)}
                  value={assignmentNote}
                />
                <Button
                  disabled={!canAssign}
                  onClick={() => assignMutation.mutate()}
                  startIcon={<UserCheck size={18} />}
                  variant="contained"
                >
                  Assign
                </Button>
              </Stack>
            </SectionPanel>

            <SectionPanel eyebrow="Workflow" title="Status">
              {statusMutation.isError ? <ErrorState error={statusMutation.error} /> : null}
              {isTerminal ? <Alert severity="info">This case already has a terminal status.</Alert> : null}
              <Stack spacing={1.5}>
                <TextField
                  disabled={isTerminal}
                  label="Status"
                  onChange={(event) => setStatus(event.target.value)}
                  select
                  size="small"
                  value={status}
                >
                  {statusOptions.map((option) => (
                    <MenuItem key={option} value={option}>
                      {formatEnum(option)}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  disabled={isTerminal}
                  label="Actor"
                  onChange={(event) => setStatusActorId(event.target.value)}
                  size="small"
                  value={statusActorId}
                />
                <TextField
                  disabled={isTerminal}
                  label="Note"
                  minRows={3}
                  multiline
                  onChange={(event) => setStatusNote(event.target.value)}
                  value={statusNote}
                />
                <Button
                  disabled={isTerminal || statusMutation.isPending}
                  onClick={() => statusMutation.mutate()}
                  startIcon={<Save size={18} />}
                  variant="contained"
                >
                  Save Status
                </Button>
              </Stack>
            </SectionPanel>
          </Box>

          <SectionPanel eyebrow="Traceability" title="Lifecycle History">
            {actionsQuery.isLoading ? <LoadingState label="Loading case history" /> : null}
            {actionsQuery.isError ? <ErrorState error={actionsQuery.error} /> : null}
            {!actionsQuery.isLoading && !actionsQuery.isError ? (
              <RecordTable
                columns={[
                  { header: 'Action', render: (row) => formatEnum(row.actionType) },
                  { header: 'Actor', render: (row) => row.actorId },
                  { header: 'From', render: (row) => <StatusChip value={row.fromStatus} />, width: 130 },
                  { header: 'To', render: (row) => <StatusChip value={row.toStatus} />, width: 130 },
                  { header: 'Assignee', render: (row) => row.assigneeId || 'Not assigned' },
                  { header: 'Note', render: (row) => row.note || 'No note' },
                  { header: 'Occurred', render: (row) => formatDateTime(row.occurredAt), width: 190 },
                ]}
                emptyLabel="No lifecycle actions have been recorded."
                rows={(actionsQuery.data ?? []).map((row) => ({ ...row, id: row.caseActionId }))}
              />
            ) : null}
          </SectionPanel>
        </Stack>
      ) : null}
    </>
  )
}

function Fact({ label, value }: { label: string; value: ReactNode }) {
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
