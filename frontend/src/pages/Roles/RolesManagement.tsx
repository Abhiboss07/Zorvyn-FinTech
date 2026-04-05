import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

const RolesManagement = () => {
  const roles = [
    { id: 1, name: 'ADMIN', description: 'Full access to all platform features and settings.', users: 3 },
    { id: 2, name: 'MANAGER', description: 'Can view logs, manage users, and approve transactions.', users: 12 },
    { id: 3, name: 'ANALYST', description: 'Read-only access to dashboard and reporting analytics.', users: 8 },
    { id: 4, name: 'USER', description: 'Standard customer account with standard limits.', users: 15420 },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="600">Access Control & Roles</Typography>
      </Box>

      <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3 }}>
        <Table>
          <TableHead sx={{ bgcolor: 'background.default' }}>
            <TableRow>
              <TableCell>Role Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Assigned Users</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {roles.map((role) => (
              <TableRow key={role.id}>
                <TableCell>
                  <Chip label={role.name} color={role.name === 'ADMIN' ? 'error' : 'primary'} size="small" variant={role.name === 'USER' ? 'outlined' : 'filled'} />
                </TableCell>
                <TableCell>{role.description}</TableCell>
                <TableCell>{role.users.toLocaleString()}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" color="primary">
                    <EditIcon fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default RolesManagement;
