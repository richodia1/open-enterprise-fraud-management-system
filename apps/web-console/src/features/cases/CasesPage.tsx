import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Stack, TextField, Typography } from '@mui/material'
import { RefreshCw } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime } from '../../lib/formatters'
import type { CaseRecord } from '../../types/api'

export function CasesPage() {
  const [tenantId, setTenantId] = useState('mvp-smoke-tenant')
  const casesQuery = useQuery({
    queryFn: () => apiRequest<CaseRecord[]>('/api/v1/cases', { searchParams: { tenantId } }),
    queryKey: ['cases', tenantId],
  })

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={() => casesQuery.refetch()} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Investigation"
        summary="View fraud cases created by the decision and explanation workflow."
        title="Cases"
      />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          label="Tenant"
          onChange={(event) => setTenantId(event.target.value)}
          size="small"
          value={tenantId}
        />
      </Stack>
      {casesQuery.isLoading ? <LoadingState label="Loading cases" /> : null}
      {casesQuery.isError ? <ErrorState error={casesQuery.error} /> : null}
      {!casesQuery.isLoading && !casesQuery.isError ? (
        <RecordTable
          columns={[
            { header: 'Case', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.title}</Typography> },
            { header: 'Transaction', render: (row) => row.transactionId },
            { header: 'Priority', render: (row) => <StatusChip value={row.priority} />, width: 120 },
            { header: 'Status', render: (row) => <StatusChip value={row.status} />, width: 120 },
            { header: 'Created', render: (row) => formatDateTime(row.createdAt), width: 190 },
          ]}
          emptyLabel="No cases found."
          rows={(casesQuery.data ?? []).map((row) => ({ ...row, id: row.caseId }))}
        />
      ) : null}
    </>
  )
}
