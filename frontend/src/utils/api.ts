import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- MOCK BACKEND DATA STATE ---
const mockState = {
  transactions: [
    { id: '1', referenceNumber: 'TX-1001', type: 'deposit', amount: 5000.0, status: 'pending', createdAt: new Date().toISOString(), user: { firstName: 'Alice', lastName: 'Smith', email: 'alice@example.com' } },
    { id: '2', referenceNumber: 'TX-1002', type: 'withdrawal', amount: 150.5, status: 'completed', createdAt: new Date(Date.now() - 86400000).toISOString(), user: { firstName: 'Bob', lastName: 'Jones', email: 'bob@example.com' } },
    { id: '3', referenceNumber: 'TX-1003', type: 'transfer', amount: 1200.0, status: 'pending', createdAt: new Date(Date.now() - 172800000).toISOString(), user: { firstName: 'Charlie', lastName: 'Brown', email: 'charlie@example.com' } },
  ],
  users: [
    { id: 'u1', email: 'admin@zorvyn.com', firstName: 'Admin', lastName: 'User', role: { name: 'ADMIN' }, isActive: true, twoFaEnabled: true, createdAt: '2023-01-15T00:00:00Z' },
    { id: 'u2', email: 'alice@example.com', firstName: 'Alice', lastName: 'Smith', role: { name: 'USER' }, isActive: true, twoFaEnabled: false, createdAt: '2023-06-20T00:00:00Z' },
    { id: 'u3', email: 'bob@example.com', firstName: 'Bob', lastName: 'Jones', role: { name: 'MANAGER' }, isActive: false, twoFaEnabled: true, createdAt: '2023-11-05T00:00:00Z' },
  ],
  auditLogs: [
    { id: 'al1', action: 'LOGIN_SUCCESS', entityType: 'User', entityId: 'u1', details: 'User logged in successfully', ipAddress: '192.168.1.100', timestamp: new Date().toISOString(), user: { firstName: 'Admin', lastName: 'User', email: 'admin@zorvyn.com' } },
    { id: 'al2', action: 'CREATE_TRANSACTION', entityType: 'Transaction', entityId: 'TX-1001', details: 'Added new deposit', ipAddress: '192.168.1.105', timestamp: new Date().toISOString(), user: { firstName: 'Alice', lastName: 'Smith', email: 'alice@example.com' } },
    { id: 'al3', action: 'LOGIN_FAIL', entityType: 'User', entityId: 'u3', details: 'Invalid credentials', ipAddress: '10.0.0.45', timestamp: new Date(Date.now() - 3600000).toISOString(), user: { firstName: 'Bob', lastName: 'Jones', email: 'bob@example.com' } },
  ]
};

// --- AXIOS MOCK INTERCEPTOR ---
// MOCK INTERCEPTOR DISABLED

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
