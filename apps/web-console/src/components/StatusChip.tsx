import { Chip } from '@mui/material'

type ChipColor = 'default' | 'error' | 'info' | 'success' | 'warning'

export function StatusChip({ value }: { value?: boolean | string }) {
  const label = labelFor(value)
  const color = colorFor(label)

  return <Chip color={color} label={label} size="small" variant={color === 'default' ? 'outlined' : 'filled'} />
}

function labelFor(value?: boolean | string) {
  if (value === undefined) {
    return 'Unknown'
  }
  if (typeof value === 'boolean') {
    return value ? 'Active' : 'Inactive'
  }
  return value
}

function colorFor(value: string): ChipColor {
  const normalized = value.toLowerCase()
  if (['active', 'approved', 'allow', 'false_positive', 'low', 'open', 'true', 'up'].includes(normalized)) {
    return 'success'
  }
  if (['block', 'blocked', 'critical', 'down', 'error', 'failed', 'fraud', 'fraud_confirmed', 'high'].includes(normalized)) {
    return 'error'
  }
  if (['draft', 'escalated', 'in_review', 'medium', 'pending', 'review', 'warning'].includes(normalized)) {
    return 'warning'
  }
  if (['closed', 'disabled', 'inactive'].includes(normalized)) {
    return 'default'
  }
  return 'info'
}
