import { Alert, Box, CircularProgress, Stack, Typography } from '@mui/material'
import { ApiError } from '../lib/apiClient'

export function LoadingState({ label }: { label: string }) {
  return (
    <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', py: 4 }}>
      <CircularProgress size={20} />
      <Typography color="text.secondary" variant="body2">
        {label}
      </Typography>
    </Stack>
  )
}

export function ErrorState({ error }: { error: unknown }) {
  const message = error instanceof ApiError ? `${error.status}: ${error.message}` : 'Backend request failed.'

  return (
    <Box sx={{ py: 2 }}>
      <Alert severity="warning">{message}</Alert>
    </Box>
  )
}

export function EmptyState({ label }: { label: string }) {
  return (
    <Box sx={{ border: '1px dashed', borderColor: 'divider', borderRadius: 2, p: 3 }}>
      <Typography color="text.secondary" variant="body2">
        {label}
      </Typography>
    </Box>
  )
}
