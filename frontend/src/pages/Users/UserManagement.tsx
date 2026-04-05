import { useState, useEffect } from 'react';
import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, IconButton, CircularProgress, Alert } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import api from '../../utils/api';
import { format } from 'date-fns';

interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: { name: string };
  isActive: boolean;
  twoFaEnabled: boolean;
  createdAt: string;
}

const UserManagement = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await api.get('/users');
        setUsers(response.data.data.content || response.data.data); // Support paginated or list response
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to load users');
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}><CircularProgress /></Box>;
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="600">User Management</Typography>
      </Box>

      {error ? (
        <Alert severity="error">{error}</Alert>
      ) : (
        <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3 }}>
          <Table>
            <TableHead sx={{ bgcolor: 'background.default' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: '600' }}>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>2FA</TableCell>
                <TableCell>Joined</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>
                    <Typography fontWeight="500">{user.firstName} {user.lastName}</Typography>
                  </TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>
                    <Chip label={user.role?.name?.replace('ROLE_', '') || 'User'} size="small" color="primary" variant="outlined" />
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={user.isActive ? 'Active' : 'Inactive'} 
                      size="small" 
                      color={user.isActive ? 'success' : 'error'} 
                      sx={{ borderRadius: 1 }}
                    />
                  </TableCell>
                  <TableCell>{user.twoFaEnabled ? 'Enabled' : 'Disabled'}</TableCell>
                  <TableCell>{format(new Date(user.createdAt), 'MMM dd, yyyy')}</TableCell>
                  <TableCell align="right">
                    <IconButton size="small" color="primary">
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error">
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {users.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>No users found.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
};

export default UserManagement;
