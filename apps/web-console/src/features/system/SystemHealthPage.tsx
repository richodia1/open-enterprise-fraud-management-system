import { useQuery } from '@tanstack/react-query'
import { Button, Stack, Typography } from '@mui/material'
import { RefreshCw } from 'lucide-react'
import { PageHeader } from '../../app/AppShell'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import type { HealthResponse } from '../../types/api'

type GatewayRoutesResponse = {
  correlationHeader: string
  jwtSecurityEnabled: boolean
  routes: GatewayRoute[]
}

type GatewayRoute = {
  id: string
  path: string
  serviceBoundary: string
}

export function SystemHealthPage() {
  const healthQuery = useQuery({
    queryFn: () => apiRequest<HealthResponse>('/actuator/health'),
    queryKey: ['system-health'],
  })
  const routesQuery = useQuery({
    queryFn: () => apiRequest<GatewayRoutesResponse>('/gateway/routes'),
    queryKey: ['gateway-routes'],
  })

  const refreshAll = () => {
    void healthQuery.refetch()
    void routesQuery.refetch()
  }

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={refreshAll} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        }
        eyebrow="Runtime"
        summary="Check gateway availability, route exposure, and current security posture."
        title="System Health"
      />
      <Stack spacing={2}>
        <SectionPanel eyebrow="Gateway" title="Health">
          {healthQuery.isLoading ? <LoadingState label="Loading gateway health" /> : null}
          {healthQuery.isError ? <ErrorState error={healthQuery.error} /> : null}
          {healthQuery.data ? <StatusChip value={healthQuery.data.status} /> : null}
        </SectionPanel>
        <SectionPanel eyebrow="Gateway" title="Routes">
          {routesQuery.isLoading ? <LoadingState label="Loading route map" /> : null}
          {routesQuery.isError ? <ErrorState error={routesQuery.error} /> : null}
          {routesQuery.data ? (
            <Stack spacing={2}>
              <Typography color="text.secondary" variant="body2">
                {routesQuery.data.correlationHeader} / JWT security: {String(routesQuery.data.jwtSecurityEnabled)}
              </Typography>
              <RecordTable
                columns={[
                  { header: 'Route', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.id}</Typography> },
                  { header: 'Boundary', render: (row) => row.serviceBoundary },
                  { header: 'Path', render: (row) => row.path },
                ]}
                emptyLabel="No routes exposed."
                rows={routesQuery.data.routes.map((row) => ({ ...row, id: row.id }))}
              />
            </Stack>
          ) : null}
        </SectionPanel>
      </Stack>
    </>
  )
}
