import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './app/AppShell'
import { AlertsPage } from './features/cases/AlertsPage'
import { CaseDetailPage } from './features/cases/CaseDetailPage'
import { CasesPage } from './features/cases/CasesPage'
import { AuditLogsPage } from './features/audit/AuditLogsPage'
import { ConfigurationPage } from './features/configuration/ConfigurationPage'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { TransactionFeedbackPage } from './features/feedback/TransactionFeedbackPage'
import { IdentityPage } from './features/identity/IdentityPage'
import { ListsPage } from './features/lists/ListsPage'
import { ReportsPage } from './features/reports/ReportsPage'
import { RulesPage } from './features/rules/RulesPage'
import { SystemHealthPage } from './features/system/SystemHealthPage'
import { TransactionsPage } from './features/transactions/TransactionsPage'

function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<DashboardPage />} />
        <Route path="transactions" element={<TransactionsPage />} />
        <Route path="transaction-feedback" element={<TransactionFeedbackPage />} />
        <Route path="cases" element={<CasesPage />} />
        <Route path="cases/:caseId" element={<CaseDetailPage />} />
        <Route path="alerts" element={<AlertsPage />} />
        <Route path="rules" element={<RulesPage />} />
        <Route path="configuration" element={<ConfigurationPage />} />
        <Route path="configuration/:resourceType" element={<ConfigurationPage />} />
        <Route path="lists" element={<ListsPage />} />
        <Route path="lists/:resourceType" element={<ListsPage />} />
        <Route path="identity" element={<IdentityPage />} />
        <Route path="identity/:resourceType" element={<IdentityPage />} />
        <Route path="audit-logs" element={<AuditLogsPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="reports/:resourceType" element={<ReportsPage />} />
        <Route path="system-health" element={<SystemHealthPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
