import { useState, type ReactNode } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  AppBar,
  Box,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Stack,
  Toolbar,
  Tooltip,
  Typography,
  useMediaQuery,
} from '@mui/material'
import { useTheme } from '@mui/material/styles'
import { Menu, Shield, X } from 'lucide-react'
import { apiBaseUrl } from '../lib/apiClient'
import { navigationItems } from './navigation'

const drawerWidth = 280

export function AppShell() {
  const theme = useTheme()
  const isDesktop = useMediaQuery(theme.breakpoints.up('lg'))
  const [mobileOpen, setMobileOpen] = useState(false)
  const drawer = <NavigationDrawer onNavigate={() => setMobileOpen(false)} />

  return (
    <Box sx={{ bgcolor: 'background.default', display: 'flex', minHeight: '100vh' }}>
      <AppBar
        color="inherit"
        elevation={0}
        position="fixed"
        sx={{
          borderBottom: '1px solid',
          borderColor: 'divider',
          ml: { lg: `${drawerWidth}px` },
          width: { lg: `calc(100% - ${drawerWidth}px)` },
        }}
      >
        <Toolbar sx={{ gap: 2, minHeight: 64 }}>
          {!isDesktop ? (
            <Tooltip title={mobileOpen ? 'Close navigation' : 'Open navigation'}>
              <IconButton
                aria-label={mobileOpen ? 'Close navigation' : 'Open navigation'}
                edge="start"
                onClick={() => setMobileOpen((open) => !open)}
              >
                {mobileOpen ? <X size={20} /> : <Menu size={20} />}
              </IconButton>
            </Tooltip>
          ) : null}
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h1" sx={{ fontSize: { xs: 22, md: 26 } }}>
              OEFMS Web Console
            </Typography>
            <Typography color="text.secondary" variant="body2">
              {apiBaseUrl}
            </Typography>
          </Box>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ flexShrink: { lg: 0 }, width: { lg: drawerWidth } }}>
        <Drawer
          ModalProps={{ keepMounted: true }}
          onClose={() => setMobileOpen(false)}
          open={mobileOpen}
          sx={{ display: { xs: 'block', lg: 'none' } }}
          variant="temporary"
        >
          {drawer}
        </Drawer>
        <Drawer
          open
          sx={{
            display: { xs: 'none', lg: 'block' },
            '& .MuiDrawer-paper': {
              borderColor: 'divider',
              borderRight: '1px solid',
              width: drawerWidth,
            },
          }}
          variant="permanent"
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          px: { xs: 2, md: 3 },
          py: 3,
          pt: 11,
        }}
      >
        <Outlet />
      </Box>
    </Box>
  )
}

function NavigationDrawer({ onNavigate }: { onNavigate: () => void }) {
  const location = useLocation()
  const navigate = useNavigate()
  const groups = ['Operations', 'Administration'] as const

  return (
    <Box sx={{ bgcolor: 'background.paper', minHeight: '100%', width: drawerWidth }}>
      <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center', p: 2.5 }}>
        <Box
          sx={{
            alignItems: 'center',
            bgcolor: 'primary.main',
            borderRadius: 2,
            color: '#ffffff',
            display: 'inline-flex',
            height: 40,
            justifyContent: 'center',
            width: 40,
          }}
        >
          <Shield size={22} />
        </Box>
        <Box>
          <Typography variant="h3" sx={{ fontSize: 18 }}>
            OEFMS
          </Typography>
          <Typography color="text.secondary" variant="body2">
            Fraud management
          </Typography>
        </Box>
      </Stack>
      <Divider />
      {groups.map((group) => (
        <Box key={group} sx={{ px: 1.5, py: 1.5 }}>
          <Typography
            color="text.secondary"
            sx={{ fontSize: 12, fontWeight: 800, px: 1.5, py: 1, textTransform: 'uppercase' }}
          >
            {group}
          </Typography>
          <List disablePadding>
            {navigationItems
              .filter((item) => item.group === group)
              .map((item) => {
                const selected =
                  item.href === '/'
                    ? location.pathname === '/'
                    : location.pathname.startsWith(item.href)
                return (
                  <ListItemButton
                    key={item.href}
                    onClick={() => {
                      navigate(item.href)
                      onNavigate()
                    }}
                    selected={selected}
                    sx={{ borderRadius: 2, mb: 0.25, minHeight: 42 }}
                  >
                    <ListItemIcon
                      sx={{ color: selected ? 'primary.main' : 'text.secondary', minWidth: 36 }}
                    >
                      <item.icon size={18} />
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Typography component="span" sx={{ fontSize: 14, fontWeight: selected ? 800 : 600 }}>
                          {item.label}
                        </Typography>
                      }
                    />
                  </ListItemButton>
                )
              })}
          </List>
        </Box>
      ))}
    </Box>
  )
}

export function PageHeader({
  actions,
  eyebrow,
  summary,
  title,
}: {
  actions?: ReactNode
  eyebrow: string
  summary: string
  title: string
}) {
  return (
    <Stack
      direction={{ xs: 'column', md: 'row' }}
      spacing={2}
      sx={{ alignItems: { xs: 'stretch', md: 'flex-end' }, mb: 2.5 }}
    >
      <Box sx={{ flexGrow: 1 }}>
        <Typography color="primary.main" sx={{ fontSize: 12, fontWeight: 800, mb: 0.5 }}>
          {eyebrow}
        </Typography>
        <Typography variant="h2">{title}</Typography>
        <Typography color="text.secondary" sx={{ maxWidth: 820, mt: 0.75 }} variant="body2">
          {summary}
        </Typography>
      </Box>
      {actions}
    </Stack>
  )
}
