import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { User, AuthResponse, LoginRequest, RegisterRequest } from '@/types'
import { authService } from '@/services/authService'
import toast from 'react-hot-toast'

interface AuthState {
  // State
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null
  
  // Actions
  login: (credentials: LoginRequest) => Promise<void>
  register: (data: RegisterRequest) => Promise<void>
  logout: () => Promise<void>
  refreshAccessToken: () => Promise<boolean>
  updateUser: (user: Partial<User>) => void
  clearError: () => void
  setLoading: (loading: boolean) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Login action
      login: async (credentials: LoginRequest) => {
        try {
          set({ isLoading: true, error: null })
          
          const response = await authService.login(credentials)
          
          if (response.success && response.data) {
            const authData = response.data
            
            set({
              user: authData.user,
              accessToken: authData.accessToken,
              refreshToken: authData.refreshToken,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            })
            
            // Store tokens in localStorage for persistence
            localStorage.setItem('accessToken', authData.accessToken)
            localStorage.setItem('refreshToken', authData.refreshToken)
            
            toast.success(`Welcome back, ${authData.user.firstName}!`)
          } else {
            throw new Error(response.message || 'Login failed')
          }
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || error.message || 'Login failed'
          
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          })
          
          // Clear stored tokens on error
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          
          toast.error(errorMessage)
          throw error
        }
      },

      // Register action
      register: async (data: RegisterRequest) => {
        try {
          set({ isLoading: true, error: null })
          
          const response = await authService.register(data)
          
          if (response.success && response.data) {
            const authData = response.data
            
            set({
              user: authData.user,
              accessToken: authData.accessToken,
              refreshToken: authData.refreshToken,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            })
            
            // Store tokens in localStorage for persistence
            localStorage.setItem('accessToken', authData.accessToken)
            localStorage.setItem('refreshToken', authData.refreshToken)
            
            toast.success(`Welcome to Payment System, ${authData.user.firstName}!`)
          } else {
            throw new Error(response.message || 'Registration failed')
          }
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || error.message || 'Registration failed'
          
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          })
          
          // Clear stored tokens on error
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          
          toast.error(errorMessage)
          throw error
        }
      },

      // Logout action
      logout: async () => {
        try {
          const { refreshToken } = get()
          
          // Call logout API if we have a refresh token
          if (refreshToken) {
            await authService.logout(refreshToken)
          }
        } catch (error) {
          // Continue with logout even if API call fails
          console.warn('Logout API call failed:', error)
        } finally {
          // Always clear local state and storage
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
          })
          
          // Clear stored tokens
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          
          toast.success('Logged out successfully')
        }
      },

      // Refresh access token
      refreshAccessToken: async (): Promise<boolean> => {
        try {
          const { refreshToken } = get()
          
          if (!refreshToken) {
            throw new Error('No refresh token available')
          }
          
          const response = await authService.refreshToken(refreshToken)
          
          if (response.success && response.data) {
            const authData = response.data
            
            set({
              user: authData.user,
              accessToken: authData.accessToken,
              refreshToken: authData.refreshToken,
              isAuthenticated: true,
              error: null,
            })
            
            // Update stored tokens
            localStorage.setItem('accessToken', authData.accessToken)
            localStorage.setItem('refreshToken', authData.refreshToken)
            
            return true
          } else {
            throw new Error(response.message || 'Token refresh failed')
          }
        } catch (error: any) {
          console.error('Token refresh failed:', error)
          
          // Clear authentication state on refresh failure
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            error: 'Session expired. Please log in again.',
          })
          
          // Clear stored tokens
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          
          return false
        }
      },

      // Update user information
      updateUser: (userData: Partial<User>) => {
        const { user } = get()
        if (user) {
          set({
            user: { ...user, ...userData },
          })
        }
      },

      // Clear error
      clearError: () => {
        set({ error: null })
      },

      // Set loading state
      setLoading: (loading: boolean) => {
        set({ isLoading: loading })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        // Validate stored tokens on rehydration
        if (state?.accessToken && state?.refreshToken) {
          // Check if tokens are still valid (you might want to add expiration check)
          const storedAccessToken = localStorage.getItem('accessToken')
          const storedRefreshToken = localStorage.getItem('refreshToken')
          
          if (storedAccessToken !== state.accessToken || storedRefreshToken !== state.refreshToken) {
            // Tokens don't match, clear authentication
            state.user = null
            state.accessToken = null
            state.refreshToken = null
            state.isAuthenticated = false
          }
        }
      },
    }
  )
)