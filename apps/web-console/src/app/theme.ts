import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#0f7a4f',
      dark: '#07543a',
      light: '#dff4e9',
    },
    secondary: {
      main: '#315f9d',
    },
    warning: {
      main: '#b7791f',
    },
    error: {
      main: '#b42318',
    },
    success: {
      main: '#15803d',
    },
    background: {
      default: '#f5f7fb',
      paper: '#ffffff',
    },
    text: {
      primary: '#17202a',
      secondary: '#596579',
    },
    divider: '#dce3ee',
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily:
      'Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    allVariants: {
      letterSpacing: 0,
    },
    h1: {
      fontSize: 30,
      fontWeight: 700,
      lineHeight: 1.18,
    },
    h2: {
      fontSize: 24,
      fontWeight: 700,
      lineHeight: 1.2,
    },
    h3: {
      fontSize: 20,
      fontWeight: 700,
      lineHeight: 1.25,
    },
    h4: {
      fontSize: 18,
      fontWeight: 700,
      lineHeight: 1.28,
    },
    body2: {
      lineHeight: 1.45,
    },
    button: {
      letterSpacing: 0,
      textTransform: 'none',
      fontWeight: 700,
    },
  },
  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          minHeight: 36,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: '1px solid #dce3ee',
          borderRadius: 8,
          boxShadow: 'none',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: {
          borderRadius: 8,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          backgroundColor: '#f8fafc',
          color: '#3f4b5f',
          fontWeight: 700,
        },
      },
    },
  },
})
