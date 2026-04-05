import { Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Typography, useTheme } from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';
import SpaceDashboardIcon from '@mui/icons-material/SpaceDashboard';
import PeopleIcon from '@mui/icons-material/People';
import ReceiptIcon from '@mui/icons-material/Receipt';
import SecurityIcon from '@mui/icons-material/Security';
import HistoryIcon from '@mui/icons-material/History';
import SettingsIcon from '@mui/icons-material/Settings';

const drawerWidth = 260;

const menuItems = [
  { text: 'Dashboard', icon: <SpaceDashboardIcon />, path: '/' },
  { text: 'Users', icon: <PeopleIcon />, path: '/users' },
  { text: 'Transactions', icon: <ReceiptIcon />, path: '/transactions' },
  { text: 'Access Control', icon: <SecurityIcon />, path: '/roles' },
  { text: 'Audit Logs', icon: <HistoryIcon />, path: '/audit' },
  { text: 'Settings', icon: <SettingsIcon />, path: '/settings' },
];

interface SidebarProps {
  mobileOpen: boolean;
  onDrawerToggle: () => void;
}

const Sidebar = ({ mobileOpen, onDrawerToggle }: SidebarProps) => {
  const theme = useTheme();
  const location = useLocation();
  const navigate = useNavigate();

  const handleNavClick = (path: string) => {
    navigate(path);
    // Close mobile drawer on navigation
    if (mobileOpen) {
      onDrawerToggle();
    }
  };

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center' }}>
        <Typography variant="h5" color="primary" sx={{ fontWeight: 700 }}>
          Zorvyn
        </Typography>
        <Typography variant="h5" sx={{ fontWeight: 300, ml: 1 }}>
          FinTech
        </Typography>
      </Box>
      <List sx={{ px: 2, flexGrow: 1 }}>
        {menuItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <ListItem key={item.text} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                selected={isActive}
                onClick={() => handleNavClick(item.path)}
                sx={{
                  borderRadius: 2,
                  '&.Mui-selected': {
                    backgroundColor: 'primary.main',
                    color: 'primary.contrastText',
                    '&:hover': {
                      backgroundColor: 'primary.dark',
                    },
                    '& .MuiListItemIcon-root': {
                      color: 'primary.contrastText',
                    },
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 40,
                    color: isActive ? 'inherit' : theme.palette.text.secondary,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontWeight: isActive ? 600 : 500,
                  }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
    >
      {/* Mobile Drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
            bgcolor: 'background.paper',
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Desktop Drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', sm: 'block' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
            bgcolor: 'background.paper',
            borderRight: `1px solid ${theme.palette.divider}`,
          },
        }}
        open
      >
        {drawer}
      </Drawer>
    </Box>
  );
};

export default Sidebar;
