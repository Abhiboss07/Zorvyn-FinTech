import { useEffect, useState } from 'react';
import { Box, Typography, CircularProgress, Alert } from '@mui/material';
import SummaryCards from './SummaryCards';
import TransactionsChart from './TransactionsChart';
import api from '../../utils/api';

interface AnalyticsData {
  totalTransactions: number;
  totalVolume: number;
  pendingTransactions: number;
  completedTransactions: number;
  rejectedTransactions: number;
  activeUsers: number;
  activeAccounts: number;
  totalBalance: number;
  byType: Record<string, { count: number; volume: number }>;
  byPeriod: Record<string, { transactionCount: number; volume: number }>;
}

const Dashboard = () => {
  const [data, setData] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const response = await api.get('/analytics/summary');
        setData(response.data.data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !data) {
    return (
      <Box sx={{ p: 2 }}>
        <Alert severity="error">{error || 'No data found'}</Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" fontWeight="600" sx={{ mb: 4 }}>
        Dashboard Overview
      </Typography>
      
      <SummaryCards data={data} />
      
      <Box sx={{ mt: 4 }}>
        <TransactionsChart data={data.byPeriod} />
      </Box>
    </Box>
  );
};

export default Dashboard;
