import React, { useEffect } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { useThemeStore } from '@/store/themeStore'
import { useNotificationStore } from '@/store/notificationStore'

// Layout Components
import AuthLayout from '@/components/layout/AuthLayout'
import DashboardLayout from '@/components/layout/DashboardLayout'
import LoadingScreen from '@/components/ui/LoadingScreen'

// Page Components
import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'
import DashboardPage from '@/pages/dashboard/DashboardPage'
import PaymentsPage from '@/pages/payments/PaymentsPage'
import PaymentDetailsPage from '@/pages/payments/PaymentDetailsPage'
import CreatePaymentPage from '@/pages/payments/CreatePaymentPage'
import AccountsPage from '@/pages/accounts/AccountsPage'
import AccountDetailsPage from '@/pages/accounts/AccountDetailsPage'
import TransactionsPage from '@/pages/transactions/TransactionsPage'
import PaymentMethodsPage from '@/pages/payment-methods/PaymentMethodsPage'
import AnalyticsPage from '@/pages/analytics/AnalyticsPage'
import SettingsPage from '@/pages/settings/SettingsPage'
import ProfilePage from '@/pages/profile/ProfilePage'
import NotFoundPage from '@/pages/NotFoundPage'

// Hook for authentication check
import { useAuthCheck } from '@/hooks/useAuthCheck'

function App() {
  const { isAuthenticated, isLoading: authLoading, user } = useAuthStore()
  const { theme, setTheme } = useThemeStore()
  const { initializeNotifications } = useNotificationStore()
  
  // Check authentication status on app startup
  useAuthCheck()

  // Initialize theme
  useEffect(() => {
    if (theme === 'system') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      const updateTheme = () => {
        document.documentElement.classList.toggle('dark', mediaQuery.matches)
      }
      
      updateTheme()
      mediaQuery.addEventListener('change', updateTheme)
      
      return () => mediaQuery.removeEventListener('change', updateTheme)
    } else {
      document.documentElement.classList.toggle('dark', theme === 'dark')
    }
  }, [theme])

  // Initialize notifications for authenticated users
  useEffect(() => {
    if (isAuthenticated && user) {
      initializeNotifications()
    }
  }, [isAuthenticated, user, initializeNotifications])

  // Show loading screen while checking authentication
  if (authLoading) {
    return <LoadingScreen />
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      <Routes>
        {/* Public Routes */}
        <Route path="/auth/*" element={
          !isAuthenticated ? (
            <AuthLayout>
              <Routes>
                <Route path="login" element={<LoginPage />} />
                <Route path="register" element={<RegisterPage />} />
                <Route path="*" element={<Navigate to="/auth/login" replace />} />
              </Routes>
            </AuthLayout>
          ) : (
            <Navigate to="/dashboard" replace />
          )
        } />

        {/* Protected Routes */}
        <Route path="/*" element={
          isAuthenticated ? (
            <DashboardLayout>
              <Routes>
                {/* Dashboard */}
                <Route path="dashboard" element={<DashboardPage />} />
                
                {/* Payments */}
                <Route path="payments" element={<PaymentsPage />} />
                <Route path="payments/create" element={<CreatePaymentPage />} />
                <Route path="payments/:id" element={<PaymentDetailsPage />} />
                
                {/* Accounts */}
                <Route path="accounts" element={<AccountsPage />} />
                <Route path="accounts/:id" element={<AccountDetailsPage />} />
                
                {/* Transactions */}
                <Route path="transactions" element={<TransactionsPage />} />
                
                {/* Payment Methods */}
                <Route path="payment-methods" element={<PaymentMethodsPage />} />
                
                {/* Analytics */}
                <Route path="analytics" element={<AnalyticsPage />} />
                
                {/* Settings & Profile */}
                <Route path="settings" element={<SettingsPage />} />
                <Route path="profile" element={<ProfilePage />} />
                
                {/* Admin Routes (Role-based access will be handled in components) */}
                <Route path="admin/*" element={
                  user?.role === 'ADMIN' ? (
                    <Routes>
                      <Route path="users" element={<div>Users Management</div>} />
                      <Route path="system" element={<div>System Settings</div>} />
                      <Route path="audit" element={<div>Audit Logs</div>} />
                    </Routes>
                  ) : (
                    <Navigate to="/dashboard" replace />
                  )
                } />
                
                {/* Default redirects */}
                <Route path="" element={<Navigate to="/dashboard" replace />} />
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </DashboardLayout>
          ) : (
            <Navigate to="/auth/login" replace />
          )
        } />
      </Routes>
    </div>
  )
}

export default App