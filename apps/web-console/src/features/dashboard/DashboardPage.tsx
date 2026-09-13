import { useQuery } from '@tanstack/react-query'
import { Box, Button, Stack, Typography } from '@mui/material'
import { RefreshCw } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import type { AlertRecord, AuditLogRecord, CaseRecord, HealthResponse, RuleDefinition } from '../../types/api'

const demoTenant = 'mvp-smoke-tenant'

export function DashboardPage() {
  const healthQuery = useQuery({
    queryFn: () => apiRequest<HealthResponse>('/actuator/health'),
    queryKey: ['gateway-health'],
  })
  const casesQuery = useQuery({
    queryFn: () => apiRequest<CaseRecord[]>('/api/v1/cases', { searchParams: { tenantId: demoTenant } }),
    queryKey: ['dashboard-cases', demoTenant],
  })
  const alertsQuery = useQuery({
    queryFn: () => apiRequest<AlertRecord[]>('/api/v1/alerts', { searchParams: { tenantId: demoTenant } }),
    queryKey: ['dashboard-alerts', demoTenant],
  })
  const rulesQuery = useQuery({
    queryFn: () => apiRequest<RuleDefinition[]>('/api/v1/rules', { searchParams: { tenantId: 'global' } }),
    queryKey: ['dashboard-rules'],
  })
  const auditQuery = useQuery({
    queryFn: () => apiRequest<AuditLogRecord[]>('/api/v1/audit-events', { searchParams: { tenantId: demoTenant } }),
    queryKey: ['dashboard-audit', demoTenant],
  })

  const refreshAll = () => {
    void healthQuery.refetch()
    void casesQuery.refetch()
    void alertsQuery.refetch()
    void rulesQuery.refetch()
    void auditQuery.refetch()
  }

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={refreshAll} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Operations"
        summary="Live entry point for investigators and administrators as the backend surfaces mature."
        title="Dashboard"
      />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0, 1fr))', xl: 'repeat(4, minmax(0, 1fr))' },
        }}
      >
        <MetricCard
          label="Gateway"
          loading={healthQuery.isLoading}
          value={healthQuery.data?.status ?? 'Unknown'}
          variant="status"
        />
        <MetricCard label="Open Cases" loading={casesQuery.isLoading} value={String(casesQuery.data?.length ?? 0)} />
        <MetricCard label="Alerts" loading={alertsQuery.isLoading} value={String(alertsQuery.data?.length ?? 0)} />
        <MetricCard label="Rules" loading={rulesQuery.isLoading} value={String(rulesQuery.data?.length ?? 0)} />
      </Box>

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', xl: '2fr 1fr' },
          mt: 2,
        }}
      >
        <SectionPanel eyebrow="Cases" title="Current Investigation Queue">
          {casesQuery.isLoading ? <LoadingState label="Loading cases" /> : null}
          {casesQuery.isError ? <ErrorState error={casesQuery.error} /> : null}
          {!casesQuery.isLoading && !casesQuery.isError ? (
            <Stack spacing={1}>
              {(casesQuery.data ?? []).slice(0, 5).map((caseFile) => (
                <Stack
                  direction={{ xs: 'column', md: 'row' }}
                  key={caseFile.caseId}
                  spacing={1}
                  sx={{ alignItems: { md: 'center' }, borderBottom: '1px solid', borderColor: 'divider', py: 1 }}
                >
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography sx={{ fontWeight: 800 }}>{caseFile.title}</Typography>
                    <Typography color="text.secondary" variant="body2">
                      {caseFile.transactionId}
                    </Typography>
                  </Box>
                  <StatusChip value={caseFile.priority} />
                  <StatusChip value={caseFile.status} />
                </Stack>
              ))}
              {(casesQuery.data ?? []).length === 0 ? (
                <Typography color="text.secondary" variant="body2">
                  No cases found for the demo tenant.
                </Typography>
              ) : null}
            </Stack>
          ) : null}
        </SectionPanel>

        <SectionPanel eyebrow="Audit" title="Recent Traceability">
          {auditQuery.isLoading ? <LoadingState label="Loading audit logs" /> : null}
          {auditQuery.isError ? <ErrorState error={auditQuery.error} /> : null}
          {!auditQuery.isLoading && !auditQuery.isError ? (
            <Stack spacing={1}>
              {(auditQuery.data ?? []).slice(0, 5).map((event) => (
                <Box key={event.auditEventId} sx={{ borderBottom: '1px solid', borderColor: 'divider', py: 1 }}>
                  <Typography sx={{ fontWeight: 800 }}>{event.action}</Typography>
                  <Typography color="text.secondary" variant="body2">
                    {event.resourceType} / {event.resourceId}
                  </Typography>
                </Box>
              ))}
              {(auditQuery.data ?? []).length === 0 ? (
                <Typography color="text.secondary" variant="body2">
                  No audit events found for the demo tenant.
                </Typography>
              ) : null}
            </Stack>
          ) : null}
        </SectionPanel>
      </Box>
    </>
  )
}

function MetricCard({
  label,
  loading,
  value,
  variant = 'number',
}: {
  label: string
  loading: boolean
  value: string
  variant?: 'number' | 'status'
}) {
  return (
    <SectionPanel title={label}>
      {loading ? (
        <LoadingState label="Loading" />
      ) : variant === 'status' ? (
        <StatusChip value={value} />
      ) : (
        <Typography variant="h2">{value}</Typography>
      )}
    </SectionPanel>
  )
}
