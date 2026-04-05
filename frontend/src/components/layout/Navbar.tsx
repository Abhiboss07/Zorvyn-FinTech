import { useState } from 'react';
import { AppBar, Avatar, Box, IconButton, Menu, MenuItem, Toolbar, Typography, useTheme } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useAuthStore } from '../../store/authStore';
import { useNavigate } from 'react-router-dom';

interface NavbarProps {
  onDrawerToggle: () => void;
}

const drawerWidth = 260;

const Navbar = ({ onDrawerToggle }: NavbarProps) => {
  const theme = useTheme();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleMenuClose();
    logout();
    navigate('/login');
  };

  return (
    <AppBar
      position="fixed"
      sx={{
        width: { sm: `calc(100% - ${drawerWidth}px)` },
        ml: { sm: `${drawerWidth}px` },
        bgcolor: 'background.paper',
        color: 'text.primary',
        boxShadow: 'none',
        borderBottom: `1px solid ${theme.palette.divider}`,
      }}
    >
      <Toolbar>
        <IconButton
          color="inherit"
          aria-label="open drawer"
          edge="start"
          onClick={onDrawerToggle}
          sx={{ mr: 2, display: { sm: 'none' } }}
        >
          <MenuIcon />
        </IconButton>

        <Box sx={{ flexGrow: 1 }} />

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton color="inherit" aria-label="notifications">
            <NotificationsIcon />
          </IconButton>

          <Box sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }} onClick={handleMenuOpen}>
            <Avatar sx={{ bgcolor: 'secondary.main', width: 36, height: 36 }}>
              {user?.firstName?.charAt(0) || 'U'}
            </Avatar>
            <Box sx={{ ml: 1, display: { xs: 'none', md: 'block' } }}>
              <Typography variant="body2" fontWeight="600">
                {user?.firstName || 'User'} {user?.lastName || ''}
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'capitalize' }}>
                {user?.role?.replace('ROLE_', '').toLowerCase() || 'user'}
              </Typography>
            </Box>
          </Box>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            <MenuItem onClick={() => { handleMenuClose(); navigate('/settings'); }}>Profile Settings</MenuItem>
            <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>Logout</MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;
