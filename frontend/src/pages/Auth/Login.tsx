import { useState } from 'react';
import { Box, Button, TextField, Typography, Container, Paper, Dialog, DialogTitle, DialogContent, DialogActions, CircularProgress } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import api from '../../utils/api';
import { toast } from 'react-toastify';

const loginSchema = yup.object({
  email: yup.string().email('Invalid email').required('Email is required'),
  password: yup.string().required('Password is required'),
});

const twoFaSchema = yup.object({
  code: yup.string().length(6, 'Code must be 6 digits').required('Code is required'),
});

interface LoginFormData {
  email: string;
  password: string;
}

interface TwoFaFormData {
  code: string;
}

const Login = () => {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [show2FA, setShow2FA] = useState(false);
  const [tempToken, setTempToken] = useState('');
  const [loading, setLoading] = useState(false);

  const { control: loginControl, handleSubmit: handleLoginSubmit, formState: { errors: loginErrors } } = useForm<LoginFormData>({
    resolver: yupResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  const { control: twoFaControl, handleSubmit: handle2FASubmit, formState: { errors: twoFaErrors } } = useForm<TwoFaFormData>({
    resolver: yupResolver(twoFaSchema),
    defaultValues: { code: '' },
  });

  const onLogin = async (data: LoginFormData) => {
    try {
      setLoading(true);
      const response = await api.post('/auth/login', data);

      if (response.data.requires2FA) {
        setTempToken(response.data.tempToken);
        setShow2FA(true);
      } else {
        setAuth(response.data.user, response.data.token);
        toast.success('Logged in successfully');
        navigate('/');
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Login failed');

      // Dev fallback when backend isn't running
      if (!error.response && import.meta.env.DEV) {
        console.warn('Dev fallback login triggered');
        setAuth(
          { id: 1, email: data.email, firstName: 'Admin', lastName: 'User', role: 'ROLE_ADMIN', twoFactorEnabled: false },
          'dev-token-123',
        );
        navigate('/');
      }
    } finally {
      setLoading(false);
    }
  };

  const onVerify2FA = async (data: TwoFaFormData) => {
    try {
      setLoading(true);
      const response = await api.post('/auth/verify-2fa', { tempToken, code: data.code });
      setAuth(response.data.user, response.data.token);
      toast.success('Verified successfully');
      setShow2FA(false);
      navigate('/');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Invalid 2FA code');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'background.default' }}>
      <Container maxWidth="sm">
        <Paper elevation={0} sx={{ p: 5, borderRadius: 4, border: '1px solid', borderColor: 'divider' }}>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography variant="h4" color="primary" fontWeight="700">Zorvyn</Typography>
            <Typography variant="h6" color="text.secondary">Sign in to your account</Typography>
          </Box>

          <form onSubmit={handleLoginSubmit(onLogin)}>
            <Controller
              name="email"
              control={loginControl}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Email Address"
                  variant="outlined"
                  margin="normal"
                  error={!!loginErrors.email}
                  helperText={loginErrors.email?.message}
                  autoComplete="email"
                />
              )}
            />
            <Controller
              name="password"
              control={loginControl}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Password"
                  type="password"
                  variant="outlined"
                  margin="normal"
                  error={!!loginErrors.password}
                  helperText={loginErrors.password?.message}
                  autoComplete="current-password"
                />
              )}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{ mt: 3, mb: 2, py: 1.5 }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
            </Button>
          </form>
        </Paper>
      </Container>

      {/* 2FA Dialog */}
      <Dialog open={show2FA} onClose={() => !loading && setShow2FA(false)}>
        <DialogTitle>Two-Factor Authentication</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 2 }}>Please enter the 6-digit code from your authenticator app.</Typography>
          <form id="2fa-form" onSubmit={handle2FASubmit(onVerify2FA)}>
            <Controller
              name="code"
              control={twoFaControl}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Authentication Code"
                  variant="outlined"
                  error={!!twoFaErrors.code}
                  helperText={twoFaErrors.code?.message}
                  autoFocus
                />
              )}
            />
          </form>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={() => setShow2FA(false)} disabled={loading}>Cancel</Button>
          <Button type="submit" form="2fa-form" variant="contained" disabled={loading}>
            {loading ? <CircularProgress size={20} color="inherit" /> : 'Verify'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Login;
