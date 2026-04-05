import { Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import ProtectedRoute from './components/auth/ProtectedRoute';
import Layout from './components/layout/Layout';
import Login from './pages/Auth/Login';

// Import newly created pages
import Dashboard from './pages/Dashboard/Dashboard';
import UserManagement from './pages/Users/UserManagement';
import TransactionManagement from './pages/Transactions/TransactionManagement';
import RolesManagement from './pages/Roles/RolesManagement';
import AuditLogs from './pages/Audit/AuditLogs';
import Settings from './pages/Settings/Settings';

function App() {
  return (
    <>
      <ToastContainer
        position="top-right"
        autoClose={3000}
        theme="dark"
        toastStyle={{ backgroundColor: '#1e293b', color: '#f8fafc' }}
      />
      <Routes>
        <Route path="/login" element={<Login />} />

        {/* Protected Dashboard Routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="/users" element={<UserManagement />} />
            <Route path="/transactions" element={<TransactionManagement />} />
            <Route path="/roles" element={<RolesManagement />} />
            <Route path="/audit" element={<AuditLogs />} />
            <Route path="/settings" element={<Settings />} />
          </Route>
        </Route>

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

export default App;
