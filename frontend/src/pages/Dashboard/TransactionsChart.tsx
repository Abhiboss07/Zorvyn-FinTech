import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface ChartProps {
  data: Record<string, { transactionCount: number; volume: number }>;
}

const TransactionsChart = ({ data }: ChartProps) => {
  const theme = useTheme();

  // Transform data for recharts
  const chartData = Object.entries(data).map(([date, stats]) => ({
    name: new Date(date).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
    volume: stats.volume,
    count: stats.transactionCount,
  }));

  return (
    <Card elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', height: '100%' }}>
      <CardContent>
        <Typography variant="h6" fontWeight="600" sx={{ mb: 3 }}>
          Transaction Volume Trends
        </Typography>
        <Box sx={{ width: '100%', height: 350 }}>
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="colorVolume" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={theme.palette.primary.main} stopOpacity={0.3} />
                  <stop offset="95%" stopColor={theme.palette.primary.main} stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="name" stroke={theme.palette.text.secondary} />
              <YAxis stroke={theme.palette.text.secondary} tickFormatter={(value) => `$${value}`} />
              <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} vertical={false} />
              <Tooltip
                contentStyle={{
                  backgroundColor: theme.palette.background.paper,
                  border: `1px solid ${theme.palette.divider}`,
                  borderRadius: 8,
                }}
                itemStyle={{ color: theme.palette.text.primary }}
              />
              <Area
                type="monotone"
                dataKey="volume"
                stroke={theme.palette.primary.main}
                strokeWidth={3}
                fillOpacity={1}
                fill="url(#colorVolume)"
              />
            </AreaChart>
          </ResponsiveContainer>
        </Box>
      </CardContent>
    </Card>
  );
};

export default TransactionsChart;
