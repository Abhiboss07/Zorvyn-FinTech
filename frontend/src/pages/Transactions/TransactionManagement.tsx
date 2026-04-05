import { useState, useEffect } from 'react';
import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, IconButton, CircularProgress, Alert, Tooltip, Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import api from '../../utils/api';
import { format } from 'date-fns';
import { toast } from 'react-toastify';

interface Transaction {
  id: string;
  referenceNumber: string;
  type: string;
  amount: number;
  status: string;
  createdAt: string;
  user: { firstName: string, lastName: string, email: string };
}

const TransactionManagement = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [rejectDialog, setRejectDialog] = useState({ open: false, txId: '' });
  const [rejectReason, setRejectReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchTransactions = async () => {
    try {
      const response = await api.get('/transactions');
      setTransactions(response.data.data.content || response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load transactions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleApprove = async (id: string) => {
    try {
      setActionLoading(true);
      await api.put(`/transactions/${id}/approve`);
      toast.success('Transaction approved successfully');
      fetchTransactions();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to approve transaction');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    try {
      setActionLoading(true);
      await api.put(`/transactions/${rejectDialog.txId}/reject`, null, { params: { reason: rejectReason }});
      toast.success('Transaction rejected successfully');
      setRejectDialog({ open: false, txId: '' });
      setRejectReason('');
      fetchTransactions();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to reject transaction');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}><CircularProgress /></Box>;
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed': return 'success';
      case 'rejected': return 'error';
      case 'pending': return 'warning';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="600">Transaction Management</Typography>
      </Box>

      {error ? (
        <Alert severity="error">{error}</Alert>
      ) : (
        <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3 }}>
          <Table>
            <TableHead sx={{ bgcolor: 'background.default' }}>
              <TableRow>
                <TableCell>Reference</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>User</TableCell>
                <TableCell>Type</TableCell>
                <TableCell align="right">Amount</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((tx) => (
                <TableRow key={tx.id}>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>{tx.referenceNumber}</Typography>
                  </TableCell>
                  <TableCell>{format(new Date(tx.createdAt), 'MMM dd, yyyy HH:mm')}</TableCell>
                  <TableCell>
                    <Typography variant="body2">{tx.user?.firstName} {tx.user?.lastName}</Typography>
                    <Typography variant="caption" color="text.secondary">{tx.user?.email}</Typography>
                  </TableCell>
                  <TableCell sx={{ textTransform: 'capitalize' }}>{tx.type}</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 600 }}>${tx.amount.toLocaleString(undefined, {minimumFractionDigits:2})}</TableCell>
                  <TableCell>
                    <Chip label={tx.status} size="small" color={getStatusColor(tx.status)} sx={{ textTransform: 'capitalize', borderRadius: 1 }} />
                  </TableCell>
                  <TableCell align="right">
                    {tx.status.toLowerCase() === 'pending' && (
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Tooltip title="Approve">
                          <IconButton size="small" color="success" onClick={() => handleApprove(tx.id)} disabled={actionLoading}>
                            <CheckCircleIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Reject">
                          <IconButton size="small" color="error" onClick={() => setRejectDialog({ open: true, txId: tx.id })} disabled={actionLoading}>
                            <CancelIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {transactions.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>No transactions found.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Reject Dialog */}
      <Dialog open={rejectDialog.open} onClose={() => setRejectDialog({ open: false, txId: '' })} fullWidth maxWidth="sm">
        <DialogTitle>Reject Transaction</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 2 }}>Please provide a reason for rejecting this transaction.</Typography>
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Rejection Reason"
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            disabled={actionLoading}
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={() => setRejectDialog({ open: false, txId: '' })} disabled={actionLoading}>Cancel</Button>
          <Button onClick={handleReject} variant="contained" color="error" disabled={actionLoading || !rejectReason.trim()}>
            {actionLoading ? 'Rejecting...' : 'Reject Transaction'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TransactionManagement;
