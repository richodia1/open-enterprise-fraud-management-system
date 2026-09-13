import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Box, Button, Stack, TextField, Typography } from '@mui/material'
import { RefreshCw } from 'lucide-react'
import { ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { SectionPanel } from '../../components/SectionPanel'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime } from '../../lib/formatters'
import type { TenantIdentityProvider } from '../../types/api'
import { ResourceWorkspace } from '../resources/ResourceWorkspace'
import { identityArea } from '../resources/resourceAreas'

export function IdentityPage() {
  const [tenantId, setTenantId] = useState('global')
  const providersQuery = useQuery({
    queryFn: () =>
      apiRequest<TenantIdentityProvider[]>('/api/v1/identity/tenant-identity-providers', {
        searchParams: { tenantId },
      }),
    queryKey: ['tenant-identity-providers', tenantId],
  })

  return (
    <Stack spacing={2.5}>
      <SectionPanel
        eyebrow="Enterprise Identity"
        title="Tenant Identity Providers"
      >
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
          <TextField
            label="Tenant"
            onChange={(event) => setTenantId(event.target.value)}
            size="small"
            value={tenantId}
          />
          <Button onClick={() => providersQuery.refetch()} startIcon={<RefreshCw size={18} />} variant="contained">
            Refresh
          </Button>
        </Stack>
        {providersQuery.isLoading ? <LoadingState label="Loading identity providers" /> : null}
        {providersQuery.isError ? <ErrorState error={providersQuery.error} /> : null}
        {!providersQuery.isLoading && !providersQuery.isError ? (
          <RecordTable
            columns={[
              { header: 'Code', render: (row) => <Typography sx={{ fontWeight: 800 }}>{row.providerCode}</Typography> },
              { header: 'Name', render: (row) => row.displayName },
              { header: 'Provider', render: (row) => row.providerType },
              { header: 'Enabled', render: (row) => <StatusChip value={row.enabled} />, width: 120 },
              { header: 'Status', render: (row) => <StatusChip value={row.status} />, width: 130 },
              { header: 'Updated', render: (row) => formatDateTime(row.updatedAt), width: 190 },
            ]}
            emptyLabel="No tenant identity providers found."
            rows={providersQuery.data ?? []}
          />
        ) : null}
      </SectionPanel>
      <Box>
        <ResourceWorkspace area={identityArea} />
      </Box>
    </Stack>
  )
}
