export type ResourceArea = {
  basePath: string
  eyebrow: string
  preferredTypes: string[]
  routeBasePath: string
  summary: string
  title: string
}

export const configurationArea: ResourceArea = {
  basePath: '/api/v1/configuration-management/resources',
  eyebrow: 'Configuration',
  preferredTypes: [
    'GLOBAL_DATA_POINTS',
    'DATA_POINTS',
    'CHANNELS',
    'EVENT_TYPES',
    'FRAUD_TYPES',
    'PAYLOAD_PROPERTIES',
  ],
  routeBasePath: '/configuration',
  summary:
    'Maintain the setup records that drive transaction classification, payload mapping, and fraud-management behavior.',
  title: 'Configuration Management',
}

export const listArea: ResourceArea = {
  basePath: '/api/v1/list-management/resources',
  eyebrow: 'Lists',
  preferredTypes: ['LIST_TYPES', 'LIST_ITEMS', 'APPROVALS'],
  routeBasePath: '/lists',
  summary:
    'Maintain operational lists that can later support watchlists, blocklists, allowlists, and import workflows.',
  title: 'List Management',
}

export const identityArea: ResourceArea = {
  basePath: '/api/v1/identity/resources',
  eyebrow: 'Identity',
  preferredTypes: [
    'CUSTOM_USERS',
    'TENANT_IDENTITY_GROUP_MAPPINGS',
    'TENANT_IDENTITY_ROLE_MAPPINGS',
  ],
  routeBasePath: '/identity',
  summary:
    'Manage identity support records while tenant identity-provider integrations mature.',
  title: 'Identity Resources',
}

export const reportArea: ResourceArea = {
  basePath: '/api/v1/report-management/resources',
  eyebrow: 'Reports',
  preferredTypes: ['DASHBOARD_METRICS', 'FRAUD_DASHBOARD_METRICS', 'GENERATED_REPORTS'],
  routeBasePath: '/reports',
  summary:
    'Track report-facing resources and dashboard foundations before full report generation is ported.',
  title: 'Report Management',
}
