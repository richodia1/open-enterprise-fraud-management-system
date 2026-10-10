import {
  Activity,
  AlertTriangle,
  BarChart3,
  ClipboardList,
  CreditCard,
  Database,
  Fingerprint,
  Gauge,
  ListChecks,
  MessageSquareReply,
  Settings2,
  ShieldCheck,
  type LucideIcon,
} from 'lucide-react'

export type NavigationItem = {
  label: string
  href: string
  icon: LucideIcon
  group: 'Operations' | 'Administration'
}

export const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', href: '/', icon: Gauge, group: 'Operations' },
  { label: 'Transactions', href: '/transactions', icon: CreditCard, group: 'Operations' },
  { label: 'Transaction Feedback', href: '/transaction-feedback', icon: MessageSquareReply, group: 'Operations' },
  { label: 'Cases', href: '/cases', icon: ClipboardList, group: 'Operations' },
  { label: 'Alerts', href: '/alerts', icon: AlertTriangle, group: 'Operations' },
  { label: 'Rules', href: '/rules', icon: ShieldCheck, group: 'Administration' },
  {
    label: 'Configuration',
    href: '/configuration',
    icon: Settings2,
    group: 'Administration',
  },
  { label: 'Lists', href: '/lists', icon: ListChecks, group: 'Administration' },
  { label: 'Identity', href: '/identity', icon: Fingerprint, group: 'Administration' },
  { label: 'Audit Logs', href: '/audit-logs', icon: Activity, group: 'Operations' },
  { label: 'Reports', href: '/reports', icon: BarChart3, group: 'Operations' },
  { label: 'System Health', href: '/system-health', icon: Database, group: 'Operations' },
]
