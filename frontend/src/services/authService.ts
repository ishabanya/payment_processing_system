import { apiClient } from './api'
import { AuthResponse, LoginRequest, RegisterRequest, ApiResponse, User } from '@/types'

export const authService = {
  // Login user
  login: async (credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    return apiClient.post<AuthResponse>('/auth/login', credentials)
  },

  // Register new user
  register: async (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    return apiClient.post<AuthResponse>('/auth/register', data)
  },

  // Refresh access token
  refreshToken: async (refreshToken: string): Promise<ApiResponse<AuthResponse>> => {
    return apiClient.post<AuthResponse>('/auth/refresh', {}, {
      headers: {
        Authorization: `Bearer ${refreshToken}`,
      },
    })
  },

  // Logout user
  logout: async (refreshToken: string): Promise<ApiResponse<void>> => {
    return apiClient.post<void>('/auth/logout', {}, {
      headers: {
        Authorization: `Bearer ${refreshToken}`,
      },
    })
  },

  // Logout from all devices
  logoutAll: async (): Promise<ApiResponse<void>> => {
    return apiClient.post<void>('/auth/logout-all')
  },

  // Validate current token
  validateToken: async (): Promise<ApiResponse<void>> => {
    return apiClient.get<void>('/auth/validate')
  },

  // Request password reset
  forgotPassword: async (email: string): Promise<ApiResponse<void>> => {
    return apiClient.post<void>('/auth/forgot-password', { email })
  },

  // Reset password with token
  resetPassword: async (token: string, newPassword: string): Promise<ApiResponse<void>> => {
    return apiClient.post<void>('/auth/reset-password', {
      token,
      newPassword,
    })
  },

  // Verify email address
  verifyEmail: async (token: string): Promise<ApiResponse<void>> => {
    return apiClient.post<void>('/auth/verify-email', { token })
  },

  // Get current user profile
  getProfile: async (): Promise<ApiResponse<User>> => {
    return apiClient.get<User>('/users/me')
  },

  // Update user profile
  updateProfile: async (data: Partial<User>): Promise<ApiResponse<User>> => {
    return apiClient.put<User>('/users/me', data)
  },

  // Change password
  changePassword: async (currentPassword: string, newPassword: string): Promise<ApiResponse<void>> => {
    return apiClient.post<void>('/users/me/change-password', {
      currentPassword,
      newPassword,
    })
  },
}

export default authService