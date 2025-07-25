import { create } from 'zustand'
import { Notification, NotificationType } from '@/types'
import toast from 'react-hot-toast'

interface NotificationState {
  // State
  notifications: Notification[]
  unreadCount: number
  isConnected: boolean
  
  // Actions
  addNotification: (notification: Omit<Notification, 'id' | 'timestamp' | 'isRead'>) => void
  markAsRead: (id: string) => void
  markAllAsRead: () => void
  removeNotification: (id: string) => void
  clearAll: () => void
  initializeNotifications: () => void
  connectWebSocket: () => void
  disconnectWebSocket: () => void
}

let wsConnection: WebSocket | null = null

export const useNotificationStore = create<NotificationState>((set, get) => ({
  // Initial state
  notifications: [],
  unreadCount: 0,
  isConnected: false,

  // Add new notification
  addNotification: (notificationData) => {
    const notification: Notification = {
      ...notificationData,
      id: `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      timestamp: new Date().toISOString(),
      isRead: false,
    }

    set((state) => ({
      notifications: [notification, ...state.notifications],
      unreadCount: state.unreadCount + 1,
    }))

    // Show toast notification based on severity
    const toastOptions = {
      duration: getSeverityDuration(notification.severity),
      position: 'top-right' as const,
    }

    switch (notification.severity) {
      case 'success':
        toast.success(notification.message, toastOptions)
        break
      case 'warning':
        toast(notification.message, {
          ...toastOptions,
          icon: '⚠️',
        })
        break
      case 'error':
        toast.error(notification.message, toastOptions)
        break
      default:
        toast(notification.message, toastOptions)
    }
  },

  // Mark notification as read
  markAsRead: (id: string) => {
    set((state) => ({
      notifications: state.notifications.map((notification) =>
        notification.id === id ? { ...notification, isRead: true } : notification
      ),
      unreadCount: Math.max(0, state.unreadCount - 1),
    }))
  },

  // Mark all notifications as read
  markAllAsRead: () => {
    set((state) => ({
      notifications: state.notifications.map((notification) => ({
        ...notification,
        isRead: true,
      })),
      unreadCount: 0,
    }))
  },

  // Remove notification
  removeNotification: (id: string) => {
    set((state) => {
      const notification = state.notifications.find((n) => n.id === id)
      const wasUnread = notification && !notification.isRead
      
      return {
        notifications: state.notifications.filter((n) => n.id !== id),
        unreadCount: wasUnread ? Math.max(0, state.unreadCount - 1) : state.unreadCount,
      }
    })
  },

  // Clear all notifications
  clearAll: () => {
    set({
      notifications: [],
      unreadCount: 0,
    })
  },

  // Initialize notifications system
  initializeNotifications: () => {
    const { connectWebSocket } = get()
    connectWebSocket()
  },

  // Connect to WebSocket for real-time notifications
  connectWebSocket: () => {
    if (wsConnection?.readyState === WebSocket.OPEN) {
      return // Already connected
    }

    const wsUrl = getWebSocketUrl()
    const token = localStorage.getItem('accessToken')

    if (!token) {
      console.warn('No access token available for WebSocket connection')
      return
    }

    try {
      wsConnection = new WebSocket(`${wsUrl}?token=${token}`)

      wsConnection.onopen = () => {
        console.log('WebSocket connected for notifications')
        set({ isConnected: true })
      }

      wsConnection.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          handleWebSocketMessage(data, get().addNotification)
        } catch (error) {
          console.error('Error parsing WebSocket message:', error)
        }
      }

      wsConnection.onclose = () => {
        console.log('WebSocket disconnected')
        set({ isConnected: false })
        
        // Attempt to reconnect after 5 seconds
        setTimeout(() => {
          const { connectWebSocket } = get()
          connectWebSocket()
        }, 5000)
      }

      wsConnection.onerror = (error) => {
        console.error('WebSocket error:', error)
        set({ isConnected: false })
      }
    } catch (error) {
      console.error('Failed to connect to WebSocket:', error)
      set({ isConnected: false })
    }
  },

  // Disconnect WebSocket
  disconnectWebSocket: () => {
    if (wsConnection) {
      wsConnection.close()
      wsConnection = null
    }
    set({ isConnected: false })
  },
}))

// Helper functions
function getSeverityDuration(severity: Notification['severity']): number {
  switch (severity) {
    case 'error':
      return 8000 // 8 seconds for errors
    case 'warning':
      return 6000 // 6 seconds for warnings
    case 'success':
      return 4000 // 4 seconds for success
    default:
      return 5000 // 5 seconds for info
  }
}

function getWebSocketUrl(): string {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = import.meta.env.VITE_WS_HOST || window.location.host
  return `${protocol}//${host}/ws/notifications`
}

function handleWebSocketMessage(
  data: any,
  addNotification: (notification: Omit<Notification, 'id' | 'timestamp' | 'isRead'>) => void
) {
  switch (data.type) {
    case 'PAYMENT_COMPLETED':
      addNotification({
        type: 'payment_completed',
        title: 'Payment Completed',
        message: `Payment of ${data.payload.formattedAmount} has been completed successfully.`,
        severity: 'success',
        actionUrl: `/payments/${data.payload.paymentId}`,
        actionLabel: 'View Payment',
        metadata: data.payload,
      })
      break

    case 'PAYMENT_FAILED':
      addNotification({
        type: 'payment_failed',
        title: 'Payment Failed',
        message: `Payment of ${data.payload.formattedAmount} has failed. ${data.payload.reason || ''}`,
        severity: 'error',
        actionUrl: `/payments/${data.payload.paymentId}`,
        actionLabel: 'View Payment',
        metadata: data.payload,
      })
      break

    case 'REFUND_PROCESSED':
      addNotification({
        type: 'refund_processed',
        title: 'Refund Processed',
        message: `Refund of ${data.payload.formattedAmount} has been processed.`,
        severity: 'info',
        actionUrl: `/payments/${data.payload.paymentId}`,
        actionLabel: 'View Transaction',
        metadata: data.payload,
      })
      break

    case 'ACCOUNT_UPDATED':
      addNotification({
        type: 'account_updated',
        title: 'Account Updated',
        message: data.payload.message || 'Your account has been updated.',
        severity: 'info',
        actionUrl: '/profile',
        actionLabel: 'View Profile',
        metadata: data.payload,
      })
      break

    case 'SECURITY_ALERT':
      addNotification({
        type: 'security_alert',
        title: 'Security Alert',
        message: data.payload.message || 'A security event has been detected on your account.',
        severity: 'warning',
        actionUrl: '/settings/security',
        actionLabel: 'Review Security',
        metadata: data.payload,
      })
      break

    case 'SYSTEM_MAINTENANCE':
      addNotification({
        type: 'system_maintenance',
        title: 'System Maintenance',
        message: data.payload.message || 'System maintenance is scheduled.',
        severity: 'info',
        metadata: data.payload,
      })
      break

    default:
      console.warn('Unknown notification type:', data.type)
  }
}

// Notification utility functions for components
export const notificationHelpers = {
  // Create payment status notification
  paymentStatus: (paymentId: string, status: string, amount: string) => {
    const store = useNotificationStore.getState()
    
    const severityMap: Record<string, Notification['severity']> = {
      COMPLETED: 'success',
      FAILED: 'error',
      CANCELLED: 'warning',
      REFUNDED: 'info',
    }

    store.addNotification({
      type: 'payment_completed',
      title: `Payment ${status}`,
      message: `Payment of ${amount} has been ${status.toLowerCase()}.`,
      severity: severityMap[status] || 'info',
      actionUrl: `/payments/${paymentId}`,
      actionLabel: 'View Payment',
    })
  },

  // Create system notification
  system: (message: string, severity: Notification['severity'] = 'info') => {
    const store = useNotificationStore.getState()
    store.addNotification({
      type: 'system_maintenance',
      title: 'System Notification',
      message,
      severity,
    })
  },

  // Create security alert
  security: (message: string) => {
    const store = useNotificationStore.getState()
    store.addNotification({
      type: 'security_alert',
      title: 'Security Alert',
      message,
      severity: 'warning',
      actionUrl: '/settings/security',
      actionLabel: 'Review Security',
    })
  },
}