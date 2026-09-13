import { useMemo, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  Tab,
  Tabs,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import { Pencil, Plus, RefreshCw, Save, Trash2, X } from 'lucide-react'
import { z } from 'zod'
import { PageHeader } from '../../app/AppShell'
import { EmptyState, ErrorState, LoadingState } from '../../components/LoadingState'
import { RecordTable } from '../../components/RecordTable'
import { StatusChip } from '../../components/StatusChip'
import { apiRequest } from '../../lib/apiClient'
import { formatDateTime } from '../../lib/formatters'
import type {
  ManagedResourceRecord,
  ManagedResourceRequest,
  ManagedResourceTypeDescriptor,
} from '../../types/api'
import type { ResourceArea } from './resourceAreas'

const resourceFormSchema = z.object({
  active: z.boolean(),
  attributesText: z.string(),
  code: z.string().min(2, 'Code is required.'),
  description: z.string(),
  name: z.string().min(2, 'Name is required.'),
  tenantId: z.string().min(1, 'Tenant is required.'),
  updatedBy: z.string().min(1, 'Updated by is required.'),
})

type ResourceFormValues = z.infer<typeof resourceFormSchema>

const defaultFormValues: ResourceFormValues = {
  active: true,
  attributesText: '{}',
  code: '',
  description: '',
  name: '',
  tenantId: 'global',
  updatedBy: 'web-console',
}

export function ResourceWorkspace({ area }: { area: ResourceArea }) {
  const { resourceType } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [localSelectedType, setLocalSelectedType] = useState(area.preferredTypes[0])
  const [tenantId, setTenantId] = useState('global')
  const [filterText, setFilterText] = useState('')
  const [activeOnly, setActiveOnly] = useState(false)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingRecord, setEditingRecord] = useState<ManagedResourceRecord | null>(null)
  const [formError, setFormError] = useState<string | null>(null)

  const form = useForm<ResourceFormValues>({
    defaultValues: defaultFormValues,
    resolver: zodResolver(resourceFormSchema),
  })

  const typesQuery = useQuery({
    queryFn: () => apiRequest<ManagedResourceTypeDescriptor[]>(`${area.basePath}/types`),
    queryKey: ['resource-types', area.basePath],
  })

  const orderedTypes = useMemo(() => {
    const types = typesQuery.data ?? []
    return [...types].sort((left, right) => {
      const leftIndex = area.preferredTypes.indexOf(left.resourceType)
      const rightIndex = area.preferredTypes.indexOf(right.resourceType)
      if (leftIndex === -1 && rightIndex === -1) {
        return left.displayName.localeCompare(right.displayName)
      }
      if (leftIndex === -1) {
        return 1
      }
      if (rightIndex === -1) {
        return -1
      }
      return leftIndex - rightIndex
    })
  }, [area.preferredTypes, typesQuery.data])

  const selectedType = resourceType ?? localSelectedType ?? orderedTypes[0]?.resourceType ?? ''
  const recordsQueryKey = ['resources', area.basePath, selectedType, tenantId, filterText, activeOnly] as const

  const recordsQuery = useQuery({
    enabled: Boolean(selectedType),
    queryFn: () =>
      apiRequest<ManagedResourceRecord[]>(`${area.basePath}/${selectedType}`, {
        searchParams: {
          active: activeOnly ? true : undefined,
          filterText: filterText || undefined,
          tenantId: tenantId || undefined,
        },
      }),
    queryKey: recordsQueryKey,
  })

  const saveMutation = useMutation({
    mutationFn: ({ payload, record }: { payload: ManagedResourceRequest; record: ManagedResourceRecord | null }) =>
      apiRequest<ManagedResourceRecord>(
        record ? `${area.basePath}/${selectedType}/${record.id}` : `${area.basePath}/${selectedType}`,
        {
          body: payload,
          method: record ? 'PUT' : 'POST',
        },
      ),
  })

  const deleteMutation = useMutation({
    mutationFn: (record: ManagedResourceRecord) =>
      apiRequest<void>(`${area.basePath}/${record.resourceType}/${record.id}`, {
        method: 'DELETE',
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['resources', area.basePath] })
    },
  })

  const selectedDescriptor = orderedTypes.find((type) => type.resourceType === selectedType)
  const records = recordsQuery.data ?? []

  function closeDialog() {
    setDialogOpen(false)
    setEditingRecord(null)
    setFormError(null)
    saveMutation.reset()
  }

  function openCreateDialog() {
    setEditingRecord(null)
    setFormError(null)
    saveMutation.reset()
    form.reset({ ...defaultFormValues, tenantId })
    setDialogOpen(true)
  }

  function openEditDialog(record: ManagedResourceRecord) {
    setEditingRecord(record)
    setFormError(null)
    saveMutation.reset()
    form.reset({
      active: record.active,
      attributesText: JSON.stringify(record.attributes ?? {}, null, 2),
      code: record.code,
      description: record.description ?? '',
      name: record.name,
      tenantId: record.tenantId,
      updatedBy: record.updatedBy || 'web-console',
    })
    setDialogOpen(true)
  }

  async function onSubmit(values: ResourceFormValues) {
    setFormError(null)
    let attributes: Record<string, string>
    try {
      attributes = parseAttributes(values.attributesText)
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Attributes must be valid JSON.')
      return
    }

    try {
      await saveMutation.mutateAsync({
        payload: {
          active: values.active,
          attributes,
          code: values.code.trim(),
          description: values.description.trim(),
          name: values.name.trim(),
          tenantId: values.tenantId.trim(),
          updatedBy: values.updatedBy.trim(),
        },
        record: editingRecord,
      })
      closeDialog()
      void queryClient.invalidateQueries({ queryKey: ['resources', area.basePath, selectedType] })
    } catch {
      // The mutation error is rendered in the dialog.
    }
  }

  return (
    <>
      <PageHeader
        actions={
          <Stack direction="row" spacing={1}>
            <Tooltip title="Refresh records">
              <IconButton aria-label="Refresh records" onClick={() => recordsQuery.refetch()}>
                <RefreshCw size={18} />
              </IconButton>
            </Tooltip>
            <Button onClick={openCreateDialog} startIcon={<Plus size={18} />} variant="contained">
              Add
            </Button>
          </Stack>
        }
        eyebrow={area.eyebrow}
        summary={area.summary}
        title={area.title}
      />

      <Paper sx={{ mb: 2, p: 1 }} variant="outlined">
        {typesQuery.isLoading ? <LoadingState label="Loading resource types" /> : null}
        {typesQuery.isError ? <ErrorState error={typesQuery.error} /> : null}
        {orderedTypes.length > 0 ? (
          <Tabs
            allowScrollButtonsMobile
            onChange={(_, value: string) => {
              setLocalSelectedType(value)
              navigate(`${area.routeBasePath}/${value}`)
            }}
            scrollButtons="auto"
            value={selectedType}
            variant="scrollable"
          >
            {orderedTypes.map((type) => (
              <Tab key={type.resourceType} label={type.displayName} value={type.resourceType} />
            ))}
          </Tabs>
        ) : null}
      </Paper>

      <Paper sx={{ mb: 2, p: 2 }} variant="outlined">
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
          <TextField
            label="Tenant"
            onChange={(event) => setTenantId(event.target.value)}
            size="small"
            value={tenantId}
          />
          <TextField
            fullWidth
            label="Filter"
            onChange={(event) => setFilterText(event.target.value)}
            size="small"
            value={filterText}
          />
          <TextField
            label="Status"
            onChange={(event) => setActiveOnly(event.target.value === 'active')}
            select
            size="small"
            sx={{ minWidth: 160 }}
            value={activeOnly ? 'active' : 'all'}
          >
            <MenuItem value="all">All</MenuItem>
            <MenuItem value="active">Active only</MenuItem>
          </TextField>
        </Stack>
      </Paper>

      {selectedDescriptor ? (
        <Box sx={{ mb: 2 }}>
          <Typography color="text.secondary" variant="body2">
            {selectedDescriptor.legacyResource}
          </Typography>
        </Box>
      ) : null}

      {recordsQuery.isLoading ? <LoadingState label="Loading records" /> : null}
      {recordsQuery.isError ? <ErrorState error={recordsQuery.error} /> : null}
      {!recordsQuery.isLoading && !recordsQuery.isError && selectedType ? (
        <RecordTable
          columns={[
            { header: 'Code', render: (row) => <Typography sx={{ fontWeight: 700 }}>{row.code}</Typography> },
            { header: 'Name', render: (row) => row.name },
            { header: 'Description', render: (row) => row.description || 'Not available' },
            { header: 'Active', render: (row) => <StatusChip value={row.active} />, width: 120 },
            { header: 'Updated', render: (row) => formatDateTime(row.updatedAt), width: 190 },
            {
              header: 'Actions',
              render: (row) => (
                <Stack direction="row" spacing={0.5}>
                  <Tooltip title="Edit record">
                    <IconButton aria-label={`Edit ${row.name}`} onClick={() => openEditDialog(row)} size="small">
                      <Pencil size={16} />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete record">
                    <IconButton
                      aria-label={`Delete ${row.name}`}
                      disabled={deleteMutation.isPending}
                      onClick={() => deleteMutation.mutate(row)}
                      size="small"
                    >
                      <Trash2 size={16} />
                    </IconButton>
                  </Tooltip>
                </Stack>
              ),
              width: 120,
            },
          ]}
          emptyLabel="No records found for this resource."
          rows={records}
        />
      ) : null}
      {!selectedType && !recordsQuery.isLoading ? <EmptyState label="No resource type selected." /> : null}

      <Dialog fullWidth maxWidth="md" onClose={closeDialog} open={dialogOpen}>
        <DialogTitle>{editingRecord ? 'Edit Resource' : 'Add Resource'}</DialogTitle>
        <Box component="form" onSubmit={form.handleSubmit(onSubmit)}>
          <DialogContent>
            <Stack spacing={2}>
              {formError ? <Alert severity="error">{formError}</Alert> : null}
              {saveMutation.isError ? <ErrorState error={saveMutation.error} /> : null}
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                <TextField
                  error={Boolean(form.formState.errors.tenantId)}
                  fullWidth
                  helperText={form.formState.errors.tenantId?.message}
                  label="Tenant"
                  size="small"
                  {...form.register('tenantId')}
                />
                <TextField
                  error={Boolean(form.formState.errors.updatedBy)}
                  fullWidth
                  helperText={form.formState.errors.updatedBy?.message}
                  label="Updated by"
                  size="small"
                  {...form.register('updatedBy')}
                />
              </Stack>
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                <TextField
                  error={Boolean(form.formState.errors.code)}
                  fullWidth
                  helperText={form.formState.errors.code?.message}
                  label="Code"
                  size="small"
                  {...form.register('code')}
                />
                <TextField
                  error={Boolean(form.formState.errors.name)}
                  fullWidth
                  helperText={form.formState.errors.name?.message}
                  label="Name"
                  size="small"
                  {...form.register('name')}
                />
              </Stack>
              <TextField fullWidth label="Description" multiline minRows={3} {...form.register('description')} />
              <Controller
                control={form.control}
                name="active"
                render={({ field }) => (
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={field.value}
                        onBlur={field.onBlur}
                        onChange={(_, checked) => field.onChange(checked)}
                      />
                    }
                    label="Active"
                  />
                )}
              />
              <TextField
                error={Boolean(form.formState.errors.attributesText)}
                fullWidth
                helperText="JSON object of additional key/value attributes."
                label="Attributes"
                minRows={5}
                multiline
                {...form.register('attributesText')}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={closeDialog} startIcon={<X size={18} />}>
              Cancel
            </Button>
            <Button disabled={saveMutation.isPending} startIcon={<Save size={18} />} type="submit" variant="contained">
              Save
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </>
  )
}

function parseAttributes(value: string) {
  if (!value.trim()) {
    return {}
  }
  const parsed = JSON.parse(value) as unknown
  if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
    throw new Error('Attributes must be a JSON object.')
  }
  return Object.fromEntries(
    Object.entries(parsed).map(([key, entry]) => [key, entry == null ? '' : String(entry)]),
  )
}
