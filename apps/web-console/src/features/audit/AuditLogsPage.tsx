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
import type { AuditLogRecord } from '../../types/api'

export function AuditLogsPage() {
  const [tenantId, setTenantId] = useState('mvp-smoke-tenant')
  const [correlationId, setCorrelationId] = useState('')
  const auditQuery = useQuery({
    queryFn: () =>
      apiRequest<AuditLogRecord[]>('/api/v1/audit-events', {
        searchParams: correlationId ? { correlationId } : { tenantId },
      }),
    queryKey: ['audit-events', tenantId, correlationId],
  })

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={() => auditQuery.refetch()} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Traceability"
        summary="Search audit records by tenant or correlation id across the MVP event flow."
        title="Audit Logs"
      />
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          label="Tenant"
          onChange={(event) => setTenantId(event.target.value)}
          size="small"
          value={tenantId}
        />
        <TextField
          fullWidth
          label="Correlation"
          onChange={(event) => setCorrelationId(event.target.value)}
          size="small"
          value={correlationId}
        />
      </Stack>
      {auditQuery.isLoading ? <LoadingState label="Loading audit logs" /> : null}
      {auditQuery.isError ? <ErrorState error={auditQuery.error} /> : null}
      {!auditQuery.isLoading && !auditQuery.isError ? (
        <RecordTable
          columns={[
            { header: 'Action', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.action}</Typography> },
            { header: 'Resource', render: (row) => `${row.resourceType} / ${row.resourceId}` },
            { header: 'Actor', render: (row) => row.actorId },
            { header: 'Tenant', render: (row) => row.tenantId },
            { header: 'Trace', render: (row) => <StatusChip value={row.correlationId ?? 'No correlation'} /> },
            { header: 'Occurred', render: (row) => formatDateTime(row.occurredAt), width: 190 },
          ]}
          emptyLabel="No audit logs found."
          rows={(auditQuery.data ?? []).map((row) => ({ ...row, id: row.auditEventId }))}
        />
      ) : null}
    </>
  )
}
