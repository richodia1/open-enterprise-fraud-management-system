import type { ReactNode } from 'react'
import { Paper, Stack, Typography, type SxProps, type Theme } from '@mui/material'

export function SectionPanel({
  children,
  eyebrow,
  sx,
  title,
}: {
  children: ReactNode
  eyebrow?: string
  sx?: SxProps<Theme>
  title: string
}) {
  return (
    <Paper sx={{ p: 2, ...sx }} variant="outlined">
      <Stack spacing={1.5}>
        <div>
          {eyebrow ? (
            <Typography color="primary.main" sx={{ fontSize: 12, fontWeight: 800 }} variant="body2">
              {eyebrow}
            </Typography>
          ) : null}
          <Typography variant="h3">{title}</Typography>
        </div>
        {children}
      </Stack>
    </Paper>
  )
}
