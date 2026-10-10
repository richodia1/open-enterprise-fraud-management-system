import { CssBaseline } from '@mui/material'
import { ThemeProvider } from '@mui/material/styles'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import { theme } from './app/theme'

describe('App', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.reject(new Error('offline during unit test'))),
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders the main console navigation', () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    })

    render(
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <QueryClientProvider client={queryClient}>
          <MemoryRouter initialEntries={['/configuration']}>
            <App />
          </MemoryRouter>
        </QueryClientProvider>
      </ThemeProvider>,
    )

    expect(screen.getByRole('heading', { name: 'OEFMS Web Console' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Configuration' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Configuration Management' })).toBeInTheDocument()
  })

  it('renders the transaction feedback integration view', () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    })

    render(
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <QueryClientProvider client={queryClient}>
          <MemoryRouter initialEntries={['/transaction-feedback']}>
            <App />
          </MemoryRouter>
        </QueryClientProvider>
      </ThemeProvider>,
    )

    expect(screen.getByRole('heading', { name: 'Transaction Feedback' })).toBeInTheDocument()
    expect(screen.getByText('fraud.feedback.ready')).toBeInTheDocument()
    expect(screen.getByText('oefms.transaction-core.fraud-feedback-ready')).toBeInTheDocument()
  })

  it('renders the transaction ingestion view', () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    })

    render(
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <QueryClientProvider client={queryClient}>
          <MemoryRouter initialEntries={['/transactions']}>
            <App />
          </MemoryRouter>
        </QueryClientProvider>
      </ThemeProvider>,
    )

    expect(screen.getByRole('heading', { name: 'Transactions' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Submit Transaction' })).toBeInTheDocument()
  })
})
