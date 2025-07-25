// API Response Types
export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T | null
  error: ErrorResponse | null
  pagination: PaginationInfo | null
  metadata: Record<string, any> | null
}

export interface ErrorResponse {
  errorId: string
  code: string
  message: string
  timestamp: string
  path: string
  details?: Record<string, string>
}

export interface PaginationInfo {
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

// Authentication Types
export interface User {
  id: string
  username: string
  email: string
  firstName: string
  lastName: string
  fullName: string
  role: UserRole
  accountId: string
  isActive: boolean
  lastLogin: string | null
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
  permissions: string[]
  sessionInfo: {
    ipAddress: string
    userAgent: string
    loginTime: string
  }
}

export interface LoginRequest {
  usernameOrEmail: string
  password: string
  rememberMe?: boolean
}

export interface RegisterRequest {
  userInfo: {
    username: string
    email: string
    password: string
    firstName: string
    lastName: string
  }
  accountInfo: {
    accountName: string
    phone?: string
  }
}

export type UserRole = 'ADMIN' | 'MERCHANT' | 'USER'

// Payment Types
export interface Payment {
  id: string
  paymentReference: string
  accountId: string
  paymentMethodId: string | null
  amount: number
  currencyCode: string
  description: string | null
  status: PaymentStatus
  merchantReference: string | null
  metadata: Record<string, any> | null
  riskScore: number | null
  processedAt: string | null
  expiresAt: string | null
  createdAt: string
  updatedAt: string
  
  // Computed fields
  canRefund: boolean
  canCancel: boolean
  isExpired: boolean
  formattedAmount: string
  statusColor: string
  timeAgo: string
  
  // Related data
  account?: Account
  paymentMethod?: PaymentMethod
  transactions?: Transaction[]
}

export interface CreatePaymentRequest {
  accountId: string
  paymentMethodId?: string
  amount: number
  currencyCode: string
  description?: string
  merchantReference?: string
  callbackUrl?: string
  successUrl?: string
  failureUrl?: string
  metadata?: Record<string, any>
  expiresAt?: string
}

export interface RefundPaymentRequest {
  amount?: number
  reason: string
  metadata?: Record<string, any>
}

export type PaymentStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'REFUNDED'

// Account Types
export interface Account {
  id: string
  accountNumber: string
  accountName: string
  email: string
  phone: string | null
  status: AccountStatus
  balance: number
  currencyCode: string
  createdAt: string
  updatedAt: string
  
  // Statistics
  totalPayments: number
  completedPayments: number
  failedPayments: number
  totalVolume: number
  averageTransactionAmount: number
  
  // Related data
  users?: User[]
  paymentMethods?: PaymentMethod[]
  recentPayments?: Payment[]
}

export interface CreateAccountRequest {
  accountName: string
  email: string
  phone?: string
  initialBalance?: number
  currencyCode?: string
}

export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'CLOSED'

// Payment Method Types
export interface PaymentMethod {
  id: string
  accountId: string
  type: PaymentMethodType
  provider: string
  lastFourDigits: string | null
  expiryMonth: number | null
  expiryYear: number | null
  isDefault: boolean
  isActive: boolean
  createdAt: string
  updatedAt: string
  
  // Computed fields
  maskedDetails: string
  isExpired: boolean
  displayName: string
  icon: string
}

export interface CreatePaymentMethodRequest {
  type: PaymentMethodType
  provider: string
  details: {
    cardNumber?: string
    expiryMonth?: number
    expiryYear?: number
    cvv?: string
    holderName?: string
    bankAccount?: string
    routingNumber?: string
    walletEmail?: string
  }
  isDefault?: boolean
}

export type PaymentMethodType = 'CREDIT_CARD' | 'DEBIT_CARD' | 'BANK_TRANSFER' | 'DIGITAL_WALLET' | 'CRYPTO'

// Transaction Types
export interface Transaction {
  id: string
  transactionReference: string
  paymentId: string
  type: TransactionType
  amount: number
  currencyCode: string
  status: PaymentStatus
  gatewayTransactionId: string | null
  gatewayResponse: Record<string, any> | null
  processingFee: number
  processedAt: string | null
  createdAt: string
  updatedAt: string
  
  // Computed fields
  netAmount: number
  isSuccessful: boolean
  isFailed: boolean
  isPending: boolean
  formattedAmount: string
  timeAgo: string
  
  // Related data
  payment?: Payment
}

export type TransactionType = 'PAYMENT' | 'REFUND' | 'CHARGEBACK' | 'ADJUSTMENT'

// Dashboard Types
export interface DashboardStats {
  overview: {
    totalRevenue: number
    totalPayments: number
    successRate: number
    averageTransactionValue: number
    revenueGrowth: number
    paymentGrowth: number
    successRateChange: number
    avgTransactionChange: number
  }
  
  recentActivity: {
    totalTransactions: number
    pendingPayments: number
    failedPayments: number
    refundRequests: number
  }
  
  performance: {
    apiResponseTime: number
    systemUptime: number
    errorRate: number
    throughput: number
  }
  
  trends: {
    dailyRevenue: ChartDataPoint[]
    paymentVolume: ChartDataPoint[]
    statusDistribution: StatusDistribution[]
    topPaymentMethods: PaymentMethodStats[]
  }
}

export interface ChartDataPoint {
  date: string
  value: number
  label?: string
  color?: string
}

export interface StatusDistribution {
  status: PaymentStatus
  count: number
  percentage: number
  color: string
}

export interface PaymentMethodStats {
  type: PaymentMethodType
  count: number
  percentage: number
  volume: number
}

// Analytics Types
export interface PaymentAnalytics {
  summary: {
    totalVolume: number
    totalCount: number
    averageAmount: number
    successRate: number
    refundRate: number
  }
  
  trends: {
    revenue: ChartDataPoint[]
    volume: ChartDataPoint[]
    successRate: ChartDataPoint[]
  }
  
  segmentation: {
    byStatus: StatusDistribution[]
    byMethod: PaymentMethodStats[]
    byAmount: AmountDistribution[]
  }
  
  forecasting: {
    predictedRevenue: ChartDataPoint[]
    trendAnalysis: TrendAnalysis
    seasonalityFactors: SeasonalityFactor[]
  }
  
  anomalies: Anomaly[]
}

export interface AmountDistribution {
  range: string
  count: number
  percentage: number
  totalAmount: number
}

export interface TrendAnalysis {
  direction: 'up' | 'down' | 'stable'
  strength: 'weak' | 'moderate' | 'strong'
  confidence: number
  description: string
}

export interface SeasonalityFactor {
  period: string
  factor: number
  impact: 'high' | 'medium' | 'low'
}

export interface Anomaly {
  id: string
  type: 'volume' | 'amount' | 'failure_rate' | 'response_time'
  severity: 'low' | 'medium' | 'high' | 'critical'
  timestamp: string
  description: string
  actualValue: number
  expectedValue: number
  deviation: number
}

// Notification Types
export interface Notification {
  id: string
  type: NotificationType
  title: string
  message: string
  severity: 'info' | 'success' | 'warning' | 'error'
  timestamp: string
  isRead: boolean
  actionUrl?: string
  actionLabel?: string
  metadata?: Record<string, any>
}

export type NotificationType = 
  | 'payment_completed' 
  | 'payment_failed' 
  | 'refund_processed' 
  | 'account_updated' 
  | 'security_alert' 
  | 'system_maintenance'

// Filter and Search Types
export interface PaymentFilters {
  status?: PaymentStatus[]
  dateRange?: {
    startDate: string
    endDate: string
  }
  amountRange?: {
    min: number
    max: number
  }
  paymentMethods?: PaymentMethodType[]
  accountIds?: string[]
  search?: string
}

export interface SearchResult<T> {
  items: T[]
  totalCount: number
  facets?: SearchFacet[]
  suggestions?: string[]
}

export interface SearchFacet {
  field: string
  values: FacetValue[]
}

export interface FacetValue {
  value: string
  count: number
  selected: boolean
}

// UI State Types
export interface LoadingState {
  isLoading: boolean
  error: string | null
  lastUpdated: string | null
}

export interface TableColumn<T> {
  key: keyof T
  label: string
  sortable?: boolean
  render?: (value: any, row: T) => React.ReactNode
  width?: string
  align?: 'left' | 'center' | 'right'
}

export interface SortConfig {
  key: string
  direction: 'asc' | 'desc'
}

// Form Types
export interface FormField {
  name: string
  label: string
  type: 'text' | 'email' | 'password' | 'number' | 'select' | 'textarea' | 'checkbox' | 'radio'
  required?: boolean
  placeholder?: string
  options?: { value: string; label: string }[]
  validation?: ValidationRule[]
}

export interface ValidationRule {
  type: 'required' | 'email' | 'min' | 'max' | 'pattern'
  value?: any
  message: string
}

// WebSocket Types
export interface WebSocketMessage {
  type: string
  payload: any
  timestamp: string
  correlationId?: string
}

export interface PaymentUpdateMessage {
  paymentId: string
  status: PaymentStatus
  amount: number
  timestamp: string
  metadata?: Record<string, any>
}

// Theme Types
export interface ThemeConfig {
  mode: 'light' | 'dark' | 'system'
  primaryColor: string
  accentColor: string
  borderRadius: 'none' | 'small' | 'medium' | 'large'
  compactMode: boolean
}

// App Configuration Types
export interface AppConfig {
  apiBaseUrl: string
  wsUrl: string
  features: {
    darkMode: boolean
    notifications: boolean
    analytics: boolean
    realTimeUpdates: boolean
    exportData: boolean
  }
  limits: {
    maxFileSize: number
    requestTimeout: number
    retryAttempts: number
  }
  ui: {
    itemsPerPage: number
    chartRefreshInterval: number
    autoRefreshEnabled: boolean
  }
}