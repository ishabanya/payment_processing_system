import React from 'react';
import { Outlet } from 'react-router-dom';
import LoadingScreen from '../ui/LoadingScreen';
import { useAuthCheck } from '../../hooks/useAuthCheck';

/**
 * Authentication layout component
 * 
 * Handles the authentication flow and provides a consistent layout
 * for authenticated routes. Includes loading states and error handling.
 * 
 * @author Emma Rodriguez - Frontend Team Lead
 * @since 2024-07-20
 */
const AuthLayout: React.FC = () => {
  const { isLoading, isAuthenticated, error } = useAuthCheck();

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-blue-50 dark:from-slate-900 dark:to-blue-900">
        <div className="text-center p-8 max-w-md mx-auto">
          <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-red-100 dark:bg-red-900/20 flex items-center justify-center">
            <svg className="w-8 h-8 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
            Authentication Error
          </h2>
          <p className="text-gray-600 dark:text-gray-300 mb-4">
            {error}
          </p>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Redirect handled by useAuthCheck hook
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 dark:from-slate-900 dark:to-blue-900">
      <Outlet />
    </div>
  );
};

export default AuthLayout;