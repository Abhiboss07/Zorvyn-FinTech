import { Box, Card, CardContent, Typography } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import PeopleOutlineIcon from '@mui/icons-material/PeopleOutline';
import AssignmentIcon from '@mui/icons-material/Assignment';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';

interface SummaryCardsProps {
  data: {
    totalTransactions: number;
    totalVolume: number;
    pendingTransactions: number;
    activeUsers: number;
  };
}

const SummaryCards = ({ data }: SummaryCardsProps) => {
  const cards = [
    {
      title: 'Total Volume',
      value: `$${data.totalVolume.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      icon: <TrendingUpIcon sx={{ color: 'success.main', fontSize: 32 }} />,
      subtitle: 'Processed across all time',
    },
    {
      title: 'Active Users',
      value: data.activeUsers.toLocaleString(),
      icon: <PeopleOutlineIcon sx={{ color: 'primary.main', fontSize: 32 }} />,
      subtitle: 'Currently active accounts',
    },
    {
      title: 'Total Transactions',
      value: data.totalTransactions.toLocaleString(),
      icon: <AccountBalanceWalletIcon sx={{ color: 'info.main', fontSize: 32 }} />,
      subtitle: 'Platform-wide volume count',
    },
    {
      title: 'Pending Transactions',
      value: data.pendingTransactions.toLocaleString(),
      icon: <AssignmentIcon sx={{ color: 'warning.main', fontSize: 32 }} />,
      subtitle: 'Awaiting admin approval',
    },
  ];

  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: 'repeat(4, 1fr)' }, gap: 3, mb: 4 }}>
      {cards.map((card, index) => (
        <Box key={index}>
          <Card elevation={0} sx={{ height: '100%', borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                <Typography color="text.secondary" variant="subtitle2" fontWeight="600">
                  {card.title}
                </Typography>
                {card.icon}
              </Box>
              <Typography variant="h4" fontWeight="700" sx={{ mb: 1 }}>
                {card.value}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {card.subtitle}
              </Typography>
            </CardContent>
          </Card>
        </Box>
      ))}
    </Box>
  );
};

export default SummaryCards;
