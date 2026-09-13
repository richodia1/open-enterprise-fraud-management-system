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
import type { AlertRecord } from '../../types/api'

export function AlertsPage() {
  const [tenantId, setTenantId] = useState('mvp-smoke-tenant')
  const alertsQuery = useQuery({
    queryFn: () => apiRequest<AlertRecord[]>('/api/v1/alerts', { searchParams: { tenantId } }),
    queryKey: ['alerts', tenantId],
  })

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={() => alertsQuery.refetch()} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Monitoring"
        summary="Review alerts generated from fraud decisions before or after case creation."
        title="Alerts"
      />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          label="Tenant"
          onChange={(event) => setTenantId(event.target.value)}
          size="small"
          value={tenantId}
        />
      </Stack>
      {alertsQuery.isLoading ? <LoadingState label="Loading alerts" /> : null}
      {alertsQuery.isError ? <ErrorState error={alertsQuery.error} /> : null}
      {!alertsQuery.isLoading && !alertsQuery.isError ? (
        <RecordTable
          columns={[
            { header: 'Alert', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.title}</Typography> },
            { header: 'Transaction', render: (row) => row.transactionId },
            { header: 'Risk', render: (row) => <StatusChip value={row.riskBand} />, width: 120 },
            { header: 'Priority', render: (row) => <StatusChip value={row.priority} />, width: 120 },
            { header: 'Status', render: (row) => <StatusChip value={row.status} />, width: 120 },
            { header: 'Created', render: (row) => formatDateTime(row.createdAt), width: 190 },
          ]}
          emptyLabel="No alerts found."
          rows={(alertsQuery.data ?? []).map((row) => ({ ...row, id: row.alertId }))}
        />
      ) : null}
    </>
  )
}
