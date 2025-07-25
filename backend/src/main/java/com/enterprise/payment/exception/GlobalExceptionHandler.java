package com.enterprise.payment.exception;

import com.enterprise.payment.dto.response.ApiResponse;
import com.enterprise.payment.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaymentNotFound(PaymentNotFoundException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Payment not found - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("PAYMENT_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, "Payment not found"));
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaymentProcessing(PaymentProcessingException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Payment processing error - Error ID: {}, Message: {}", errorId, ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("PAYMENT_PROCESSING_ERROR")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Payment processing failed"));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientFunds(InsufficientFundsException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Insufficient funds - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("INSUFFICIENT_FUNDS")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Insufficient funds"));
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidPaymentStatus(InvalidPaymentStatusException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Invalid payment status - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("INVALID_PAYMENT_STATUS")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Invalid payment status"));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Account not found - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("ACCOUNT_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, "Account not found"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("User not found - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("USER_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, "User not found"));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Duplicate resource - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(error, "Resource already exists"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Validation error - Error ID: {}", errorId);
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("VALIDATION_ERROR")
                .message("Invalid request parameters")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .details(errors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Validation failed"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Constraint violation - Error ID: {}", errorId);
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (existing, replacement) -> existing
                ));
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("CONSTRAINT_VIOLATION")
                .message("Data constraint violation")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .details(errors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Constraint violation"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Data integrity violation - Error ID: {}", errorId, ex);
        
        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique")) {
                message = "A record with this information already exists";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Referenced record does not exist";
            }
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("DATA_INTEGRITY_VIOLATION")
                .message(message)
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(error, "Data integrity violation"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Authentication error - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("AUTHENTICATION_ERROR")
                .message("Authentication failed")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(error, "Authentication required"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Bad credentials - Error ID: {}", errorId);
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("BAD_CREDENTIALS")
                .message("Invalid username or password")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(error, "Invalid credentials"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Access denied - Error ID: {}, Message: {}", errorId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("ACCESS_DENIED")
                .message("Access denied")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(error, "Access denied"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Invalid JSON - Error ID: {}", errorId);
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("INVALID_JSON")
                .message("Invalid JSON format")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Invalid request format"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Type mismatch - Error ID: {}, Parameter: {}", errorId, ex.getName());
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("TYPE_MISMATCH")
                .message(String.format("Invalid value for parameter '%s'", ex.getName()))
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Invalid parameter type"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected error - Error ID: {}", errorId, ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(error, "Internal server error"));
    }
}