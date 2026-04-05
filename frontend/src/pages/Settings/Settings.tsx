import { useState } from 'react';
import { Box, Typography, Card, CardContent, Switch, FormGroup, FormControlLabel, Button, TextField } from '@mui/material';
import { useAuthStore } from '../../store/authStore';
import { toast } from 'react-toastify';

const Settings = () => {
  const user = useAuthStore((state: any) => state.user);
  const [saving, setSaving] = useState(false);

  // Form states
  const [email] = useState(user?.email || '');
  const [firstName, setFirstName] = useState(user?.firstName || '');
  const [lastName, setLastName] = useState(user?.lastName || '');
  const [twoFaEnabled, setTwoFaEnabled] = useState(user?.twoFaEnabled || false);

  const handleSave = async () => {
    setSaving(true);
    // Simulate API call for settings update
    setTimeout(() => {
      toast.success('Settings updated successfully');
      setSaving(false);
    }, 1000);
  };

  return (
    <Box sx={{ maxWidth: 800 }}>
      <Typography variant="h5" fontWeight="600" sx={{ mb: 4 }}>Account Settings</Typography>

      <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3, mb: 4 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h6" fontWeight="600" sx={{ mb: 3 }}>Profile Information</Typography>
          
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            <TextField 
              label="Email Address" 
              value={email} 
              disabled 
              fullWidth 
              helperText="Contact admin to change your email address."
            />
            
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField 
                label="First Name" 
                value={firstName} 
                onChange={(e) => setFirstName(e.target.value)} 
                fullWidth 
              />
              <TextField 
                label="Last Name" 
                value={lastName} 
                onChange={(e) => setLastName(e.target.value)} 
                fullWidth 
              />
            </Box>
          </Box>
        </CardContent>
      </Card>

      <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3, mb: 4 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h6" fontWeight="600" sx={{ mb: 1 }}>Security Preferences</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Manage your account security and authentication methods.
          </Typography>
          
          <FormGroup>
            <FormControlLabel 
              control={<Switch checked={twoFaEnabled} onChange={(e) => setTwoFaEnabled(e.target.checked)} color="primary" />} 
              label={
                <Box>
                  <Typography fontWeight="500">Two-Factor Authentication (2FA)</Typography>
                  <Typography variant="caption" color="text.secondary">Require an Authenticator code when logging in.</Typography>
                </Box>
              } 
              sx={{ p: 2, border: '1px solid', borderColor: 'divider', borderRadius: 2, ml: 0 }}
            />
          </FormGroup>
        </CardContent>
      </Card>

      <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Button variant="contained" onClick={handleSave} disabled={saving} size="large" sx={{ px: 4 }}>
          {saving ? 'Saving...' : 'Save Changes'}
        </Button>
      </Box>
    </Box>
  );
};

export default Settings;
