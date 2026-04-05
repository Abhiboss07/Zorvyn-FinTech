import { useState, useEffect } from 'react';
import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, CircularProgress, Alert } from '@mui/material';
import api from '../../utils/api';
import { format } from 'date-fns';

interface AuditLog {
  id: string;
  action: string;
  entityType: string;
  entityId: string;
  details: string;
  ipAddress: string;
  timestamp: string;
  user: { firstName: string, lastName: string, email: string };
}

const AuditLogs = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchLogs = async () => {
    try {
      const response = await api.get('/audit-logs');
      setLogs(response.data.data.content || response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load audit logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}><CircularProgress /></Box>;
  }

  const getActionColor = (action: string) => {
    if (action.includes('LOGIN')) return 'primary';
    if (action.includes('CREATE')) return 'success';
    if (action.includes('UPDATE')) return 'info';
    if (action.includes('DELETE') || action.includes('REJECT') || action.includes('FAIL')) return 'error';
    return 'default';
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="600">Audit Logs</Typography>
      </Box>

      {error ? (
        <Alert severity="error">{error}</Alert>
      ) : (
        <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3 }}>
          <Table size="small">
            <TableHead sx={{ bgcolor: 'background.default' }}>
              <TableRow>
                <TableCell>Timestamp</TableCell>
                <TableCell>User</TableCell>
                <TableCell>Action</TableCell>
                <TableCell>Entity Details</TableCell>
                <TableCell>IP Address</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.map((log) => (
                <TableRow key={log.id}>
                  <TableCell sx={{ minWidth: 150 }}>{format(new Date(log.timestamp), 'MMM dd, yyyy HH:mm:ss')}</TableCell>
                  <TableCell>
                    {log.user ? (
                      <>
                        <Typography variant="body2">{log.user.firstName} {log.user.lastName}</Typography>
                        <Typography variant="caption" color="text.secondary">{log.user.email}</Typography>
                      </>
                    ) : (
                      <Typography variant="body2" color="text.secondary">System</Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip label={log.action} size="small" color={getActionColor(log.action) as any} variant="outlined" sx={{ borderRadius: 1 }} />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>{log.entityType} <Typography component="span" variant="caption" color="text.secondary">({log.entityId})</Typography></Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', maxWidth: 300, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {log.details}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{log.ipAddress || 'unknown'}</Typography>
                  </TableCell>
                </TableRow>
              ))}
              {logs.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 3 }}>No audit logs found.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
};

export default AuditLogs;
