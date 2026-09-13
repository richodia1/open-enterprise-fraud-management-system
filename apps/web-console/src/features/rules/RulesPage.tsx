import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Stack, TextField, Typography } from '@mui/material'
import { RefreshCw } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import type { RuleDefinition } from '../../types/api'

export function RulesPage() {
  const [tenantId, setTenantId] = useState('global')
  const rulesQuery = useQuery({
    queryFn: () => apiRequest<RuleDefinition[]>('/api/v1/rules', { searchParams: { tenantId } }),
    queryKey: ['rules', tenantId],
  })

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={() => rulesQuery.refetch()} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Decisioning"
        summary="View configured rule definitions that support rule-based fraud decisions."
        title="Rule Management"
      />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          label="Tenant"
          onChange={(event) => setTenantId(event.target.value)}
          size="small"
          value={tenantId}
        />
      </Stack>
      {rulesQuery.isLoading ? <LoadingState label="Loading rules" /> : null}
      {rulesQuery.isError ? <ErrorState error={rulesQuery.error} /> : null}
      {!rulesQuery.isLoading && !rulesQuery.isError ? (
        <RecordTable
          columns={[
            { header: 'Code', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.ruleCode}</Typography> },
            { header: 'Name', render: (row) => row.name ?? 'Not available' },
            { header: 'Category', render: (row) => row.category ?? 'Not available' },
            { header: 'Status', render: (row) => <StatusChip value={row.status ?? row.active} />, width: 130 },
            { header: 'Score', render: (row) => row.riskScore ?? 'Not available', width: 120 },
          ]}
          emptyLabel="No rules found."
          rows={(rulesQuery.data ?? []).map((row) => ({ ...row, id: row.ruleCode }))}
        />
      ) : null}
    </>
  )
}
