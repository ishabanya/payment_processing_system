# Payment System DTOs (Data Transfer Objects)

This directory contains comprehensive DTO classes for the payment system API, organized into three main categories:
- **Request DTOs** (`request/`) - For incoming API requests
- **Response DTOs** (`response/`) - For API responses  
- **Analytics DTOs** (`analytics/`) - For analytics and reporting

## Overview

All DTOs follow these conventions:
- Use proper validation annotations (`@Valid`, `@NotNull`, `@NotBlank`, `@Email`, etc.)
- Include Jackson annotations for JSON serialization/deserialization
- Follow camelCase field naming conventions
- Use Lombok for constructors, getters, and setters
- Use appropriate field types (`BigDecimal` for money, `OffsetDateTime` for timestamps)
- Include utility methods where appropriate
- Provide factory methods for entity conversion

## Request DTOs (`request/`)

### Authentication & User Management
- **`LoginRequest`** - User login credentials with optional security info
- **`RegisterRequest`** - User registration with account creation
- **`CreateUserRequest`** - Create new user with role assignment
- **`CreateAccountRequest`** - Create new account with initial setup

### Payment Operations
- **`CreatePaymentRequest`** - Payment creation with amount, currency, method selection
- **`UpdatePaymentStatusRequest`** - Payment status updates with transition validation
- **`RefundPaymentRequest`** - Payment refund operations (full/partial)

### Payment Methods
- **`CreatePaymentMethodRequest`** - Payment method registration with type-specific validation

## Response DTOs (`response/`)

### Core API Response Structure
- **`ApiResponse<T>`** - Generic wrapper for all API responses with metadata, pagination, and error handling
- **`ErrorResponse`** - Standardized error response format with detailed error information

### Entity Responses
- **`PaymentResponse`** - Complete payment details with related data and computed fields
- **`AccountResponse`** - Account information with statistics and related entities
- **`UserResponse`** - User information (password excluded) with security status
- **`TransactionResponse`** - Transaction details with performance metrics
- **`PaymentMethodResponse`** - Payment method without sensitive details, with usage statistics
- **`AuthResponse`** - JWT authentication response with tokens, permissions, and session info

## Analytics DTOs (`analytics/`)

### Dashboard & Overview
- **`DashboardStatsResponse`** - Comprehensive dashboard statistics including:
  - Overview metrics (revenue, payments, success rate)
  - Payment/transaction breakdowns by status, method, currency
  - Account statistics and growth metrics  
  - Trend analysis with daily/monthly data
  - Top performers and alert information

### Detailed Analytics
- **`PaymentAnalyticsResponse`** - In-depth payment analytics including:
  - Status and method breakdowns with growth metrics
  - Currency distribution and exchange rate impacts
  - Risk analysis with fraud detection metrics
  - Performance metrics (processing times, throughput)
  - Failure analysis with common patterns
  - Time distribution and geographic data
  - Forecasting and trend analysis

- **`TransactionStatsResponse`** - Comprehensive transaction analytics including:
  - Type, status, and provider breakdowns
  - Performance metrics (SLA compliance, throughput)
  - Fee analysis with optimization opportunities
  - Volume analysis with concentration risk
  - Peak load analysis and capacity utilization
  - Anomaly detection and recommendations

## Key Features

### Validation
All DTOs include comprehensive validation:
```java
@NotNull(message = "Amount is required")
@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
@Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
private BigDecimal amount;
```

### Jackson Configuration
Proper JSON serialization with null exclusion:
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonProperty("paymentReference")
private String paymentReference;
```

### Factory Methods
Easy entity-to-DTO conversion:
```java
public static PaymentResponse fromEntity(Payment payment) {
    // Conversion logic with computed fields
}
```

### Business Logic Validation
Custom validation methods for business rules:
```java
public boolean isValidStatusTransition(Payment.PaymentStatus currentStatus) {
    // Business rule validation
}
```

### Computed Fields
DTOs include calculated fields for UI convenience:
```java
@JsonProperty("isExpired")
private Boolean isExpired;

@JsonProperty("availableRefundAmount") 
private BigDecimal availableRefundAmount;
```

### Flexible Response Options
Multiple factory methods for different use cases:
```java
// Minimal response
PaymentResponse.fromEntity(payment)

// With related data
PaymentResponse.fromEntityWithDetails(payment, includeAccount, includePaymentMethod, includeTransactions)
```

## Usage Examples

### Creating a Payment Request
```java
CreatePaymentRequest request = new CreatePaymentRequest();
request.setAmount(new BigDecimal("100.00"));
request.setCurrencyCode("USD");
request.setDescription("Product purchase");
request.setPaymentMethodId(1L);
```

### Successful API Response
```java
PaymentResponse paymentData = PaymentResponse.fromEntity(payment);
ApiResponse<PaymentResponse> response = ApiResponse.success(paymentData, "Payment created successfully");
```

### Error Response
```java
ErrorResponse error = ErrorResponse.badRequest("Invalid payment amount")
    .withPath("/api/payments")
    .withRequestId(requestId);
```

### Analytics Query
```java
DashboardStatsResponse stats = analyticsService.getDashboardStats(
    DateRange.thisMonth(), 
    List.of(accountId)
);
```

## Dependencies

- **Jakarta Validation** - For request validation
- **Jackson** - For JSON processing
- **Lombok** - For reducing boilerplate code
- **Spring Boot** - For framework integration

## Best Practices

1. **Always validate inputs** - Use appropriate validation annotations
2. **Hide sensitive data** - Never expose passwords, tokens, or payment details in responses
3. **Use computed fields** - Include calculated values to reduce client-side logic
4. **Provide context** - Include related entity summaries when helpful
5. **Support pagination** - Use metadata for large result sets
6. **Enable filtering** - Include filter objects for complex queries
7. **Version compatibility** - Use `@JsonInclude(JsonInclude.Include.NON_NULL)` for backwards compatibility
8. **Error handling** - Provide detailed error information with actionable messages

## Integration

These DTOs integrate seamlessly with:
- Spring Boot REST controllers
- Spring Data JPA repositories
- Spring Security authentication
- OpenAPI documentation generation
- Jackson JSON processing
- Bean Validation framework

The comprehensive DTO structure ensures type safety, validation, and consistent API responses across the entire payment processing system.