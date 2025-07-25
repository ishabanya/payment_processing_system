import { useEffect } from 'react'
import { useAuthStore } from '@/store/authStore'
import { authService } from '@/services/authService'

export const useAuthCheck = () => {
  const { isAuthenticated, accessToken, refreshAccessToken, logout, setLoading } = useAuthStore()

  useEffect(() => {
    const checkAuthStatus = async () => {
      setLoading(true)
      
      try {
        // If we have tokens but are not authenticated, try to validate them
        const storedAccessToken = localStorage.getItem('accessToken')
        const storedRefreshToken = localStorage.getItem('refreshToken')
        
        if (storedAccessToken && storedRefreshToken && !isAuthenticated) {
          // Try to validate the current token
          try {
            const response = await authService.validateToken()
            if (response.success) {
              // Token is valid, get user profile
              const profileResponse = await authService.getProfile()
              if (profileResponse.success && profileResponse.data) {
                useAuthStore.setState({
                  user: profileResponse.data,
                  accessToken: storedAccessToken,
                  refreshToken: storedRefreshToken,
                  isAuthenticated: true,
                  error: null,
                })
              }
            }
          } catch (error) {
            // Token validation failed, try to refresh
            console.log('Token validation failed, attempting refresh...')
            const refreshSuccessful = await refreshAccessToken()
            if (!refreshSuccessful) {
              // Refresh also failed, logout
              await logout()
            }
          }
        } else if (!storedAccessToken && !storedRefreshToken) {
          // No tokens stored, ensure we're logged out
          useAuthStore.setState({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            error: null,
          })
        }
      } catch (error) {
        console.error('Auth check failed:', error)
        // On any error, clear authentication state
        await logout()
      } finally {
        setLoading(false)
      }
    }

    checkAuthStatus()
  }, [])

  // Set up token refresh interval
  useEffect(() => {
    if (!isAuthenticated || !accessToken) return

    // Refresh token every 30 minutes
    const refreshInterval = setInterval(async () => {
      try {
        await refreshAccessToken()
      } catch (error) {
        console.error('Automatic token refresh failed:', error)
        // Don't logout on refresh failure, let the user continue
        // The token will be refreshed on the next API call
      }
    }, 30 * 60 * 1000) // 30 minutes

    return () => clearInterval(refreshInterval)
  }, [isAuthenticated, accessToken, refreshAccessToken])

  // Handle page visibility change to refresh token when page becomes visible
  useEffect(() => {
    if (!isAuthenticated) return

    const handleVisibilityChange = async () => {
      if (document.visibilityState === 'visible') {
        try {
          // Check if token is still valid when page becomes visible
          await authService.validateToken()
        } catch (error) {
          // Token is no longer valid, try to refresh
          const refreshSuccessful = await refreshAccessToken()
          if (!refreshSuccessful) {
            console.warn('Token refresh failed on page visibility change')
          }
        }
      }
    }

    document.addEventListener('visibilitychange', handleVisibilityChange)
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange)
  }, [isAuthenticated, refreshAccessToken])

  return {
    isAuthenticated,
    isLoading: useAuthStore(state => state.isLoading),
    user: useAuthStore(state => state.user),
    error: useAuthStore(state => state.error),
  }
}